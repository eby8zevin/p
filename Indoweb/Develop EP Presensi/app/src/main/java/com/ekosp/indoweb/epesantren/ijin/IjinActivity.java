package com.ekosp.indoweb.epesantren.ijin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.util.Pair;

import com.ekosp.indoweb.epesantren.MainActivity;
import com.ekosp.indoweb.epesantren.R;
import com.ekosp.indoweb.epesantren.databinding.ActivityIjinBinding;
import com.ekosp.indoweb.epesantren.helper.ApiClient;
import com.ekosp.indoweb.epesantren.helper.ApiInterface;
import com.ekosp.indoweb.epesantren.helper.GlobalVar;
import com.ekosp.indoweb.epesantren.helper.SessionManager;
import com.ekosp.indoweb.epesantren.model.DataIjin;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.ekosp.indoweb.epesantren.model.LocationModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.karan.churi.PermissionManager.PermissionManager;
import com.squareup.okhttp.MediaType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IjinActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "IjinActivity";
    private static final int REQUEST_IJIN = 0;
    private PermissionManager permission;
    private MediaType JSON;
    private SessionManager session;
    private LocationModel lastLocation;
    private ApiInterface apiService;
    private ProgressDialog progressDialog;
    private DataPonpes mDataPonpes;

    private String userName;
    private String kodess;

    @SerializedName("is_correct")
    @Expose
    private Boolean isCorrect;

    private Locale localeID = new Locale("in", "ID");
    private ActivityIjinBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIjinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        JSON = MediaType.parse("application/json; charset=utf-8");
        // Session Manager
        session = new SessionManager(this);
        mDataPonpes = session.getSessionDataPonpes();
        Intent intent = getIntent();
        if (intent.getParcelableExtra(GlobalVar.PARAM_LAST_LOCATION) != null)
            lastLocation = intent.getParcelableExtra(GlobalVar.PARAM_LAST_LOCATION);

        if (intent.getStringExtra(GlobalVar.PARAM_DATA_USER) != null) {
            userName = intent.getStringExtra(GlobalVar.PARAM_DATA_USER);
        }
        if (intent.getStringExtra(GlobalVar.PARAM_KODES_USER) != null) {
            kodess = intent.getStringExtra(GlobalVar.PARAM_KODES_USER);
        }

        binding.btnSubmit.setOnClickListener(v -> simpan());

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id) {
                    case R.id.cuti:
                        binding.jenis.setText("CUTI");
                        break;
                    case R.id.sakit:
                        binding.jenis.setText("SAKIT");
                        break;
                    case R.id.lain:
                        binding.jenis.setText("LAIN-LAIN");
                        break;
                }
            }
        });

        binding.inputTgl.setInputType(InputType.TYPE_NULL);

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Pilih Tanggal");

        final MaterialDatePicker<Pair<Long, Long>> dateRangePicker = builder.build();
        binding.inputTgl.setOnClickListener(v -> dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER"));
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            Calendar calendar_2 = Calendar.getInstance();

            Long startDate = selection.first;
            Long endDate = selection.second;

            calendar.setTimeInMillis(startDate);
            calendar_2.setTimeInMillis(endDate);

            SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", localeID);
            SimpleDateFormat format_2 = new SimpleDateFormat("yyyy-MM-dd", localeID);

            String date = format.format(calendar.getTime()) + " - " + format.format(calendar_2.getTime());

            binding.inputTgl.setText(date);
            binding.inputTglAwal.setText(format_2.format(calendar.getTime()));
            binding.inputTglAkhir.setText(format_2.format(calendar_2.getTime()));
        });

        binding.btnBckHp.setOnClickListener(v -> bckHP());
    }

    @NonNull
    @Override
    public String toString() {
        return "IjinActivity{" +
                "isCorrect=" + isCorrect + '}';
    }

    public void simpan() {
        Log.d(TAG, "Simpan");

        binding.btnSubmit.setEnabled(false);
        progressDialog = new ProgressDialog(IjinActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Mohon Tunggu...");
        progressDialog.show();

        if (!validate()) {
            onSimpanFailed("Tambah Presensi Ijin Gagal. Silahkan Periksa Data Anda Terlebih Dahulu");
            return;
        }

        executeSimpan();
    }

    private void executeSimpan() {
        String kodes = kodess;
        String uname = userName;
        String jenis_ = binding.jenis.getText().toString();
        String tgl_awal_ = binding.inputTglAwal.getText().toString();
        String tgl_akhir_ = binding.inputTglAkhir.getText().toString();
        String keterangan_ = Objects.requireNonNull(binding.inputKeterangan.getText()).toString();
        try {
            apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataIjin> call = apiService.submitIjincoba(kodes, uname, jenis_, tgl_awal_, tgl_akhir_, keterangan_);
            call.enqueue(new Callback<DataIjin>() {
                @Override
                public void onResponse(@NonNull Call<DataIjin> call, @NonNull Response<DataIjin> response) {
                    if (response.isSuccessful()) {
                        DataIjin ijin = response.body();
                        if (Objects.requireNonNull(ijin).getCorrect()) {
                            onSimpanSuccess1("" + ijin.getMessage() + "");
                        } else {
                            onSimpanFailed("" + ijin.getMessage() + "");
                        }
                    } else
                        Toast.makeText(IjinActivity.this, "Terjadi gangguan koneksi ke server", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<DataIjin> call, @NonNull Throwable t) {
                    Log.e("IjinActivity", "onFailure: " + t.getMessage());
                }

            });
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    public void onSimpanSuccess1(String t) {
        StyleableToast.makeText(this, "" + t + "", Toast.LENGTH_LONG, R.style.mytoast).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onSimpanFailed(String t) {
        StyleableToast.makeText(this, "" + t + "", Toast.LENGTH_LONG, R.style.mytoast_danger).show();
        binding.btnSubmit.setEnabled(true);
        progressDialog.dismiss();
    }

    public boolean validate() {
        boolean valid = true;
        String jenise = binding.jenis.getText().toString();
        String tgl_a = binding.inputTglAwal.getText().toString();
        String tgl_ak = binding.inputTglAkhir.getText().toString();
        String keterangane_ = binding.inputKeterangan.getText().toString();

        if (jenise.isEmpty()) {
            binding.jenis.setError("Pilih Salah Satu Jenis");
            valid = false;
        } else {
            binding.jenis.setError(null);
        }

        if (tgl_a.isEmpty()) {
            binding.inputTglAwal.setError("Pilih Tanggal");
            valid = false;
        } else {
            binding.inputTglAwal.setError(null);
        }

        if (tgl_ak.isEmpty()) {
            binding.inputTglAkhir.setError("Pilih Tanggal");
            valid = false;
        } else {
            binding.inputTglAkhir.setError(null);
        }

        if (keterangane_.isEmpty()) {
            binding.inputKeterangan.setError("Keterangan Tidak Boleh Kosong");
            valid = false;
        } else {
            binding.inputKeterangan.setError(null);
        }

        return valid;
    }

    public void bckHP() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IJIN) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission.checkResult(requestCode, permissions, grantResults);
    }
}