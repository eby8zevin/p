package com.ekosp.indoweb.epesantren.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ekosp.indoweb.epesantren.LoginPonpesActivity
import com.ekosp.indoweb.epesantren.MainActivity
import com.ekosp.indoweb.epesantren.R
import com.ekosp.indoweb.epesantren.databinding.BottomSheetLogoutBinding
import com.ekosp.indoweb.epesantren.databinding.FragmentProfileBinding
import com.ekosp.indoweb.epesantren.helper.ApiClient
import com.ekosp.indoweb.epesantren.helper.ApiInterface
import com.ekosp.indoweb.epesantren.helper.SessionManager
import com.ekosp.indoweb.epesantren.model.DataPonpes
import com.ekosp.indoweb.epesantren.model.DataUser
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.muddz.styleabletoast.StyleableToast
import jp.wasabeef.glide.transformations.BlurTransformation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    //#1 Defining a BottomSheetBehavior instance
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CardView>
    private lateinit var session: SessionManager
    private lateinit var dataUser: DataUser
    private lateinit var dataPonpes: DataPonpes

    private var kodes: String? = null
    private var uname: String? = null

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

        setListener()
        setUI()
    }

    override fun onResume() {
        super.onResume()
        session = SessionManager(requireContext())
        dataUser = session.sessionDataUser
        dataPonpes = session.sessionDataPonpes

        kodes = dataPonpes.kodes
        uname = dataUser.nip

        try {
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)

            binding?.infoNamaUser2?.text = dataUser.nama
            binding?.jabatan?.text = dataUser.jabatan
            binding?.nip?.text = dataUser.nip
            binding?.email?.text = dataUser.email
            binding?.noPonsel?.text = dataUser.phone

            binding?.profileImage?.let {
                Glide.with(this@ProfileFragment)
                    .load(dataUser.photo)
                    .error(R.drawable.profile)
                    .into(it)
            }

            binding?.fotoBlur?.let {
                Glide.with(this@ProfileFragment)
                    .load(dataUser.photo)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(50, 3)))
                    .error(R.drawable.profile)
                    .into(it)
            }

            val call2 = apiService.checkPonpes(kodes)
            call2.enqueue(object : Callback<DataPonpes?> {
                override fun onResponse(
                    call_2: Call<DataPonpes?>,
                    response: Response<DataPonpes?>
                ) {
                    dataPonpes = response.body()!!
                    binding?.pesantren?.text = dataPonpes.namaPonpes
                }

                override fun onFailure(call_2: Call<DataPonpes?>, t: Throwable) {
                    Log.e("ProfileFragment", "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.e("Token e", e.toString())
        }
    }

    private fun setUI() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.peekHeight = 5
    }

    private fun setListener() {
        binding!!.btnLogout.setOnClickListener {
            val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_COLLAPSED
            else
                BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.state = state
        }

        bindingBS!!.btnBatal.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bindingBS!!.btnKeluar.setOnClickListener {
            StyleableToast.makeText(
                requireContext(),
                "Berhasil Keluar",
                Toast.LENGTH_SHORT,
                R.style.mytoast
            ).show()
            val i = Intent(activity, LoginPonpesActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            session.logoutUser(uname, kodes)
            startActivity(i)
            (activity as MainActivity).finish()
        }
    }
}