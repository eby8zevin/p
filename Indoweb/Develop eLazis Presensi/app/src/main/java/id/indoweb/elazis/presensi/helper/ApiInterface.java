package id.indoweb.elazis.presensi.helper;

import id.indoweb.elazis.presensi.model.AndroidVersionResponse;
import id.indoweb.elazis.presensi.model.DataIjin;
import id.indoweb.elazis.presensi.model.DataPonpes;
import id.indoweb.elazis.presensi.model.DataUser;
import id.indoweb.elazis.presensi.model.data_laporan.DataLaporan;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("rest-api/get_data_user_multi.php")
    Call<DataUser> checkLogin(
            @Query("kode_sekolah") String kodes,
            @Query("nip") String uname,
            @Query("password") String pass);

    @GET("rest-api/submitIjin_coba.php")
    Call<DataIjin> submitIjincoba(
            @Query("kode_sekolah") String kodes,
            @Query("id_pegawai") String uname,
            @Query("jenis") String jenis_,
            @Query("tgl_awal") String tgl_awal_,
            @Query("tgl_akhir") String tgl_akhir_,
            @Query("keterangan") String keterangan_);

    @GET("rest-api/get_ponpes.php")
    Call<DataPonpes> checkPonpes(
            @Query("kode_sekolah") String kodes);

    @GET("rest-api/android_version.php")
    Call<AndroidVersionResponse> getAppsVersion();

    @GET("rest-api/get_data_laporan.php")
    Call<DataLaporan> getDataLaporan(
            @Query("kode_sekolah") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("type") String type,
            @Query("tahun") String tahun,
            @Query("bulan") String bulan);

    @GET("rest-api/get_data_laporantahun.php")
    Call<DataLaporan> getDataLaporantahun(
            @Query("kode_sekolah") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("type") String type,
            @Query("tahun") String tahun);
}