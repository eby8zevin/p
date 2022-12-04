package id.indoweb.elazis.presensi.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;

import id.indoweb.elazis.presensi.R;
import id.indoweb.elazis.presensi.databinding.ActivityLoginBinding;
import id.indoweb.elazis.presensi.helper.ApiClient;
import id.indoweb.elazis.presensi.helper.ApiInterface;
import id.indoweb.elazis.presensi.helper.SessionManager;
import id.indoweb.elazis.presensi.model.AndroidVersionResponse;
import id.indoweb.elazis.presensi.model.DataPonpes;
import id.indoweb.elazis.presensi.model.DataUser;

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
    private PermissionManager permission;
    MediaType JSON;
    private SessionManager session;
    private DataPonpes mDataPonpes;
    private DataUser mDataUser;
    private ProgressDialog progressDialog;
    private String latestAppVersion = "";
    private String thisAppVersion = "";

    String password;
    private ActivityLoginBinding binding;
    LoginPonpesActivity getMethod = new LoginPonpesActivity();

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

        String nip = session.getSavedUsername();
        if (nip != null) {
            binding.inputNip.setText(nip);
        }

        mDataPonpes = session.getSessionDataPonpes();

        Glide.with(getApplicationContext())
                .load("http://" + mDataPonpes.getLogo() + "")
                .error(R.drawable.elazis)
                .into(binding.imgLogo);
        binding.tvSchoolName.setText(mDataPonpes.getNamaPonpes());

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
        getMethod.hideKeyboard();
    }

    private void deleteData() {
        getMethod.hideKeyboard();
        session.deleteSavedUsername();
        binding.inputNip.setText("");
        StyleableToast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT, R.style.mytoast).show();
    }

    private void checkLogin() {
        Log.d(TAG, "Login");

        String schoolCode = mDataPonpes.getKodes();
        String nip = Objects.requireNonNull(binding.inputNip.getText()).toString();
        password = Objects.requireNonNull(binding.inputPassword.getText()).toString();
        session.savePwd(password);

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

            binding.btnLogin.setEnabled(false);
            progressDialog = new ProgressDialog(LoginActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Mohon Tunggu...");
            progressDialog.show();
        }
    }

    private void checkServer(String schoolCode, String nip, String password) {
        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataUser> call = apiService.checkLogin(schoolCode, nip, password);
            call.enqueue(new Callback<DataUser>() {
                @Override
                public void onResponse(@NonNull Call<DataUser> call, @NonNull Response<DataUser> response) {
                    if (response.isSuccessful()) {
                        mDataUser = response.body();
                        if (Objects.requireNonNull(mDataUser).getCorrect()) {
                            onLoginSuccess(mDataUser);
                        } else {
                            onLoginFailed(mDataUser.getMessage());
                        }
                    } else
                        StyleableToast.makeText(LoginActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                }

                @Override
                public void onFailure(@NonNull Call<DataUser> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    private void onLoginSuccess(DataUser du) {
        binding.btnLogin.setEnabled(true);
        progressDialog.dismiss();
        // save in sessionManager
        session.createLoginSession(du);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void onLoginFailed(String loginFailed) {
        StyleableToast.makeText(this, loginFailed, Toast.LENGTH_LONG, R.style.mytoast_danger).show();
        binding.btnLogin.setEnabled(true);
        progressDialog.dismiss();
        getMethod.hideKeyboard();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permission.checkResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}