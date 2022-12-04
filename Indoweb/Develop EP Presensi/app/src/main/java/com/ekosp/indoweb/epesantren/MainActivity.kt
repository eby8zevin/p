package com.ekosp.indoweb.epesantren

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import com.ekosp.indoweb.epesantren.databinding.ActivityHomePageBinding
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.DataUser
import com.ekosp.indoweb.epesantren.model.DeviceData
import com.instacart.library.truetime.TrueTimeRx
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private var session: SessionManager? = null
    private var dataUser: DataUser? = null
    private var dataPonpes: DataPonpes? = null
    private val tag = MainActivity::class.java.simpleName

    private val localeID: Locale = Locale("in", "ID")
    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!TrueTimeRx.isInitialized()) {
            Log.e(tag, "Sorry TrueTime not yet initialized.")
            return
        }
        val trueTime = TrueTimeRx.now()
        val deviceTime = Date()

        session = SessionManager(this)
        dataUser = session!!.sessionDataUser
        dataPonpes = session!!.sessionDataPonpes

        setListener()

        when (dataPonpes?.getwaktu_indonesia()) {
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
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", localeID) //formatting according to my need
        val date: String = formatter.format(today)

        Log.i(
            "MainActivity",
            formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT")).toString()
        )
        Log.i(
            "MainActivity",
            formatDate(deviceTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone(date)).toString()
        )
    }

    private fun formatDate(date: Date, pattern: String, timeZone: TimeZone): String? {
        val format: DateFormat = SimpleDateFormat(pattern, localeID)
        format.timeZone = timeZone
        return format.format(date)
    }

    @Suppress("DEPRECATION")
    private fun setListener() {
        binding.bnMain.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home_menu -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.open_home_page)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.hadir_menu -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.open_laporan_page)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.profil_menu -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.open_profile_page)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        detectIMEIandPhoneNUmber()
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun detectIMEIandPhoneNUmber() {
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        try {
            val imei = tm.deviceId
            val androidID = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
            session!!.saveDeviceData(DeviceData(imei, androidID))
            Log.e("data_hp", "imei = $imei")
            Log.e("data_hp", "android id = $androidID")
        } catch (e: Exception) {
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Anda yakin ingin menutup aplikasi?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Ya") { _: DialogInterface?, _: Int ->
                finishAffinity()
            }
            .create().show()
    }
}