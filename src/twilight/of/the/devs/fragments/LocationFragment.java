package twilight.of.the.devs.fragments;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import twilight.of.the.devs.provider.TouryProvider;
import twilight.of.the.devs.provider.TouryProvider.TouryProviderMetaData;
import twilight.of.the.devs.toury.MainActivity;
import twilight.of.the.devs.toury.R;
import twilight.of.the.devs.utils.OrientationManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
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
import android.widget.TextView;

public class LocationFragment extends Fragment {

	public static final String ARG_SECTION_NUMBER = "section_number";
	private final String TAG = LocationFragment.class.getName();
	private TextView mTextView, geofenceText;
	private Button mButton;
	private Location mLocation;

	public LocationFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment,
				container, false);
		mTextView = (TextView) rootView
				.findViewById(R.id.current_location);
		TextView title = (TextView)rootView.findViewById(R.id.fragment_title);
		title.setText("Current Location:");
		geofenceText = (TextView)rootView.findViewById(R.id.geofence);
		mButton = (Button)rootView.findViewById(R.id.button1);
		Button geofence = (Button)rootView.findViewById(R.id.geofenceBtn);
		geofence.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fetchGeofence();
			}
		});
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submitLocation();
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
		new AsyncTask<Void, Void, String>(){

			@Override
			protected String doInBackground(Void... params) {
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
//                        return obj.toString();
//                    }
//
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
				Cursor c = getActivity().getContentResolver().query(TouryProviderMetaData.MarkersTableMetaData.CONTENT_URI, 
						null, 
						null, 
						null, 
						TouryProviderMetaData.MarkersTableMetaData.DEFAULT_SORT_ORDER);
				StringBuilder sb = new StringBuilder();
				//if(c.moveToFirst())
					while(c.moveToNext()){
						sb.append(c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.LATITUDE)) + " ");
						sb.append(c.getDouble(c.getColumnIndex(TouryProviderMetaData.MarkersTableMetaData.LONGITUDE)) + "\n");
					}
				return sb.toString();
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
			mTextView.setText("Your current location: \n" + loc);
	}
	
	public void submitLocation(){
		new Thread(new Runnable(){
			public void run(){

//				HttpClient client = new DefaultHttpClient();
//                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
//                HttpResponse response;
                OrientationManager om = ((MainActivity)getActivity()).getOM();
//
//                try {
//                    HttpPost post = new HttpPost("http://valis.strangled.net:9000/api/tours/");
//                    String authorizationString = "Basic " + Base64.encodeToString(
//    				        ("randy" + ":" + "greenday").getBytes(),
//    				        Base64.NO_WRAP); 
//                    JSONObject jsonParam = new JSONObject();
//    				jsonParam.put("title", "Post from Android");
//    				jsonParam.put("description", "This is a description from Toury for Android");
//    				jsonParam.put("latitude",mLocation.getLatitude());
//    				jsonParam.put("longitude",mLocation.getLongitude());
//    				jsonParam.put("radius", 5);
//    				jsonParam.put("direction", om.getHeading());
//                    StringEntity se = new StringEntity( jsonParam.toString());  
//                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//                    post.addHeader("Authorization", authorizationString);
//                    post.setEntity(se);
//                    response = client.execute(post);
//
//                    /*Checking response */
//                    if(response!=null){
//                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
//                    }
//
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
				ContentValues values = new ContentValues();
				values.put(TouryProviderMetaData.MarkersTableMetaData.TITLE, "Title");
				values.put(TouryProviderMetaData.MarkersTableMetaData.DESCRIPTION, "A description from Toury Android");
				values.put(TouryProviderMetaData.MarkersTableMetaData.LATITUDE, mLocation.getLatitude());
				values.put(TouryProviderMetaData.MarkersTableMetaData.LONGITUDE, mLocation.getLongitude());
				values.put(TouryProviderMetaData.MarkersTableMetaData.RADIUS, 15.0);
				values.put(TouryProviderMetaData.MarkersTableMetaData.DIRECTION, om.getHeading());
				getActivity().getContentResolver().insert(TouryProviderMetaData.MarkersTableMetaData.CONTENT_URI, values);
			}
			
		}).start();
	}
	
	
}
