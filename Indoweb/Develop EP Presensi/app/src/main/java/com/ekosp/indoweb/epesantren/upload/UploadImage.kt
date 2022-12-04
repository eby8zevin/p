@file:Suppress("DEPRECATION")

package com.ekosp.indoweb.epesantren.upload

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ekosp.indoweb.epesantren.AfterAbsen
import com.ekosp.indoweb.epesantren.MainActivity
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.databinding.ActivityUploadImageBinding
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.GlobalVar
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.helper.Utils
import com.ekosp.indoweb.epesantren.model.LocationModel
import com.github.dhaval2404.imagepicker.ImagePicker
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
    private var datUser: HashMap<String, String>? = null
    private var datPonpes: HashMap<String, String>? = null
    private var type: String? = null
    private var compressedImage: File? = null
    private var locationModel: LocationModel? = null

    private lateinit var countDownTimer: CountDownTimer

    private var notResumed = false

    //Declare my Handler in global to be used also in onResume() method
    private var myHandler: Handler? = null

    private var nameLocation: String? = null
    private lateinit var binding: ActivityUploadImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        type = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI)
        locationModel = intent.getParcelableExtra(GlobalVar.PARAM_LAST_LOCATION)
        nameLocation = intent.getStringExtra("LOKASI_TUJUAN")

        session = SessionManager(this)
        datUser = session!!.dataUserPreference
        datPonpes = session!!.dataPonpesPreference

        binding.lokasiUser.text = nameLocation
        binding.wibe.text = " " + datPonpes?.get(SessionManager.KEY_WAKTU_INDONESIA).toString()

        // Checking camera availability
        if (!Utils.isDeviceSupportCamera(applicationContext)) {
            Toast.makeText(
                applicationContext,
                "Sorry! Your device doesn't support camera",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

        try {
            val pInfo = this.packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName
            binding.infoHdrAbsen.text = buildString {
                append("Absen ")
                append(type)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        setListener()
        startCountdown()
    }

    private fun setListener() {
        binding.capturePicture.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .compress(250)         //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    500,
                    500
                )  //Final image resolution will be less than 1080 x 1080(Optional)
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
                            Log.d("Compressed Foto", "Compressed image save in $filePath")
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
                                "Ambil foto dibatalkan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        binding.btnUpload.setOnClickListener {
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

        binding.btnBck.setOnClickListener {
            countDownTimer.cancel()
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
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

    /**
     * Uploading the file to server
     */
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

            val httpPost = HttpPost(ApiClient.FILE_UPLOAD_URL)

            try {
                val entity =
                    AndroidMultiPartEntity { num: Long -> publishProgress((num / totalSize.toFloat() * 100).toInt()) }
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
                    StringBody(type)
                )
                entity.addPart(
                    "keterangan",
                    StringBody(binding.inputKeterangan.text.toString())
                )
                entity.addPart("lokasi", StringBody(binding.lokasiUser.text.toString()))
                totalSize = entity.contentLength
                httpPost.entity = entity

                // Making server call
                val response = httpclient.execute(httpPost)
                val rEntity = response.entity
                val statusCode = response.statusLine.statusCode
                responseString = if (statusCode == 200) {
                    // Server response
                    EntityUtils.toString(rEntity)
                } else {
                    ("Error occurred! Http Status Code: "
                            + statusCode)
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
            Log.e("RESPONSE", "result: $result")
            super.onPostExecute(result)
            toAfterAbsen()
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg p0: Void?): String? {
            return uploadFile()
        }
    }

    private fun toAfterAbsen() {
        val intent = Intent(this, AfterAbsen::class.java)
        intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, type)

        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
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
        val durasi = 30 * 1000
        countDownTimer = object : CountDownTimer(durasi.toLong(), 1000) {
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
                binding.btnUpload.text = milis
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
}