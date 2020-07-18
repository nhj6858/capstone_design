package com.example.loginUI;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RepeatActivity extends AppCompatActivity {
    NetworkManager networkManager;
    NotificationManager notificationManager;
    TextView attend;
    Button btn;
    int compare;
    int repeatCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat);
        attend = findViewById(R.id.attend);
        btn = findViewById(R.id.ReAttendBtn);


        repeatCount = PreferenceManager.GetInteger(getApplicationContext(),"repeatCount");//횟수 불러옴
        attend.setText("Repeat Count : " + repeatCount);
        Log.d("okhyo", "RepeatActivity RepeatCount : " + repeatCount);

        TimeCheck();
//        PushCheck();
//        EndAttend();

        if(repeatCount>0) {//재출석 횟수가 0보다 클때
            if (compare >= 0) {//출석시간이 이미 지났을 경우 재출석
                try {
                    Toast.makeText(getApplicationContext(),"재 출석 요청 보냄",Toast.LENGTH_SHORT).show();
                    networkManager.AttendPost(getApplicationContext());//출석요청
                    repeatCount--;//재요청 횟수 차감
                    PreferenceManager.SaveInteger(getApplicationContext(), "repeatCount", repeatCount);//횟수 저장


                    Intent intent = new Intent(RepeatActivity.this, RepeatActivity.class);
                    startActivity(intent);
                    finish();// 지각을 위해 한번 출석 후 다음시간의 출석까지 찍기위한 현재 액티비티 재시작

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {//출석 시간 이전일 경우 Attend Activity 로 돌아가서 service 자동 출석 대기

                repeatCount--;//재요청 횟수 차감
                PreferenceManager.SaveInteger(getApplicationContext(), "repeatCount", repeatCount);//횟수 저장

                Toast.makeText(getApplicationContext(),"재 출석 요청을 보내지 않음",Toast.LENGTH_SHORT).show();


                String Major = PreferenceManager.GetString(getApplicationContext(), "Major");
                String Minor = PreferenceManager.GetString(getApplicationContext(), "Minor");
                String UUID = PreferenceManager.GetString(getApplicationContext(), "UUID");

                Intent intent = new Intent(RepeatActivity.this, ScanActivity.class);
                intent.putExtra("Major", Major);
                intent.putExtra("Minor", Minor);
                intent.putExtra("UUID", UUID);
                startActivity(intent);
                finish();
            }
        }else {
            NetworkManager.list_x++;
            if(NetworkManager.list_x < NetworkManager.list.size()){
                Intent intent = new Intent(RepeatActivity.this, ScanActivity.class);
                startActivity(intent);
                finish();
            }else if(NetworkManager.list_x >= NetworkManager.list.size()) {
                NetworkManager.list_x=0;
                Toast.makeText(getApplicationContext(),"오늘의 강의가 전부 끝났음",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RepeatActivity.this,ScanActivity.class);
                startActivity(intent);
                finish();

            }
        }

//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if(repeatCount>0) {//재출석 횟수가 0보다 클때
//                    if (compare >= 0) {//출석시간이 이미 지났을 경우 재출석
//                        try {
//                            Toast.makeText(getApplicationContext(),"재 출석 요청 보냄",Toast.LENGTH_SHORT).show();
//                            networkManager.AttendPost(getApplicationContext());//출석요청
//                            repeatCount--;//재요청 횟수 차감
//                            PreferenceManager.SaveInteger(getApplicationContext(), "repeatCount", repeatCount);//횟수 저장
//
//
//                            Intent intent = new Intent(RepeatActivity.this, RepeatActivity.class);
//                            startActivity(intent);
//                            finish();// 지각을 위해 한번 출석 후 다음시간의 출석까지 찍기위한 현재 액티비티 재시작
//
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//
//                    } else {//출석 시간 이전일 경우 Attend Activity 로 돌아가서 service 자동 출석 대기
//
//                        repeatCount--;//재요청 횟수 차감
//                        PreferenceManager.SaveInteger(getApplicationContext(), "repeatCount", repeatCount);//횟수 저장
//
//                        Toast.makeText(getApplicationContext(),"재 출석 요청을 보내지 않음",Toast.LENGTH_SHORT).show();
//
//
//                        String Major = PreferenceManager.GetString(getApplicationContext(), "Major");
//                        String Minor = PreferenceManager.GetString(getApplicationContext(), "Minor");
//                        String UUID = PreferenceManager.GetString(getApplicationContext(), "UUID");
//
//                        Intent intent = new Intent(RepeatActivity.this, ScanActivity.class);
//                        intent.putExtra("Major", Major);
//                        intent.putExtra("Minor", Minor);
//                        intent.putExtra("UUID", UUID);
//                        startActivity(intent);
//                        finish();
//                    }
//                }else {
//                    NetworkManager.list_x++;
//                    if(NetworkManager.list_x < NetworkManager.list.size()){
//                        Intent intent = new Intent(RepeatActivity.this, ScanActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }else if(NetworkManager.list_x >= NetworkManager.list.size()) {
//                        NetworkManager.list_x=0;
//                        Toast.makeText(getApplicationContext(),"오늘의 강의가 전부 끝났음",Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(RepeatActivity.this,ScanActivity.class);
//                        startActivity(intent);
//                        finish();
//
//                    }
//                }
//
//
//            }
//
//
//
//        });

    }
    public void TimeCheck(){
        repeatCount = PreferenceManager.GetInteger(getApplicationContext(),"repeatCount");//횟수 불러옴
        if(repeatCount !=0){
            networkManager = new NetworkManager();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");

            String today = dateFormat.format(new Date());
            Date dateNow = null;
            try {
                dateNow = dateFormat.parse(today);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String start_time = PreferenceManager.GetString(getApplicationContext(),"start_time");
            Log.d("okhyo","Repeat Activity start time : " + start_time);

            Date dateCreated = null;
            try {
                dateCreated = dateFormat.parse(String.valueOf(start_time));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            compare = dateNow.compareTo(dateCreated);
            // 1이면 현재시간이 더 큰 값 0 이면 일치 -1이면 비교 시간 이전
            Log.d("okhyo", "compare : "+ String.valueOf(compare));
        }
    }

//    private void setAlarm(Context context) throws ParseException { //자동화를 위한 알람설정
//        Calendar calendar = Calendar.getInstance();
//        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
//        calendar.setTime(dateFormat.parse(NetworkManager.start_time));
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent("ALARM_ALERT");
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
//
//        alarmManager.set(AlarmManager.RTC,calendar.getTimeInMillis(), pendingIntent);
//    }

    public class TimeReceiver extends BroadcastReceiver {
        public TimeReceiver(){ }

        @Override
        public void onReceive(Context context, Intent intent) {


        }
    }

    public void PushCheck(){ // 완료후 PUSH 알림
        notificationManager = (NotificationManager) RepeatActivity.this.getSystemService(RepeatActivity.this.NOTIFICATION_SERVICE);

        Intent intent = new Intent(RepeatActivity.this.getApplicationContext(), RepeatActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(getBaseContext(), RepeatActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);

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

}

