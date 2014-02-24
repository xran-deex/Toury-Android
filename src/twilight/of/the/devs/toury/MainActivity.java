package twilight.of.the.devs.toury;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;

import twilight.of.the.devs.fragments.CompassFragment;
import twilight.of.the.devs.fragments.LocationFragment;
import twilight.of.the.devs.mylibrary.SimpleGeofence;
import twilight.of.the.devs.provider.TouryProvider.TouryProviderMetaData;
import twilight.of.the.devs.utils.OrientationManager;
import twilight.of.the.devs.utils.OrientationManager.OnChangedListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
					ConnectionCallbacks,
					OnConnectionFailedListener,
					OnAddGeofencesResultListener,
					OnRemoveGeofencesResultListener,
					LocationListener {
	
	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	/*
     * Use to set an expiration time for a geofence. After this amount
     * of time Location Services will stop tracking the geofence.
     */
    private static final long SECONDS_PER_HOUR = 60;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
            SECONDS_PER_HOUR *
            MILLISECONDS_PER_SECOND;
	private static final String TAG = MainActivity.class.getName();
	
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	private LocationRequest mLocationRequest;
	private SharedPreferences mPrefs;
	private Editor mEditor;
	private boolean mUpdatesRequested;
	private OrientationManager mOrientationManager;
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent, mTransitionPendingIntent;
    // Defines the allowable request types.
    public enum REQUEST_TYPE {ADD, CONNECT, REMOVE_ALL}
    private REQUEST_TYPE mRequestType;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
	private List<Geofence> mCurrentGeofences;
	private BluetoothAdapter mBluetoothAdapter;
	private HashMap<String, BluetoothDevice> devices;
	private BluetoothDevice mConnectedDevice;
	private HashMap<SimpleGeofence, Integer> mTriggeredGeofences;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String id = intent.getStringExtra("geofence_id");
			int transitionType = intent.getIntExtra("transition_type", -1);
			Cursor c = getContentResolver().query(TouryProviderMetaData.MarkersTableMetaData.CONTENT_URI, 
					null, 
					"_id = ?", 
					new String[]{id}, 
					null);
			SimpleGeofence g = null;
			if(c.moveToFirst()){
				g = new SimpleGeofence(
						""+c.getLong(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData._ID)), 
						c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.LATITUDE)), 
						c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.LONGITUDE)), 
						(float) c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.RADIUS)), 
						100000000L, 
						c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.DIRECTION)),
						Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT|Geofence.GEOFENCE_TRANSITION_DWELL);
				g.setDescription(c.getString(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.DESCRIPTION)));
			}
			mTriggeredGeofences.put(g, transitionType);
			Toast.makeText(MainActivity.this, "Added geofence to hashmap", Toast.LENGTH_SHORT).show();
			Log.d(TAG, mTriggeredGeofences.toString());
//			NotificationCompat.Builder builder = 
//            		new NotificationCompat.Builder(MainActivity.this);
//            if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER && mOrientationManager.getHeading() == g.getDirection()){
//            		builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
//            		.setContentTitle("Entered Geofence")
//            		.setContentText("You have entered a geofence: " + g.toString())
//            .setStyle(new NotificationCompat.BigTextStyle().bigText("You have entered a geofence: " + g.toString()));
////            		i.putExtra("loc", "Entered geofence: " + g.toString());
////            		i.putExtra("id", g.getId());
//            }
//            else if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
//            	builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
//        		.setContentTitle("Dwelling Geofence")
//        		.setContentText("You dwelling in a geofence: " + g.toString())
//            	.setStyle(new NotificationCompat.BigTextStyle().bigText("You dwelling in a geofence: " + g.toString()));
////            	i.putExtra("loc", "Dwelling in geofence: " + g.toString());
////            	i.putExtra("id", triggerIds[0]);
//            }
//            else {
//            	builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
//        		.setContentTitle("Exited Geofence")
//        		.setContentText("You dwelling in a geofence: " + g.toString())
//            	.setStyle(new NotificationCompat.BigTextStyle().bigText("You have exited a geofence: " + g.toString()));
////            	i.putExtra("loc", "Exited geofence: " + triggerList.get(0).toString());
////            	i.putExtra("id", triggerIds[0]);
//            }
			new ConnectThread(mBluetoothAdapter, mConnectedDevice).execute(g);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("location"));

		// Start with the request flag set to false
        mInProgress = false;
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
		
		//mLocationClient = new LocationClient(this, this, this);
		
		mUpdatesRequested = true;
		
	    //mCurrentLocation = mLocationClient.getLastLocation();
		getGeofenceList();
		mTriggeredGeofences = new HashMap<SimpleGeofence, Integer>();
	    
	    // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        SensorManager sensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mOrientationManager = new OrientationManager(sensorManager, locationManager);
        mOrientationManager.addOnChangedListener(new OnChangedListener() {
			
			@Override
			public void onOrientationChanged(OrientationManager orientationManager) {
				Fragment frag = ((SectionsPagerAdapter)mViewPager.getAdapter()).getItem(mViewPager.getCurrentItem());
		        if(frag instanceof CompassFragment)
		        	((CompassFragment)frag).setTextViewText("Your current heading: " + mOrientationManager.getHeading());
		        Set<SimpleGeofence> gset = mTriggeredGeofences.keySet();
		        for(SimpleGeofence g : gset){
		        NotificationCompat.Builder builder = 
	            		new NotificationCompat.Builder(MainActivity.this);
		        int heading = (int)mOrientationManager.getHeading();
		        int direction = (int)g.getDirection();
		        Log.d(TAG, ""+heading + ", " + direction);
	            if(mTriggeredGeofences.get(g) == Geofence.GEOFENCE_TRANSITION_ENTER && isWithinTenDegrees(heading, direction)){
	            		builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
	            		.setContentTitle("Entered Geofence")
	            		.setContentText("You have entered a geofence: " + g.toString())
	            .setStyle(new NotificationCompat.BigTextStyle().bigText("You have entered a geofence: " + g.toString()));
//	            		i.putExtra("loc", "Entered geofence: " + g.toString());
//	            		i.putExtra("id", g.getId());
	            		NotificationManager mNotificationManager =
	                    	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	                    	
	                    	// mId allows you to update the notification later on.
	                    	mNotificationManager.notify(1, builder.build());
	                    	mTriggeredGeofences.remove(g);
	            }
//	            else if (mTriggeredGeofences.get(g) == Geofence.GEOFENCE_TRANSITION_DWELL) {
//	            	builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
//	        		.setContentTitle("Dwelling Geofence")
//	        		.setContentText("You dwelling in a geofence: " + g.toString())
//	            	.setStyle(new NotificationCompat.BigTextStyle().bigText("You dwelling in a geofence: " + g.toString()));
////	            	i.putExtra("loc", "Dwelling in geofence: " + g.toString());
////	            	i.putExtra("id", triggerIds[0]);
//	            }
//	            else {
//	            	builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
//	        		.setContentTitle("Exited Geofence")
//	        		.setContentText("You dwelling in a geofence: " + g.toString())
//	            	.setStyle(new NotificationCompat.BigTextStyle().bigText("You have exited a geofence: " + g.toString()));
////	            	i.putExtra("loc", "Exited geofence: " + triggerList.get(0).toString());
////	            	i.putExtra("id", triggerIds[0]);
//	            }
	            
		        }
			}
			
			@Override
			public void onLocationChanged(OrientationManager orientationManager) {
			}
			
			@Override
			public void onAccuracyChanged(OrientationManager orientationManager) {
			}
		});
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_SHORT).show();
		    finish();
		}
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        devices = new HashMap<String, BluetoothDevice>();
    	// If there are paired devices
    	if (pairedDevices.size() > 0) {
    	    // Loop through paired devices
    	    for (BluetoothDevice device : pairedDevices) {
    	        // Add the name and address to an array adapter to show in a ListView
    	        Log.d(TAG, device.getName() + "\n" + device.getAddress());
    	        mConnectedDevice = device;
    	        devices.put(device.getName(), device);
    	    }
    	}
    	if(mConnectedDevice == null){
    		Toast.makeText(this, "No connected devices", Toast.LENGTH_SHORT).show();
    	}
    	CharSequence[] list = new CharSequence[devices.keySet().size()];
		int i = 0;
		for(Iterator<String> it = devices.keySet().iterator(); it.hasNext(); ){
			list[i] = it.next();
			i++;
		}
    	displayDialogList(list);
        
	}
	
	public boolean isWithinTenDegrees(int one, int two){
		return Math.abs(one - two) <= 10;
	}
	
	public BluetoothAdapter getBTAdapter(){
		return mBluetoothAdapter;
	}
	
	public BluetoothDevice getBTDevice(){
		return mConnectedDevice;
	}
	
	public void displayDialogList(final CharSequence[] list){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Select a device")
	           .setItems(list, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
		               mConnectedDevice = devices.get(list[which]);
	           }
	    }).setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
//				MainActivity.this.finish();
			}
		});
	    builder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                    break;
                }
        }
     }
    
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode = resultCode;//connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
        }
		return false;
    }

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private final String TAG = SectionsPagerAdapter.class.getName();
		private LocationFragment locFrag;
		private CompassFragment compassFrag;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if(position == 0){
				if(locFrag != null)
					fragment = locFrag;
				else {
					locFrag = new LocationFragment();
					fragment = locFrag;
				}
				//mOrientationManager.stop();
			} else {
				if(compassFrag != null){
					fragment = compassFrag;
				} else {
					compassFrag = new CompassFragment();
					fragment = compassFrag;
				}
				//mOrientationManager.start();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.frag1).toUpperCase(l);
			case 1:
				return getString(R.string.frag2).toUpperCase(l);
			}
			return null;
		}
	}
	
	public OrientationManager getOM(){
		return mOrientationManager;
	}
	
	// Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
           // showErrorDialog(connectionResult.getErrorCode());
        }
	}

	@Override
	public void onConnected(Bundle arg0) {
		// Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
     // If already requested, start periodic updates
        //if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        //}
            switch (mRequestType) {
            case ADD :
                // Get the PendingIntent for the request
                mTransitionPendingIntent =
                        getTransitionPendingIntent();
                // Send a request to add the current geofences
//                mLocationClient.removeGeofences(mTransitionPendingIntent, this);
                mLocationClient.addGeofences(
                        mCurrentGeofences, mTransitionPendingIntent, this);
            case CONNECT:
            	return;
            case REMOVE_ALL:
            	// Get the PendingIntent for the request
                mTransitionPendingIntent =
                        getTransitionPendingIntent();
                // Send a request to remove the current geofences
                mLocationClient.removeGeofences(mTransitionPendingIntent, this);
            }
	}

	@Override
	public void onDisconnected() {
		// Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
     // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mLocationClient = null;
	}
	
	/*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
       // mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
    	// If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
           // removeLocationUpdates(this);
        	mUpdatesRequested = false;
        }
        mLocationClient.disconnect();
        super.onStop();
    }
    
    @Override
    protected void onPause() {
    	// Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mOrientationManager.stop();
        mEditor.commit();
    	super.onPause();
    }
    
    @Override
    protected void onResume() {
    	/*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

        // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
        mOrientationManager.start();
    	super.onResume();
    }

	@Override
	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        
        Fragment frag = ((SectionsPagerAdapter)mViewPager.getAdapter()).getItem(mViewPager.getCurrentItem());
        if(frag instanceof LocationFragment)
        	((LocationFragment)frag).setTextViewText(location);
	}	

	@Override
	public void onAddGeofencesResult(int statusCode, String[] arg1) {
		// If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        	Log.d(TAG, "Added geofence: success");
        } else {
        // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        	Log.d(TAG, "Error while adding geofence");
        }
        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        //mLocationClient.disconnect();
	}
	
	/*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        // Create an explicit Intent
        Intent intent = new Intent(this,
                ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
	
    
    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void addGeofences() {
        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.ADD;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }
    
    public void getGeofenceList(){
    	List<Geofence> result = new LinkedList<Geofence>();
    	
    	new AsyncTask<Void, Void, List<Geofence>>(){

			@Override
			protected List<Geofence> doInBackground(Void... params) {
				List<Geofence> result = new LinkedList<Geofence>();
//				HttpClient client = new DefaultHttpClient();
//                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
//                HttpResponse response;
//
//                try {
//                    HttpGet post = new HttpGet("http://valis.strangled.net:9000/api/tours/");
//                    String authorizationString = "Basic " + Base64.encodeToString(
//    				        ("randy" + ":" + "greenday").getBytes(),
//    				        Base64.NO_WRAP); 
//                    
//                    
//                    post.addHeader("Authorization", authorizationString);
//                    response = client.execute(post);
//                    
//                    
//
//                    /*Checking response */
//                    if(response!=null){
//                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
//                        String res = new DataInputStream(in).readLine();
//                        JSONObject obj = new JSONObject(res);
//                        JSONArray results = obj.getJSONArray("results");
					mCurrentGeofences = new LinkedList<Geofence>();
					Cursor c = getContentResolver().query(TouryProviderMetaData.MarkersTableMetaData.CONTENT_URI, null, null, null, null);
					//if(c.moveToFirst())
					while(c.moveToNext()){
						SimpleGeofence g = new SimpleGeofence(
								""+c.getLong(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData._ID)), 
								c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.LATITUDE)), 
								c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.LONGITUDE)), 
								(float) c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.RADIUS)), 
								100000000L, 
								c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.DIRECTION)),
								Geofence.GEOFENCE_TRANSITION_ENTER);//|Geofence.GEOFENCE_TRANSITION_EXIT|Geofence.GEOFENCE_TRANSITION_DWELL);
                    	mCurrentGeofences.add(g.toGeofence());
					}
                        
//                        for(int i = 1; i <= results.length(); i++){
//                        	JSONObject j = results.getJSONObject(i-1);
//                        	SimpleGeofence g = new SimpleGeofence(j.getString("url"), j.getDouble("latitude"), j.getDouble("longitude"), (float) j.getDouble("radius"), 100000000L, Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT|Geofence.GEOFENCE_TRANSITION_DWELL);
//                        	mCurrentGeofences.add(g.toGeofence());
                      //  }
                   // }

//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
                if(!mCurrentGeofences.isEmpty())
                	addGeofences();
                else {
                	mRequestType = REQUEST_TYPE.CONNECT;
                	mLocationClient = new LocationClient(MainActivity.this, MainActivity.this, MainActivity.this);
                	mLocationClient.connect();
                }
                	
				return null;
			}
    		
    	}.execute();
    }
    
    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
    public void removeGeofences() {
        // Start a request to add geofences
        mRequestType = REQUEST_TYPE.REMOVE_ALL;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the proper request
         * can be restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        /*
         * Create a new location client object. Since the current
         * activity class implements ConnectionCallbacks and
         * OnConnectionFailedListener, pass the current activity object
         * as the listener for both parameters
         */
        mLocationClient = new LocationClient(this, this, this);
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is underway
            mInProgress = true;
            // Request a connection from the client to Location Services
            mLocationClient.connect();
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
        getContentResolver().delete(TouryProviderMetaData.MarkersTableMetaData.CONTENT_URI, null, null);
    }

	@Override
	public void onRemoveGeofencesByPendingIntentResult(int arg0,
			PendingIntent arg1) {
		if(arg0 == LocationStatusCodes.SUCCESS)
			Log.d(TAG, "Geofences removed successfully");
		
	}

	@Override
	public void onRemoveGeofencesByRequestIdsResult(int arg0, String[] arg1) {
		// TODO Auto-generated method stub
		
	}

}
