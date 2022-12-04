package com.ekosp.indoweb.adminsekolah.ui.fragment

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
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.FragmentHomeBinding
import com.ekosp.indoweb.adminsekolah.helper.ApiClient
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.helper.SessionManager
import com.ekosp.indoweb.adminsekolah.model.DataSekolah
import com.ekosp.indoweb.adminsekolah.model.DataUser
import com.ekosp.indoweb.adminsekolah.ui.absen.MyLocationActivity
import com.ekosp.indoweb.adminsekolah.ui.izin.IjinActivity
import io.github.muddz.styleabletoast.StyleableToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var session: SessionManager
    private lateinit var sekolahData: DataSekolah.SekolahData
    private lateinit var userData: DataUser.UserData

    private lateinit var manager: LocationManager
    private var gpsProvider: Boolean = true
    private var networkProvider: Boolean = true

    private lateinit var binding: FragmentHomeBinding
    private val _binding get() = binding
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

        manager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        getSession()
        setUI()
        getData()

        binding.checkIn.setOnClickListener { checkIn() }
        binding.checkOut.setOnClickListener { checkOut() }
        binding.permitLeave.setOnClickListener { permitLeave() }

        if (userData.mode_absen == GlobalVar.ABSEN_DATANG_PULANG) {
            binding.tvDatangPulang.visibility = View.VISIBLE
            binding.valueDatangPulang.visibility = View.VISIBLE
            binding.checkOut.visibility = View.VISIBLE
        } else if (userData.mode_absen == GlobalVar.ABSEN_PELAJARAN) {
            binding.tvDatangPulang.visibility = View.GONE
            binding.valueDatangPulang.visibility = View.GONE
            binding.checkOut.visibility = View.GONE
        }
    }

    private fun getSession() {
        session = SessionManager(requireContext())
        sekolahData = session.sessionDataSekolah
        userData = session.sessionDataUser
    }

    private fun setUI() {
        binding.infoSchoolName.text = buildString {
            append("Sekolah ")
            append(sekolahData.nama_sekolah)
        }

        val today: Date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", localeID)
        val date: String = formatter.format(today)
        binding.dateDay.text = date

        val timeIndonesia = sekolahData.waktu_indonesia
        when (timeIndonesia) {
            "WIB" -> {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"))
            }
            "WITA" -> {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Makassar"))
            }
            else -> {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jayapura"))
            }
        }
        binding.gmt.text = timeIndonesia

        binding.infoUserName.text = userData.nama
        binding.infoJob.text = userData.jabatan
    }

    private fun checkIn() {
        if (!gpsProvider && !networkProvider) {
            buildAlertMessageNoGps()
        } else if (userData.mode_absen == GlobalVar.ABSEN_DATANG_PULANG) {
            if (userData.max_datang.isEmpty()) {
                val i = Intent(context, MyLocationActivity::class.java)
                i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.PARAM_DATANG)
                startActivity(i)
                activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
            } else {
                StyleableToast.makeText(
                    requireContext(),
                    "Terima kasih. \nAnda sudah melakukan Absen Datang",
                    Toast.LENGTH_SHORT,
                    R.style.mytoast
                ).show()
            }
        } else if (userData.mode_absen == GlobalVar.ABSEN_PELAJARAN) {
            val i = Intent(context, MyLocationActivity::class.java)
            i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.ABSEN_PELAJARAN)
            startActivity(i)
            activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        } else {
            Log.i(TAG, "Cooming soon Present 2 Mode")
        }
    }

    private fun checkOut() {
        if (userData.max_datang.isEmpty()) {
            StyleableToast.makeText(
                requireContext(),
                "Silahkan melakukan \nAbsen Datang terlebih dahulu.",
                Toast.LENGTH_SHORT,
                R.style.mytoast
            ).show()
        } else if (userData.max_pulang.isEmpty()) {
            if (!gpsProvider && !networkProvider) {
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
        i.putExtra(GlobalVar.PARAM_DATA_USER, userData.username)
        i.putExtra(GlobalVar.PARAM_KODES_USER, sekolahData.kode_sekolah)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
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
        val schoolCode = sekolahData.kode_sekolah
        val nip = userData.nip
        val password = session.savedPwd
        try {
            showLoading(true)
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.checkLogin(schoolCode, nip, password)
            call.enqueue(object : Callback<DataUser> {
                override fun onResponse(call: Call<DataUser>, response: Response<DataUser>) {
                    showLoading(false)
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        binding.tvTimeCome.text = responseBody.data.max_datang
                        binding.tvTimeGoHome.text = responseBody.data.max_pulang

                        Glide.with(this@HomeFragment)
                            .load(responseBody.data.photo)
                            .centerCrop()
                            .error(R.drawable.profile)
                            .skipMemoryCache(true)
                            .into(binding.imgProfile)
                    } else {
                        Log.e(TAG, "onFailure: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DataUser>, t: Throwable) {
                    showLoading(false)
                    Log.e(TAG, "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        getSession()
        getData()
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}