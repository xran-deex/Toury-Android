package twilight.of.the.devs.fragments;

import java.util.LinkedList;
import java.util.List;

import twilight.of.the.devs.mylibrary.Tour;
import twilight.of.the.devs.provider.TouryProvider.TouryProviderMetaData;
import twilight.of.the.devs.provider.TouryProvider.TouryProviderMetaData.ToursTableMetaData;
import twilight.of.the.devs.toury.Callback;
import twilight.of.the.devs.toury.MainActivity;
import twilight.of.the.devs.toury.R;
import twilight.of.the.devs.toury.SyncService;
import twilight.of.the.devs.toury.TouryREST;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

public class WelcomeFragment extends Fragment implements Callback {
	
	private MainActivity mActivity;
	private TouryREST toury;
	private ArrayAdapter<Tour> mAdapter;
	private List<Tour> tours;
	private Button mNewTourBtn;
	private Button mSyncBtn;
	private TextView mCurrentTourTextView;
	private static final String TAG = WelcomeFragment.class.getName();
	
	@Override
	public void onAttach(Activity activity) {
		mActivity = (MainActivity)activity;
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.welcome,
				container, false);
		mCurrentTourTextView = (TextView)rootView.findViewById(R.id.textViewCurrentTour);
		Button mButton = (Button)rootView.findViewById(R.id.loadTourBtn);

		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mActivity.getLoadFromREST()){
					Log.d(TAG , "Fetching from REST");
					toury.fetchTours();
				} else {
					Cursor c = getActivity().getContentResolver().query(Uri.withAppendedPath(ToursTableMetaData.CONTENT_URI, "tour"),
							null, 
							null, 
							null, 
							TouryProviderMetaData.ToursTableMetaData.DEFAULT_SORT_ORDER);
					tours = new LinkedList<Tour>();
					
					while(c.moveToNext()){
						Tour t = new Tour(c.getInt(c.getColumnIndex(ToursTableMetaData._ID)), c.getString(c.getColumnIndex(ToursTableMetaData.NAME)));
						tours.add(t);
					}
					callback();
				}
			}
		});
		
		mNewTourBtn = (Button)rootView.findViewById(R.id.createTourBtn);
		mNewTourBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				createTour();
				
			}
		});

		mAdapter = new ArrayAdapter<Tour>(mActivity, android.R.layout.simple_list_item_1);
		toury = new TouryREST(this);
		
		
		return rootView;
	}

	@Override
	public void callback() {
		mAdapter.clear();
		if(mActivity.getLoadFromREST()){
			mAdapter.addAll(toury.getTours());
		} else {
			mAdapter.addAll(tours);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
	    builder.setTitle("Select a tour")
	           .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   mCurrentTourTextView.setText("Current Tour: " + mAdapter.getItem(which).getName());
	            	   mActivity.loadTour(mAdapter.getItem(which).getId());
	           }
	    }).setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		});
	    builder.create().show();
	   
	}
	
	public void createTour() {
		final EditText te = new EditText(getActivity());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(te)
		.setTitle("New Tour")
		.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				ContentValues values = new ContentValues();
				values.put(ToursTableMetaData.NAME, te.getText().toString());
				values.put(ToursTableMetaData.UNSYNCED, 1);
				getActivity().getContentResolver().insert(Uri.withAppendedPath(ToursTableMetaData.CONTENT_URI, "tour"), values);
				//run sync
				Intent i = new Intent(getActivity(), SyncService.class);
				getActivity().startService(i);
			}
		});
		builder.create().show();
		
	}
}
