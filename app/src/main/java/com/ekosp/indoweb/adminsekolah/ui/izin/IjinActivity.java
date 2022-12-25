package com.ekosp.indoweb.adminsekolah.ui.izin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

import com.ekosp.indoweb.adminsekolah.R;
import com.ekosp.indoweb.adminsekolah.databinding.ActivityIjinBinding;
import com.ekosp.indoweb.adminsekolah.helper.ApiClient;
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface;
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar;
import com.ekosp.indoweb.adminsekolah.model.DefaultResponse;
import com.ekosp.indoweb.adminsekolah.ui.MainActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IjinActivity extends AppCompatActivity {

    private static final String TAG = "IjinActivity";
    private String mediaPath;

    private ProgressDialog progressDialog;
    private PermissionManager permission;

    private String codeSchool;
    private String username;

    private ActivityIjinBinding binding;
    private final Locale localeID = new Locale("in", "ID");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIjinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permission = new PermissionManager() {
        };
        permission.checkAndRequestPermissions(this);

        codeSchool = getIntent().getStringExtra(GlobalVar.PARAM_KODES_USER);
        username = getIntent().getStringExtra(GlobalVar.PARAM_DATA_USER);

        binding.radioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            if (id == R.id.ijin) {
                binding.inputType.setText(R.string.IJIN);
            } else if (id == R.id.sakit) {
                binding.inputType.setText(R.string.SAKIT);
            } else if (id == R.id.lain) {
                binding.inputType.setText(R.string.LAIN_LAIN);
            } else {
                binding.inputType.setText("");
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
        binding.uploadFile.setOnClickListener(v -> selectFile());
        binding.btnSubmit.setOnClickListener(v -> checkData());
    }

    public void back() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void selectFile() {
        // Set click listener on button
        binding.uploadFile.setOnClickListener(
                v -> {
                    // check condition
                    if (ActivityCompat.checkSelfPermission(IjinActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // When permission is not granted
                        // Result permission
                        ActivityCompat.requestPermissions(IjinActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else if (ActivityCompat.checkSelfPermission(IjinActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // When permission is not granted
                        // Result permission
                        ActivityCompat.requestPermissions(IjinActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                    } else {
                        // When permission is granted
                        // Create method
                        uploadFile();
                    }
                });
    }

    private void uploadFile() {
        final CharSequence[] optionsOpen = {"Pilih dari Galeri", "Pilih dari File Manager", "Batal"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setItems(optionsOpen, (dialogInterface, choose) -> {
            if (optionsOpen[choose].equals("Pilih dari Galeri")) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                resultLauncher.launch(i);
            } else if (optionsOpen[choose].equals("Pilih dari File Manager")) {
                String[] mimeType = {"image/*", "application/pdf"};
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*|application/pdf");
                i.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
                resultLauncher.launch(i);
            } else if (optionsOpen[choose].equals("Batal")) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private static String getRealPathFromURI(Uri uri, Context context) {
        @SuppressLint("Recycle") Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read;
            int maxBufferSize = 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            Log.e("Size", size);
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    @SuppressLint("Range")
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {

                        Uri sUri = data.getData();
                        mediaPath = getRealPathFromURI(sUri, IjinActivity.this);

                        String displayName = null;
                        String uriString = sUri.toString();
                        File myFile = new File(uriString);
                        if (uriString.startsWith("content://")) {
                            Cursor cursor = null;
                            try {
                                cursor = this.getContentResolver().query(sUri, null, null, null, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                }
                            } finally {
                                Objects.requireNonNull(cursor).close();
                            }
                        } else if (uriString.startsWith("file://")) {
                            displayName = myFile.getName();
                        }

                        binding.tvInputFile.setVisibility(View.VISIBLE);
                        binding.tvInputFile.setText(displayName);
                    }
                }
            }
    );

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
        } else if (TextUtils.isEmpty(binding.tvInputFile.getText())) {
            Toast.makeText(this, "Dokumen Tidak Boleh Kosong!", Toast.LENGTH_SHORT).show();
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

        RequestBody codeSchool = RequestBody.create(schoolCode, MediaType.parse("text/plain"));
        RequestBody employeeId = RequestBody.create(idEmployee, MediaType.parse("text/plain"));
        RequestBody typePermit = RequestBody.create(type, MediaType.parse("text/plain"));
        RequestBody startDate = RequestBody.create(dateStart, MediaType.parse("text/plain"));
        RequestBody endDate = RequestBody.create(dateEnd, MediaType.parse("text/plain"));
        RequestBody description = RequestBody.create(desc, MediaType.parse("text/plain"));

        File myFile = new File(mediaPath);
        RequestBody requestBodyFile = RequestBody.create(myFile, MediaType.parse("*/*"));
        MultipartBody.Part fileUpload = MultipartBody.Part.createFormData("file", myFile.getName(), requestBodyFile);

        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DefaultResponse> call = apiService.ijinDatangPulang(codeSchool, employeeId, typePermit, startDate, endDate, description, fileUpload);
            call.enqueue(new Callback<DefaultResponse>() {
                @Override
                public void onResponse(@NonNull Call<DefaultResponse> call, @NonNull Response<DefaultResponse> response) {
                    progressDialog.dismiss();
                    binding.btnSubmit.setEnabled(true);
                    DefaultResponse responseBody = response.body();
                    if (response.isSuccessful() && responseBody != null) {
                        if (responseBody.getIs_correct()) {
                            onSaveSuccess(responseBody.getMessage());
                        } else {
                            StyleableToast.makeText(IjinActivity.this, responseBody.getMessage(), Toast.LENGTH_LONG, R.style.mytoast_danger).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DefaultResponse> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    binding.btnSubmit.setEnabled(true);
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    StyleableToast.makeText(IjinActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    public void onSaveSuccess(String response) {
        StyleableToast.makeText(this, response, Toast.LENGTH_SHORT, R.style.mytoast).show();

        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission.checkResult(requestCode, permissions, grantResults);
        // check condition
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // When permission is granted
            // Call method
            uploadFile();
        } else {
            // When permission is denied
            // Display toast
            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }
}