package id.indoweb.elazis.presensi.model.data_laporan

data class DataLaporan(
    var hadir: String,
    var izin_sakit: String,
    var terlambat: String,
    var percentase: Float,
    var percentase_hari: String,
    var hadir_tahun_ini: String,
    var rekap: MutableList<Rekap> ?
)