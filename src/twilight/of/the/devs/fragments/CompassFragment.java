package twilight.of.the.devs.fragments;

import twilight.of.the.devs.toury.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CompassFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
	private final String TAG = CompassFragment.class.getName();
	private TextView mTextView;

	public CompassFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_dummy,
				container, false);
		mTextView = (TextView) rootView
				.findViewById(R.id.section_label);
//		if(getArguments().getInt(ARG_SECTION_NUMBER) == 1)
//			mTextView.setText("Your current location: ");
		return rootView;
	}
	
	public void setTextViewText(String loc){
		//Log.d(TAG, ""+getArguments().getInt(ARG_SECTION_NUMBER));
		if(mTextView != null)
			mTextView.setText("Your current heading: \n" + loc);
	}
}
