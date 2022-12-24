package com.ekosp.indoweb.adminsekolah.ui.laporan

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ActivityKehadiranTahunanBinding
import com.ekosp.indoweb.adminsekolah.helper.ApiClient
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.model.data_laporan.DataLaporan
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KehadiranTahunanActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "KTActivity"
    }

    private var schoolCode: String? = null
    private var idEmployee: String? = null
    private var selectYear: String? = null

    private lateinit var binding: ActivityKehadiranTahunanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKehadiranTahunanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        schoolCode = intent.getStringExtra(GlobalVar.SCHOOL_CODE)
        idEmployee = intent.getStringExtra(GlobalVar.ID_EMPLOYEE)
        selectYear = intent.getStringExtra(GlobalVar.SELECT_YEAR)

        binding.title.text = buildString {
            append(resources.getString(R.string.present_in_year))
            append(selectYear)
        }

        getData()
        binding.btnBack.setOnClickListener { back() }
    }

    private fun back() {
        onBackPressed()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
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
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        setUI(
                            responseBody.percentase,
                            responseBody.percentase_hari,
                            responseBody.hadir,
                            responseBody.ijin,
                            responseBody.sakit,
                            responseBody.lain,
                            responseBody.terlambat
                        )
                    } else {
                        Log.e(TAG, "onFailure: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {
                    Log.e(TAG, "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun setUI(
        percentage: Float,
        dayPercentage: String,
        present: String,
        permit: String,
        sick: String,
        other: String,
        late: String
    ) {

        binding.progressValueTahunan.text = percentage.toString()
        binding.progressHariTahunan.text = dayPercentage
        binding.progresBarTahunan.progress = (percentage / 100)

        binding.numHadirTahunan.text = buildString {
            append(present)
            append(" Hari")
        }

        binding.numIjinTahunan.text = buildString {
            append(permit)
            append(" Hari")
        }

        binding.numSickTahunan.text = buildString {
            append(sick)
            append(" Hari")
        }

        binding.numOtherTahunan.text = buildString {
            append(other)
            append(" Hari")
        }

        binding.numTerlambatTahunan.text = buildString {
            append(late)
            append(" Kali")
        }
    }
}