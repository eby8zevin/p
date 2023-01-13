package id.indoweb.elazis.presensi.helper;

import id.indoweb.elazis.presensi.model.AndroidVersionResponse;
import id.indoweb.elazis.presensi.model.DataIjin;
import id.indoweb.elazis.presensi.model.DataPonpes;
import id.indoweb.elazis.presensi.model.DataUser;
import id.indoweb.elazis.presensi.model.data_laporan.DataLaporan;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("android_version.php")
    Call<AndroidVersionResponse> getAppsVersion();

    @GET("elazis.php")
    Call<DataPonpes> checkLembaga(
            @Query("kode_lembaga") String kodes);

    @FormUrlEncoded
    @POST("login.php")
    Call<DataUser> checkLogin(
            @Field("kode_lembaga") String kodes,
            @Field("nip") String uname,
            @Field("password") String pass);

    @GET("laporan_bulan.php")
    Call<DataLaporan> getDataLaporan(
            @Query("kode_lembaga") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("type") String type,
            @Query("tahun") String tahun,
            @Query("bulan") String bulan);

    @GET("laporan_tahun.php")
    Call<DataLaporan> getDataLaporantahun(
            @Query("kode_lembaga") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("type") String type,
            @Query("tahun") String tahun);

    @FormUrlEncoded
    @POST("izin.php")
    Call<DataIjin> submitIjincoba(
            @Field("kode_lembaga") String kodes,
            @Field("id_pegawai") String uname,
            @Field("jenis") String jenis_,
            @Field("tgl_awal") String tgl_awal_,
            @Field("tgl_akhir") String tgl_akhir_,
            @Field("keterangan") String keterangan_);
}