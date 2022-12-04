package id.indoweb.elazis.presensi.ui.fragment

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

import com.google.android.material.bottomsheet.BottomSheetBehavior
import id.indoweb.elazis.presensi.R
import id.indoweb.elazis.presensi.databinding.BottomSheetLogoutBinding
import id.indoweb.elazis.presensi.databinding.FragmentProfileBinding

import id.indoweb.elazis.presensi.helper.SessionManager
import id.indoweb.elazis.presensi.model.DataPonpes
import id.indoweb.elazis.presensi.model.DataUser
import id.indoweb.elazis.presensi.ui.EditProfileActivity
import id.indoweb.elazis.presensi.ui.LoginPonpesActivity
import id.indoweb.elazis.presensi.ui.MainActivity
import io.github.muddz.styleabletoast.StyleableToast
import jp.wasabeef.glide.transformations.BlurTransformation

class ProfileFragment : Fragment() {
    //#1 Defining a BottomSheetBehavior instance
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CardView>
    private lateinit var session: SessionManager
    private lateinit var dataPonpes: DataPonpes
    private lateinit var dataUser: DataUser

    private var binding: FragmentProfileBinding? = null
    private val _binding get() = binding!!
    private var bindingBS: BottomSheetLogoutBinding? = null

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
        bottomSheetBehavior = BottomSheetBehavior.from(bindingBS!!.bottomSheet)

        getSession()
        setUI()
        binding?.editProfile?.setOnClickListener { editProfile() }
        binding?.btnLogout?.setOnClickListener { clickLogout() }
        bindingBS?.btnBatal?.setOnClickListener { logoutCancel() }
        bindingBS?.btnKeluar?.setOnClickListener { logoutYes() }
    }

    private fun getSession() {
        session = SessionManager(requireContext())
        dataUser = session.sessionDataUser
        dataPonpes = session.sessionDataPonpes
    }

    private fun editProfile() {
        val i = Intent(activity, EditProfileActivity::class.java)
        i.putExtra("PHOTO", dataUser.photo)
        i.putExtra("NAME", dataUser.nama)
        i.putExtra("POSITION", dataUser.jabatan)
        i.putExtra("NIP", dataUser.nip)
        i.putExtra("EMAIL", dataUser.email)
        i.putExtra("NO_HP", dataUser.phone)
        startActivity(i)
    }

    private fun setUI() {
        Glide.with(this@ProfileFragment)
            .load(dataUser.photo)
            .error(R.drawable.profile)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(50, 3)))
            .into(binding!!.imgBlur)

        Glide.with(this@ProfileFragment)
            .load(dataUser.photo)
            .error(R.drawable.profile)
            .into(binding!!.profileImage)

        binding?.infoUserName?.text = dataUser.nama
        binding?.tvSchoolName?.text = dataPonpes.namaPonpes
        binding?.tvJob?.text = dataUser.jabatan
        binding?.nip?.text = dataUser.nip
        binding?.email?.text = dataUser.email
        binding?.noHp?.text = dataUser.phone

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
        session.logoutUser(dataUser.nip, dataPonpes.kodes)
        startActivity(i)
        (activity as MainActivity).overridePendingTransition(
            R.anim.push_left_in,
            R.anim.push_left_out
        )
        (activity as MainActivity).finish()
    }

    override fun onResume() {
        super.onResume()
        getSession()
        setUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        bindingBS = null
    }
}