package com.example.loginUI;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NetworkService {


    public static final String API_URL = "http://15.165.91.167:8000/";

    //id pw 입력후 token 요청
    @FormUrlEncoded
    @POST("api-token-auth/")
    Call<ResponseBody>post_login(@FieldMap Map<String, String> options);

    //token을 header에 넣고 강의 ID 요청
    @GET("stdlect/")
    Call<ResponseBody>get_lecture();


    //token 을 header 에 넣고 user 정보 요청
    @POST("lecture/")
    Call<ResponseBody>get_data(@FieldMap Map<String,String> options);

    //출석여부 확인 후 출석 요청
    @FormUrlEncoded
    @POST("attend/")
    Call<ResponseBody>post_attend(@FieldMap Map<String,String> options);


    //수업종료시 출석체크
    @FormUrlEncoded
    @POST("attend/")
    Call<ResponseBody>end_atted(@FieldMap Map<String,String> options);
    //@Path("id") int id,

//    @GET("restmain/{pk}/")
//    Call<ResponseBody>get_Testpk(@Path("pk") int pk, @Query("format=") String json);

//    @FormUrlEncoded
//    @POST("restmain")
//    Call<Json_Test>post_Test(@FieldMap HashMap<String,Object> param);




    @DELETE("restmain/{pk}/")
    Call<ResponseBody>delete_Patch_Test(@Path("pk") int pk, @Query("format") String json);

}
