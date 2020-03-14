package exarbete.listeningapp.recording;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import exarbete.listeningapp.MainActivity;
import exarbete.listeningapp.R;

/**
 * Created by svett_000 on 04/05/2016.
 */
public class NotificationHandler {

    private NotificationHandler(){

    }

    public static void notifyRecordingChange(boolean recording, Activity activity){
        Intent resumeIntent = new Intent(activity, MainActivity.class);
        resumeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(activity, 0, resumeIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(R.drawable.ic_drawer_item_audio_recording)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setWhen(System.currentTimeMillis())
                        .setOngoing(true)
                        .setContentTitle("AudioApp")
                        .setContentText("Listening for sounds.")
                        .setContentIntent(resultPendingIntent);

        int mNotificationId = 001;

        NotificationManager mNotifyMgr = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);
        if(recording){
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }else{
            mNotifyMgr.cancel(mNotificationId);
        }

    }

}
