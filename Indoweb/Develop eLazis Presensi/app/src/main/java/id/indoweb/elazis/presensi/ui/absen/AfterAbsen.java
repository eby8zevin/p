package id.indoweb.elazis.presensi.ui.absen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import id.indoweb.elazis.presensi.R;
import id.indoweb.elazis.presensi.databinding.AfterAbsenBinding;
import id.indoweb.elazis.presensi.ui.MainActivity;

import id.indoweb.elazis.presensi.helper.GlobalVar;
import id.indoweb.elazis.presensi.helper.SessionManager;

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
        if (intent != null) TYPE = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI);

        binding.tvPresentSuccess.setText(String.format("Absen %s Anda Berhasil Dikirim !", TYPE));
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