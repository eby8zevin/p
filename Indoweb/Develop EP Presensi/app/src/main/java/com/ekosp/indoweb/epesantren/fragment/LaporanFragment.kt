package com.ekosp.indoweb.epesantren.fragment

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
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.adapter.ListRekapKehadiranAdapter
import com.ekosp.indoweb.epesantren.databinding.FragmentLaporanBinding
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.ApiInterface
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.helper.parseDateToddMMyyyy
import com.ekosp.indoweb.epesantren.laporan.KehadiranTahunanActivity
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.DataUser
import com.ekosp.indoweb.epesantren.model.data_laporan.DataLaporan
import com.ekosp.indoweb.epesantren.model.data_laporan.Rekap
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

    private var dataKehadiran: MutableList<Rekap>? = null
    private lateinit var detailKehadiranTahun: DataLaporan
    private lateinit var adapter: ListRekapKehadiranAdapter

    private lateinit var session: SessionManager
    private lateinit var user: DataUser
    private lateinit var ponpes: DataPonpes

    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var bottomSheet: BottomSheetDialog? = null
    private var yearsSelect = ""
    private var monthSelect = ""

    private var kodes: String? = null
    private var idpegawai: String? = null
    private var indonesia = Locale("in", "ID")

    private var binding: FragmentLaporanBinding? = null
    private val _binding get() = binding!!

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

        loadKehadiran()
        setListener()
        setSpinner()
        spinnerYear()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setSpinner() {
        binding!!.spMonth.onItemSelectedListener = this

        val formatter = SimpleDateFormat("MMMM", indonesia)
        val listMonth = arrayListOf<String>()
        Calendar.getInstance().let { calendar ->
            calendar.add(Calendar.MONTH, -11)
            for (i in 0 until 12) {
                listMonth.add(formatter.format(calendar.timeInMillis))
                calendar.add(Calendar.MONTH, 1)
            }
        }

        val aa = ArrayAdapter(requireContext(), R.layout.item_spinner, listMonth)
        aa.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        binding!!.spMonth.adapter = aa

        val current = LocalDateTime.now()
        val formatter2 = DateTimeFormatter.ofPattern("MMMM", indonesia)
        val mNow = current.format(formatter2)

        val spinnerPosition: Int = aa.getPosition(mNow)
        binding!!.spMonth.setSelection(spinnerPosition)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun spinnerYear() {
        binding!!.spYear.onItemSelectedListener = this

        val list = arrayListOf<String>()
        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        for (i in 2020..thisYear) {
            list.add(i.toString())
        }

        val adapterYear = ArrayAdapter(requireContext(), R.layout.item_spinner, list)
        adapterYear.setDropDownViewResource(R.layout.item_spinner_radio_btn)
        binding!!.spYear.adapter = adapterYear

        val current = LocalDateTime.now()
        val pattern = DateTimeFormatter.ofPattern("yyyy", indonesia)
        val yNow = current.format(pattern)

        val position: Int = adapterYear.getPosition(yNow)
        binding!!.spYear.setSelection(position)
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        monthSelect = binding!!.spMonth.selectedItem.toString()
        yearsSelect = binding!!.spYear.selectedItem.toString()
        loadKehadiran()
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {
    }

    private fun setListener() {
        binding!!.kehadiranTahunan.setOnClickListener {
            val i = Intent(requireActivity(), KehadiranTahunanActivity::class.java)
            i.putExtra("dataPonpes", kodes)
            i.putExtra("dataUser", idpegawai)
            i.putExtra("yearSelect", yearsSelect)
            activity?.startActivity(i)
        }
    }

    private fun loadKehadiran() {
        binding!!.rvKehadiran.setHasFixedSize(true)
        binding!!.rvKehadiran.itemAnimator = DefaultItemAnimator()
        binding!!.rvKehadiran.layoutManager = LinearLayoutManager(requireContext())

        session = SessionManager(requireContext())
        user = session.sessionDataUser
        ponpes = session.sessionDataPonpes

        kodes = ponpes.kodes
        idpegawai = user.username
        try {
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.getDataLaporan(
                "$kodes",
                "$idpegawai",
                "BULANAN",
                yearsSelect,
                monthSelect
            )

            call.enqueue(object : Callback<DataLaporan?> {
                override fun onResponse(
                    call: Call<DataLaporan?>,
                    response: Response<DataLaporan?>
                ) {
                    if (response.isSuccessful) {
                        val percentage = response.body()?.percentase

                        binding?.progressValue?.text = "$percentage %"
                        binding?.progressHari?.text = response.body()?.percentase_hari
                        binding?.progresBar?.progress = percentage?.let { it / 100 }!!

                        binding?.numHadir?.text = response.body()?.hadir + " Hari"
                        binding?.numIjin?.text = response.body()?.izin_cuti + " Hari"
                        binding?.numAlpha?.text = response.body()?.alpa.toString() + " Hari"
                        binding?.numTerlambat?.text = response.body()?.terlambat + " Jam"
                        binding?.presentaseHadirTahunIni?.text = response.body()?.hadir_tahun_ini

                        detailKehadiranTahun = response.body()!!
                        dataKehadiran = response.body()?.rekap

                        adapter = ListRekapKehadiranAdapter(
                            requireContext(),
                            dataKehadiran!!,
                            this@LaporanFragment
                        )
                        binding!!.rvKehadiran.adapter = adapter

                        if (dataKehadiran!!.size <= 0) {
                            binding!!.headerRv.visibility = View.GONE
                            binding!!.ivNotFound.visibility = View.VISIBLE
                            binding!!.tvNotFound.visibility = View.VISIBLE
                        } else {
                            binding!!.headerRv.visibility = View.VISIBLE
                            binding!!.ivNotFound.visibility = View.GONE
                            binding!!.tvNotFound.visibility = View.GONE
                        }
                    }
                }

                override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {

                }
            })
        } catch (e: Exception) {
            Log.e("Token e", e.toString())
        }
    }

    @SuppressLint("InflateParams")
    override fun selectKehadiran(data: Rekap) {
        val view: View = layoutInflater.inflate(R.layout.bottom_sheet_detail_kehadiran, null, false)
        if (sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        when (data.status) {
            "Hadir" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.green_pesantren_2))
            "Sakit" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            "Cuti" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.lime_dark))
            "Keperluan Lain" -> view.findViewById<TextView>(R.id.txt_status)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        view.findViewById<TextView>(R.id.txt_status).text = data.status

        val tglRekap = parseDateToddMMyyyy(data.hari)
        view.findViewById<TextView>(R.id.txt_tgl_kehadiran).text = tglRekap

        view.findViewById<TextView>(R.id.txt_jam_datang).text = data.detail.jam_datang
        view.findViewById<TextView>(R.id.txt_jam_pulang).text = data.detail.jam_pulang
        view.findViewById<TextView>(R.id.txt_lokasi).text = data.detail.lokasi
        view.findViewById<TextView>(R.id.txt_catatan).text = data.detail.catatan_absen

        bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet?.setContentView(view)

        bottomSheet?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.translucent)
        bottomSheet?.show()
        bottomSheet?.setOnDismissListener {
            bottomSheet = null
        }
    }
}