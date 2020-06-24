package com.example.loginUI;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AttendActivity extends AppCompatActivity {

    private MinewBeaconManager BeaconManager;
    TextView UserTxt,LectureTxt,TimeTxt;
    Button btn,resultbtn;
    String Major,Minor,UUID;
    int compare,state;
    UserRssi comp = new UserRssi();

    private boolean isScanning,isExist;
    private static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend);

        UserTxt = (TextView) findViewById(R.id.UserTxt);
        LectureTxt = (TextView) findViewById(R.id.LectureTxt);
        TimeTxt = (TextView) findViewById(R.id.TimeTxt);

        btn = (Button) findViewById(R.id.AttendBtn);

        resultbtn = (Button) findViewById(R.id.resultBtn2);

        resultbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendActivity.this,ResultActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Intent intent = getIntent();

        Major = intent.getExtras().getString("Major");
        Minor = intent.getExtras().getString("Minor");
        UUID = intent.getExtras().getString("UUID");

        initManager();
        initListener();

        int repeatCount = PreferenceManager.GetInteger(getApplicationContext(),"repeatCount");
        Log.d("okhyo","attend activity repeatcount : " + repeatCount);

        if(repeatCount <= 0){
            NetworkManager.list_x++;
            if(NetworkManager.list_x < NetworkManager.list.size()){
                Intent Aintent = new Intent(AttendActivity.this, ScanActivity.class);
                startActivity(Aintent);
                finish();
            }else{
                Toast.makeText(getApplicationContext(),"오늘의 강의가 전부 끝났음",Toast.LENGTH_SHORT).show();
                Intent Bintent = new Intent(AttendActivity.this,MainActivity.class);
                startActivity(Bintent);
                finish();
            }
        }

        AttendPost();
    }
    private void initManager() {
        BeaconManager = MinewBeaconManager.getInstance(this);
    }
    //비콘 설정 manager

    private void initListener() { // 비콘 스캔

                if (BeaconManager != null) {
                    BluetoothState bluetoothState = BeaconManager.checkBluetoothState();
                    switch (bluetoothState) {
                        case BluetoothStateNotSupported:
                            Toast.makeText(AttendActivity.this, "Not Support BLE", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case BluetoothStatePowerOff:
                            showBLEDialog();
                            return;
                        case BluetoothStatePowerOn:
                            break;
                    }
                }
                if (isScanning) {
                    isScanning = false;
                    if (BeaconManager != null) {
                        BeaconManager.stopScan();
                    }
                } else {
                    isScanning = true;
                    try {
                        BeaconManager.startScan();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


        BeaconManager.setDeviceManagerDelegateListener(new MinewBeaconManagerListener() {
            /**
             * if the manager find some new beacon, it will call back this method.
             * 관리자가 새로운 비콘을 찾으면 이 메소드를 호출합니다.
             *
             * @param minewBeacons new beacons the manager scanned
             */
            @Override
            public void onAppearBeacons(List<MinewBeacon> minewBeacons) {
                isExist=true;
                for (MinewBeacon minewBeacon : minewBeacons) {
                    if(isExist=true)log_post(minewBeacon,"IN");
                    Log.d("beacon",  " 비콘이 나타남");
                }
            }

            /**
             * if a beacon didn't update data in 10 seconds, we think this beacon is out of rang, the manager will call back this method.
             * 만약 비콘이 10 초 안에 데이터를 업데이트하지 않았다면, 우리는이 비콘이 울렸다 고 생각합니다. 관리자는이 방법을 다시 호출 할 것입니다.            *
             * *  @param minewBeacons beacons out of range
             */
            @Override
            public void onDisappearBeacons(List<MinewBeacon> minewBeacons) {
                isExist=false;
                for (MinewBeacon minewBeacon : minewBeacons) {
                    if(isExist=false) log_post(minewBeacon,"OUT");
                    Log.d("beacon", " 비콘이 사라짐");
                }

            }

            @Override
            public void onRangeBeacons(final List<MinewBeacon> minewBeacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(minewBeacons, comp);
                        if (state == 1 || state == 2) {
                        } else {
                            for (MinewBeacon minewBeacon : minewBeacons) {
                                Major = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getStringValue();
                                Minor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();

                                Log.d("beacon" ,Major + Minor + " 스캔중");

                            }
                        }


                    }// 비콘 major minor uuid 값 저장

                });
            }

            @Override
            public void onUpdateState(BluetoothState bluetoothState) {

            }
        });
    }

    public void log_post(MinewBeacon minewBeacon,String value){
        String beacon_major = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getStringValue();
        String beacon_minor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();
        String lecture_major = PreferenceManager.GetString(getApplicationContext(),"beacon_major");
        String lecture_minor = PreferenceManager.GetString(getApplicationContext(),"beacon_minor");

        if(beacon_major.equals(lecture_major) && beacon_minor.equals(lecture_minor)){
            NetworkManager networkManager = new NetworkManager();
            try {
                networkManager.logPost(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }



    private void showBLEDialog() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    public void AttendPost(){
        final NetworkManager networkManager = new NetworkManager();


        String username = PreferenceManager.GetString(getApplicationContext(), "username");
        String room_name = PreferenceManager.GetString(getApplicationContext(), "room_name");
        String start_time = PreferenceManager.GetString(getApplicationContext(),"start_time");
        final String beacon_major = PreferenceManager.GetString(getApplicationContext(), "beacon_major");
        final String beacon_minor = PreferenceManager.GetString(getApplicationContext(), "beacon_minor");
        final int repeatCount = PreferenceManager.GetInteger(getApplicationContext(), "count");


        UserTxt.setText("학번 : " + username);
        LectureTxt.setText("현재 강의실 : " + room_name);
        TimeTxt.setText("강의 시작 시간 : " + start_time);

        TimeCheck();
        btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(compare<1){
                        Toast.makeText(getApplicationContext(),"출석 시간이 되지 않음",Toast.LENGTH_LONG).show();
                    }else{
                        if (Major.equals(beacon_major) && Minor.equals(beacon_minor)) {
                            //비콘 스캔 값과 가져온 비콘 값을 비교후 일치시 전송
                            if (repeatCount != 0) {
                                try {
                                    networkManager.AttendPost(getApplicationContext());

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                                if (NetworkManager.resultTK) {
                                    Intent intent = new Intent(AttendActivity.this, RepeatActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                        }
                    }

                }
            });

    }
    public void TimeCheck(){
        int repeatCount = PreferenceManager.GetInteger(getApplicationContext(),"repeatCount");//횟수 불러옴
        if(repeatCount !=0){
            NetworkManager networkManager = new NetworkManager();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");

            String today = dateFormat.format(new Date());
            Date dateNow = null;
            try {
                dateNow = dateFormat.parse(today);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String start_time = PreferenceManager.GetString(getApplicationContext(),"start_time");
            Log.d("okhyo","Result Activity start time : " + start_time);

            Date dateCreated = null;
            try {
                dateCreated = dateFormat.parse(String.valueOf(start_time));
            } catch (ParseException e) {
                e.printStackTrace();
            }
           compare = dateNow.compareTo(dateCreated);
            // 1이면 현재시간이 더 큰 값 0 이면 일치 -1이면 비교 시간 이전
            Log.d("okhyo", "attend activity compare : "+ String.valueOf(compare));
        }
    }
}
