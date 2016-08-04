package com.pl.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationTestActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.button1) Button btn1;
    @BindView(R.id.button2) Button btn2;
    private static int sId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_test);
        ButterKnife.bind(this);
        //btn1 = (Button) findViewById(R.id.button1);
        btn1.setOnClickListener(this);
        //btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Notification notification = new Notification();
        notification.icon = R.mipmap.ic_launcher;
        notification.tickerText = "Hello World";
        notification.when = System.currentTimeMillis();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (v == btn1) {
            sId++;
            notification.setLatestEventInfo(this, "notification_test", "This is a notification...", pendingIntent);
        } else if (v == btn2) {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification_test);
            remoteViews.setTextViewText(R.id.tv_msg, "Notification test: " + sId);
            remoteViews.setOnClickPendingIntent(R.id.btn_response, pendingIntent);
            notification.contentView = remoteViews;
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(sId, notification);
    }

}
