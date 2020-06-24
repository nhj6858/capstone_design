package com.example.loginUI;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result2);

        resultTxt = findViewById(R.id.ResultTxt);
        NetworkManager networkManager = new NetworkManager();
        try {
            Call<ResponseBody> getResult = networkManager.Getresult(getApplicationContext());
            getResult.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            String  res = response.body().string();
                           // if (res == null) return;
                            JSONObject jsonObject = new JSONObject(res);
                            lecture = jsonObject.getString("lecture");
                            attend = jsonObject.getString("final_attend");

                            resultTxt.setText(lecture + " : [출석 결과 : " + attend + "  ]");

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "수업의 모든 출석이 진행되었는지 확인 요망", Toast.LENGTH_SHORT).show();


    }

    @Override
    protected void onStart() {
        super.onStart();



    }
}
