package id.indoweb.elazis.presensi.ui.fragment

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
import id.indoweb.elazis.presensi.R
import id.indoweb.elazis.presensi.databinding.FragmentHomeBinding

import id.indoweb.elazis.presensi.helper.ApiClient
import id.indoweb.elazis.presensi.helper.ApiInterface
import id.indoweb.elazis.presensi.helper.GlobalVar
import id.indoweb.elazis.presensi.helper.SessionManager
import id.indoweb.elazis.presensi.model.DataPonpes
import id.indoweb.elazis.presensi.model.DataUser
import id.indoweb.elazis.presensi.ui.absen.MyLocationActivity
import id.indoweb.elazis.presensi.ui.izin.IjinActivity
import id.indoweb.elazis.presensi.ui.laporan.LaporanWeb
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
    private var binding: FragmentHomeBinding? = null
    private val _binding get() = binding!!
    private val localeID: Locale = Locale("in", "ID")

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
        getSession()
        setUI()
        getData()
        binding?.checkIn?.setOnClickListener { checkIn() }
        binding?.checkOut?.setOnClickListener { checkOut() }
        binding?.permitLeave?.setOnClickListener { permitLeave() }
        binding?.btnReportWeb?.visibility = View.INVISIBLE
        binding?.btnReportWeb?.setOnClickListener { reportWeb() }
    }

    private fun getSession() {
        session = SessionManager(requireContext())
        dataPonpes = session.sessionDataPonpes
        dataUser = session.sessionDataUser
    }

    private fun setUI() {
        binding?.infoSchoolName?.text = buildString {
            append("Sekolah ")
            append(dataPonpes.namaPonpes)
        }

        val today: Date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", localeID)
        val date: String = formatter.format(today)
        binding?.dateDay?.text = date

        val timeIndonesia = dataPonpes.getwaktu_indonesia()
        when (timeIndonesia) {
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
        binding?.gmt?.text = timeIndonesia

        binding?.infoUserName?.text = dataUser.nama
        binding?.infoJob?.text = dataUser.jabatan
    }

    private fun checkIn() {
        if (dataUser.maxDatang.isEmpty()) {
            if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            } else {
                val i = Intent(context, MyLocationActivity::class.java)
                i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.PARAM_DATANG)
                startActivity(i)
                activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            }
        } else {
            StyleableToast.makeText(
                requireContext(),
                "Terima kasih. \nAnda sudah melakukan Absen Datang",
                Toast.LENGTH_SHORT,
                R.style.mytoast
            ).show()
        }
    }

    private fun checkOut() {
        if (dataUser.maxDatang.isEmpty()) {
            StyleableToast.makeText(
                requireContext(),
                "Silahkan melakukan \nAbsen Datang terlebih dahulu.",
                Toast.LENGTH_SHORT,
                R.style.mytoast
            ).show()
        } else if (dataUser.maxPulang.isEmpty()) {
            if (!manager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()
            } else {
                val i = Intent(context, MyLocationActivity::class.java)
                i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.PARAM_PULANG)
                startActivity(i)
                activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            }
        } else {
            StyleableToast.makeText(
                requireContext(),
                "Terima kasih. \nAnda sudah melakukan Absen Pulang",
                Toast.LENGTH_SHORT,
                R.style.mytoast
            ).show()
        }
    }

    private fun permitLeave() {
        val i = Intent(activity, IjinActivity::class.java)
        i.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.username)
        i.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.kodes)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun reportWeb() {
        val i = Intent(requireActivity(), LaporanWeb::class.java)
        i.putExtra(GlobalVar.PARAM_KODES_USER, dataPonpes.kodes)
        i.putExtra(GlobalVar.PARAM_DATA_USER, dataUser.username)
        startActivity(i)
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

    private fun getData() {
        val schoolCode = dataPonpes.kodes
        val nip = dataUser.nip
        val password = session.pwd
        try {
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.checkLogin(schoolCode, nip, password)
            call.enqueue(object : Callback<DataUser?> {
                override fun onResponse(call: Call<DataUser?>, response: Response<DataUser?>) {
                    if (response.isSuccessful) {
                        dataUser = response.body()!!
                        binding?.tvTimeCome?.text = dataUser.maxDatang
                        binding?.tvTimeGoHome?.text = dataUser.maxPulang

                        binding?.let {
                            Glide.with(this@HomeFragment)
                                .load(dataUser.photo)
                                .error(R.drawable.profile)
                                .into(it.imgProfile)
                        }
                    }
                }

                override fun onFailure(call: Call<DataUser?>, t: Throwable) {
                    Log.e("HomeFragment", "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.e("HomeFragment", e.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        getSession()
        setUI()
        getData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}