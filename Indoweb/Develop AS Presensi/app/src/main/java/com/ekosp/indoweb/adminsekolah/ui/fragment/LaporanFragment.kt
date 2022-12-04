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
import com.ekosp.indoweb.adminsekolah.adapter.ListRekapKehadiranAdapter
import com.ekosp.indoweb.adminsekolah.databinding.FragmentLaporanBinding
import com.ekosp.indoweb.adminsekolah.helper.*
import com.ekosp.indoweb.adminsekolah.model.DataSekolah
import com.ekosp.indoweb.adminsekolah.model.DataUser
import com.ekosp.indoweb.adminsekolah.model.data_laporan.DataLaporan
import com.ekosp.indoweb.adminsekolah.model.data_laporan.Rekap
import com.ekosp.indoweb.adminsekolah.ui.laporan.KehadiranTahunanActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LaporanFragment : Fragment(),
    ListRekapKehadiranAdapter.ClickListener, AdapterView.OnItemSelectedListener {

    private lateinit var dataKehadiran: MutableList<Rekap>
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var bottomSheet: BottomSheetDialog

    private lateinit var adapter: ListRekapKehadiranAdapter
    private lateinit var session: SessionManager

    private lateinit var sekolahData: DataSekolah.SekolahData
    private lateinit var userData: DataUser.UserData

    private var selectMonth: String? = null
    private var selectYear: String? = null

    private lateinit var binding: FragmentLaporanBinding
    private val _binding get() = binding
    private val localeID: Locale = Locale("in", "ID")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLaporanBinding.inflate(inflater, container, false)
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
        session = SessionManager(context)
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
            val call = apiService.getDataLaporan(
                schoolCode,
                idEmployee,
                "BULANAN",
                selectYear,
                selectMonth
            )

            call.enqueue(object : Callback<DataLaporan?> {
                override fun onResponse(
                    call: Call<DataLaporan?>,
                    response: Response<DataLaporan?>
                ) {
                    showLoading(false)
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        if (responseBody.is_correct) {
                            setUI(
                                responseBody.percentase,
                                responseBody.percentase_hari,
                                responseBody.hadir,
                                responseBody.ijin,
                                responseBody.sakit,
                                responseBody.lain,
                                responseBody.terlambat,
                                responseBody.hadir_tahun_ini,
                                responseBody.rekap
                            )
                        } else {
                            Log.e("response", "$responseBody")
                        }
                    } else {
                        Log.e(TAG, "onFailure: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {
                    showLoading(false)
                    Log.e(TAG, "onFailure: " + t.message)
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun setUI(
        valueProgress: Float,
        dayProgress: String,
        present: String,
        permit: String,
        sick: String,
        other: String,
        late: String,
        presentYear: String,
        recap: MutableList<Rekap>
    ) {
        binding.progressValue.text = "$valueProgress"
        binding.progressDay.text = dayProgress
        binding.progressBar.progress = (valueProgress / 100)

        binding.valuePresent.text = buildString {
            append(present)
            append(" Hari")
        }

        binding.valuePermit.text = buildString {
            append(permit)
            append(" Hari")
        }

        binding.valueSick.text = buildString {
            append(sick)
            append(" Hari")
        }

        binding.valueOther.text = buildString {
            append(other)
            append(" Hari")
        }

        binding.valueLate.text = buildString {
            append(late)
            append(" Kali")
        }
        binding.percentageYear.text = presentYear

        dataKehadiran = recap
        adapter = ListRekapKehadiranAdapter(
            requireContext(),
            dataKehadiran,
            this@LaporanFragment
        )

        binding.rvPresence.itemAnimator = DefaultItemAnimator()
        binding.rvPresence.layoutManager = LinearLayoutManager(activity)
        binding.rvPresence.setHasFixedSize(true)
        binding.rvPresence.adapter = adapter

        if (dataKehadiran.size <= 0) {
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
        val i = Intent(requireActivity(), KehadiranTahunanActivity::class.java)
        i.putExtra(GlobalVar.SCHOOL_CODE, sekolahData.kode_sekolah)
        i.putExtra(GlobalVar.ID_EMPLOYEE, userData.username)
        i.putExtra(GlobalVar.SELECT_YEAR, selectYear)
        activity?.startActivity(i)
        activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    // from Adapter
    @SuppressLint("InflateParams")
    override fun selectKehadiran(data: Rekap) {
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_detail_kehadiran, null, false)

        if (sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        when (data.status) {
            "Hadir" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_pesantren_2
                    )
                )
            "Ijin" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Sakit" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.lime_dark))
            "Lain-lain" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        view.findViewById<TextView>(R.id.txt_status).text = data.status

        view.findViewById<TextView>(R.id.txt_tgl_kehadiran).text = data.hari

        view.findViewById<TextView>(R.id.txt_jam_datang).text = data.detail.jam_datang
        view.findViewById<TextView>(R.id.txt_jam_pulang).text = data.detail.jam_pulang

        view.findViewById<TextView>(R.id.txt_lokasi).text = data.detail.lokasi_datang
        view.findViewById<TextView>(R.id.txt_lokasipulang).text = data.detail.lokasi_pulang

        view.findViewById<TextView>(R.id.txt_catatan).text = data.detail.catatan_datang
        view.findViewById<TextView>(R.id.txt_catatanpulang).text = data.detail.catatan_pulang

        if (data.status == "Ijin" || data.status == "Sakit" || data.status == "Lain-lain") {

            view.findViewById<TextView>(R.id.txt_v_jamdatang).text =
                resources.getString(R.string.time_present)
            view.findViewById<TextView>(R.id.txt_v_lokasi).text =
                resources.getString(R.string.location)
            view.findViewById<TextView>(R.id.txt_v_catatan).text =
                resources.getString(R.string.catatan)

            view.findViewById<TextView>(R.id.txt_v_jampulang).visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.txt_v_lokasipulang).visibility = View.INVISIBLE
            view.findViewById<TextView>(R.id.txt_v_catatanpulang).visibility = View.INVISIBLE
        }

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
        private const val TAG = "LaporanFragment"
    }
}