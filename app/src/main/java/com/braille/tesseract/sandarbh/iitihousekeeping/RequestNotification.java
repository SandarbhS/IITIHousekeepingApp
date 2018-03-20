package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RequestNotification extends IntentService {

    private final int NOTIFICATION_ID = 23458;
    private boolean NOTIFICATION_FLAG = false;
    private NotificationCompat.Builder builder;

    public RequestNotification() {
        super("RequestNotification");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.e("Service : ","Service Started");

        Intent openActivity = new Intent(getBaseContext(),Login.class);
        openActivity.putExtra("NOTIFICATION_CALL",true);
        openActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),0,openActivity,PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(getBaseContext());
        builder.setContentTitle("New Request")
                .setAutoCancel(true)
                .setContentText("There is a new request!")
                .setSmallIcon(R.mipmap.housekeeping_logo_final)
                .addAction(new NotificationCompat.Action(R.drawable.info_icon,"OPEN",pendingIntent))
                .setTicker("New Request");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setLights(Color.GREEN,500,1000);
        }

        builder.setContentIntent(pendingIntent);
        builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e("Service : ","Entering...");
        DatabaseReference DB = FirebaseDatabase.getInstance().getReference();

        DB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Service : ","Value Changed : "+dataSnapshot.getChildrenCount());

                Notification notification = builder.build();
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                //To ensure that notification isn't sent on app startup but only on changes.
                if (NOTIFICATION_FLAG)
                    manager.notify(NOTIFICATION_ID,notification);

                NOTIFICATION_FLAG = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onDestroy() {
        Log.e("Service : ","Service Stopped!");
        super.onDestroy();
    }
}
