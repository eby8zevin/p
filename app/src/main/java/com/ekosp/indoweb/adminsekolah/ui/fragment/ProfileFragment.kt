package com.ekosp.indoweb.adminsekolah.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ekosp.indoweb.adminsekolah.ui.LoginPonpesActivity
import com.ekosp.indoweb.adminsekolah.ui.MainActivity
import com.ekosp.indoweb.adminsekolah.R
import com.ekosp.indoweb.adminsekolah.databinding.BottomSheetLogoutBinding
import com.ekosp.indoweb.adminsekolah.databinding.FragmentProfileBinding
import com.ekosp.indoweb.adminsekolah.helper.SessionManager
import com.ekosp.indoweb.adminsekolah.model.DataSekolah
import com.ekosp.indoweb.adminsekolah.model.DataUser
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.muddz.styleabletoast.StyleableToast
import jp.wasabeef.glide.transformations.BlurTransformation

class ProfileFragment : Fragment() {
    //#1 Defining a BottomSheetBehavior instance
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CardView>
    private lateinit var session: SessionManager

    private lateinit var sekolahData: DataSekolah.SekolahData
    private lateinit var userData: DataUser.UserData

    private lateinit var binding: FragmentProfileBinding
    private val _binding get() = binding
    private lateinit var bindingBS: BottomSheetLogoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        bindingBS = _binding.bsLogout
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //#2 Initializing the BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(bindingBS.bottomSheet)

        getSession()
        setUI()

        binding.editProfile.visibility = View.GONE

        binding.btnLogout.setOnClickListener { clickLogout() }
        bindingBS.btnBatal.setOnClickListener { logoutCancel() }
        bindingBS.btnKeluar.setOnClickListener { logoutYes() }
    }

    private fun getSession() {
        session = SessionManager(requireContext())
        sekolahData = session.sessionDataSekolah
        userData = session.sessionDataUser
    }

    private fun editProfile() {
        val i = Intent(activity, MainActivity::class.java)
        i.putExtra("PHOTO", userData.photo)
        i.putExtra("NAME", userData.nama)
        i.putExtra("POSITION", userData.jabatan)
        i.putExtra("NIP", userData.nip)
        i.putExtra("EMAIL", userData.email)
        i.putExtra("NO_HP", userData.phone)
        startActivity(i)
    }

    private fun setUI() {
        Glide.with(this@ProfileFragment)
            .load(userData.photo)
            .error(R.drawable.profile)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(50, 3)))
            .skipMemoryCache(true)
            .into(binding.imgBlur)

        Glide.with(this@ProfileFragment)
            .load(userData.photo)
            .error(R.drawable.profile)
            .skipMemoryCache(true)
            .into(binding.profileImage)

        binding.infoUserName.text = userData.nama
        binding.tvSchoolName.text = sekolahData.kode_sekolah
        binding.tvJob.text = userData.jabatan
        binding.nip.text = userData.nip
        binding.email.text = userData.email
        binding.noHp.text = userData.phone

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.peekHeight = 5
    }

    private fun clickLogout() {
        val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            BottomSheetBehavior.STATE_COLLAPSED
        else
            BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.state = state
    }

    private fun logoutCancel() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun logoutYes() {
        StyleableToast.makeText(
            requireContext(),
            "Berhasil Keluar",
            Toast.LENGTH_SHORT,
            R.style.mytoast
        ).show()
        val i = Intent(activity, LoginPonpesActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        session.logoutUser(sekolahData.kode_sekolah, userData.nip)
        startActivity(i)
        (activity as MainActivity).overridePendingTransition(
            R.anim.push_left_in,
            R.anim.push_left_out
        )
        (activity as MainActivity).finish()
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
    }
}