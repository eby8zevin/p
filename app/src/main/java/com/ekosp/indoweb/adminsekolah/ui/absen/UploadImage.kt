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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ActivityUploadImageBinding
import com.ekosp.indoweb.adminsekolah.helper.ApiClient
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.helper.SessionManager
import com.ekosp.indoweb.adminsekolah.helper.Utils
import com.ekosp.indoweb.adminsekolah.model.LocationModel
import com.ekosp.indoweb.adminsekolah.ui.MainActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import io.github.muddz.styleabletoast.StyleableToast
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONException
import org.json.JSONObject
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
                            this@UploadImage,
                            ImagePicker.getError(data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            this@UploadImage,
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

    private fun uploadToServer() {
        if (binding.imgPreview.drawable != null) {
            progressDialog.show()
            UploadFileToServer().execute()
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

            progressDialog.dismiss()
            if (resError == true) {
                Log.e(TAG, "response: $resMessage")
                toAfterPresent(resMessage.toString())
            } else {
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
        val i = Intent(this, AfterAbsen::class.java)
        i.putExtra(GlobalVar.PARAM_RESPONSE_UPLOAD, response)
        startActivity(i)
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
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