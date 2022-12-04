package id.indoweb.elazis.presensi.ui.fragment

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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.indoweb.elazis.presensi.R
import id.indoweb.elazis.presensi.databinding.FragmentLaporanBinding

import id.indoweb.elazis.presensi.adapter.ListRekapKehadiranAdapter
import id.indoweb.elazis.presensi.helper.ApiClient
import id.indoweb.elazis.presensi.helper.ApiInterface
import id.indoweb.elazis.presensi.helper.SessionManager
import id.indoweb.elazis.presensi.helper.parseDateToddMMyyyy
import id.indoweb.elazis.presensi.model.DataPonpes
import id.indoweb.elazis.presensi.model.DataUser
import id.indoweb.elazis.presensi.model.data_laporan.DataLaporan
import id.indoweb.elazis.presensi.model.data_laporan.Rekap
import id.indoweb.elazis.presensi.ui.laporan.KehadiranTahunanActivity
import io.github.muddz.styleabletoast.StyleableToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LaporanFragment : Fragment(),
    ListRekapKehadiranAdapter.ClickListener, AdapterView.OnItemSelectedListener {

    private var dataKehadiran: MutableList<Rekap>? = null
    private lateinit var adapter: ListRekapKehadiranAdapter
    private lateinit var session: SessionManager
    private lateinit var user: DataUser
    private lateinit var ponpes: DataPonpes

    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var bottomSheet: BottomSheetDialog? = null

    private var selectMonth: String? = null
    private var selectYear: String? = null

    private var binding: FragmentLaporanBinding? = null
    private val _binding get() = binding!!
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
        getData()

        spinnerMonth()
        spinnerYear()
        binding?.toAttendanceYear?.setOnClickListener { attendanceYear() }
    }

    private fun getSession() {
        session = SessionManager(requireContext())
        user = session.sessionDataUser
        ponpes = session.sessionDataPonpes
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun spinnerMonth() {
        binding?.spinnerMonth?.onItemSelectedListener = this

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
        binding?.spinnerMonth?.adapter = adapter

        val current = LocalDateTime.now()
        val pattern = DateTimeFormatter.ofPattern("MMMM", localeID)
        val mNow = current.format(pattern)

        val position: Int = adapter.getPosition(mNow)
        binding?.spinnerMonth?.setSelection(position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun spinnerYear() {
        binding?.spinnerYear?.onItemSelectedListener = this

        val listYear = arrayListOf<String>()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        for (i in 2020..year) {
            listYear.add(i.toString())
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, listYear)
        adapter.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        binding?.spinnerYear?.adapter = adapter

        val current = LocalDateTime.now()
        val pattern = DateTimeFormatter.ofPattern("yyyy", localeID)
        val yNow = current.format(pattern)

        val position: Int = adapter.getPosition(yNow)
        binding?.spinnerYear?.setSelection(position)
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        selectMonth = binding?.spinnerMonth?.selectedItem.toString()
        selectYear = binding?.spinnerYear?.selectedItem.toString()
        getData()
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {
    }

    private fun getData() {
        val schoolCode: String = ponpes.kodes
        val idEmployee: String = user.username

        try {
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
                    if (response.isSuccessful) {
                        val res = response.body()!!
                        setUI(
                            res.percentase,
                            res.percentase_hari,
                            res.hadir,
                            res.izin_cuti,
                            res.alpa,
                            res.terlambat,
                            res.hadir_tahun_ini,
                            res.rekap!!
                        )
                    } else {
                        StyleableToast.makeText(
                            context!!,
                            "Terjadi Gangguan Koneksi Ke Server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {
                    Log.e("LaporanFragment", "onFailure: " + t.message)
                }
            })

        } catch (e: Exception) {
            Log.e("LaporanFragment", e.toString())
        }
    }

    private fun setUI(
        valueProgress: Float,
        dayProgress: String,
        present: String,
        permitLeave: String,
        alpha: Int,
        late: String,
        presentYear: String,
        recap: MutableList<Rekap>
    ) {
        binding?.rvPresence?.setHasFixedSize(true)
        binding?.rvPresence?.itemAnimator = DefaultItemAnimator()
        binding?.rvPresence?.layoutManager = LinearLayoutManager(requireContext())

        binding?.progressValue?.text = "$valueProgress"
        binding?.progressDay?.text = dayProgress
        binding?.progressBar?.progress = (valueProgress / 100)

        binding?.presentValue?.text = buildString {
            append(present)
            append(" Hari")
        }
        binding?.permitLeaveValue?.text = buildString {
            append(permitLeave)
            append(" Hari")
        }
        binding?.alphaValue?.text = buildString {
            append(alpha)
            append(" Hari")
        }
        binding?.lateValue?.text = buildString {
            append(late)
            append(" Jam")
        }
        binding?.percentageYear?.text = presentYear

        dataKehadiran = recap
        adapter = ListRekapKehadiranAdapter(
            requireContext(),
            dataKehadiran!!,
            this@LaporanFragment
        )
        binding?.rvPresence?.adapter = adapter
    }

    private fun attendanceYear() {
        val i = Intent(requireActivity(), KehadiranTahunanActivity::class.java)
        i.putExtra("CODE_SCHOOL", ponpes.kodes)
        i.putExtra("ID_EMPLOYEE", user.username)
        i.putExtra("YEAR_SELECT", selectYear)
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
                        R.color.colorPrimaryDark
                    )
                )
            "Sakit" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Cuti" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.lime_dark))
            "Keperluan Lain" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        view.findViewById<TextView>(R.id.txt_status).text = data.status

        val dateRecap = parseDateToddMMyyyy(data.hari)
        view.findViewById<TextView>(R.id.tv_tgl_kehadiran).text = dateRecap

        view.findViewById<TextView>(R.id.tv_jam_datang).text = data.detail.jam_datang
        view.findViewById<TextView>(R.id.tv_jam_pulang).text = data.detail.jam_pulang
        view.findViewById<TextView>(R.id.tv_location).text = data.detail.lokasi
        view.findViewById<TextView>(R.id.tv_noted).text = data.detail.catatan_absen

        bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet?.setContentView(view)
        bottomSheet?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.translucent)
        bottomSheet?.show()
        bottomSheet?.setOnDismissListener {
            bottomSheet = null
        }
    }

    override fun onResume() {
        super.onResume()
        getSession()
        getData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}