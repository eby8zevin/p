package com.ekosp.indoweb.adminsekolah.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ekosp.indoweb.adminsekolah.R;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends Activity {

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreen.this, LoginPonpesActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }, SPLASH_TIME_OUT);
    }
}