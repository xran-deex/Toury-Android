package twilight.of.the.devs.fragments;

import twilight.of.the.devs.toury.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LocationFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
	private final String TAG = LocationFragment.class.getName();
	private TextView mTextView;

	public LocationFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_dummy,
				container, false);
		mTextView = (TextView) rootView
				.findViewById(R.id.section_label);
		mTextView.setText("Your current location: ");
		return rootView;
	}
	
	public void setTextViewText(String loc){
		if(mTextView != null)
			mTextView.setText("Your current location: \n" + loc);
	}
}
