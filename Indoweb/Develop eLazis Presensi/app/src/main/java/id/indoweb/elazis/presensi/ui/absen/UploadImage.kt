@file:Suppress("DEPRECATION")

package id.indoweb.elazis.presensi.ui.absen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
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
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

class UploadImage : AppCompatActivity() {

    var totalSize: Long = 0
    private var session: SessionManager? = null
    private var datPonpes: HashMap<String, String>? = null
    private var datUser: HashMap<String, String>? = null
    private var typePresent: String? = null
    private var compressedImage: File? = null
    private var locationModel: LocationModel? = null

    //Declare my Handler in global to be used also in onResume() method
    private var myHandler: Handler? = null
    private var notResumed = false

    private var destLocation: String? = null
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
        if (binding.imgPreview.drawable == null) {
            StyleableToast.makeText(
                this@UploadImage,
                "Silahkan Ambil Foto terlebih dahulu!",
                Toast.LENGTH_SHORT,
                R.style.mytoast_danger
            ).show()
        } else {
            countDownTimer.cancel()
            Utils.showProgressDialog(this, "Mohon tunggu ...").show()
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

        private fun uploadFile(): String? {
            var responseString: String?
            val httpclient: HttpClient = DefaultHttpClient()
            val domains = datPonpes?.get(SessionManager.KEY_DOMAIN_PONPES).toString()

            val httpPost = if (domains == "demo") HttpPost(ApiClient.FILE_UPLOAD_URL_DEMO)
            else HttpPost(ApiClient.FILE_UPLOAD_URL)

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
                    "device_id",
                    StringBody(session!!.deviceData.deviceId)
                )
                entity.addPart(
                    "imei",
                    StringBody(session!!.deviceData.imei)
                )
                entity.addPart(
                    "id_pegawai",
                    StringBody(datUser!![SessionManager.KEY_USERNAME].toString())
                )
                entity.addPart(
                    "kode_sekolah",
                    StringBody(datPonpes!![SessionManager.KEY_KODES].toString())
                )
                entity.addPart(
                    "domain",
                    StringBody(datPonpes!![SessionManager.KEY_DOMAIN_PONPES].toString())
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
                    "type",
                    StringBody(typePresent)
                )
                entity.addPart(
                    "keterangan",
                    StringBody(binding.inputNotes.text.toString())
                )
                entity.addPart("lokasi", StringBody(binding.locUser.text.toString()))
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
            Log.i("Response From Server", "$result")
            toAfterPresent()
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg p0: Void?): String? {
            return uploadFile()
        }
    }

    private fun toAfterPresent() {
        val i = Intent(this, AfterAbsen::class.java)
        i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, typePresent)
        startActivity(i)
        finish()
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun startCountdown() {
        val duration = 30 * 1000
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

                if (millisUntilFinished <= 1000)
                    Toast.makeText(
                        this@UploadImage,
                        "Waktu habis.\nSilahkan absen ulang",
                        Toast.LENGTH_SHORT
                    ).show()
            }

            override fun onFinish() {
                finish()
            }
        }
        countDownTimer.start()
    }

    override fun onResume() {
        super.onResume()
        getSession()
    }

    public override fun onStop() {
        super.onStop()
        notResumed = true
        myHandler = Handler()
        myHandler!!.postDelayed({ if (notResumed) finish() }, 20000)
    }

    public override fun onDestroy() {
        super.onDestroy()
        Log.d("debug", "onDestroyCalled")
    }
}