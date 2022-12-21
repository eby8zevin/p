@file:Suppress("DEPRECATION")

package com.ekosp.indoweb.adminsekolah.ui.izin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.CompoundButtonCompat
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ActivityIjinPelajaranBinding
import com.ekosp.indoweb.adminsekolah.helper.ApiClient
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.model.DefaultResponse
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataClass
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataMajors
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataSchedule
import com.ekosp.indoweb.adminsekolah.ui.MainActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.karan.churi.PermissionManager.PermissionManager
import io.github.muddz.styleabletoast.StyleableToast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class IjinPelajaranActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "IjinPelajaranActivity"
    }

    private lateinit var permission: PermissionManager
    private lateinit var mediaPath: String
    private lateinit var progressDialog: ProgressDialog

    private lateinit var schoolCode: String
    private lateinit var idEmployee: String
    private var textTypePermit: String? = null
    private var textDate: String? = null

    private lateinit var idMajor: String
    private val valueIdMajors = ArrayList<String>()
    private val valueNameMajors = ArrayList<String>()

    private lateinit var idClass: String
    private lateinit var nameClass: String
    private var valueIdClass = ArrayList<String>()
    private var valueNameClass = ArrayList<String>()

    private var dataCheckbox = ArrayList<DataSchedule.Jadwal>()
    private var idLessonCheckbox = ArrayList<String>()
    private var nameLessonCheckbox = ArrayList<String>()

    private lateinit var binding: ActivityIjinPelajaranBinding
    private val localeID = Locale("in", "ID")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIjinPelajaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permission = object : PermissionManager() {}
        permission.checkAndRequestPermissions(this)

        schoolCode = intent.getStringExtra(GlobalVar.PARAM_KODES_USER).toString()
        idEmployee = intent.getStringExtra(GlobalVar.PARAM_DATA_USER).toString()

        binding.btnBack.setOnClickListener { back() }
        typePermit()
        binding.inputDate.inputType = InputType.TYPE_NULL
        binding.inputDate.setOnClickListener { inputDate() }
        binding.uploadFile.setOnClickListener { selectFile() }
        binding.btnSubmit.setOnClickListener { checkData() }
    }

    private fun back() {
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    private fun typePermit() {
        binding.radioGroup.setOnCheckedChangeListener { group, isChecked ->
            val checked: RadioButton = group.findViewById(isChecked)
            textTypePermit = checked.text.toString().uppercase()
        }
    }

    private fun inputDate() {
        val materialDateBuilder: MaterialDatePicker.Builder<*> =
            MaterialDatePicker.Builder.datePicker()

        materialDateBuilder.setTitleText("Pilih Tanggal")

        val materialDatePicker = materialDateBuilder.build()
        materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")

        materialDatePicker.addOnPositiveButtonClickListener {

            val sdf = SimpleDateFormat("dd MMMM yyyy", localeID)
            val sdfToServer = SimpleDateFormat("yyyy-MM-dd", localeID)

            val dateInput = sdf.format(it)
            binding.inputDate.setText(dateInput)

            textDate = sdfToServer.format(it)

            getMajors(schoolCode)
        }
    }

    private fun getMajors(code: String) {
        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val callMajors = apiService.getDataMajors(code)
        callMajors.enqueue(object : Callback<DataMajors> {
            override fun onResponse(
                call: Call<DataMajors>,
                response: Response<DataMajors>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    valueIdMajors.clear()
                    valueNameMajors.clear()

                    if (responseBody.units != null) {
                        for (i in 0 until responseBody.units.size) {
                            valueIdMajors.add(responseBody.units[i].id_unit)
                            valueNameMajors.add(responseBody.units[i].nama_unit)
                        }
                        spinnerMajors()
                    } else {
                        Toast.makeText(
                            this@IjinPelajaranActivity,
                            responseBody.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<DataMajors>, t: Throwable) {
                Log.e("getMajors", "onFailure: " + t.message)
            }
        })
    }

    private fun spinnerMajors() {
        val adapter = ArrayAdapter(this, R.layout.spinner_lesson, valueNameMajors)
        adapter.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        binding.spMajors.adapter = adapter

        binding.spMajors.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                idMajor = valueIdMajors[position]
                valueNameMajors[position]

                getClass(schoolCode, idMajor)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun getClass(schoolCode: String, id_majors: String) {
        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val callClass = apiService.getDataClass(schoolCode, id_majors)
        callClass.enqueue(object : Callback<DataClass> {
            override fun onResponse(
                call: Call<DataClass>,
                response: Response<DataClass>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    valueIdClass.clear()
                    valueNameClass.clear()

                    if (responseBody.kelas != null) {
                        for (i in 0 until responseBody.kelas.size) {
                            valueIdClass.add(responseBody.kelas[i].id_kelas)
                            valueNameClass.add(responseBody.kelas[i].nama_kelas)
                        }
                        spinnerClass()
                    } else {
                        Toast.makeText(
                            this@IjinPelajaranActivity,
                            responseBody.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<DataClass>, t: Throwable) {
                Log.e("getClass", "onFailure: " + t.message)
            }
        })
    }

    private fun spinnerClass() {
        val adapter = ArrayAdapter(this, R.layout.spinner_lesson, valueNameClass)
        adapter.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        binding.spClass.adapter = adapter

        binding.spClass.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                idClass = valueIdClass[position]
                nameClass = valueNameClass[position]

                getSchedule(schoolCode, idEmployee, idClass)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun getSchedule(schoolCode: String, idEmployee: String, idClass: String) {
        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val callSchedule =
            apiService.getDataScheduleIjin(schoolCode, idEmployee, idClass, textDate)
        callSchedule.enqueue(object : Callback<DataSchedule> {
            override fun onResponse(
                call: Call<DataSchedule>,
                response: Response<DataSchedule>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    dataCheckbox.clear()
                    if (responseBody.jadwal != null) {
                        dataCheckbox.addAll(responseBody.jadwal)
                        checkboxLesson()
                        binding.checkboxContainer.visibility = View.VISIBLE
                    } else {
                        binding.checkboxContainer.visibility = View.GONE
                        Toast.makeText(
                            this@IjinPelajaranActivity,
                            "Tidak ada Jadwal Pelajaran di Kelas $nameClass",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<DataSchedule>, t: Throwable) {
                Log.e("getSchedule", "onFailure: " + t.message)
            }
        })
    }

    private fun checkboxLesson() {
        idLessonCheckbox.clear()
        nameLessonCheckbox.clear()
        binding.checkboxContainer.removeAllViews()

        for (schedule in dataCheckbox) {
            val idLesson = schedule.id_nama_pelajaran
            val nameLesson = schedule.nama_pelajaran
            val timeSchedule = schedule.waktu

            val checkBox = CheckBox(this)
            CompoundButtonCompat.setButtonTintList(
                checkBox,
                ColorStateList.valueOf(Color.parseColor("#1377E1"))
            )

            checkBox.id = idLesson.toInt()
            checkBox.text = buildString {
                append(nameLesson)
                append(" (")
                append(timeSchedule)
                append(")")
            }

            binding.checkboxContainer.addView(checkBox)

            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                val lessonId = buttonView.id
                val lessonName = buttonView.text.toString()

                if (isChecked) {
                    idLessonCheckbox.add(lessonId.toString())
                    nameLessonCheckbox.add(lessonName)
                } else {
                    idLessonCheckbox.remove(lessonId.toString())
                    nameLessonCheckbox.remove(lessonName)
                }
            }
        }
    }

    private fun selectFile() {
        // check condition
        if (ActivityCompat.checkSelfPermission(
                this@IjinPelajaranActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // When permission is not granted
            // Result permission
            ActivityCompat.requestPermissions(
                this@IjinPelajaranActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else if (ActivityCompat.checkSelfPermission(
                this@IjinPelajaranActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // When permission is not granted
            // Result permission
            ActivityCompat.requestPermissions(
                this@IjinPelajaranActivity,
                arrayOf(Manifest.permission.CAMERA),
                1
            )
        } else {
            // When permission is granted
            // Call method
            uploadFile()
        }
    }

    private fun uploadFile() {
        val optionsOpen = arrayOf("Pilih dari Galeri", "Pilih dari File Manager", "Batal")

        val builder = AlertDialog.Builder(this)

        builder.setItems(optionsOpen) { dialogInterface, choose ->
            if (optionsOpen[choose] == "Pilih dari Galeri") {
                val i = Intent(Intent.ACTION_PICK)
                i.type = "image/*"
                resultLauncher.launch(i)
            } else if (optionsOpen[choose] == "Pilih dari File Manager") {
                val mimeType = arrayOf("image/*", "application/pdf")
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*|application/pdf"
                i.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
                resultLauncher.launch(i)
            } else if (optionsOpen[choose] == "Batal") {
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    @SuppressLint("Recycle")
    private fun getRealPathFromURI(uri: Uri, context: Context): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = (returnCursor.getLong(sizeIndex)).toString()
        val file = File(context.filesDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)!!
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1024
            val bytesAvailable = inputStream.available()

            val bufferSize = min(bytesAvailable, maxBufferSize)

            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size ${file.length()}")
            Log.e("Size", size)
            inputStream.close()
            outputStream.close()
            Log.e("File Path", "Path ${file.path}")
            Log.e("File Size", "Size ${file.length()}")
        } catch (e: Exception) {
            Log.e("Exception", e.message.toString())
        }
        return file.path
    }

    @SuppressLint("Range")
    val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val sUri = data.data
                if (sUri != null) {
                    mediaPath = getRealPathFromURI(sUri, this@IjinPelajaranActivity)

                    var displayName: String? = null
                    val uriString = sUri.toString()
                    val myFile = File(uriString)
                    if (uriString.startsWith("content://")) {
                        var cursor: Cursor? = null
                        try {
                            cursor = this@IjinPelajaranActivity.contentResolver.query(
                                sUri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName =
                                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            }
                        } finally {
                            cursor?.close()
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.name
                    }

                    binding.tvInputFile.setText(displayName)
                }
            }
        }
    }

    private fun checkData() {
        if (textTypePermit == null) {
            Toast.makeText(this, "Jenis Izin Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
        } else if (nameLessonCheckbox.isEmpty()) {
            Toast.makeText(this, "Mata Pelajaran Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(binding.tvInputFile.text)) {
            Toast.makeText(this, "Dokumen Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
        } else {
            sendToServer()

            binding.btnSubmit.isEnabled = false
            progressDialog = ProgressDialog(this, R.style.AppTheme_Dark_Dialog)
            progressDialog.isIndeterminate = true
            progressDialog.setMessage("Mohon Tunggu...")
            progressDialog.show()

        }
    }

    private fun sendToServer() {

        val rbSchoolCode = schoolCode.toRequestBody("text/plain".toMediaType())
        val rbIdEmployee = idEmployee.toRequestBody("text/plain".toMediaType())
        val rbTypePermit = textTypePermit?.toRequestBody("text/plain".toMediaType())
        val rbDate = textDate?.toRequestBody("text/plain".toMediaType())
        val rbIdUnit = idMajor.toRequestBody("text/plain".toMediaType())
        val rbIdClass = idClass.toRequestBody("text/plain".toMediaType())

        val rbIdLesson = ArrayList<RequestBody>()
        for (i in 0 until idLessonCheckbox.size) {
            rbIdLesson.add(idLessonCheckbox[i].toRequestBody("text/plain".toMediaType()))
        }

        val myFile = File(mediaPath)
        val requestBodyFile = myFile.asRequestBody("*/*".toMediaTypeOrNull())
        val fileUpload = MultipartBody.Part.createFormData("file", myFile.name, requestBodyFile)

        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val call = apiService.ijinPelajaran(
            rbSchoolCode,
            rbIdEmployee,
            rbTypePermit,
            rbDate,
            rbIdUnit,
            rbIdClass,
            rbIdLesson,
            fileUpload
        )
        call.enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(
                call: Call<DefaultResponse>,
                response: Response<DefaultResponse>
            ) {
                progressDialog.dismiss()
                binding.btnSubmit.isEnabled = true
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    if (responseBody.is_correct) {
                        StyleableToast.makeText(
                            applicationContext,
                            responseBody.message,
                            Toast.LENGTH_SHORT,
                            R.style.mytoast
                        ).show()
                        val i = Intent(this@IjinPelajaranActivity, MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(i)
                        finish()
                        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
                    } else {
                        StyleableToast.makeText(
                            applicationContext,
                            responseBody.message,
                            Toast.LENGTH_LONG,
                            R.style.mytoast_danger
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                progressDialog.dismiss()
                binding.btnSubmit.isEnabled = true
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permission.checkResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // When permission is granted
            // Call method
            uploadFile()
        } else {
            // When permission is denied
            // Display toast
            Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}