package twilight.of.the.devs.toury;

import twilight.of.the.devs.fragments.SettingsFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class SettingsActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		
		super.onCreate(arg0);
		setContentView(R.layout.settings_layout);
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.settings_frame, new SettingsFragment())
		.commit();
	}
}
