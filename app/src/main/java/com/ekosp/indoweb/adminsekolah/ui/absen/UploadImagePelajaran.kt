@file:Suppress("DEPRECATION")

package com.ekosp.indoweb.adminsekolah.ui.absen

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ActivityUploadImagePelajaranBinding
import com.ekosp.indoweb.adminsekolah.helper.*
import com.ekosp.indoweb.adminsekolah.model.DefaultResponse
import com.ekosp.indoweb.adminsekolah.model.LocationModel
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataClass
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataMajors
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataSchedule
import com.ekosp.indoweb.adminsekolah.ui.MainActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import io.github.muddz.styleabletoast.StyleableToast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

class UploadImagePelajaran : AppCompatActivity() {

    private var session: SessionManager? = null
    private var datPonpes: HashMap<String, String>? = null
    private var datUser: HashMap<String, String>? = null

    private var typePresent: String? = null
    private var destLocation: String? = null
    private var compressedImage: File? = null
    private var locationModel: LocationModel? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var schoolCode: String
    private lateinit var idEmployee: String
    private lateinit var nameEmployee: String
    private lateinit var longi: String
    private lateinit var lati: String

    private lateinit var idMajor: String
    private val valueIdMajors = arrayListOf<String>()
    private val valueNameMajors = arrayListOf<String>()

    private lateinit var idClass: String
    private var valueIdClass = ArrayList<String>()
    private var valueNameClass = ArrayList<String>()

    private lateinit var idSchedule: String
    private lateinit var idLesson: String
    private lateinit var timeLesson: String
    private var valueIdSchedule = ArrayList<String>()
    private var valueIdLesson = ArrayList<String>()
    private var valueNameLesson = ArrayList<String>()
    private var valueTimeLesson = ArrayList<String>()

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var binding: ActivityUploadImagePelajaranBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadImagePelajaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Checking camera availability
        if (!Utils.isDeviceSupportCamera(applicationContext)) {
            Toast.makeText(
                applicationContext,
                "Sorry! Your device doesn't support camera",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

        getSession()
        getLoading()

        typePresent = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI)
        locationModel = intent.getParcelableExtra(GlobalVar.PARAM_LAST_LOCATION)
        destLocation = intent.getStringExtra(GlobalVar.DESTINATION_LOCATION)

        schoolCode = datPonpes?.get(SessionManager.KEY_KODES).toString()
        idEmployee = datUser?.get(SessionManager.KEY_USERNAME).toString()
        nameEmployee = datUser?.get(SessionManager.KEY_NAME).toString()
        longi = locationModel?.longitude.toString()
        lati = locationModel?.latitude.toString()

        binding.infoToolbarTitle.text = buildString {
            append("Absen ")
            append(typePresent)
        }
        binding.locUser.text = destLocation

        binding.layoutClass.visibility = View.GONE
        binding.layoutLesson.visibility = View.GONE
        binding.layoutTime.visibility = View.GONE
        getMajors(schoolCode)

        binding.btnBack.setOnClickListener { back() }
        binding.capturePicture.setOnClickListener { openCamera() }
        binding.btnSend.setOnClickListener { checkData() }
        startCountdown()
    }

    private fun getLoading() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mohon tunggu ...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
    }

    private fun getSession() {
        session = SessionManager(this)
        datUser = session?.dataUserPreference
        datPonpes = session?.dataSekolahPreference
    }

    private fun getMajors(schoolCode: String) {
        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val callMajors = apiService.getDataMajors(schoolCode)
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
                            this@UploadImagePelajaran,
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
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, valueNameMajors)
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
                        binding.layoutClass.visibility = View.VISIBLE
                    } else {
                        binding.layoutClass.visibility = View.GONE

                        Toast.makeText(
                            this@UploadImagePelajaran,
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
                valueNameClass[position]

                getSchedule(schoolCode, idEmployee, idClass)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun getSchedule(schoolCode: String, idEmployee: String, idClass: String) {
        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val callSchedule = apiService.getDataSchedule(schoolCode, idEmployee, idClass)
        callSchedule.enqueue(object : Callback<DataSchedule> {
            override fun onResponse(
                call: Call<DataSchedule>,
                response: Response<DataSchedule>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    valueIdSchedule.clear()
                    valueIdLesson.clear()
                    valueNameLesson.clear()
                    valueTimeLesson.clear()

                    if (responseBody.jadwal != null) {
                        for (i in 0 until responseBody.jadwal.size) {
                            valueIdSchedule.add(responseBody.jadwal[i].jadwal_id)
                            valueIdLesson.add(responseBody.jadwal[i].id_nama_pelajaran)
                            valueNameLesson.add(responseBody.jadwal[i].nama_pelajaran)
                            valueTimeLesson.add(responseBody.jadwal[i].waktu)
                        }
                        spinnerLesson()
                        binding.layoutLesson.visibility = View.VISIBLE
                        binding.layoutTime.visibility = View.VISIBLE
                    } else {
                        binding.layoutLesson.visibility = View.GONE
                        binding.layoutTime.visibility = View.GONE

                        Toast.makeText(
                            this@UploadImagePelajaran,
                            responseBody.message,
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

    private fun spinnerLesson() {
        val adapterLesson = ArrayAdapter(this, R.layout.spinner_lesson, valueNameLesson)
        adapterLesson.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        binding.spLesson.adapter = adapterLesson

        binding.spLesson.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                idSchedule = valueIdSchedule[position]
                idLesson = valueIdLesson[position]
                valueNameLesson[position]
                val timeLesson = valueTimeLesson[position]

                binding.tvTime.text = timeLesson
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun checkData() {
        if (binding.imgPreview.drawable == null) {
            StyleableToast.makeText(
                this@UploadImagePelajaran,
                "Silahkan ambil Foto terlebih dahulu!",
                Toast.LENGTH_SHORT,
                R.style.mytoast_danger
            ).show()
        } else if (valueIdLesson.isEmpty()) {
            Toast.makeText(
                this@UploadImagePelajaran,
                "Anda belum memilih Jadwal Pelajaran !",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            progressDialog.show()
            sendToServer()
        }
    }

    private fun sendToServer() {

        val myFile = compressedImage as File
        val imageFile = myFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "image",
            myFile.name,
            imageFile
        )

        val codeSchool = schoolCode.toRequestBody("text/plain".toMediaType())
        val employeeId = idEmployee.toRequestBody("text/plain".toMediaType())
        val employeeName = nameEmployee.toRequestBody("text/plain".toMediaType())
        val presentType = typePresent?.toRequestBody("text/plain".toMediaType())
        val locationDesc = destLocation?.toRequestBody("text/plain".toMediaType())
        val longitude = longi.toRequestBody("text/plain".toMediaType())
        val latitude = lati.toRequestBody("text/plain".toMediaType())
        val majorId = idMajor.toRequestBody("text/plain".toMediaType())
        val classId = idClass.toRequestBody("text/plain".toMediaType())
        val schoolCodeId = idSchedule.toRequestBody("text/plain".toMediaType())
        val lessonId = idLesson.toRequestBody("text/plain".toMediaType())
        val lessonTime = timeLesson.toRequestBody("text/plain".toMediaType())

        val apiService = ApiClient.getClient().create(ApiInterface::class.java)
        val callAbsentLesson =
            apiService.postDataAbsentLesson(
                codeSchool,
                employeeId,
                employeeName,
                presentType,
                locationDesc,
                longitude,
                latitude,
                imageMultipart,
                majorId,
                classId,
                schoolCodeId,
                lessonId,
                lessonTime
            )
        callAbsentLesson.enqueue(object : Callback<DefaultResponse> {
            override fun onResponse(
                call: Call<DefaultResponse>,
                response: Response<DefaultResponse>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {

                    if (responseBody.is_correct) {
                        toAfterPresent(responseBody.message)
                    } else {
                        progressDialog.cancel()
                        StyleableToast.makeText(
                            this@UploadImagePelajaran,
                            responseBody.message,
                            Toast.LENGTH_SHORT,
                            R.style.mytoast_danger
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message)
            }
        })
    }

    private fun toAfterPresent(response: String) {
        countDownTimer.cancel()
        progressDialog.cancel()
        val i = Intent(this, AfterAbsen::class.java)
        i.putExtra(GlobalVar.PARAM_RESPONSE_UPLOAD, response)
        startActivity(i)
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun openCamera() {
        ImagePicker.with(this)
            .cameraOnly()
            .compress(250) //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                500,
                500
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .start { resultCode, data ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        val fileUri = data?.data
                        binding.imgPreview.visibility = View.VISIBLE
                        binding.imgPreview.setImageURI(fileUri)

                        //You can get File object from intent
                        compressedImage = ImagePicker.getFile(data)

                        //You can also get File Path from intent
                        val filePath: String? = ImagePicker.getFilePath(data)
                        Log.e(
                            "Compressed Foto", String.format(
                                "Size : %s", getReadableFileSize(
                                    compressedImage!!.length()
                                )
                            )
                        )
                        Log.d("Compressed Photo", "Compressed image save in $filePath")
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(
                            this@UploadImagePelajaran,
                            ImagePicker.getError(data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            this@UploadImagePelajaran,
                            "Ambil Foto Batal !",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun startCountdown() {
        val duration: Long = 61 * 1000
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val milis = String.format(
                    "KIRIM ABSEN %02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            )
                )
                binding.btnSend.text = milis
                println(milis)
            }

            override fun onFinish() {
                finish()
                Toast.makeText(
                    this@UploadImagePelajaran,
                    "Waktu habis.\nSilahkan absen ulang",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        countDownTimer.start()
    }

    private fun back() {
        countDownTimer.cancel()
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        finish()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    override fun onResume() {
        super.onResume()
        getSession()
    }

    companion object {
        private const val TAG = "UploadImagePelajaran"
    }
}