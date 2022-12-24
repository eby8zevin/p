package com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran

data class DataLaporanTahunPelajaran(
    var is_correct: Boolean,
    var message: String,
    var data: MutableList<PelajaranLaporanTahunData>
)

data class PelajaranLaporanTahunData(
    var bulan: String,
    var hadir: String,
    var ijin: String,
    var sakit: String,
    var lain: String
)
