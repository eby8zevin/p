package com.ekosp.indoweb.epesantren.laporan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.databinding.ActivityKehadiranTahunanBinding
import com.ekosp.indoweb.epesantren.fragment.LaporanFragment
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.ApiInterface
import com.ekosp.indoweb.epesantren.model.data_laporan.DataLaporan
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KehadiranTahunanActivity : AppCompatActivity() {

    var dataPonpes: String? = null
    var dataUser: String? = null
    var yearSelect: String? = null

    private lateinit var binding: ActivityKehadiranTahunanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKehadiranTahunanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataPonpes = intent.getStringExtra("dataPonpes")
        dataUser = intent.getStringExtra("dataUser")
        yearSelect = intent.getStringExtra("yearSelect")
        if (dataPonpes != null && dataUser != null) {
            getDataLaporanTahunan()
        } else {
            Toast.makeText(this, "Data Not Found", Toast.LENGTH_SHORT).show()
        }

        val back: ImageView = findViewById(R.id.arrow)
        back.setOnClickListener { back() }
    }

    private fun back() {
        val i = Intent(this, LaporanFragment::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        finish()
    }

    private fun getDataLaporanTahunan() {

        val kodes: String = dataPonpes.toString()
        val idpegawai: String = dataUser.toString()
        try {
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.getDataLaporantahun(
                kodes,
                idpegawai,
                "TAHUNAN",
                yearSelect,
            )

            call.enqueue(object : Callback<DataLaporan?> {
                override fun onResponse(
                    call: Call<DataLaporan?>,
                    response: Response<DataLaporan?>
                ) {
                    if (response.isSuccessful) {
                        val percentage = response.body()?.percentase

                        binding.tahun.text = yearSelect.toString()
                        binding.progressValueTahunan.text = "$percentage %"
                        binding.progressHariTahunan.text = response.body()?.percentase_hari
                        binding.progresBarTahunan.progress = percentage?.let { it / 100 }!!

                        binding.numHadirTahunan.text = response.body()?.hadir + " Hari"
                        binding.numIjinTahunan.text = response.body()?.izin_cuti + " Hari"
                        binding.numAlphaTahunan.text = response.body()?.alpa.toString() + " Hari"
                        binding.numTerlambatTahunan.text = response.body()?.terlambat + " Jam"
                    }
                }

                override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {

                }
            })
        } catch (e: Exception) {
            Log.e("Token e", e.toString())
        }
    }
}