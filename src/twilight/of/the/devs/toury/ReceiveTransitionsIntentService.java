package twilight.of.the.devs.toury;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class ReceiveTransitionsIntentService extends IntentService {

	private static final String TAG = ReceiveTransitionsIntentService.class.getName();

	public ReceiveTransitionsIntentService() {
		super("TransitionReceiever");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                    Integer.toString(errorCode));
            /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            // Get the type of transition (entry or exit)
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if (
                (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                 ||
                (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
                ||
                (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL)
               ) {
                List <Geofence> triggerList =
                        LocationClient.getTriggeringGeofences(intent);

                String[] triggerIds = new String[triggerList.size()];

                for (int i = 0; i < triggerIds.length; i++) {
                    // Store the Id of each geofence
                    triggerIds[i] = triggerList.get(i).getRequestId();
                   
                }
                /*
                 * At this point, you can store the IDs for further use
                 * display them, or display the details associated with
                 * them.
                 */
                Intent i = new Intent("location");
                NotificationCompat.Builder builder = 
                		new NotificationCompat.Builder(this);
                if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){
                		builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
                		.setContentTitle("Entered Geofence")
                		.setContentText("You have entered a geofence: " + triggerList.toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText("You have entered a geofence: " + triggerList.toString()));
                		i.putExtra("loc", "Entered geofence: " + triggerList.get(0).toString());
                		i.putExtra("id", triggerIds[0]);
                }
                else if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
                	builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
            		.setContentTitle("Dwelling Geofence")
            		.setContentText("You dwelling in a geofence: " + triggerList.toString())
                	.setStyle(new NotificationCompat.BigTextStyle().bigText("You dwelling in a geofence: " + triggerList.toString()));
                	i.putExtra("loc", "Dwelling in geofence: " + triggerList.get(0).toString());
                	i.putExtra("id", triggerIds[0]);
                }
                else {
                	builder.setSmallIcon(android.R.drawable.ic_menu_mapmode)
            		.setContentTitle("Exited Geofence")
            		.setContentText("You dwelling in a geofence: " + triggerList.toString())
                	.setStyle(new NotificationCompat.BigTextStyle().bigText("You have exited a geofence: " + triggerList.toString()));
                	i.putExtra("loc", "Exited geofence: " + triggerList.get(0).toString());
                	i.putExtra("id", triggerIds[0]);
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                NotificationManager mNotificationManager =
                	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                	
                	// mId allows you to update the notification later on.
                	mNotificationManager.notify(1, builder.build());
                	
//                if(transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
//                	Toast.makeText(this, "Entered: " + triggerList.toString(), Toast.LENGTH_SHORT).show();
//                else
//                	Toast.makeText(this, "Dwelling: " + triggerList.toString(), Toast.LENGTH_SHORT).show();
                
            }
        // An invalid transition was reported
        } 
	}

}
