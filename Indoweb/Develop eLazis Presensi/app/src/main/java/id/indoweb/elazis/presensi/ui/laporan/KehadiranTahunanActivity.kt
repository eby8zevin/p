package id.indoweb.elazis.presensi.ui.laporan

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import id.indoweb.elazis.presensi.R
import id.indoweb.elazis.presensi.databinding.ActivityKehadiranTahunanBinding

import id.indoweb.elazis.presensi.helper.ApiClient
import id.indoweb.elazis.presensi.helper.ApiInterface
import id.indoweb.elazis.presensi.model.data_laporan.DataLaporan
import io.github.muddz.styleabletoast.StyleableToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KehadiranTahunanActivity : AppCompatActivity() {

    private var schoolCode: String? = null
    private var idEmployee: String? = null
    private var selectYear: String? = null

    private lateinit var binding: ActivityKehadiranTahunanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKehadiranTahunanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        schoolCode = intent.getStringExtra("CODE_SCHOOL")
        idEmployee = intent.getStringExtra("ID_EMPLOYEE")
        selectYear = intent.getStringExtra("YEAR_SELECT")

        getData()
        binding.arrow.setOnClickListener { back() }
    }

    private fun back() {
        onBackPressed()
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun getData() {
        try {
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.getDataLaporantahun(
                schoolCode,
                idEmployee,
                "TAHUNAN",
                selectYear,
            )
            call.enqueue(object : Callback<DataLaporan?> {
                override fun onResponse(
                    call: Call<DataLaporan?>,
                    response: Response<DataLaporan?>
                ) {
                    if (response.isSuccessful) {
                        val res = response.body()!!
                        setUI(
                            res.percentase,
                            res.percentase_hari,
                            res.hadir,
                            res.izin_cuti,
                            res.alpa,
                            res.terlambat
                        )
                    } else {
                        StyleableToast.makeText(
                            this@KehadiranTahunanActivity,
                            "Terjadi Gangguan Koneksi Ke Server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {
                    Log.e("KehadiranTahunan", "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.e("KehadiranTahunan", e.toString())
        }
    }

    private fun setUI(
        percentage: Float,
        dayPercentage: String,
        present: String,
        permitLeave: String,
        alpha: Int,
        late: String
    ) {
        binding.year.text = selectYear

        binding.progressValueTahunan.text = percentage.toString()
        binding.progressHariTahunan.text = dayPercentage
        binding.progresBarTahunan.progress = (percentage / 100)

        binding.tvPresent.text = buildString {
            append(present)
            append(" Hari")
        }
        binding.tvPermitLeave.text = buildString {
            append(permitLeave)
            append(" Hari")
        }
        binding.tvAlpha.text = buildString {
            append(alpha)
            append(" Hari")
        }
        binding.tvLate.text = buildString {
            append(late)
            append(" Jam")
        }
    }
}