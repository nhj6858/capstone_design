package com.example.loginUI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    EditText loginid, loginpw;
    static String requestID;
    static String requestPW;
    String responseTK, beacon_uuid;
    static String loginTK; // 자동로그인 여부 확인
    Button btn;
    InputMethodManager imm;
    private Intent mBackgroundServiceIntent;
    private BackgroundService mBackgroundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBackgroundService = new BackgroundService(getApplicationContext());
        mBackgroundServiceIntent = new Intent(getApplicationContext(), mBackgroundService.getClass());
        // 서비스가 실행 중인지 확인
        if (!BootReceiver.isServiceRunning(this, mBackgroundService.getClass())) {
            // 서비스가 실행하고 있지 않는 경우 서비스 실행
            startService(mBackgroundServiceIntent);
        }

        loginid = findViewById(R.id.loginid);
        loginpw = findViewById(R.id.loginpw);
        loginpw.setTransformationMethod(new PasswordTransformationMethod());
        btn = findViewById(R.id.button);


        loginTK = PreferenceManager.GetString(getApplicationContext(), "token"); // token 에 저장된 값을 불러옴

        if (!(loginTK.isEmpty())) {// 자동로그인
            requestID = PreferenceManager.GetString(getApplicationContext(), "username");
            requestPW = PreferenceManager.GetString(getApplicationContext(), "password");
            loginid.setText(requestID);
            loginpw.setText(requestPW);
            LoginRequest();//과정을 보여주기 위해 버튼 시연을 위한 임시 주석처리
        }

        loginid.setOnEditorActionListener(editorActionListener);
        loginpw.setOnEditorActionListener(editorActionListener);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 로그인 버튼 클릭시 로그인
                requestID = loginid.getText().toString();
                requestPW = loginpw.getText().toString();
                Log.d("okhyo", requestID + requestPW);
                LoginRequest();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    public void linearOnClick(View view) { //키보드 숨기기 설정
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(loginid.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(loginpw.getWindowToken(), 0);

    }

    // EditText 입력 시 다음항목 입력시 포커스 및 다음버튼으로 바뀌기
    EditText.OnEditorActionListener editorActionListener = new EditText.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                imm.hideSoftInputFromWindow(loginpw.getWindowToken(), 0);
                v.clearFocus();
                return true;
            }
            return false;
        }
    };


    public void LoginRequest() { // 로그인 과정

        NetworkManager networkManager = new NetworkManager();
        Call<ResponseBody> login = networkManager.LoginRequest(requestID,requestPW);


        login.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    try {
                        String res = response.body().string();
                        JSONObject jsonObject = new JSONObject(res);
                        responseTK = jsonObject.getString("token");//callback 된 token 저장
                        beacon_uuid = jsonObject.getString("uuid");
                        Log.d("okhyo responseTK",responseTK);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }

                    if (loginTK.isEmpty()) { // 자동 로그인을 위해 각각의 값 저장
                        PreferenceManager.SaveString(getApplicationContext(), "token", responseTK);
                        PreferenceManager.SaveString(getApplicationContext(), "username", requestID);
                        PreferenceManager.SaveString(getApplicationContext(), "password", requestPW);
                    }

                }
                PreferenceManager.SaveString(getApplicationContext(),"uuid",beacon_uuid);

                responseTK = PreferenceManager.GetString(getApplicationContext(),"token");
                Log.d("okhyo","token" + responseTK);

                if (!(responseTK.isEmpty())) { // token 이 저정된 경우 로그인 창으로 넘어감
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    //intent.putExtra("beacon_uuid",beacon_uuid);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                 Toast.makeText(getApplicationContext(), "ID, PW 를 확인하세요.", Toast.LENGTH_SHORT).show();
            }
        });


    }


}

