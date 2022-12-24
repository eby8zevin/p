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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.FragmentHomePelajaranBinding
import com.ekosp.indoweb.adminsekolah.helper.ApiClient
import com.ekosp.indoweb.adminsekolah.helper.ApiInterface
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.helper.SessionManager
import com.ekosp.indoweb.adminsekolah.model.DataSekolah
import com.ekosp.indoweb.adminsekolah.model.DataUser
import com.ekosp.indoweb.adminsekolah.ui.JadwalPelajaranActivity
import com.ekosp.indoweb.adminsekolah.ui.absen.MyLocationActivity
import com.ekosp.indoweb.adminsekolah.ui.izin.IjinPelajaranActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomePelajaranFragment : Fragment() {

    private lateinit var session: SessionManager
    private lateinit var sekolahData: DataSekolah.SekolahData
    private lateinit var userData: DataUser.UserData

    private lateinit var manager: LocationManager
    private var gpsProvider: Boolean = true
    private var networkProvider: Boolean = true

    private lateinit var binding: FragmentHomePelajaranBinding
    private val _binding get() = binding
    private val localeID: Locale = Locale("in", "ID")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomePelajaranBinding.inflate(inflater, container, false)
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
        binding.permitLeave.setOnClickListener { permitLeave() }
        binding.checkSchedule.setOnClickListener { openWebView() }
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

        println(userData.max_datang)
    }

    private fun checkIn() {
        if (!gpsProvider && !networkProvider) {
            buildAlertMessageNoGps()
        } else {
            val i = Intent(context, MyLocationActivity::class.java)
            i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, GlobalVar.ABSEN_PELAJARAN)
            startActivity(i)
            activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    private fun permitLeave() {
        val i = Intent(activity, IjinPelajaranActivity::class.java)
        i.putExtra(GlobalVar.PARAM_DATA_USER, userData.username)
        i.putExtra(GlobalVar.PARAM_KODES_USER, sekolahData.kode_sekolah)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun openWebView() {
        val i = Intent(activity, JadwalPelajaranActivity::class.java)
        i.putExtra(GlobalVar.PARAM_KODES_USER, sekolahData.kode_sekolah)
        i.putExtra(GlobalVar.PARAM_DATA_USER, userData.username)
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

                        Glide.with(this@HomePelajaranFragment)
                            .load(responseBody.data.photo)
                            .centerCrop()
                            .error(R.drawable.profile)
                            .skipMemoryCache(true)
                            .into(binding.imgProfile)
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
        private const val TAG = "HomePelajaranFragment"
    }
}