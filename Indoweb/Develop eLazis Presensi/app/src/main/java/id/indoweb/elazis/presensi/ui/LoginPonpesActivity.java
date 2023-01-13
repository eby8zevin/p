package id.indoweb.elazis.presensi.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.karan.churi.PermissionManager.PermissionManager;

import java.util.Objects;

import id.indoweb.elazis.presensi.R;
import id.indoweb.elazis.presensi.databinding.ActivityLoginPonpesBinding;
import id.indoweb.elazis.presensi.helper.ApiClient;
import id.indoweb.elazis.presensi.helper.ApiInterface;
import id.indoweb.elazis.presensi.helper.SessionManager;
import id.indoweb.elazis.presensi.model.AndroidVersionResponse;
import id.indoweb.elazis.presensi.model.DataPonpes;
import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPonpesActivity extends Activity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "LoginPonpesActivity";
    private PermissionManager permission;

    private SessionManager session;
    private DataPonpes mDataPonpes;
    private ProgressDialog progressDialog;
    private String latestAppVersion = "";
    private String thisAppVersion = "";

    private ActivityLoginPonpesBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPonpesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hideKeyboard();
        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        // Session Manager
        session = new SessionManager(this);

        String schoolCodeSM = session.getSavedKodes();
        if (schoolCodeSM != null) {
            binding.inputSchoolCode.setText(schoolCodeSM);
        }

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
        hideKeyboard();
        session.deleteSavedKodes();
        binding.inputSchoolCode.setText("");
        StyleableToast.makeText(this, "Data Tersimpan Berhasil Dihapus", Toast.LENGTH_SHORT, R.style.mytoast).show();
    }

    private void checkLogin() {
        hideKeyboard();

        String schoolCode = Objects.requireNonNull(binding.inputSchoolCode.getText()).toString();

        if (schoolCode.isEmpty()) {
            binding.inputSchoolCode.setError("Kode eLazis Tidak Boleh Kosong");
            binding.inputSchoolCode.requestFocus();
        } else {
            checkServer(schoolCode);

            binding.btnLogin.setEnabled(false);
            progressDialog = new ProgressDialog(LoginPonpesActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Mohon Tunggu...");
            progressDialog.show();
        }
    }

    private void checkServer(String schoolCode) {
        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataPonpes> call = apiService.checkLembaga(schoolCode);
            call.enqueue(new Callback<DataPonpes>() {
                @Override
                public void onResponse(@NonNull Call<DataPonpes> call, @NonNull Response<DataPonpes> response) {
                    progressDialog.dismiss();
                    binding.btnLogin.setEnabled(true);

                    mDataPonpes = response.body();
                    if (response.isSuccessful() && mDataPonpes != null) {
                        if (Objects.requireNonNull(mDataPonpes).getCorrect()) {
                            onLoginSuccess(mDataPonpes);
                        } else {
                            StyleableToast.makeText(LoginPonpesActivity.this, mDataPonpes.getMessage(), Toast.LENGTH_LONG, R.style.mytoast_danger).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DataPonpes> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    progressDialog.dismiss();
                    binding.btnLogin.setEnabled(true);
                    StyleableToast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    private void onLoginSuccess(DataPonpes dp) {
        // save in sessionManager
        session.createLanjutSession(dp);

        Intent intent = new Intent(this, LoginActivity.class);
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
                }
            }

            @Override
            public void onFailure(@NonNull Call<AndroidVersionResponse> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permission.checkResult(requestCode, permissions, grantResults);
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}