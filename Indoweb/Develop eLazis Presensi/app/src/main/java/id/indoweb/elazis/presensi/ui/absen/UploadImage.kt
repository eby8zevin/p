@file:Suppress("DEPRECATION")

package id.indoweb.elazis.presensi.ui.absen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import id.indoweb.elazis.presensi.R
import id.indoweb.elazis.presensi.databinding.ActivityUploadImageBinding
import id.indoweb.elazis.presensi.helper.ApiClient
import id.indoweb.elazis.presensi.helper.GlobalVar
import id.indoweb.elazis.presensi.helper.SessionManager
import id.indoweb.elazis.presensi.helper.Utils
import id.indoweb.elazis.presensi.model.LocationModel
import id.indoweb.elazis.presensi.ui.MainActivity
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

    var totalSize: Long = 0
    private lateinit var progressDialog: ProgressDialog

    private var session: SessionManager? = null
    private var datPonpes: HashMap<String, String>? = null
    private var datUser: HashMap<String, String>? = null
    private var typePresent: String? = null
    private var compressedImage: File? = null
    private var locationModel: LocationModel? = null

    private var destLocation: String? = null
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var binding: ActivityUploadImageBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
        startCountdown()

        val intent = intent
        typePresent = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI)
        locationModel = intent.getParcelableExtra(GlobalVar.PARAM_LAST_LOCATION)
        destLocation = intent.getStringExtra("LOKASI_TUJUAN")

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
    }

    private fun getSession() {
        session = SessionManager(this)
        datUser = session!!.dataUserPreference
        datPonpes = session!!.dataPonpesPreference
    }

    private fun back() {
        countDownTimer.cancel()
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
        finish()
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun getLoading() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mohon tunggu ...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
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
                            "Ambil Foto Batal!",
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
        if (binding.imgPreview.drawable == null) {
            StyleableToast.makeText(
                this@UploadImage,
                "Silahkan Ambil Foto terlebih dahulu!",
                Toast.LENGTH_SHORT,
                R.style.mytoast_danger
            ).show()
        } else {
            countDownTimer.cancel()
            progressDialog.show()
            UploadFileToServer().execute()
        }
    }

    // Uploading the file to server
    @SuppressLint("StaticFieldLeak")
    private inner class UploadFileToServer : AsyncTask<Void?, Int?, String?>() {
        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("super.onPreExecute()", "android.os.AsyncTask")
        )
        override fun onPreExecute() {
            super.onPreExecute()
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg p0: Void?): String? {
            return uploadFile()
        }

        private fun uploadFile(): String? {
            var responseString: String?
            val httpclient: HttpClient = DefaultHttpClient()

            val httpPost = HttpPost(ApiClient.FILE_UPLOAD_URL)

            try {
                val entity =
                    AndroidMultiPartEntity { num: Long ->
                        publishProgress(
                            (num / totalSize.toFloat() * 100).toInt()
                        )
                    }
                if (compressedImage != null) {
                    entity.addPart("image", FileBody(compressedImage))
                    Log.e(
                        "UPLOAD_IMAGE",
                        "path foto pas upload : " + compressedImage!!.absolutePath
                    )
                }

                // Extra parameters if you want to pass to server
                entity.addPart(
                    "kode_lembaga",
                    StringBody(datPonpes!![SessionManager.KEY_KODES].toString())
                )
                entity.addPart(
                    "id_pegawai",
                    StringBody(datUser!![SessionManager.KEY_USERNAME].toString())
                )
                entity.addPart(
                    "type",
                    StringBody(typePresent)
                )
                entity.addPart(
                    "lokasi",
                    StringBody(binding.locUser.text.toString())
                )
                entity.addPart(
                    "longi",
                    StringBody(locationModel!!.longitude.toString())
                )
                Log.i("UPLOAD", "Longitude:" + locationModel!!.longitude)
                entity.addPart(
                    "lati",
                    StringBody(locationModel!!.latitude.toString())
                )
                Log.i("UPLOAD", "latitude:" + locationModel!!.latitude)
                entity.addPart(
                    "keterangan",
                    StringBody(binding.inputNotes.text.toString())
                )
                totalSize = entity.contentLength
                httpPost.entity = entity

                // Making server call
                val response = httpclient.execute(httpPost)
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
        val i = Intent(this, AfterAbsen::class.java)
        i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, response)
        startActivity(i)
        finish()
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun startCountdown() {
        val duration = 60 * 1000
        countDownTimer = object : CountDownTimer(duration.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val milis = String.format(
                    "KIRIM ABSEN %d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            )
                )
                binding.btnSend.text = milis
                println(milis)
            }

            override fun onFinish() {
                Toast.makeText(
                    this@UploadImage,
                    "Waktu habis. Silahkan absen ulang !",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        countDownTimer.start()
    }

    override fun onResume() {
        super.onResume()
        getSession()
    }

    companion object {
        private const val TAG = "UploadImage"
    }
}