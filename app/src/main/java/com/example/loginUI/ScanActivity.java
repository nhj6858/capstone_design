package com.example.loginUI;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1000;

    private MinewBeaconManager mMinewBeaconManager;

    private static final int REQUEST_ENABLE_BT = 2;
    private boolean isScanning = false;

    UserRssi comp = new UserRssi();
    private TextView tooltext,UserTxt,LectureTxt,TimeTxt;
    private boolean mIsRefreshing;
    private int state,compare;
    String Major;
    String Minor;
    String UUID;

    private RecyclerView mRecycle;
    private BeaconListAdapter mAdapter;
    TextView scanview, textView;
    Button btn,resultGo;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        initView();
        initManager();
        checkBluetooth();
        checkLocationPermition();
        initListener();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mRecycle.setVisibility(View.VISIBLE);
        scanview.setVisibility(View.VISIBLE);
        mMinewBeaconManager.startScan();
//        DataRequest();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecycle.setVisibility(View.VISIBLE);
        scanview.setVisibility(View.VISIBLE);
        mMinewBeaconManager.startScan();
        //AttendPost();
    }

    private void checkLocationPermition() { // 위치 확인 허가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                // 권한 없음
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION);
            } else {
                // ACCESS_FINE_LOCATION 에 대한 권한이 이미 있음.
            }
        }
// OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        else {
        }
    }

    /**
     * check Bluetooth state
     */
    private void checkBluetooth() { //블루투스 상태체크
        BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
        switch (bluetoothState) {
            case BluetoothStateNotSupported:
                Toast.makeText(this, "Not Support BLE", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BluetoothStatePowerOff:
                showBLEDialog();
                break;
            case BluetoothStatePowerOn:
                break;
        }
    }


    private void initView() { // 화면 달기
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        tooltext = (TextView) findViewById(R.id.tooltext);
        scanview = (TextView) findViewById(R.id.scanview);
        btn = (Button) findViewById(R.id.postbtn);//정해진 시간에 자동출석 체크 미구현으로 인해 버튼으로 대체
        UserTxt = (TextView) findViewById(R.id.UserTxt);
        LectureTxt = (TextView) findViewById(R.id.LectureTxt);
        TimeTxt = (TextView) findViewById(R.id.TimeTxt);
        resultGo = (Button)findViewById(R.id.resultGo);

        resultGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanActivity.this,ResultActivity.class);
                startActivity(intent);
                finish();
//                if(!(NetworkManager.list.isEmpty())){
//                    Intent intent = new Intent(ScanActivity.this,ResultActivity.class);
//                    startActivity(intent);
//                    finish();
//                }else{
//                    Toast.makeText(getApplicationContext(),"결과를 불러올 출석이 존재하지 않음",Toast.LENGTH_SHORT);
//                }


            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttendPost();
            }
        });


        mRecycle = (RecyclerView) findViewById(R.id.recyeler);
        layoutManager = new LinearLayoutManager(this);
        mRecycle.setLayoutManager(layoutManager);
        mAdapter = new BeaconListAdapter();
        mRecycle.setAdapter(mAdapter);
        mRecycle.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager
                .HORIZONTAL));

    }

    private void initManager() {
        mMinewBeaconManager = MinewBeaconManager.getInstance(this);
    }
    //비콘 설정 manager

    private void initListener() { // 비콘 스캔
        tooltext.setOnClickListener(new View.OnClickListener() { //비콘 스캔 버튼
            @Override
            public void onClick(View v) {

                if (mMinewBeaconManager != null) {
                    BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
                    switch (bluetoothState) {
                        case BluetoothStateNotSupported:
                            Toast.makeText(ScanActivity.this, "Not Support BLE", Toast.LENGTH_SHORT).show();
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
                    tooltext.setText("Start");
                    if (mMinewBeaconManager != null) {
                        Log.d("beacon", "stop scan");
                        mMinewBeaconManager.stopScan();
                    }
                } else {
                    isScanning = true;
                    tooltext.setText("Stop");
                    try {
                        Log.d("beacon", "start scan");
                        mMinewBeaconManager.startScan();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if (mMinewBeaconManager != null) {
            BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
            switch (bluetoothState) {
                case BluetoothStateNotSupported:
                    Log.d("okhyo", "Not Support BLE");
                    Toast.makeText(ScanActivity.this, "Not Support BLE", Toast.LENGTH_SHORT).show();
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
            tooltext.setText("Start");
            if (mMinewBeaconManager != null) {
                mMinewBeaconManager.stopScan();
            }
        } else {
            isScanning = true;
            tooltext.setText("Stop");
            try {
                mMinewBeaconManager.startScan();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mMinewBeaconManager.setDeviceManagerDelegateListener(new MinewBeaconManagerListener() {
            /**
             *   if the manager find some new beacon, it will call back this method.
             *   관리자가 새로운 비콘을 찾으면 이 메소드를 호출합니다.
             *  @param minewBeacons  new beacons the manager scanned
             */
            @Override
            public void onAppearBeacons(List<MinewBeacon> minewBeacons) {
                if(NetworkManager.getDataTK){
                    for (MinewBeacon minewBeacon : minewBeacons) {
                        log_post(minewBeacon,"IN");
                        Log.d("beacon", " 비콘이 나타남");
                    }
                }

            }

            /**
             *  if a beacon didn't update data in 10 seconds, we think this beacon is out of rang, the manager will call back this method.
             *  만약 비콘이 10 초 안에 데이터를 업데이트하지 않았다면, 우리는이 비콘이 울렸다 고 생각합니다. 관리자는이 방법을 다시 호출 할 것입니다.            *
             *              *  @param minewBeacons beacons out of range
             */
            @Override
            public void onDisappearBeacons(List<MinewBeacon> minewBeacons) {
                if(NetworkManager.getDataTK){
                    for (MinewBeacon minewBeacon : minewBeacons) {
                        log_post(minewBeacon,"OUT");
                        Log.d("beacon", " 비콘이 사라짐");
                    }
                }



            }

            /**
             *  the manager calls back this method every 1 seconds, you can get all scanned beacons.
             *  관리자는이 메소드를 1 초마다 다시 호출하면 모든 스캔 비콘을 얻을 수 있습니다.
             *  @param minewBeacons all scanned beacons
             */
            @Override
            public void onRangeBeacons(final List<MinewBeacon> minewBeacons) {
                //프록시 메소드를 통해 스캔 데이터 업데이트를 얻습니다, 주기적으로 콜백하여 주변 장치의 최신 스캔데이터 가져옴
                // 비콘이 범위에 있을시 주기적 스캔
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("beacon", "scanning");
                        Collections.sort(minewBeacons, comp);
                        if (state == 1 || state == 2) {
                        } else {
                            for (MinewBeacon minewBeacon : minewBeacons) {
                                Major = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getStringValue();
                                Minor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();
                                UUID = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).getStringValue();
                                scanview.setText("Major : " + Major + "  Minor : " + Minor + "  UUID : " + UUID);
                                mAdapter.setItems(minewBeacons);

                                String deviceName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
                                Log.d("beacon",deviceName + " 비콘 스캔중");
                                if (UUID.equals(PreferenceManager.GetString(getApplicationContext(), "uuid"))) {
                                    if (NetworkManager.list.isEmpty()) {
                                        LectureCall();
                                    } else {
                                        if (NetworkManager.list_x >= NetworkManager.list.size()) {
//                                            Intent intent = new Intent(ScanActivity.this,ResultActivity.class);
//                                            startActivity(intent);
//                                            finish();
                                        } else {
//                                            Log.d("okhyo","getdataTK" + NetworkManager.getDataTK);
                                            if (NetworkManager.getDataTK == false) {
                                                DataRequest();
                                            } else if (NetworkManager.getDataTK) {
                                                btn.setText("ATTEND USUABLE");
                                                AttendPost();
                                                //mRecycle.setVisibility(View.GONE);
                                                //scanview.setVisibility(View.GONE);

                                                String username = PreferenceManager.GetString(getApplicationContext(), "username");
                                                String room_name = PreferenceManager.GetString(getApplicationContext(), "room_name");
                                                String start_time = PreferenceManager.GetString(getApplicationContext(), "start_time");
                                                UserTxt.setText("학번 : " + username);
                                                LectureTxt.setText("현재 강의명 : " + room_name);
                                                TimeTxt.setText("강의 시작 시간 : " + start_time);

                                            }
                                        }

                                    }
                                }



                            }
                        }// 비콘 major minor uuid 값 저장


                    }
                });
            }

            /**
             *  the manager calls back this method when BluetoothStateChanged.
             *  관리자는 BluetoothStateChanged 일 때이 메소드를 다시 호출합니다.
             *  @param state BluetoothState
             */
            @Override
            public void onUpdateState(BluetoothState state) {
                switch (state) {
                    case BluetoothStatePowerOn:
                        Toast.makeText(getApplicationContext(), "BluetoothStatePowerOn", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothStatePowerOff:
                        Toast.makeText(getApplicationContext(), "BluetoothStatePowerOff", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stop scan
        mMinewBeaconManager.stopScan();
        if (isScanning) {
            mMinewBeaconManager.stopScan();
        }
    }

    private void showBLEDialog() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                break;
        }
    }

    public void LectureCall() {

        NetworkManager networkManager = new NetworkManager();
        try {
            networkManager.LectureCall(getApplicationContext());
            Log.d("okhttp","network lectuercall success");

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("okhttp","network lecture call fail");
        }


    }

    public void DataRequest()  {// server 에 데이터 전송
        NetworkManager networkManager = new NetworkManager();
        // lecture 에 따른 lecture 정보 가져오기
        try {
            networkManager.DataRequest(getApplicationContext());
            Log.d("okhttp","network datarequest success");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("okhttp","network datarequest fail");
        }

    }

    public void AttendPost() {
        final NetworkManager networkManager = new NetworkManager();


        final String beacon_major = PreferenceManager.GetString(getApplicationContext(), "beacon_major");
        final String beacon_minor = PreferenceManager.GetString(getApplicationContext(), "beacon_minor");
        final int repeatCount = PreferenceManager.GetInteger(getApplicationContext(), "repeatCount");


        TimeCheck();
        if (compare < 1) {
            Toast.makeText(getApplicationContext(), "출석 시간이 되지 않음", Toast.LENGTH_LONG).show();
        } else {
            Log.d("okhyo", "attend activity repeatcount : " + repeatCount);

            if (repeatCount <= 0) {
                NetworkManager.list_x++;
                if (NetworkManager.list_x < NetworkManager.list.size()) {
                    Intent Aintent = new Intent(ScanActivity.this, ScanActivity.class);
                    startActivity(Aintent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "오늘의 강의가 전부 끝났음", Toast.LENGTH_SHORT).show();
                    mMinewBeaconManager.stopScan();
                    Intent Bintent = new Intent(ScanActivity.this, ResultActivity.class);
                    startActivity(Bintent);
                    finish();
                }
            }

            if (Major.equals(beacon_major) && Minor.equals(beacon_minor)) {
                //비콘 스캔 값과 가져온 비콘 값을 비교후 일치시 전송
                if (repeatCount != 0) {
                    try {
                        networkManager.AttendPost(getApplicationContext());
                    } catch (ParseException e) {
                        Toast.makeText(getApplicationContext(), "출석요청 에러", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                    if (NetworkManager.resultTK) {
                        NetworkManager.resultTK = false;
                        btn.setText("ATTEND");
                        Intent intent = new Intent(ScanActivity.this, RepeatActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "NetworkManager resultTK error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "수업이 끝났는데 출석 요청", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "현재 강의실의 비콘값과 일치하지 않음", Toast.LENGTH_SHORT).show();
            }
        }
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(compare<1){
//                    Toast.makeText(getApplicationContext(),"출석 시간이 되지 않음",Toast.LENGTH_LONG).show();
//                }else{
//                    if (Major.equals(beacon_major) && Minor.equals(beacon_minor)) {
//                        //비콘 스캔 값과 가져온 비콘 값을 비교후 일치시 전송
//                        if (repeatCount != 0) {
//                            try {
//                                networkManager.AttendPost(getApplicationContext());
//
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//
//
//                            if (NetworkManager.resultTK) {
//                                NetworkManager.getDataTK=false;
//                                btn.setText("ATTEND");
//                                Intent intent = new Intent(ScanActivity.this, RepeatActivity.class);
//                                startActivity(intent);
//                                finish();
//                            }
//                        }
//
//                    }
//                }
//
//            }
//        });


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
            Log.d("okhyo", "Time Check start time : " + start_time);

            Date dateCreated = null;
            try {
                dateCreated = dateFormat.parse(String.valueOf(start_time));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            compare = dateNow.compareTo(dateCreated);
            // 1이면 현재시간이 더 큰 값 0 이면 일치 -1이면 비교 시간 이전
            Log.d("okhyo", "Time check compare : " + String.valueOf(compare));
        }
    }

    public void log_post(MinewBeacon minewBeacon,String value){
        String beacon_major = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getStringValue();
        String beacon_minor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();
        String lecture_major = PreferenceManager.GetString(getApplicationContext(),"beacon_major");
        String lecture_minor = PreferenceManager.GetString(getApplicationContext(),"beacon_minor");

        if(beacon_major.equals(lecture_major) && beacon_minor.equals(lecture_minor)){
            NetworkManager networkManager = new NetworkManager();
            try {
                networkManager.logPost(getApplicationContext(), value);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

}
