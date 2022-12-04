package com.ekosp.indoweb.adminsekolah.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    //public static final String BASE_URL = "https://m.adminsekolah.net/rest-api/"; //server Real
    public static final String BASE_URL_DEMO = "https://demo.adminsekolah.net/rest-api/"; //server Demo

    public static final String BASE_URL = "http://192.168.1.206/indoweb/adminsekolah_presensi/rest-api/";

    // File upload url (replace the ip with your server address)
    public static final String FILE_UPLOAD_URL = BASE_URL + "absen_datangpulang.php";
    public static final String FILE_UPLOAD_URL_DEMO = BASE_URL_DEMO + "absen_datangpulang.php";

    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "uploads/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // add to fix java.net.SocketTimeoutException: timeout
            //https://stackoverflow.com/questions/39219094/sockettimeoutexception-in-retrofit
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}