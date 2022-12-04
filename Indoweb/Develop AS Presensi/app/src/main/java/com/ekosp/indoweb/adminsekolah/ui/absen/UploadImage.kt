@file:Suppress("DEPRECATION")

package com.ekosp.indoweb.adminsekolah.ui.absen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ActivityUploadImageBinding
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
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

class UploadImage : AppCompatActivity() {

    companion object {
        private const val TAG = "UploadImage"
    }

    private var totalSize: Long = 0
    private lateinit var progressDialog: ProgressDialog

    private var session: SessionManager? = null
    private var datPonpes: HashMap<String, String>? = null
    private var datUser: HashMap<String, String>? = null

    private var typePresent: String? = null
    private var destLocation: String? = null
    private var compressedImage: File? = null
    private var locationModel: LocationModel? = null

    private lateinit var schoolCode: String
    private lateinit var idEmployee: String
    private lateinit var nameEmployee: String
    private lateinit var longi: String
    private lateinit var lati: String

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var binding: ActivityUploadImageBinding

    private lateinit var idMajor: String
    private lateinit var nameMajor: String
    private val valueIdMajors = ArrayList<String>()
    private val valueNameMajors = ArrayList<String>()

    private lateinit var idClass: String
    private lateinit var nameClass: String
    private var valueIdClass = ArrayList<String>()
    private var valueNameClass = ArrayList<String>()

    private lateinit var idSchedule: String
    private lateinit var idLesson: String
    private lateinit var nameLesson: String
    private lateinit var timeLesson: String
    private var valueIdSchedule = ArrayList<String>()
    private var valueIdLesson = ArrayList<String>()
    private var valueNameLesson = ArrayList<String>()
    private var valueTimeLesson = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadImageBinding.inflate(layoutInflater)
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

        val intent = intent
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

        val timeIndonesia = datPonpes?.get(SessionManager.KEY_WAKTU_INDONESIA).toString()
        binding.gmt.text = buildString {
            append(" ")
            append(timeIndonesia)
        }
        binding.locUser.text = destLocation

        binding.btnBack.setOnClickListener { back() }
        binding.capturePicture.setOnClickListener { openCamera() }
        binding.btnSend.setOnClickListener { uploadToServer() }

        if (typePresent == GlobalVar.ABSEN_PELAJARAN) {
            binding.inputNotes.visibility = View.GONE
            binding.layoutSelectLesson.visibility = View.VISIBLE

            binding.layoutClass.visibility = View.INVISIBLE
            binding.layoutLesson.visibility = View.INVISIBLE
            binding.layoutTime.visibility = View.INVISIBLE

            getMajors(schoolCode)
        }

        binding.btnSend.setOnClickListener { uploadToServer() }
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
                        binding.layoutMajors.visibility = View.VISIBLE
                        spinnerMajors()
                    } else {
                        binding.layoutClass.visibility = View.GONE
                        binding.layoutLesson.visibility = View.GONE
                        binding.layoutTime.visibility = View.GONE
                        Toast.makeText(this@UploadImage, responseBody.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("getMajors", "onFailure: ${response.message()}")
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
                nameMajor = valueNameMajors[position]
                Log.i("name major", nameMajor)

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
                        binding.layoutClass.visibility = View.VISIBLE
                        spinnerClass()
                    } else {
                        binding.layoutLesson.visibility = View.INVISIBLE
                        binding.layoutTime.visibility = View.INVISIBLE
                        Toast.makeText(this@UploadImage, responseBody.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("getClass", "onFailure: ${response.message()}")
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
                Log.i("name class", nameClass)

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
                        binding.layoutLesson.visibility = View.VISIBLE
                        binding.layoutTime.visibility = View.VISIBLE
                        spinnerLesson()
                    } else {
                        binding.layoutLesson.visibility = View.INVISIBLE
                        binding.layoutTime.visibility = View.INVISIBLE

                        Toast.makeText(this@UploadImage, responseBody.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("getSchedule", "onFailure: ${response.message()}")
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
                nameLesson = valueNameLesson[position]
                timeLesson = valueTimeLesson[position]

                Log.i("name lesson", "$idLesson - $nameLesson")

                binding.tvTime.text = timeLesson
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun usingRetrofit() {

        if (valueIdLesson.isEmpty()) {
            Toast.makeText(
                this@UploadImage,
                "Anda belum memilih Jadwal Pelajaran !",
                Toast.LENGTH_SHORT
            ).show()
        } else {

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
                                this@UploadImage,
                                responseBody.message,
                                Toast.LENGTH_SHORT,
                                R.style.mytoast_danger
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                    Log.e("DefaultResponse", "onFailure: " + t.message)
                }
            })
        }
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
                            "Compressed Foto :", String.format(
                                "Size : %s", getReadableFileSize(
                                    compressedImage!!.length()
                                )
                            )
                        )
                        Log.d("Compressed Photo", "Compressed image save in $filePath")
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(
                            this@UploadImage,
                            ImagePicker.getError(data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            this@UploadImage,
                            "Ambil Foto Batal",
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

    private fun uploadToServer() {
        if (binding.imgPreview.drawable != null) {
            progressDialog.show()
            if (typePresent == GlobalVar.ABSEN_PELAJARAN) {
                usingRetrofit()
            } else {
                UploadFileToServer().execute()
            }
        } else {
            StyleableToast.makeText(
                this@UploadImage,
                "Silahkan ambil Foto terlebih dahulu!",
                Toast.LENGTH_SHORT,
                R.style.mytoast_danger
            ).show()
        }
    }

    /**
     * Uploading the file to server
     * */
    @SuppressLint("StaticFieldLeak")
    private inner class UploadFileToServer : AsyncTask<Void?, Int?, String?>() {

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("super.onPreExecute()", "android.os.AsyncTask")
        )
        @Override
        override fun onPreExecute() {
            super.onPreExecute()
        }

        @Deprecated("Deprecated in Java")
        @Override
        override fun doInBackground(vararg p0: Void?): String? {
            return uploadFile()
        }

        @SuppressWarnings("deprecation")
        private fun uploadFile(): String? {
            var responseString: String?

            val httpClient: HttpClient = DefaultHttpClient()

            val domains = datPonpes?.get(SessionManager.KEY_DOMAIN_PONPES).toString()
            val postURL: String = if (domains == "demo") ApiClient.FILE_UPLOAD_URL_DEMO
            else ApiClient.FILE_UPLOAD_URL

            val httpPost = HttpPost(postURL)

            try {
                val entity =
                    AndroidMultiPartEntity { num: Long ->
                        publishProgress(
                            (num / totalSize.toFloat() * 100).toInt()
                        )
                    }

                // Adding file data to http body
                entity.addPart("image", FileBody(compressedImage))
                Log.e(
                    "UPLOAD_IMAGE",
                    "photo path when uploading : " + compressedImage!!.absolutePath
                )

                // Extra parameters if you want to pass to server
                // idEmployee
                entity.addPart(
                    "id_pegawai",
                    StringBody(idEmployee)
                )
                // schoolCode
                entity.addPart(
                    "kode_sekolah",
                    StringBody(schoolCode)
                )
                // typePresent
                entity.addPart(
                    "type",
                    StringBody(typePresent)
                )
                // location
                entity.addPart(
                    "lokasi",
                    StringBody(binding.locUser.text.toString())
                )
                // longitude user
                entity.addPart(
                    "longi",
                    StringBody(longi)
                )
                Log.i("UPLOAD", "Longitude:" + locationModel?.longitude)
                // latitude user
                entity.addPart(
                    "lati",
                    StringBody(lati)
                )
                Log.i("UPLOAD", "Latitude:" + locationModel?.latitude)
                // desc
                entity.addPart(
                    "keterangan",
                    StringBody(binding.inputNotes.text.toString())
                )

                totalSize = entity.contentLength
                httpPost.entity = entity

                // Making server call
                val response = httpClient.execute(httpPost)
                val resEntity = response.entity

                val statusCode = response.statusLine.statusCode
                responseString = if (statusCode == 200) {
                    // Server response
                    EntityUtils.toString(resEntity)
                } else {
                    "Error occurred! Http Status Code: $statusCode"
                }

            } catch (e: ClientProtocolException) {
                responseString = e.toString()
            } catch (e: IOException) {
                responseString = e.toString()
            }
            return responseString
        }

        @Deprecated("Deprecated in Java")
        @Override
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            var resError: Boolean? = null
            var resMessage: String? = null
            Log.e(TAG, "Response From Server: $result")
            try {
                val jsonObject = JSONObject(result.toString())
                resError = jsonObject.optBoolean("is_correct")
                resMessage = jsonObject.optString("message")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            if (resError == true) {
                Log.e(TAG, "response: $resMessage")
                toAfterPresent(resMessage.toString())
            } else {
                progressDialog.cancel()
                StyleableToast.makeText(
                    this@UploadImage,
                    "$resMessage",
                    Toast.LENGTH_SHORT,
                    R.style.mytoast_danger
                ).show()
            }
        }
    }

    private fun toAfterPresent(response: String) {
        countDownTimer.cancel()
        progressDialog.cancel()
        val i = Intent(this, AfterAbsen::class.java)
        i.putExtra(GlobalVar.PARAM_RESPONSE_UPLOAD, response)
        startActivity(i)
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
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
                    this@UploadImage,
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
}