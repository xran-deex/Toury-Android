package twilight.of.the.devs.utils;

import java.util.Iterator;
import java.util.Set;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import twilight.of.the.devs.fragments.LocationFragment;
import twilight.of.the.devs.mylibrary.Marker;
import twilight.of.the.devs.toury.MainActivity;
import twilight.of.the.devs.toury.MainActivity.SectionsPagerAdapter;

public class OrientationListener implements OrientationManager.OnChangedListener {
	
	private static final String TAG = OrientationListener.class.getName();
	private MainActivity mActivity;

	public OrientationListener(MainActivity activity) {
		mActivity = activity;
	}

	@Override
	public void onOrientationChanged(OrientationManager om) {
		Fragment frag = ((SectionsPagerAdapter)mActivity.getViewPager().getAdapter()).getItem(mActivity.getViewPager().getCurrentItem());
        if(frag instanceof LocationFragment)
        	((LocationFragment)frag).setTextViewText("Your current heading: " + om.getHeading());
        Set<Marker> gset = mActivity.getTriggeredMarkers().keySet();

        Marker g;
        Iterator<Marker> iter = gset.iterator();
        while(iter.hasNext()){
        	g = iter.next();
	        if (g == null) {
	        	iter.remove();
	        	continue;
	        }
	        NotificationCompat.Builder builder = 
	        		new NotificationCompat.Builder(mActivity);
	        int heading = (int)om.getHeading();
	        int direction = (int)g.getDirection();

	        if(mActivity.getTriggeredMarkers().get(g) == Geofence.GEOFENCE_TRANSITION_ENTER && isWithinTenDegrees(heading, direction)){
	        		builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
	        		.setContentTitle("Entered Geofence")
	        		.setContentText("You have entered a geofence: " + g.toString())
	        		.setStyle(new NotificationCompat.BigTextStyle().bigText("You have entered a geofence: " + g.toString()));

	        		NotificationManager mNotificationManager =
	                	    (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);

	                	mNotificationManager.notify(1, builder.build());
	                	iter.remove();
	        }
//	        else if (mActivity.getTriggeredMarkers().get(g) == Geofence.GEOFENCE_TRANSITION_DWELL) {
//	        	builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
//	    		.setContentTitle("Dwelling Geofence")
//	    		.setContentText("Your dwelling: " + g.toString())
//	        	.setStyle(new NotificationCompat.BigTextStyle().bigText("You dwelling in a geofence: " + g.toString()));
//	        }
//	        else {
//	        	builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
//	    		.setContentTitle("Exited Geofence")
//	    		.setContentText("You have exited in a geofence: " + g.toString())
//	        	.setStyle(new NotificationCompat.BigTextStyle().bigText("You have exited a geofence: " + g.toString()));
//	
//	        }
        
        }
	}
	
	@Override
	public void onLocationChanged(OrientationManager orientationManager) {
	}
	
	@Override
	public void onAccuracyChanged(OrientationManager orientationManager) {
	}
	
	public boolean isWithinTenDegrees(int one, int two){
		return Math.abs(one - two) <= 10;
	}

}
