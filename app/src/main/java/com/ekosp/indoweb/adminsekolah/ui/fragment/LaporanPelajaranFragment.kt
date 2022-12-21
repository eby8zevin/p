package com.ekosp.indoweb.adminsekolah.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.adapter.ListRekapPelajaranAdapter
import com.ekosp.indoweb.adminsekolah.databinding.FragmentLaporanPelajaranBinding
import com.ekosp.indoweb.adminsekolah.helper.ApiClient
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.helper.SessionManager
import com.ekosp.indoweb.adminsekolah.model.DataSekolah
import com.ekosp.indoweb.adminsekolah.model.DataUser
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataLaporanPelajaran
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.RekapPelajaran
import com.ekosp.indoweb.adminsekolah.ui.laporan.PelajaranTahunanActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LaporanPelajaranFragment : Fragment(),
    ListRekapPelajaranAdapter.ClickListener, AdapterView.OnItemSelectedListener {

    private lateinit var dataLessonReport: MutableList<RekapPelajaran>
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var bottomSheet: BottomSheetDialog

    private lateinit var adapter: ListRekapPelajaranAdapter
    private lateinit var session: SessionManager

    private lateinit var sekolahData: DataSekolah.SekolahData
    private lateinit var userData: DataUser.UserData

    private var selectMonth: String? = null
    private var selectYear: String? = null

    private lateinit var binding: FragmentLaporanPelajaranBinding
    private val _binding get() = binding
    private val localeID: Locale = Locale("in", "ID")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLaporanPelajaranBinding.inflate(inflater, container, false)
        return _binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSession()

        spinnerMonth()
        spinnerYear()
        binding.toAttendanceYear.setOnClickListener { attendanceYear() }
    }

    private fun getSession() {
        session = SessionManager(requireContext())
        sekolahData = session.sessionDataSekolah
        userData = session.sessionDataUser
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun spinnerMonth() {
        binding.spinnerMonth.onItemSelectedListener = this

        val formatter = SimpleDateFormat("MMMM", localeID)
        val listMonth = arrayListOf<String>()
        Calendar.getInstance().let { calendar ->
            calendar.add(Calendar.MONTH, -11)
            for (i in 0 until 12) {
                listMonth.add(formatter.format(calendar.timeInMillis))
                calendar.add(Calendar.MONTH, 1)
            }
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, listMonth)
        adapter.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        binding.spinnerMonth.adapter = adapter

        val current = LocalDateTime.now()
        val pattern = DateTimeFormatter.ofPattern("MMMM", localeID)
        val mNow = current.format(pattern)

        val position: Int = adapter.getPosition(mNow)
        binding.spinnerMonth.setSelection(position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun spinnerYear() {
        binding.spinnerYear.onItemSelectedListener = this

        val listYear = arrayListOf<String>()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        for (i in 2020..year) {
            listYear.add(i.toString())
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, listYear)
        adapter.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        binding.spinnerYear.adapter = adapter

        val current = LocalDateTime.now()
        val pattern = DateTimeFormatter.ofPattern("yyyy", localeID)
        val yNow = current.format(pattern)

        val position: Int = adapter.getPosition(yNow)
        binding.spinnerYear.setSelection(position)
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        selectMonth = binding.spinnerMonth.selectedItem.toString()
        selectYear = binding.spinnerYear.selectedItem.toString()
        getData()
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {
    }

    private fun getData() {
        val schoolCode: String = sekolahData.kode_sekolah
        val idEmployee: String = userData.username

        try {
            showLoading(true)
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.getDataLessonReport(
                schoolCode,
                idEmployee,
                "BULANAN",
                selectYear,
                selectMonth
            )

            call.enqueue(object : Callback<DataLaporanPelajaran> {
                override fun onResponse(
                    call: Call<DataLaporanPelajaran>,
                    response: Response<DataLaporanPelajaran>
                ) {
                    showLoading(false)
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        if (responseBody.is_correct) {
                            setUI(
                                responseBody.data.hadir,
                                responseBody.data.ijin,
                                responseBody.data.sakit,
                                responseBody.data.lain,
                                responseBody.data.hadir_tahun_ini,
                                responseBody.data.rekap
                            )
                        } else {
                            Log.e("response", "$responseBody")
                        }
                    } else {
                        Log.e(TAG, "onFailure: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DataLaporanPelajaran>, t: Throwable) {
                    showLoading(false)
                    Log.e(TAG, "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun setUI(
        hadir: String,
        ijin: String,
        sakit: String,
        lain: String,
        hadir_tahun_ini: String,
        rekap: MutableList<RekapPelajaran>
    ) {

        binding.presentValue.text = buildString {
            append(hadir)
            append(resources.getString(R.string.mapel))
        }

        binding.permitValue.text = buildString {
            append(ijin)
            append(resources.getString(R.string.mapel))
        }

        binding.sickValue.text = buildString {
            append(sakit)
            append(resources.getString(R.string.mapel))
        }

        binding.otherValue.text = buildString {
            append(lain)
            append(resources.getString(R.string.mapel))
        }

        binding.percentageYear.text = hadir_tahun_ini

        dataLessonReport = rekap
        adapter = ListRekapPelajaranAdapter(
            requireContext(),
            dataLessonReport,
            this@LaporanPelajaranFragment
        )

        binding.rvPresence.itemAnimator = DefaultItemAnimator()
        binding.rvPresence.layoutManager = LinearLayoutManager(activity)
        binding.rvPresence.setHasFixedSize(true)
        binding.rvPresence.adapter = adapter

        if (dataLessonReport.size <= 0) {
            binding.headerRv.visibility = View.GONE
            binding.ivNotFound.visibility = View.VISIBLE
            binding.tvNotFound.visibility = View.VISIBLE
        } else {
            binding.headerRv.visibility = View.VISIBLE
            binding.ivNotFound.visibility = View.GONE
            binding.tvNotFound.visibility = View.GONE
        }
    }

    private fun attendanceYear() {
        val i = Intent(requireActivity(), PelajaranTahunanActivity::class.java)
        i.putExtra(GlobalVar.SCHOOL_CODE, sekolahData.kode_sekolah)
        i.putExtra(GlobalVar.ID_EMPLOYEE, userData.username)
        i.putExtra(GlobalVar.SELECT_YEAR, selectYear)
        activity?.startActivity(i)
        activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    @SuppressLint("InflateParams")
    override fun selectLessonReport(data: RekapPelajaran) {
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_detail_lesson, null, false)

        if (sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        when (data.status) {
            "Hadir" -> view.findViewById<TextView>(R.id.tv_status)
                .setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_pesantren_2
                    )
                )
            "Ijin" -> view.findViewById<TextView>(R.id.tv_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Sakit" -> view.findViewById<TextView>(R.id.tv_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.lime_dark))
            "Lain-lain" -> view.findViewById<TextView>(R.id.tv_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        view.findViewById<TextView>(R.id.value_tgl_kehadiran).text = data.tanggal

        view.findViewById<TextView>(R.id.tv_status).text = data.status

        view.findViewById<TextView>(R.id.value_jamabsen).text = data.jam_absen
        view.findViewById<TextView>(R.id.value_jampelajaran).text = data.jam_pelajaran

        view.findViewById<TextView>(R.id.value_lokasi).text = data.lokasi
        view.findViewById<TextView>(R.id.value_pelajaran).text = data.nama_pelajaran

        bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.setContentView(view)
        bottomSheet.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.translucent)
        bottomSheet.show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressLoading.visibility = View.VISIBLE
        } else {
            binding.progressLoading.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        getSession()
        getData()
    }

    companion object {
        private const val TAG = "LessonReportFragment"
    }
}