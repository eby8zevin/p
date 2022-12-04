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

import com.bumptech.glide.Glide;
import com.ekosp.indoweb.adminsekolah.R;
import com.ekosp.indoweb.adminsekolah.databinding.ActivityLoginBinding;
import com.ekosp.indoweb.adminsekolah.helper.ApiClient;
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface;
import com.ekosp.indoweb.adminsekolah.helper.SessionManager;
import com.ekosp.indoweb.adminsekolah.model.AndroidVersionResponse;
import com.ekosp.indoweb.adminsekolah.model.DataSekolah;
import com.ekosp.indoweb.adminsekolah.model.DataUser;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";

    private PermissionManager permission;
    private SessionManager session;

    private DataSekolah.SekolahData sekolahData;
    private DataUser dataUser;

    private String latestAppVersion = "";
    private String thisAppVersion = "";

    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        // Session Manager
        session = new SessionManager(this);
        sekolahData = session.getSessionDataSekolah();

        Glide.with(getApplicationContext())
                .load(sekolahData.getLogo())
                .error(R.drawable.logoadminsekolah)
                .skipMemoryCache(true)
                .into(binding.imgLogo);
        binding.tvSchoolName.setText(sekolahData.getNama_sekolah());

        String nip = session.getSavedNIP();
        binding.inputNip.setText(nip);

        binding.btnBack.setOnClickListener(v -> back());
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

    private void back() {
        Intent i = new Intent(this, LoginPonpesActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    private void deleteData() {
        session.deleteSavedNIP();
        binding.inputNip.setText("");
        StyleableToast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT, R.style.mytoast).show();
    }

    private void checkLogin() {
        Log.d(TAG, "Login");

        String schoolCode = sekolahData.getKode_sekolah();
        String nip = Objects.requireNonNull(binding.inputNip.getText()).toString();
        String password = Objects.requireNonNull(binding.inputPassword.getText()).toString();
        session.savedPwd(password);

        if (nip.isEmpty()) {
            binding.inputNip.setError("NIP Tidak Boleh Kosong");
            binding.inputNip.requestFocus();
        } else if (password.isEmpty()) {
            binding.inputPassword.setError("Password Tidak Boleh Kosong");
            binding.inputNip.requestFocus();
        } else if (password.length() < 2 || password.length() > 20) {
            binding.inputPassword.setError("Panjang Password Min. 2 Karakter & Max. 20 Karakter");
            binding.inputPassword.requestFocus();
        } else {
            checkServer(schoolCode, nip, password);
        }
    }

    private void checkServer(String schoolCode, String nip, String password) {
        try {
            showLoading(true);
            binding.deleteData.setEnabled(false);
            binding.btnLogin.setEnabled(false);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataUser> call = apiService.checkLogin(schoolCode, nip, password);
            call.enqueue(new Callback<DataUser>() {
                @Override
                public void onResponse(@NonNull Call<DataUser> call, @NonNull Response<DataUser> response) {
                    showLoading(false);
                    binding.deleteData.setEnabled(true);
                    binding.btnLogin.setEnabled(true);
                    dataUser = response.body();
                    if (response.isSuccessful() && dataUser != null) {
                        if (dataUser.getCorrect()) {
                            onLoginSuccess(dataUser.getData());
                        } else {
                            StyleableToast.makeText(LoginActivity.this, dataUser.getMessage(), Toast.LENGTH_LONG, R.style.mytoast_danger).show();
                        }
                    } else {
                        Log.e(TAG, "onFailure: " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DataUser> call, @NonNull Throwable t) {
                    showLoading(false);
                    binding.deleteData.setEnabled(true);
                    binding.btnLogin.setEnabled(true);
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    StyleableToast.makeText(LoginActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    private void onLoginSuccess(DataUser.UserData data) {
        // save in sessionManager
        session.createLoginSession(data);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("Info");
            alertDialog.setMessage("Anda membutuhkan versi app terbaru. Versi " + latestAppVersion);
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
                    Log.e(TAG, "onFailure: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AndroidVersionResponse> call, @NonNull Throwable t) {
                Log.e("LatestAppVersion", "onFailure: " + t.getMessage());
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