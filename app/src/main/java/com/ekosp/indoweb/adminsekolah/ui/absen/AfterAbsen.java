package com.ekosp.indoweb.adminsekolah.ui.absen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ekosp.indoweb.adminsekolah.ui.MainActivity;
import com.ekosp.indoweb.adminsekolah.R;
import com.ekosp.indoweb.adminsekolah.databinding.AfterAbsenBinding;
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar;
import com.ekosp.indoweb.adminsekolah.helper.SessionManager;

public class AfterAbsen extends Activity {

    private String TYPE;
    SessionManager session;
    private AfterAbsenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AfterAbsenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);

        Intent intent = getIntent();
        if (intent != null) TYPE = intent.getStringExtra(GlobalVar.PARAM_RESPONSE_UPLOAD);

        //binding.tvPresentSuccess.setText(String.format("Absen %s Anda Berhasil Dikirim !", TYPE));
        binding.tvPresentSuccess.setText(TYPE);
        binding.btnBack.setOnClickListener(v -> back());
    }

    private void back() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}