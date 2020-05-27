package com.example.loginUI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.loginUI.MainActivity.requestID;
import static com.example.loginUI.MainActivity.requestPW;


public class NetworkManager {
    static String username;

    static String lecture;
    static String lecture_id;
    static String room_code;
    static String room_name;
    static String beacon_major;
    static String beacon_minor;
    static String start_time;
    static String end_time;
    static String responseTK;
    static String beacon_uuid;
    static int term = 0;
    static int repeatCount;
    static int list_x;
    int id;

    static ArrayList<String> list = new ArrayList<>();
    static boolean getDataTK = false, resultTK = false;

    Context context;
    Retrofit retrofit;
    NetworkService networkService;
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

    public void LoginRequest() {// 로그인 과정

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(NetworkService.API_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        networkService = retrofit.create(NetworkService.class);

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("username", requestID);
        hashMap.put("password", requestPW);

        Call<ResponseBody> login = networkService.post_login(hashMap); // id pw 넣고 로그인 요청

        login.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String res = response.body().string();
                        JSONObject jsonObject = new JSONObject(res);
                        responseTK = jsonObject.getString("token");//callback 된 token 저장
                        beacon_uuid = jsonObject.getString("uuid");


                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }

                    if (MainActivity.loginTK.isEmpty()) { // 자동 로그인을 위해 각각의 값 저장
                        PreferenceManager.SaveString(context, "token", responseTK);
                        PreferenceManager.SaveString(context, "username", requestID);
                        PreferenceManager.SaveString(context, "password", requestPW);
                    } else {
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "ID, PW 를 확인하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void LectureCall(){
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);


        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                String token = " Token " + responseTK;
                Request newRequest;
                if (token != null && !token.equals("")) { // 토큰이 없는 경우
                    // Authorization 헤더에 토큰 추가
                    newRequest = chain.request().newBuilder().addHeader("Authorization", token).build();
                } else newRequest = chain.request();
                return chain.proceed(newRequest);
            }
        };// 토큰을 통해 해당 user 의 정보 요청

        clientBuilder.interceptors().add(interceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(NetworkService.API_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        networkService = retrofit.create(NetworkService.class);


        Call<ResponseBody> getlecture = networkService.get_lecture();

        getlecture.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try{
                        String res = response.body().string();
                        JSONArray jsonArray = new JSONArray(res);
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            list.add(jsonObject.getString(String.valueOf(i+1)));
                        }
                        list_x=0;
                    }catch (Exception e){

                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


    public void DataRequest() throws JSONException {//lecture id 에 따른 lecture 정보 가져오기

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);


        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("username", requestID);
        hashMap.put("lecture_id", list.get(0));

        final Call<ResponseBody> getData = networkService.get_data(hashMap);

        getData.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try { // user 의 정보 저장
                        String res = response.body().string();
                        JSONObject jsonObject = new JSONObject(res);
                        room_name = jsonObject.getString("name");
                        term = jsonObject.getInt("term");
                        repeatCount = jsonObject.getInt("count");
                        beacon_major = jsonObject.getString("beacon_major");
                        beacon_minor = jsonObject.getString("beacon_minor");
                        start_time = jsonObject.getString("start_time");

                        //getDataTK = true;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void AttendPost() throws ParseException { // 출석 요청

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);


        retrofit = new Retrofit.Builder()
                .baseUrl(NetworkService.API_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        networkService = retrofit.create(NetworkService.class);

        String startATD = timeCheck();

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("username", username);
        hashMap.put("lecture", lecture_id);
        hashMap.put("result", startATD);


        Call<ResponseBody> S_attend = networkService.post_attend(hashMap);


        S_attend.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                resultTK = true;
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");

        if(NetworkManager.repeatCount != 0){
            calendar.setTime(dateFormat.parse(start_time));
            calendar.add(Calendar.MINUTE,term);
            NetworkManager.start_time = dateFormat.format(calendar.getTime());
        }


    }

    public void ReAttend(){

    }

    public void EndAttend() {

        String endATD = null;

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);


        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                String token = " Token " + responseTK;
                Log.d("hyo token", token);
                Request newRequest;
                if (token != null && !token.equals("")) { // 토큰이 없는 경우
                    // Authorization 헤더에 토큰 추가
                    newRequest = chain.request().newBuilder().addHeader("Authorization", token).build();
                } else newRequest = chain.request();
                return chain.proceed(newRequest);
            }
        };// 토큰을 통해 해당 user 의 정보 요청

        clientBuilder.interceptors().add(interceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(NetworkService.API_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        networkService = retrofit.create(NetworkService.class);

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String today = dateFormat.format(new Date());

        Map<String, String> hashMap = new HashMap<>();

        hashMap.put("username", username);
        hashMap.put("lecture", lecture_id);

        try {
            Date dateNow = dateFormat.parse(today);
            Date dateCreated = dateFormat.parse(end_time);
            long duration = dateNow.getTime() - dateCreated.getTime();
            long min = duration / 60000;

            if (min <= 20 && min > 0) {
                endATD = "{\"" + end_time + "\":\"ATTEND\"}";
                Log.d("hyo", endATD);
            } else {
                endATD = "{\"" + end_time + "\":\"ABSENT\"}";
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        hashMap.put("result", endATD);

        Call<ResponseBody> E_attend = networkService.end_atted(hashMap);

        E_attend.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("hyo", "End Result Success");

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    public String timeCheck() throws ParseException {

        StringBuilder Check = new StringBuilder("{\"" + start_time);

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String today = dateFormat.format(new Date());
        Date dateNow = dateFormat.parse(today);



        try {
            Date dateCreated = dateFormat.parse(String.valueOf(start_time));
            long duration = dateNow.getTime() - dateCreated.getTime();
            long min = duration / 60000;

            if (min <= 5 && min > 0) {
                Check.append("\":\"ATTEND\"}");


            } else if (min > 5 && min <= 55) {
                Check.append("\":\"LATE\"}");

            } else {
                Check.append("\":\"ABSENT\"}");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }// 시간에 따른 출석 여부 입력


        return Check.toString();
    }

}
