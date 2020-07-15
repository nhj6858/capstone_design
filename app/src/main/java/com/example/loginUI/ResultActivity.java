package com.example.loginUI;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    TextView resultTxt;
    String lecture, attend;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                Intent intent = new Intent(ResultActivity.this,ScanActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.resultToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorwhite), PorterDuff.Mode.SRC_ATOP);



        resultTxt = findViewById(R.id.ResultLecTxt);
//        NetworkManager networkManager = new NetworkManager();
//        try {
//            Call<ResponseBody> getResult = networkManager.Getresult(getApplicationContext());
//            getResult.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    if (response.isSuccessful()) {
//                        try {
//                            String  res = response.body().string();
//                           // if (res == null) return;
//                            JSONObject jsonObject = new JSONObject(res);
//                            lecture = jsonObject.getString("lecture");
//                            attend = jsonObject.getString("final_attend");
//
//                            resultTxt.setText("강의명 : "+lecture + "\n[출석 결과 : " + attend + "  ]");
//
//                        } catch (IOException | JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//                }
//            });
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        Toast.makeText(getApplicationContext(), "수업의 모든 출석이 진행되었는지 확인 요망", Toast.LENGTH_SHORT).show();


    }

    @Override
    protected void onStart() {
        super.onStart();



    }
}
