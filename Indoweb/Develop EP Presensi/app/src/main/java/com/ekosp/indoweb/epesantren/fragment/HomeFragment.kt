package com.ekosp.indoweb.epesantren.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ekosp.indoweb.epesantren.LoginActivity.pass
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.databinding.FragmentHomeBinding
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.ApiInterface
import com.ekosp.indoweb.epesantren.helper.GlobalVar
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.ijin.IjinActivity
import com.ekosp.indoweb.epesantren.laporan.LaporanWeb
import com.ekosp.indoweb.epesantren.locator.MyLocationActivity
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.DataUser
import io.github.muddz.styleabletoast.StyleableToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var session: SessionManager
    private lateinit var dataUser: DataUser
    private lateinit var dataPonpes: DataPonpes

    private var manager: LocationManager? = null
    private val localeID: Locale = Locale("in", "ID")

    private var binding: FragmentHomeBinding? = null
    private val _binding get() = binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setLaporanweb()
        setUI()

        binding!!.checkIn.setOnClickListener { checkIn() }
        binding!!.checkOut.setOnClickListener { checkOut() }
        binding!!.izinCuti.setOnClickListener { izinCuti() }
        binding!!.laporanweb.visibility = View.GONE
    }

    private fun checkIn() {
        if (dataUser.maxDatang.isEmpty()) {
            if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            } else {
                val intent = Intent(activity, MyLocationActivity::class.java)
                intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.PARAM_DATANG)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            }
        } else {
            StyleableToast.makeText(
                requireContext(),
                "Terima kasih. \nAnda sudah melakukan Absen Datang",
                Toast.LENGTH_SHORT,
                R.style.mytoast_danger
            ).show()
        }
    }

    private fun checkOut() {
        if (dataUser.maxDatang.isEmpty()) {
            StyleableToast.makeText(
                requireContext(),
                "Silahkan melakukan \nAbsen Datang terlebih dahulu. \nTerima kasih",
                Toast.LENGTH_SHORT,
                R.style.mytoast_danger
            ).show()
        } else if (dataUser.maxPulang.isEmpty()) {
            val intent = Intent(activity, MyLocationActivity::class.java)
            intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.PARAM_PULANG)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        } else {
            StyleableToast.makeText(
                requireContext(),
                "Terima kasih. \nAnda sudah melakukan Absen Pulang",
                Toast.LENGTH_SHORT,
                R.style.mytoast_danger
            ).show()
        }
    }

    private fun izinCuti() {
        val intent = Intent(activity, IjinActivity::class.java)
        intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.username)
        intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.kodes)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    override fun onResume() {
        super.onResume()

        session = SessionManager(requireContext())
        dataUser = session.sessionDataUser
        dataPonpes = session.sessionDataPonpes

        when (dataPonpes.getwaktu_indonesia()) {
            "WIB" -> {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"))
            }
            "WITA" -> {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ujung_Pandang"))
            }
            else -> {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jayapura"))
            }
        }

        val today: Date = Calendar.getInstance().time //getting date
        val formatter =
            SimpleDateFormat("EEEE, d MMMM yyyy", localeID) //formatting according to my need
        val date: String = formatter.format(today)
        binding!!.hariTanggal.text = date

        val kodes: String = dataPonpes.kodes
        val uname: String = dataUser.nip
        val pass: String = pass

        try {
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.checkLogin(kodes, uname, pass)
            call.enqueue(object : Callback<DataUser?> {
                override fun onResponse(call: Call<DataUser?>, response: Response<DataUser?>) {
                    dataUser = response.body()!!
                    binding?.datangTime?.text = dataUser.maxDatang
                    binding?.pulangTime?.text = dataUser.maxPulang

                    binding?.imageProfile?.let {
                        Glide.with(this@HomeFragment)
                            .load(dataUser.photo)
                            .error(R.drawable.profile)
                            .into(it)
                    }
                }

                override fun onFailure(call: Call<DataUser?>, t: Throwable) {
                    Log.e("HomeFragment", "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.e("Token e", e.toString())
        }
    }

    private fun setUI() {
        session = SessionManager(requireContext())
        dataUser = session.sessionDataUser
        dataPonpes = session.sessionDataPonpes

        val namePonpes = dataPonpes.namaPonpes
        binding?.infoNamaPonpes?.text = buildString {
            append("Ponpes ")
            append(namePonpes)
        }
        binding?.infoNamaUser?.text = dataUser.nama
        binding?.infoJabatan?.text = dataUser.jabatan
        binding?.wib?.text = dataPonpes.getwaktu_indonesia()
    }

    private fun setLaporanweb() {
        binding!!.laporanweb.setOnClickListener {
            val intent = Intent(requireActivity(), LaporanWeb::class.java)
            intent.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.username)
            intent.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.kodes)
            //startActivity(intent)
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Sepertinya GPS mati, mohon hidupkan GPS untuk bisa melakukan absensi!")
            .setCancelable(false)
            .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            }
            .setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}