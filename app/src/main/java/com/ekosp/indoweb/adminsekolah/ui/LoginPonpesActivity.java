package com.ekosp.indoweb.adminsekolah.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ekosp.indoweb.adminsekolah.R;
import com.ekosp.indoweb.adminsekolah.databinding.ActivityLoginPonpesBinding;
import com.ekosp.indoweb.adminsekolah.helper.ApiClient;
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface;
import com.ekosp.indoweb.adminsekolah.helper.SessionManager;
import com.ekosp.indoweb.adminsekolah.model.AndroidVersionResponse;
import com.ekosp.indoweb.adminsekolah.model.DataSekolah;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPonpesActivity extends Activity {

    private static final String TAG = "LoginPonpesActivity";

    private PermissionManager permission;
    private SessionManager session;
    private DataSekolah dataSekolah;

    private String latestAppVersion = "";
    private String thisAppVersion = "";

    private ActivityLoginPonpesBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPonpesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        // Session Manager
        session = new SessionManager(this);

        String schoolCodeSM = session.getSavedKodes();
        binding.inputSchoolCode.setText(schoolCodeSM);

        binding.btnLogin.setOnClickListener(v -> checkLogin());
        binding.deleteData.setOnClickListener(v -> deleteData());

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            binding.tvVersionApk.setText(String.format("versi %s", version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        checkForLatestAppVersion();
    }

    private void deleteData() {
        session.deleteSavedKodes();
        binding.inputSchoolCode.setText("");
        StyleableToast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT, R.style.mytoast).show();
    }

    private void checkLogin() {
        Log.d(TAG, "Lanjut");

        String schoolCode = Objects.requireNonNull(binding.inputSchoolCode.getText()).toString();

        if (schoolCode.isEmpty()) {
            binding.inputSchoolCode.setError("Kode Sekolah Tidak Boleh Kosong");
            binding.inputSchoolCode.requestFocus();
        } else {
            checkServer(schoolCode);
        }
    }

    private void checkServer(String schoolCode) {
        try {
            showLoading(true);
            binding.deleteData.setEnabled(false);
            binding.btnLogin.setEnabled(false);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataSekolah> call = apiService.checkSchool(schoolCode);
            call.enqueue(new Callback<DataSekolah>() {
                @Override
                public void onResponse(@NonNull Call<DataSekolah> call, @NonNull Response<DataSekolah> response) {
                    showLoading(false);
                    binding.deleteData.setEnabled(true);
                    binding.btnLogin.setEnabled(true);
                    dataSekolah = response.body();
                    if (response.isSuccessful() && dataSekolah != null) {
                        if (dataSekolah.getCorrect()) {
                            onLoginSuccess(dataSekolah.getData());
                        } else {
                            StyleableToast.makeText(LoginPonpesActivity.this, dataSekolah.getMessage(), Toast.LENGTH_LONG, R.style.mytoast_danger).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DataSekolah> call, @NonNull Throwable t) {
                    showLoading(false);
                    binding.deleteData.setEnabled(true);
                    binding.btnLogin.setEnabled(true);
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    StyleableToast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    private void onLoginSuccess(DataSekolah.SekolahData data) {
        // save in sessionManager
        session.createLanjutSession(data);

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
            alertDialog.setTitle("Info!");
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
                } else {
                    Log.e(TAG, "LatestAppVersion: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AndroidVersionResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permission.checkResult(requestCode, permissions, grantResults);
    }

    private void showLoading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}