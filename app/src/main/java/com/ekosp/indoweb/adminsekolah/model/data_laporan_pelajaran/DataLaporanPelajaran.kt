package com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran

data class DataLaporanPelajaran(
    var is_correct: Boolean,
    var message: String,
    var data: PelajaranLaporanData
)

data class PelajaranLaporanData(
    var hadir: String,
    var ijin: String,
    var sakit: String,
    var lain: String,
    var hadir_tahun_ini: String,
    var rekap: MutableList<RekapPelajaran>
)

data class RekapPelajaran(
    var tanggal: String,
    var jam_absen: String,
    var status: String,
    var kelas: String,
    var nama_pelajaran: String,
    var jam_pelajaran: String,
    var lokasi: String
)