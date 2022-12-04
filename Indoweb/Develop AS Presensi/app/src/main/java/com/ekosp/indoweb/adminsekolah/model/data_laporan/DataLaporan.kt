package com.ekosp.indoweb.adminsekolah.model.data_laporan

data class DataLaporan(
    var is_correct: Boolean,
    var message: String,
    var hadir: String,
    var ijin: String,
    var sakit: String,
    var lain: String,
    var terlambat: String,
    var percentase: Float,
    var percentase_hari: String,
    var hadir_tahun_ini: String,
    var rekap: MutableList<Rekap>
)