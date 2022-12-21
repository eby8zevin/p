package com.ekosp.indoweb.adminsekolah.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ActivityJadwalPelajaranBinding
import com.ekosp.indoweb.adminsekolah.helper.ApiClient
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataClass
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataMajors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JadwalPelajaranActivity : AppCompatActivity() {

    private lateinit var schoolCode: String
    private lateinit var idEmployee: String

    private lateinit var idMajor: String
    private lateinit var nameMajor: String
    private val valueIdMajors = ArrayList<String>()
    private val valueNameMajors = ArrayList<String>()

    private lateinit var idClass: String
    private lateinit var nameClass: String
    private var valueIdClass = ArrayList<String>()
    private var valueNameClass = ArrayList<String>()

    private lateinit var binding: ActivityJadwalPelajaranBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJadwalPelajaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        schoolCode = intent.getStringExtra(GlobalVar.PARAM_KODES_USER).toString()
        idEmployee = intent.getStringExtra(GlobalVar.PARAM_DATA_USER).toString()

        getMajors(schoolCode)
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
                            this@JadwalPelajaranActivity,
                            responseBody.message,
                            Toast.LENGTH_SHORT
                        ).show()
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
        binding.spMajor.adapter = adapter

        binding.spMajor.onItemSelectedListener = object :
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
                showLoading(true)
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
                        binding.spClass.visibility = View.VISIBLE
                        spinnerClass()
                    } else {
                        binding.spClass.visibility = View.GONE
                        Toast.makeText(
                            this@JadwalPelajaranActivity,
                            responseBody.message,
                            Toast.LENGTH_SHORT
                        ).show()
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

                getWebView(schoolCode, idEmployee, idClass)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun getWebView(code: String, idEmployee: String, idClass: String) {
        binding.webView.webViewClient = WebViewClient()
        binding.webView.loadUrl(ApiClient.SCHEDULE_URL + code + "&id_pegawai=" + idEmployee + "&id_kelas=" + idClass)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.setSupportZoom(true)
    }

    // Overriding WebViewClient functions
    inner class WebViewClient : android.webkit.WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            showLoading(true)
        }

        // Load the URL
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            showLoading(true)
            view.loadUrl(url)
            return true
        }

        // ProgressBar will disappear once page is loaded
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}