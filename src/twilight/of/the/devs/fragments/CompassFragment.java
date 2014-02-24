package twilight.of.the.devs.fragments;

import twilight.of.the.devs.toury.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class CompassFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private final String TAG = CompassFragment.class.getName();
	private TextView mTextView;

	public CompassFragment() {
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
		Button btn = (Button)rootView.findViewById(R.id.geofenceBtn);
		btn.setVisibility(View.INVISIBLE);
		Button btn2 = (Button)rootView.findViewById(R.id.button1);
		btn2.setVisibility(View.INVISIBLE);
		return rootView;
	}
	
	public void setTextViewText(String loc){
		if(mTextView != null)
			mTextView.setText(loc);
	}
}
