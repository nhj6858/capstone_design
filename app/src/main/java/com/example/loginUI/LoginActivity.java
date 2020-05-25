package com.example.loginUI;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import org.json.JSONException;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1000;

    private MinewBeaconManager mMinewBeaconManager;

    private static final int REQUEST_ENABLE_BT = 2;
    private boolean isScanning;

    UserRssi comp = new UserRssi();
    private TextView tooltext;
    private boolean mIsRefreshing;
    private int state;
    static String Major;
    static String Minor;
    static String UUID;

    TextView scanview,textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initManager();
        checkBluetooth();
        checkLocationPermition();
        initListener();

        if (NetworkManager.beacon_uuid == UUID) {
            try{
                LectureCall();
            }catch (Exception e){
            }
        }
       try {
           DataRequest();
       }catch (Exception e){

       }

    }

    private void checkLocationPermition() { // 위치 확인 허가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                // 권한 없음
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION);
            } else{
                // ACCESS_FINE_LOCATION 에 대한 권한이 이미 있음.
            }
        }
// OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        else{
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

        tooltext= (TextView) findViewById(R.id.tooltext);
        scanview = (TextView) findViewById(R.id.scanview);
        textView = (TextView) findViewById(R.id.text);
    }

    private void initManager() {
        mMinewBeaconManager = MinewBeaconManager.getInstance(this);
    }
    //비콘 설정 manager

    private void initListener() { // 비콘 스캔

        if (mMinewBeaconManager != null) {
            BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
            switch (bluetoothState) { // 블루투스 켜져 잇는지 확인
                case BluetoothStateNotSupported:
                    Toast.makeText(LoginActivity.this, "Not Support BLE", Toast.LENGTH_SHORT).show();
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
            if (mMinewBeaconManager != null) {
                mMinewBeaconManager.stopScan();
            }
        } else {
            isScanning = true;
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
            }

            /**
             *  if a beacon didn't update data in 10 seconds, we think this beacon is out of rang, the manager will call back this method.
             *  만약 비콘이 10 초 안에 데이터를 업데이트하지 않았다면, 우리는이 비콘이 울렸다 고 생각합니다. 관리자는이 방법을 다시 호출 할 것입니다.            *
             *              *  @param minewBeacons beacons out of range
             */
            @Override
            public void onDisappearBeacons(List<MinewBeacon> minewBeacons) {
                /*for (MinewBeacon minewBeacon : minewBeacons) {
                    String deviceName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
                    Toast.makeText(getApplicationContext(), deviceName + "  out range", Toast.LENGTH_SHORT).show();
                }*/
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
                        Collections.sort(minewBeacons, comp);
                        //Log.e("tag", state + "");
                        if (state == 1 || state == 2) {
                        } else {
                            for(MinewBeacon minewBeacon : minewBeacons){
                                Major = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Major).getStringValue();
                                Minor = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Minor).getStringValue();
                                UUID = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).getStringValue();
                                scanview.setText("Major : " + Major +"  Minor : " + Minor +"  UUID : " + UUID);
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

    public void LectureCall(){
        NetworkManager networkManager = new NetworkManager();
        try {
            networkManager.LectureCall();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void DataRequest() throws ParseException {// server 에 데이터 전송
        NetworkManager networkManager = new NetworkManager();


         // lecture 에 따른 lecture 정보 가져오기
            try {
                networkManager.DataRequest();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        String username = PreferenceManager.GetString(getApplicationContext(),"username");

        String now_room  = NetworkManager.room_name;
        String now_room_beacon_major = NetworkManager.beacon_major;
        String now_room_beacon_minor = NetworkManager.beacon_minor;


        textView.setText("학번 : "+ username + "  현재 강의실 : " + now_room );


        if(Major.equals(now_room_beacon_major) && Minor.equals(now_room_beacon_minor)){
            //비콘 스캔 값과 가져온 비콘 값을 비교후 일치시 전송
            if(NetworkManager.repeatCount != 0){
                networkManager.AttendPost();

                if(NetworkManager.resultTK= true){
                    Intent intent = new Intent(LoginActivity.this, ResultActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

        }


    }
}
