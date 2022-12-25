package com.ekosp.indoweb.adminsekolah.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.ActivityHomePageBinding
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar
import com.ekosp.indoweb.adminsekolah.helper.SessionManager
import com.ekosp.indoweb.adminsekolah.model.DataSekolah
import com.ekosp.indoweb.adminsekolah.model.DataUser
import com.ekosp.indoweb.adminsekolah.model.DeviceData
import com.instacart.library.truetime.TrueTime
import com.instacart.library.truetime.TrueTimeRx
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var dataSekolah: DataSekolah.SekolahData
    private lateinit var dataUser: DataUser.UserData

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TrueTime.isInitialized()

        session = SessionManager(this)
        dataUser = session.sessionDataUser
        dataSekolah = session.sessionDataSekolah

        getBottomNavigation()
    }

    @Suppress("DEPRECATION")
    private fun getBottomNavigation() {

        binding.bottomNavigation.selectedItemId = R.id.menu_profile
        binding.bottomNavigation.setOnNavigationItemSelectedListener {

            if (dataUser.mode_absen == GlobalVar.ABSEN_DATANG_PULANG) {
                when (it.itemId) {
                    R.id.menu_home -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.open_home_page)
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.menu_present -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.open_laporan_page)
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.menu_profile -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.open_profile_page)
                        return@setOnNavigationItemSelectedListener true
                    }
                }
            } else if (dataUser.mode_absen == GlobalVar.ABSEN_PELAJARAN) {
                when (it.itemId) {
                    R.id.menu_home -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.open_home_lesson)
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.menu_present -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.open_lesson_report)
                        return@setOnNavigationItemSelectedListener true
                    }

                    R.id.menu_profile -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.open_profile_page)
                        return@setOnNavigationItemSelectedListener true
                    }
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Anda yakin ingin menutup aplikasi?")
            .setNegativeButton("Tidak", null)
            .setPositiveButton("Ya") { _: DialogInterface?, _: Int -> finishAffinity() }
            .create().show()
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
            session.saveDeviceData(DeviceData(imei, androidID))
            Log.e("data_hp", "imei = $imei")
            Log.e("data_hp", "android id = $androidID")
        } catch (e: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        detectIMEIandPhoneNUmber()

        if (!TrueTimeRx.isInitialized()) {
            Log.d("TrueTimeRx", "Sorry TrueTime not yet initialized.")
            return
        }

        dateTrueTime()
    }

    private fun dateTrueTime() {
        val trueTime: Date = TrueTime.now()
        val deviceTime = Date()

        when (dataSekolah.waktu_indonesia) {
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

        val today: Date = Calendar.getInstance().time //getting date
        val formatter =
            SimpleDateFormat("ZZZZ", Locale.getDefault()) //formatting according to my need
        val date: String = formatter.format(today)

        Log.e(
            "TrueTime",
            formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT")).toString()
        )

        Log.e(
            "deviceTime",
            formatDate(
                deviceTime,
                "yyyy-MM-dd HH:mm:ss",
                TimeZone.getTimeZone(date)
            ).toString()
        )
    }

    private fun formatDate(date: Date, pattern: String, timeZone: TimeZone): String? {
        val format: DateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
        format.timeZone = timeZone
        return format.format(date)
    }
}