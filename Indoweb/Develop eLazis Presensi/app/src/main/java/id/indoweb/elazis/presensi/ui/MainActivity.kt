package id.indoweb.elazis.presensi.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import id.indoweb.elazis.presensi.R
import id.indoweb.elazis.presensi.databinding.ActivityHomePageBinding

import id.indoweb.elazis.presensi.helper.SessionManager
import id.indoweb.elazis.presensi.model.DataPonpes
import id.indoweb.elazis.presensi.model.DataUser
import id.indoweb.elazis.presensi.model.DeviceData
import com.instacart.library.truetime.TrueTimeRx

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

    private lateinit var binding: ActivityHomePageBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (!TrueTimeRx.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized.", Toast.LENGTH_SHORT).show()
            return
        }

        session = SessionManager(this)
        dataUser = session?.sessionDataUser
        dataPonpes = session?.sessionDataPonpes

        getBottomNavigation()
    }

    @Suppress("DEPRECATION")
    private fun getBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
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
            false
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
            session!!.saveDeviceData(DeviceData(imei, androidID))
            Log.e("data_hp", "imei = $imei")
            Log.e("data_hp", "android id = $androidID")
        } catch (e: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        detectIMEIandPhoneNUmber()
    }
}