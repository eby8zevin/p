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

import com.bumptech.glide.Glide;
import com.ekosp.indoweb.epesantren.databinding.ActivityLoginBinding;
import com.ekosp.indoweb.epesantren.helper.ApiClient;
import com.ekosp.indoweb.epesantren.helper.ApiInterface;
import com.ekosp.indoweb.epesantren.helper.SessionManager;
import com.ekosp.indoweb.epesantren.model.AndroidVersionResponse;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.ekosp.indoweb.epesantren.model.DataUser;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.okhttp.MediaType;

import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    MediaType JSON;
    private ProgressDialog progressDialog;
    private DataUser mDataUser;
    private DataPonpes mDataPonpes;
    private SessionManager session;
    private String latestAppVersion = "";
    private String thisAppVersion = "";

    private PermissionManager permission;
    public static String pass;

    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        JSON = MediaType.parse("application/json; charset=utf-8");
        // Session Manager
        session = new SessionManager(this);

        binding.btnLogin.setOnClickListener(v -> login());
        binding.deleteData.setOnClickListener(view -> deleteData());

        String user = session.getSavedUsername();
        if (user != null) {
            binding.inputUsername.setText(user);
        }

        mDataPonpes = session.getSessionDataPonpes();

        Glide.with(getApplicationContext())
                .load("http://" + mDataPonpes.getLogo())
                .error(R.drawable.epesantren_rbg)
                .into(binding.imgFromUrl);

        binding.welcomeText.setText(mDataPonpes.getNamaPonpes());

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            binding.tvVersiApk.setText(String.format("versi %s", version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        checkForLatestAppVersion();
    }

    public void deleteData() {
        session.deleteSavedUsername();
        binding.inputUsername.setText("");
        StyleableToast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT, R.style.mytoast).show();
    }

    public void login() {
        Log.d(TAG, "Login");

        binding.btnLogin.setEnabled(false);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Mohon Tunggu...");
        progressDialog.show();

        if (!validate()) {
            onLoginFailed();
            return;
        }

        executeLogin();
    }

    private void executeLogin() {
        String kodes = mDataPonpes.getKodes();
        String uname = Objects.requireNonNull(binding.inputUsername.getText()).toString();
        pass = Objects.requireNonNull(binding.inputPin.getText()).toString();

        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataUser> call = apiService.checkLogin(kodes, uname, pass);
            call.enqueue(new Callback<DataUser>() {
                @Override
                public void onResponse(@NonNull Call<DataUser> call, @NonNull Response<DataUser> response) {
                    if (response.isSuccessful()) {
                        mDataUser = response.body();
                        if (Objects.requireNonNull(mDataUser).getCorrect()) {
                            gotoHomeActivity(mDataUser);
                        } else {
                            onLoginFailed();
                        }
                    } else
                        StyleableToast.makeText(LoginActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                }

                @Override
                public void onFailure(@NonNull Call<DataUser> call, @NonNull Throwable t) {
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
                } else
                    StyleableToast.makeText(LoginActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
            }

            @Override
            public void onFailure(@NonNull Call<AndroidVersionResponse> call, @NonNull Throwable t) {
                Log.d("RETROFIT", "failed to fetch data from API" + t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
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

    public void backToPonpes(View v) {
        Intent intent = new Intent(this, LoginPonpesActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onLoginFailed() {
        StyleableToast.makeText(this, "Masukan Data Login Yang Terdaftar", Toast.LENGTH_LONG, R.style.mytoast_danger).show();
        binding.btnLogin.setEnabled(true);
        progressDialog.dismiss();
    }

    public boolean validate() {
        boolean valid = true;
        String username = Objects.requireNonNull(binding.inputUsername.getText()).toString();
        String pin = Objects.requireNonNull(binding.inputPin.getText()).toString();

        if (username.isEmpty()) {
            binding.inputUsername.setError("Masukan NIP Yang Valid");
            valid = false;
        } else {
            binding.inputUsername.setError(null);
        }

        if (pin.isEmpty() || pin.length() < 2 || pin.length() > 20) {
            binding.inputPin.setError("Panjang Password Min. 2 Karakter & Max. 20 Karakter");
            valid = false;
        } else {
            binding.inputPin.setError(null);
        }
        return valid;
    }

    private void gotoHomeActivity(DataUser du) {
        binding.btnLogin.setEnabled(true);
        progressDialog.dismiss();
        // simpan di sessionmanager
        session.createLoginSession(du);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permission.checkResult(requestCode, permissions, grantResults);
    }
}