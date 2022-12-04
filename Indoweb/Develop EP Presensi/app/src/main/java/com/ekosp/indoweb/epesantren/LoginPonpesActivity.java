package com.ekosp.indoweb.epesantren;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.ekosp.indoweb.epesantren.databinding.ActivityLoginPonpesBinding;
import com.ekosp.indoweb.epesantren.helper.ApiClient;
import com.ekosp.indoweb.epesantren.helper.ApiInterface;
import com.ekosp.indoweb.epesantren.helper.SessionManager;
import com.ekosp.indoweb.epesantren.model.AndroidVersionResponse;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.okhttp.MediaType;

import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPonpesActivity extends Activity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "LoginPonpesActivity";
    private static final int REQUEST_LANJUT = 0;
    MediaType JSON;
    private ProgressDialog progressDialog;
    private DataPonpes mDataPonpes;
    private SessionManager session;
    private String latestAppVersion = "";
    private String thisAppVersion = "";

    private PermissionManager permission;

    private ActivityLoginPonpesBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPonpesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        JSON = MediaType.parse("application/json; charset=utf-8");
        // Session Manager
        session = new SessionManager(this);

        binding.btnLanjut.setOnClickListener(v -> lanjut());

        String kodes = session.getSavedKodes();
        if (kodes != null) {
            binding.inputKodes.setText(kodes);
        }

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            binding.tvVersiApk.setText(String.format("versi %s", version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        checkForLatestAppVersion();
    }

    public void hapusKodes(View v) {
        session.deleteSavedKodes();
        binding.inputKodes.setText("");
        StyleableToast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT, R.style.mytoast).show();
    }

    public void lanjut() {
        Log.d(TAG, "Lanjut");

        binding.btnLanjut.setEnabled(false);
        progressDialog = new ProgressDialog(LoginPonpesActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Mohon Tunggu...");
        progressDialog.show();

        if (!validate()) {
            onLanjutFailed();
            return;
        }
        executeLanjut();
    }

    private void executeLanjut() {
        String kodes = Objects.requireNonNull(binding.inputKodes.getText()).toString();

        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataPonpes> call = apiService.checkPonpes(kodes);
            call.enqueue(new Callback<DataPonpes>() {
                @Override
                public void onResponse(@NonNull Call<DataPonpes> call, @NonNull Response<DataPonpes> response) {
                    if (response.isSuccessful()) {
                        mDataPonpes = response.body();
                        if (Objects.requireNonNull(mDataPonpes).getCorrect()) {
                            gotoLoginActivity(mDataPonpes);
                        } else {
                            onLanjutFailed();
                        }
                    } else
                        StyleableToast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                }

                @Override
                public void onFailure(@NonNull Call<DataPonpes> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    private void isThisAppLatestVersion(String latestAppVersion) {
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            thisAppVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!thisAppVersion.equalsIgnoreCase(latestAppVersion)) {
            // close apps, need updated apps
            AlertDialog alertDialog = new AlertDialog.Builder(LoginPonpesActivity.this).create();
            alertDialog.setTitle("Info");
            alertDialog.setMessage("Anda Membutuhkan Versi App Terbaru. Versi " + latestAppVersion);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });
            alertDialog.show();
        }
    }

    private void checkForLatestAppVersion() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<AndroidVersionResponse> call = apiService.getAppsVersion();
        call.enqueue(new Callback<AndroidVersionResponse>() {
            @Override
            public void onResponse(@NonNull Call<AndroidVersionResponse> call, @NonNull Response<AndroidVersionResponse> response) {
                if (response.isSuccessful()) {
                    latestAppVersion = Objects.requireNonNull(response.body()).getData().getVersionName();
                    isThisAppLatestVersion(latestAppVersion);
                } else
                    StyleableToast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
            }

            @Override
            public void onFailure(@NonNull Call<AndroidVersionResponse> call, @NonNull Throwable t) {
                Log.d("RETROFIT", "failed to fetch data from API" + t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LANJUT) {
            if (resultCode == RESULT_OK) {
                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLanjutFailed() {
        StyleableToast.makeText(this, "Masukan Kode Pesantren Yang Terdaftar", Toast.LENGTH_LONG, R.style.mytoast_danger).show();
        binding.btnLanjut.setEnabled(true);
        progressDialog.dismiss();
    }

    public boolean validate() {
        boolean valid = true;
        String kodes = Objects.requireNonNull(binding.inputKodes.getText()).toString();

        if (kodes.isEmpty()) {
            binding.inputKodes.setError("Masukan Kode Pesantren Yang Valid");
            valid = false;
        } else {
            binding.inputKodes.setError(null);
        }

        return valid;
    }

    private void gotoLoginActivity(DataPonpes dp) {
        binding.btnLanjut.setEnabled(true);
        progressDialog.dismiss();
        // simpan di sessionmanager
        session.createLanjutSession(dp);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permission.checkResult(requestCode, permissions, grantResults);
    }
}