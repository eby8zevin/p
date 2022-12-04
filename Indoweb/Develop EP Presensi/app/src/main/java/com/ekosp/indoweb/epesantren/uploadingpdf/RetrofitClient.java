package com.ekosp.indoweb.epesantren.uploadingpdf;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL="http://ujipresensi.my.id/mobile-ep/";
    private static RetrofitClient myClient;
    private Retrofit retrofit;

    private RetrofitClient(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }

//    public static final String IMAGE_DIRECTORY_NAME = "uploads/";

    public static synchronized RetrofitClient getInstance(){
        if (myClient == null){
            myClient = new RetrofitClient();
        }
        return myClient;
    }

    public Api getAPI(){
        return retrofit.create(Api.class);

    }
}
