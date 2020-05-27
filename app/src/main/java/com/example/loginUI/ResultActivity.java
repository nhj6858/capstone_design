package com.example.loginUI;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ResultActivity extends AppCompatActivity {
    NotificationManager notificationManager;
    TextView attend;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        attend = findViewById(R.id.attend);

//        PushCheck();
//        EndAttend();
        while(NetworkManager.repeatCount !=0){
            try {
                setAlarm(context);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(NetworkManager.repeatCount == 0){
            NetworkManager.list_x++;
            if(NetworkManager.list_x == NetworkManager.list.size()){
                Intent intent = new Intent(ResultActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }


        }

    }

    private void setAlarm(Context context) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        calendar.setTime(dateFormat.parse(NetworkManager.start_time));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("ALARM_ALERT");
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);

        alarmManager.set(AlarmManager.RTC,calendar.getTimeInMillis(), pendingIntent);
    }

    public class Receiver extends BroadcastReceiver {
        public Receiver(){ }

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ReAttend();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    public void PushCheck(){ // 로그인 완료후 PUSH 알림
        notificationManager = (NotificationManager) ResultActivity.this.getSystemService(ResultActivity.this.NOTIFICATION_SERVICE);

        Intent intent = new Intent(ResultActivity.this.getApplicationContext(),ResultActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(getBaseContext(),ResultActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.push)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("제목")
                .setContentText("내용")
                .setTicker("상태바")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(0,builder.build());

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

    }

    public void ReAttend() throws ParseException {
        NetworkManager networkManager = new NetworkManager();

        try {
            networkManager.AttendPost();
            NetworkManager.repeatCount--;
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void EndAttend(){
        NetworkManager networkManager = new NetworkManager();
        networkManager.EndAttend();

        attend.setText("End Attend Success");
    }

}

