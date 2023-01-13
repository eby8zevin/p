package id.indoweb.elazis.presensi.ui.izin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import id.indoweb.elazis.presensi.R;
import id.indoweb.elazis.presensi.databinding.ActivityIjinBinding;
import id.indoweb.elazis.presensi.helper.ApiClient;
import id.indoweb.elazis.presensi.helper.ApiInterface;
import id.indoweb.elazis.presensi.helper.GlobalVar;
import id.indoweb.elazis.presensi.model.DataIjin;
import id.indoweb.elazis.presensi.ui.MainActivity;
import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IjinActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "IjinActivity";

    private ProgressDialog progressDialog;
    private PermissionManager permission;
    private String codeSchool;
    private String username;

    String encodePDF;
    private ActivityIjinBinding binding;
    private final Locale localeID = new Locale("in", "ID");

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIjinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        Intent intent = getIntent();
        if (intent.getStringExtra(GlobalVar.PARAM_KODES_USER) != null) {
            codeSchool = intent.getStringExtra(GlobalVar.PARAM_KODES_USER);
        }
        if (intent.getStringExtra(GlobalVar.PARAM_DATA_USER) != null) {
            username = intent.getStringExtra(GlobalVar.PARAM_DATA_USER);
        }

        binding.radioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            if (id == R.id.ijin) {
                binding.inputType.setText(R.string.CUTI);
            } else if (id == R.id.sakit) {
                binding.inputType.setText(R.string.SAKIT);
            } else if (id == R.id.lain) {
                binding.inputType.setText(R.string.LAIN_LAIN);
            }
        });

        binding.inputDate.setInputType(InputType.TYPE_NULL);
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Pilih Tanggal");

        final MaterialDatePicker<Pair<Long, Long>> dateRangePicker = builder.build();
        binding.inputDate.setOnClickListener(v -> dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER"));
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendarFirst = Calendar.getInstance();
            Calendar calendarSecond = Calendar.getInstance();

            Long startDate = selection.first;
            Long endDate = selection.second;

            calendarFirst.setTimeInMillis(startDate);
            calendarSecond.setTimeInMillis(endDate);

            SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", localeID);
            SimpleDateFormat formatToServer = new SimpleDateFormat("yyyy-MM-dd", localeID);

            String date = format.format(calendarFirst.getTime()) + " - " + format.format(calendarSecond.getTime());

            binding.inputDate.setText(date);
            binding.inputDateStart.setText(formatToServer.format(calendarFirst.getTime()));
            binding.inputDateEnd.setText(formatToServer.format(calendarSecond.getTime()));
        });

        binding.btnBack.setOnClickListener(v -> back());
        binding.uploadFile.setOnClickListener(v -> uploadFile());
        binding.btnSubmit.setOnClickListener(v -> checkData());
    }

    public void back() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void checkData() {
        Log.d(TAG, "Simpan");

        String schoolCode = codeSchool;
        String idEmployee = username;
        String type = binding.inputType.getText().toString();
        String dateStart = binding.inputDateStart.getText().toString();
        String dateEnd = binding.inputDateEnd.getText().toString();
        String desc = Objects.requireNonNull(binding.inputDesc.getText()).toString();

        if (type.isEmpty()) {
            Toast.makeText(this, "Jenis Izin Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
        } else if (dateStart.isEmpty() && dateEnd.isEmpty()) {
            Toast.makeText(this, "Tanggal Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
        } else if (desc.isEmpty()) {
            binding.inputDesc.setError("Keterangan Tidak Boleh Kosong");
            binding.inputDesc.requestFocus();
        } else {
            saveToServer(schoolCode, idEmployee, type, dateStart, dateEnd, desc);

            binding.btnSubmit.setEnabled(false);
            progressDialog = new ProgressDialog(IjinActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Mohon Tunggu...");
            progressDialog.show();
        }
    }

    private void saveToServer(String schoolCode, String idEmployee, String type, String dateStart, String dateEnd, String desc) {
        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataIjin> call = apiService.submitIjincoba(schoolCode, idEmployee, type, dateStart, dateEnd, desc);
            call.enqueue(new Callback<DataIjin>() {
                @Override
                public void onResponse(@NonNull Call<DataIjin> call, @NonNull Response<DataIjin> response) {
                    if (response.isSuccessful()) {
                        DataIjin permit = response.body();
                        Log.i("RETROFIT", Objects.requireNonNull(permit).toString());
                        if (permit.getCorrect()) {
                            onSaveSuccess(permit.getMessage());
                        } else {
                            onSaveFailed(permit.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DataIjin> call, @NonNull Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                    binding.btnSubmit.setEnabled(true);
                    progressDialog.dismiss();
                    Toast.makeText(IjinActivity.this, "Terjadi gangguan koneksi ke server", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    public void onSaveSuccess(String response) {
        StyleableToast.makeText(this, response, Toast.LENGTH_LONG, R.style.mytoast).show();
        binding.btnSubmit.setEnabled(true);
        progressDialog.dismiss();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onSaveFailed(String response) {
        StyleableToast.makeText(this, response, Toast.LENGTH_LONG, R.style.mytoast_danger).show();
        binding.btnSubmit.setEnabled(true);
        progressDialog.dismiss();
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1212 && resultCode == RESULT_OK && data != null) {
            // Get the Uri of the selected file
            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    Objects.requireNonNull(cursor).close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }

            try {
                InputStream inputStream = this.getContentResolver().openInputStream(uri);
                byte[] pdfInBytes = new byte[inputStream.available()];
                inputStream.read(pdfInBytes);
                encodePDF = Base64.encodeToString(pdfInBytes, Base64.DEFAULT);

                Toast.makeText(this, displayName, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            binding.inputFile.setText(displayName);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission.checkResult(requestCode, permissions, grantResults);
    }

    private void uploadFile() {
        String[] mimeType = {"image/*", "application/pdf"};
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*|application/pdf");
        i.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
        i = Intent.createChooser(i, "Pilih Dokumen");
        startActivityForResult(i, 1212);
    }
}