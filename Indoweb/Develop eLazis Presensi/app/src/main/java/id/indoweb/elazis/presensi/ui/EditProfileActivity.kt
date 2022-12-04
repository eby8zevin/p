package id.indoweb.elazis.presensi.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import id.indoweb.elazis.presensi.R
import id.indoweb.elazis.presensi.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val requestPermission = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Edit Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_hp)

        Glide.with(this)
            .load(intent.getStringExtra("PHOTO"))
            .error(R.drawable.profile)
            .into(binding.profilePicture)
        binding.editName.setText(intent.getStringExtra("NAME"))
        binding.editPosition.setText(intent.getStringExtra("POSITION"))
        binding.editNip.setText(intent.getStringExtra("NIP"))
        binding.editEmail.setText(intent.getStringExtra("EMAIL"))
        binding.editNoHp.setText(intent.getStringExtra("NO_HP"))

        if (checkAndRequestPermissions(this)) {
            binding.profilePicture.setOnClickListener { chooseImage(this) }
        }
        binding.btnSubmit.setOnClickListener { check() }
    }

    private fun check() {
        val name = binding.editName.text.toString()
        val position = binding.editPosition.text.toString()
        val nip = binding.editNip.text.toString()
        val email = binding.editEmail.text.toString()
        val noHp = binding.editNoHp.text.toString()

        if (name.isEmpty()) {
            binding.editName.error = "Nama Tidak Boleh Kosong"
            binding.editName.requestFocus()
        } else if (position.isEmpty()) {
            binding.editPosition.error = "Jabatan Tidak Boleh Kosong"
            binding.editPosition.requestFocus()
        } else if (nip.isEmpty()) {
            binding.editNip.error = "NIP Tidak Boleh Kosong"
            binding.editNip.requestFocus()
        } else if (email.isEmpty()) {
            binding.editEmail.error = "Email Tidak Boleh Kosong"
            binding.editEmail.requestFocus()
        } else if (noHp.isEmpty()) {
            binding.editNoHp.error = "No. Ponsel Tidak Boleh Kosong"
            binding.editNoHp.requestFocus()
        } else {
            toServer(name, position, nip, noHp)
        }
    }

    private fun toServer(name: String, position: String, nip: String, noHp: String) {
        Toast.makeText(
            this,
            "$name \n$position \n$nip \n$noHp \nData send to server",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun chooseImage(context: Context) {
        val optionsMenu = arrayOf<CharSequence>(
            "Buka Camera",
            "Pilih dari Galeri",
            "Batal"
        )

        val builder = AlertDialog.Builder(context)

        builder.setItems(
            optionsMenu
        ) { dialogInterface, i ->
            if (optionsMenu[i] == "Buka Camera") {

                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, 0)
            } else if (optionsMenu[i] == "Pilih dari Galeri") {

                val pickPhoto =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhoto, 1)
            } else if (optionsMenu[i] == "Batal") {
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    binding.profilePicture.setImageBitmap(selectedImage)
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor: Cursor? =
                            contentResolver.query(selectedImage, filePathColumn, null, null, null)
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath: String = cursor.getString(columnIndex)
                            binding.profilePicture.setImageBitmap(
                                BitmapFactory.decodeFile(
                                    picturePath
                                )
                            )
                            cursor.close()
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions(context: Activity?): Boolean {
        val storePermission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val cameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (storePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                .add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                context, listPermissionsNeeded
                    .toTypedArray(),
                requestPermission
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            requestPermission -> if (ContextCompat.checkSelfPermission(
                    this@EditProfileActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    applicationContext,
                    "FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT
                )
                    .show()
            } else if (ContextCompat.checkSelfPermission(
                    this@EditProfileActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    applicationContext,
                    "FlagUp Requires Access to Your Storage.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                chooseImage(this@EditProfileActivity)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}