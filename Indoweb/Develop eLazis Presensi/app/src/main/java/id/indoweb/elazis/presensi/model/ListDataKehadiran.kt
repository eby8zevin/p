package id.indoweb.elazis.presensi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ListDataKehadiran(val tgl_hadir: String, val status: String) : Parcelable