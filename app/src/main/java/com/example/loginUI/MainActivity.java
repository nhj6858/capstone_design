package com.example.loginUI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    EditText loginid,loginpw;
    static String requestID;
    static String requestPW;
    String responseTK;
    static String loginTK; // 자동로그인 여부 확인
    Button btn;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loginid=findViewById(R.id.loginid);
        loginpw=findViewById(R.id.loginpw);
        loginpw.setTransformationMethod(new PasswordTransformationMethod());
        btn=findViewById(R.id.button);

        SharedPreferences sharedPreferences = getSharedPreferences("loginFile",MODE_PRIVATE);
        loginTK = sharedPreferences.getString("token",""); // token 에 저장된 값을 불러옴

        if(!(loginTK.isEmpty())){// 자동로그인
            requestID = sharedPreferences.getString("username","");
            requestPW = sharedPreferences.getString("password","");
            LoginRequest();
        }

        loginid.setOnEditorActionListener(editorActionListener);
        loginpw.setOnEditorActionListener(editorActionListener);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 로그인 버튼 클릭시 로그인
                requestID = loginid.getText().toString();
                requestPW = loginpw.getText().toString();
                LoginRequest();
            }
        });

    }

    public void linearOnClick(View view) { //키보드 숨기기 설정
        imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(loginid.getWindowToken(),0);
        imm.hideSoftInputFromWindow(loginpw.getWindowToken(),0);
    }

    // EditText 입력 시 다음항목 입력시 포커스 및 다음버튼으로 바뀌기
    EditText.OnEditorActionListener editorActionListener = new EditText.OnEditorActionListener(){

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL){
                if(imm.hideSoftInputFromWindow(loginpw.getWindowToken(),0)){
                    v.setInputType(EditorInfo.TYPE_NULL);
                    //v.setFocusable(false);
                    v.clearFocus();
                }
                return true;
            }
            return false;
        }
    };


    public void LoginRequest() { // 로그인 과정
        NetworkManager networkManager = new NetworkManager();
        networkManager.LoginRequest();

        responseTK = PreferenceManager.GetString(getApplicationContext(),"token");
        Log.d("hyo Login",responseTK);


        if (!(responseTK.isEmpty())) { // token 이 저정된 경우 로그인 창으로 넘어감
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}

