package twilight.of.the.devs.fragments;

import java.io.DataInputStream;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import twilight.of.the.devs.mylibrary.Marker;
import twilight.of.the.devs.provider.TouryProvider.TouryProviderMetaData;
import twilight.of.the.devs.provider.TouryProvider.TouryProviderMetaData.MarkersTableMetaData;
import twilight.of.the.devs.toury.MainActivity;
import twilight.of.the.devs.toury.R;
import twilight.of.the.devs.toury.SyncService;
import twilight.of.the.devs.utils.OrientationManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LocationFragment extends Fragment {

	private final String TAG = LocationFragment.class.getName();
	private TextView mTextView, geofenceText, mHeadingTextView;
	private Button mButton;
	private Location mLocation;
	private MainActivity mActivity;
	private Button geofence;
	static public int tour_id = 0;

	public LocationFragment() {
	}
	
	@Override
	public void onAttach(Activity activity) {
		mActivity = (MainActivity)activity;
		super.onAttach(activity);
	}
	
	public void setTextViewText(String loc){
		if(mHeadingTextView != null)
			mHeadingTextView.setText(loc);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.debug_fragment,
				container, false);
		mTextView = (TextView) rootView
				.findViewById(R.id.current_location);
		TextView title = (TextView)rootView.findViewById(R.id.fragment_title);
		title.setText("Current Location:");
		geofenceText = (TextView)rootView.findViewById(R.id.geofence);
		
		mHeadingTextView = (TextView)rootView.findViewById(R.id.heading);
		mButton = (Button)rootView.findViewById(R.id.createMarkerBtn);
		
		geofence = (Button)rootView.findViewById(R.id.geofenceBtn);
		geofence.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				StringBuilder sb = new StringBuilder();
				for(Marker m : mActivity.getCurrentMarkers()){
					sb.append(m.getTitle() + ": ");
					sb.append(m.getDescription() + ", Dir: ");
					sb.append(((int)m.getDirection()) + "\n");
				}
				geofenceText.setText(sb.toString());
				//fetchGeofence();
			}
		});
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//submitLocation();
				View view = getActivity().getLayoutInflater().inflate(R.layout.new_marker_dialog_layout, null);
				final EditText title = (EditText)view.findViewById(R.id.editTextTitle);
				final EditText description = (EditText)view.findViewById(R.id.editTextDescription);
				final EditText radius = (EditText)view.findViewById(R.id.editTextRadius);
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setView(view)
				.setTitle("New Marker")
				.setPositiveButton("Create", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						submitLocation(title.getText().toString(), description.getText().toString(), Double.parseDouble(radius.getText().toString()));
					}
				}).create().show();
			}
		});
		Button remove = (Button)rootView.findViewById(R.id.remove_btn);
		remove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				((MainActivity)getActivity()).removeGeofences();
			}
		});
		return rootView;
	}
	
	protected void fetchGeofence() {
		Log.d(TAG, "fetching markers for tour: " + tour_id);
		new AsyncTask<Void, Void, String>(){

			@Override
			protected String doInBackground(Void... params) {
				if(mActivity.getLoadFromREST()){
					HttpClient client = new DefaultHttpClient();
	                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
	                HttpResponse response;
	
	                try {
	                    HttpGet get = new HttpGet("http://valis.strangled.net:7000/tour/" + tour_id + "?format=json");
	                    response = client.execute(get);	                          
	
	                    /*Checking response */
	                    if(response!=null){
	                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
	                        String res = new DataInputStream(in).readLine();
	                        JSONArray obj = new JSONArray(res);
	                        return obj.toString();
	                    }
	
	                } catch(Exception e) {
	                    e.printStackTrace();
	                }
				} else {
					Uri uri = Uri.withAppendedPath(TouryProviderMetaData.ToursTableMetaData.CONTENT_URI , "tour/" + tour_id + "/markers");
					Cursor c = getActivity().getContentResolver().query(uri, 
							null, 
							null, 
							null, 
							null);
					StringBuilder sb = new StringBuilder();

					while(c.moveToNext()){
						sb.append(c.getString(c.getColumnIndex(MarkersTableMetaData.TITLE)) + ": ");
						sb.append(c.getString(c.getColumnIndex(MarkersTableMetaData.DESCRIPTION)) + ", DIR: ");
						sb.append((int)c.getDouble(c.getColumnIndex(MarkersTableMetaData.DIRECTION)) + "\n");
					}
					Log.d(TAG, sb.toString());
					return sb.toString();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String result) {
				geofenceText.setText(result);
				super.onPostExecute(result);
			}
		}.execute();
	}

	public void setTextViewText(Location loc){
		mLocation = loc;
		
		if(mTextView != null)
			mTextView.setText(loc.toString());
	}
	
	public void submitLocation(final String title, final String description, final double radius){
		new Thread(new Runnable(){
			public void run(){
				OrientationManager om = mActivity.getOrientationManager();
				if(mActivity.getLoadFromREST()){
					HttpClient client = new DefaultHttpClient();
	                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
	                HttpResponse response;
	                
	
	                try {
	                    HttpPost post = new HttpPost("http://valis.strangled.net:7000/api/markers/");
	                    String authorizationString = "Basic " + Base64.encodeToString(
	    				        ("randy" + ":" + "greenday").getBytes(),
	    				        Base64.NO_WRAP); 
	                    JSONObject jsonParam = new JSONObject();
	    				jsonParam.put("title", title);
	    				jsonParam.put("description", description);
	    				jsonParam.put("trigger_latitude",mLocation.getLatitude());
	    				jsonParam.put("marker_longitude",mLocation.getLongitude());
	    				jsonParam.put("marker_latitude",mLocation.getLatitude());
	    				jsonParam.put("trigger_longitude",mLocation.getLongitude());
	    				jsonParam.put("radius", radius);
	    				jsonParam.put("direction", om.getHeading());
	                    StringEntity se = new StringEntity( jsonParam.toString());  
	                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	                    post.addHeader("Authorization", authorizationString);
	                    post.setEntity(se);
	                    response = client.execute(post);
	
	                    /*Checking response */
	                    if(response!=null){
	                        response.getEntity().getContent();
	                    }

	                } catch(Exception e) {
	                    e.printStackTrace();
	                }
				} else {
					Log.d(TAG, "Latitude: " + mLocation.getLatitude());
					ContentValues values = new ContentValues();
					values.put(MarkersTableMetaData.TITLE, title);
					values.put(MarkersTableMetaData.DESCRIPTION, description);
					values.put(MarkersTableMetaData.TRIGGER_LATITUDE, mLocation.getLatitude());
					values.put(MarkersTableMetaData.TRIGGER_LONGITUDE, mLocation.getLongitude());
					values.put(MarkersTableMetaData.MARKER_LATITUDE, mLocation.getLatitude());
					values.put(MarkersTableMetaData.MARKER_LONGITUDE, mLocation.getLongitude());
					values.put(MarkersTableMetaData.RADIUS, radius);
					values.put(MarkersTableMetaData.DIRECTION, om.getHeading());
					values.put(MarkersTableMetaData.TOUR_ID, tour_id);
					values.put(MarkersTableMetaData.UNSYNCED, 1);
					Uri uri = Uri.withAppendedPath(TouryProviderMetaData.ToursTableMetaData.CONTENT_URI, "tour/" + tour_id + "/markers");
					getActivity().getContentResolver().insert(uri, values);
				}
				//run sync
				Intent i = new Intent(getActivity(), SyncService.class);
				getActivity().startService(i);
			}
			
		}).start();
	}
	
	
}
