package com.example.loginUI;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.concurrent.TimeUnit;

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
    String responseTK;
    public static String beacon_uuid = null;
    static int term = 0;
    public static int repeatCount;
    public static int list_x;
    int id;
    public int end;

    static ArrayList<String> list = new ArrayList<>();
    public static boolean getDataTK = false;
    public static boolean resultTK = false;
    public static boolean attendTK = false;
    public static boolean lecture_call = false;


    Retrofit retrofit;
    NetworkService networkService;
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS).readTimeout(50, TimeUnit.SECONDS);
    //OkHttpClient clientBuilder2 = new OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS).readTimeout(50, TimeUnit.SECONDS).build();

    public void Retrofit(){
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        retrofit = new Retrofit.Builder()
                .baseUrl(NetworkService.API_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        networkService = retrofit.create(NetworkService.class);
    }

    public Call<ResponseBody> LoginRequest(String id,String pw) {// 로그인 과정

        Retrofit();


        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("username", id);
        hashMap.put("password", pw);

        Call<ResponseBody> login = networkService.post_login(hashMap); // id pw 넣고 로그인 요청

        return login;

    }
    public boolean LectureCall(final Context context){
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        responseTK = PreferenceManager.GetString(context, "token");


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
                        //JSONArray jsonArray = new JSONArray(res);
//                        for(int i=0;i<jsonArray.length();i++){
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            list.add(jsonObject.getString(String.valueOf(i+1)));
//                        }
                        JSONObject jsonObject = new JSONObject(res);
                        list.clear();
                        for(int i=0;i<jsonObject.length();i++){
                            list.add(jsonObject.getString(String.valueOf(i+1)));
                        }
                        Log.d("okhyo",list.get(0));
                        list_x=0;
                        lecture_call=true;
                }catch (Exception e){

                }

                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("okhyo","lecture call save fail");
            }

        });


        return lecture_call;

    }


    public void DataRequest(final Context context) throws JSONException {//lecture id 에 따른 lecture 정보 가져오기


        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        responseTK = PreferenceManager.GetString(context, "token");

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

        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("username", requestID);
        hashMap.put("lecture_id", list.get(list_x));

        Call<ResponseBody> getData = networkService.get_data(hashMap);

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
                        Log.d("okhyo","beacon major" +beacon_major);

                        PreferenceManager.SaveString(context, "room_name", room_name);
                        PreferenceManager.SaveInteger(context, "term", term);
                        PreferenceManager.SaveInteger(context, "repeatCount", repeatCount);
                        PreferenceManager.SaveString(context, "beacon_major", beacon_major);
                        PreferenceManager.SaveString(context, "beacon_minor", beacon_minor);
                        PreferenceManager.SaveString(context, "start_time", start_time);

                        getDataTK = true;

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

    public void AttendPost(final Context context) throws ParseException { // 출석 요청

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        responseTK = PreferenceManager.GetString(context, "token");

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

        String startATD = "";
        Log.d("okhyo",startATD);

        startATD = timeCheck(context);

       // end = PreferenceManager.GetInteger(context,"repeatCount");
        //end = PreferenceManager.GetInteger(context,"repeatCount");
        Log.d("okhyo : " , "end : " +Integer.toString(end));
        Map<String, String> hashMap = new HashMap<>();

        hashMap.put("username", requestID);
        hashMap.put("lecture", list.get(list_x));
        hashMap.put("result", startATD);
        if(repeatCount ==1) {hashMap.put("end","end");getDataTK=false;list_x++;}


        Call<ResponseBody> S_attend = networkService.post_attend(hashMap);
        repeatCount = repeatCount -1;
        //PreferenceManager.SaveInteger(context,"repeatCount",end);



        S_attend.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if(end ==1){
//                    try {
//                        Getresult(context,list.get(list_x));
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                }

            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });


        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");


        if(repeatCount != 0){
            calendar.setTime(dateFormat.parse(start_time));
            calendar.add(Calendar.MINUTE,term);

            start_time = dateFormat.format(calendar.getTime());
//            repeatCount--;
//            Log.d("okhyo","RepeatCount : " +repeatCount);
            Log.d("okhyo","attend 이후 start time " + start_time);

            PreferenceManager.SaveString(context,"start_time",start_time);

        }

        resultTK = true;

    }


    public void logPost(final Context context, String value) throws ParseException {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        responseTK = PreferenceManager.GetString(context, "token");

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

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String today = dateFormat.format(new Date());


        Map<String, String> hashMap = new HashMap<>();

        hashMap.put("username", requestID);
        hashMap.put("lecture", list.get(list_x));
        hashMap.put("time", today);
        hashMap.put("check",value);

        Call<ResponseBody> logPost = networkService.log_post(hashMap);

        logPost.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    public Call<ResponseBody> Getresult(final Context context,String lectureCode) throws ParseException {
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.interceptors().clear();
        clientBuilder.addInterceptor(loggingInterceptor);

        responseTK = PreferenceManager.GetString(context, "token");

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

        Map<String, String> hashMap = new HashMap<>();

        hashMap.put("username", requestID);
        hashMap.put("lecture", lectureCode);

        Call<ResponseBody> getResult = networkService.get_result(hashMap);

        return getResult;
    }

    public String timeCheck(Context context) throws ParseException {

        start_time=PreferenceManager.GetString(context,"start_time");
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


            }  else {
                Check.append("\":\"ABSENT\"}");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }// 시간에 따른 출석 여부 입력


        return Check.toString();
    }

}
