package twilight.of.the.devs.toury;

import java.util.Locale;

import twilight.of.the.devs.fragments.CompassFragment;
import twilight.of.the.devs.fragments.LocationFragment;
import twilight.of.the.devs.utils.OrientationManager;
import twilight.of.the.devs.utils.OrientationManager.OnChangedListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
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
					GooglePlayServicesClient.ConnectionCallbacks,
					GooglePlayServicesClient.OnConnectionFailedListener,
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		
		mLocationClient = new LocationClient(this, this, this);
		
		mUpdatesRequested = true;
		
	    //mCurrentLocation = mLocationClient.getLastLocation();
	    
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
		        	((CompassFragment)frag).setTextViewText("Your current heading: " + orientationManager.getHeading());
			}
			
			@Override
			public void onLocationChanged(OrientationManager orientationManager) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAccuracyChanged(OrientationManager orientationManager) {
				// TODO Auto-generated method stub
				
			}
		});
        //mOrientationManager.start();

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
			//Log.d(TAG, ""+position);
			Fragment fragment = null;
			if(position == 0){
				if(locFrag != null)
					fragment = locFrag;
				else {
					Log.d(TAG, "Creating LocationFragment at position: " + position);
					locFrag = new LocationFragment();
					fragment = locFrag;
				}
				mOrientationManager.stop();
			} else {
				if(compassFrag != null){
					fragment = compassFrag;
				} else {
					compassFrag = new CompassFragment();
					Bundle args = new Bundle();
					args.putInt(CompassFragment.ARG_SECTION_NUMBER, position + 1);
					compassFrag.setArguments(args);
					fragment = compassFrag;
				}
				mOrientationManager.start();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
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
	}

	@Override
	public void onDisconnected() {
		// Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
	}
	
	/*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
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
    	super.onResume();
    }

	@Override
	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "CurrentItem: "+mViewPager.getCurrentItem());
        Fragment frag = ((SectionsPagerAdapter)mViewPager.getAdapter()).getItem(mViewPager.getCurrentItem());
        if(frag instanceof LocationFragment)
        	((LocationFragment)frag).setTextViewText(msg);
	}
	
	/**
     * A single Geofence object, defined by its center and radius.
     */
    public class SimpleGeofence {
            // Instance variables
            private final String mId;
            private final double mLatitude;
            private final double mLongitude;
            private final float mRadius;
            private long mExpirationDuration;
            private int mTransitionType;

        /**
         * @param geofenceId The Geofence's request ID
         * @param latitude Latitude of the Geofence's center.
         * @param longitude Longitude of the Geofence's center.
         * @param radius Radius of the geofence circle.
         * @param expiration Geofence expiration duration
         * @param transition Type of Geofence transition.
         */
        public SimpleGeofence(
                String geofenceId,
                double latitude,
                double longitude,
                float radius,
                long expiration,
                int transition) {
            // Set the instance fields from the constructor
            this.mId = geofenceId;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mRadius = radius;
            this.mExpirationDuration = expiration;
            this.mTransitionType = transition;
        }
        // Instance field getters
        public String getId() {
            return mId;
        }
        public double getLatitude() {
            return mLatitude;
        }
        public double getLongitude() {
            return mLongitude;
        }
        public float getRadius() {
            return mRadius;
        }
        public long getExpirationDuration() {
            return mExpirationDuration;
        }
        public int getTransitionType() {
            return mTransitionType;
        }
        /**
         * Creates a Location Services Geofence object from a
         * SimpleGeofence.
         *
         * @return A Geofence object
         */
        public Geofence toGeofence() {
            // Build a new Geofence object
            return new Geofence.Builder()
                    .setRequestId(getId())
                    .setTransitionTypes(mTransitionType)
                    .setCircularRegion(
                            getLatitude(), getLongitude(), getRadius())
                    .setExpirationDuration(mExpirationDuration)
                    .build();
        }
    }
    
    /**
     * Storage for geofence values, implemented in SharedPreferences.
     */
    public class SimpleGeofenceStore {
        // Keys for flattened geofences stored in SharedPreferences
        public static final String KEY_LATITUDE =
                "com.example.android.geofence.KEY_LATITUDE";
        public static final String KEY_LONGITUDE =
                "com.example.android.geofence.KEY_LONGITUDE";
        public static final String KEY_RADIUS =
                "com.example.android.geofence.KEY_RADIUS";
        public static final String KEY_EXPIRATION_DURATION =
                "com.example.android.geofence.KEY_EXPIRATION_DURATION";
        public static final String KEY_TRANSITION_TYPE =
                "com.example.android.geofence.KEY_TRANSITION_TYPE";
        // The prefix for flattened geofence keys
        public static final String KEY_PREFIX =
                "com.example.android.geofence.KEY";
        /*
         * Invalid values, used to test geofence storage when
         * retrieving geofences
         */
        public static final long INVALID_LONG_VALUE = -999l;
        public static final float INVALID_FLOAT_VALUE = -999.0f;
        public static final int INVALID_INT_VALUE = -999;
        // The SharedPreferences object in which geofences are stored
        private final SharedPreferences mPrefs;
        // The name of the SharedPreferences
        private static final String SHARED_PREFERENCES =
                "SharedPreferences";
        // Create the SharedPreferences storage with private access only
        public SimpleGeofenceStore(Context context) {
            mPrefs =
                    context.getSharedPreferences(
                            SHARED_PREFERENCES,
                            Context.MODE_PRIVATE);
        }
        /**
         * Returns a stored geofence by its id, or returns null
         * if it's not found.
         *
         * @param id The ID of a stored geofence
         * @return A geofence defined by its center and radius. See
         */
        public SimpleGeofence getGeofence(String id) {
            /*
             * Get the latitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lat = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_LATITUDE),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the longitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lng = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_LONGITUDE),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the radius for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            float radius = mPrefs.getFloat(
                    getGeofenceFieldKey(id, KEY_RADIUS),
                    INVALID_FLOAT_VALUE);
            /*
             * Get the expiration duration for the geofence identified
             * by id, or INVALID_LONG_VALUE if it doesn't exist
             */
            long expirationDuration = mPrefs.getLong(
                    getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                    INVALID_LONG_VALUE);
            /*
             * Get the transition type for the geofence identified by
             * id, or INVALID_INT_VALUE if it doesn't exist
             */
            int transitionType = mPrefs.getInt(
                    getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                    INVALID_INT_VALUE);
            // If none of the values is incorrect, return the object
            if (
                lat != INVALID_FLOAT_VALUE &&
                lng != INVALID_FLOAT_VALUE &&
                radius != INVALID_FLOAT_VALUE &&
                expirationDuration !=
                        INVALID_LONG_VALUE &&
                transitionType != INVALID_INT_VALUE) {

                // Return a true Geofence object
                return new SimpleGeofence(
                        id, lat, lng, radius, expirationDuration,
                        transitionType);
            // Otherwise, return null.
            } else {
                return null;
            }
        }
        /**
         * Save a geofence.
         * @param geofence The SimpleGeofence containing the
         * values you want to save in SharedPreferences
         */
        public void setGeofence(String id, SimpleGeofence geofence) {
            /*
             * Get a SharedPreferences editor instance. Among other
             * things, SharedPreferences ensures that updates are atomic
             * and non-concurrent
             */
            Editor editor = mPrefs.edit();
            // Write the Geofence values to SharedPreferences
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_LATITUDE),
                    (float) geofence.getLatitude());
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_LONGITUDE),
                    (float) geofence.getLongitude());
            editor.putFloat(
                    getGeofenceFieldKey(id, KEY_RADIUS),
                    geofence.getRadius());
            editor.putLong(
                    getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                    geofence.getExpirationDuration());
            editor.putInt(
                    getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                    geofence.getTransitionType());
            // Commit the changes
            editor.commit();
        }
        public void clearGeofence(String id) {
            /*
             * Remove a flattened geofence object from storage by
             * removing all of its keys
             */
            Editor editor = mPrefs.edit();
            editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
            editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
            editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
            editor.remove(getGeofenceFieldKey(id,
                    KEY_EXPIRATION_DURATION));
            editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
            editor.commit();
        }
        /**
         * Given a Geofence object's ID and the name of a field
         * (for example, KEY_LATITUDE), return the key name of the
         * object's values in SharedPreferences.
         *
         * @param id The ID of a Geofence object
         * @param fieldName The field represented by the key
         * @return The full key name of a value in SharedPreferences
         */
        private String getGeofenceFieldKey(String id,
                String fieldName) {
            return KEY_PREFIX + "_" + id + "_" + fieldName;
        }
    }
}
