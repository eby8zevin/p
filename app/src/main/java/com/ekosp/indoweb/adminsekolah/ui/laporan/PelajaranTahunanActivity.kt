package com.ekosp.indoweb.adminsekolah.ui.laporan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.adapter.PelajaranTahunanAdapter
import com.ekosp.indoweb.adminsekolah.databinding.ActivityPelajaranTahunanBinding
import com.ekosp.indoweb.adminsekolah.helper.ApiClient
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataLaporanTahunPelajaran
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.PelajaranLaporanTahunData
import com.ekosp.indoweb.adminsekolah.ui.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PelajaranTahunanActivity : AppCompatActivity() {

    private lateinit var adapter: PelajaranTahunanAdapter
    private lateinit var binding: ActivityPelajaranTahunanBinding

    companion object {
        private const val TAG = "PTActivity"
    }

    private var schoolCode: String? = null
    private var idEmployee: String? = null
    private var selectYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPelajaranTahunanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        schoolCode = intent.getStringExtra(GlobalVar.SCHOOL_CODE)
        idEmployee = intent.getStringExtra(GlobalVar.ID_EMPLOYEE)
        selectYear = intent.getStringExtra(GlobalVar.SELECT_YEAR)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = resources.getString(R.string.present_in_year) + selectYear
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_hp)

        getData()
    }

    private fun getData() {
        try {
            showLoading(true)
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.getDataLessonReportYear(
                schoolCode,
                idEmployee,
                "TAHUNAN",
                selectYear,
            )
            call.enqueue(object : Callback<DataLaporanTahunPelajaran> {
                override fun onResponse(
                    call: Call<DataLaporanTahunPelajaran>,
                    response: Response<DataLaporanTahunPelajaran>
                ) {
                    showLoading(false)
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        if (responseBody.is_correct) {
                            setUI(
                                responseBody.data
                            )
                        } else {
                            Log.e("response", "$responseBody")
                        }
                    } else {
                        Log.e(TAG, "onFailure: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DataLaporanTahunPelajaran>, t: Throwable) {
                    showLoading(false)
                    Log.e(TAG, "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun setUI(
        data: MutableList<PelajaranLaporanTahunData>
    ) {

        adapter = PelajaranTahunanAdapter(
            this,
            data
        )
        binding.rvLessonYear.itemAnimator = DefaultItemAnimator()
        binding.rvLessonYear.layoutManager = LinearLayoutManager(this)
        binding.rvLessonYear.setHasFixedSize(true)
        binding.rvLessonYear.adapter = adapter
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            finish()
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
        }

        return super.onOptionsItemSelected(item)
    }
}