package com.ekosp.indoweb.adminsekolah.helper;

import com.ekosp.indoweb.adminsekolah.model.AndroidVersionResponse;
import com.ekosp.indoweb.adminsekolah.model.DataIjin;
import com.ekosp.indoweb.adminsekolah.model.DataSekolah;
import com.ekosp.indoweb.adminsekolah.model.DataUser;
import com.ekosp.indoweb.adminsekolah.model.DefaultResponse;
import com.ekosp.indoweb.adminsekolah.model.data_laporan.DataLaporan;
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataClass;
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataLaporanTahunPelajaran;
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataMajors;
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataLaporanPelajaran;
import com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran.DataSchedule;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiInterface {

    @GET("android_version.php")
    Call<AndroidVersionResponse> getAppsVersion();

    @GET("sekolah.php")
    Call<DataSekolah> checkSchool(
            @Query("kode_sekolah") String kode_sekolah);

    @FormUrlEncoded
    @POST("login.php")
    Call<DataUser> checkLogin(
            @Field("kode_sekolah") String kode_sekolah,
            @Field("nip") String nip,
            @Field("password") String password);

    @GET("laporan_datangpulang.php")
    Call<DataLaporan> getDataLaporan(
            @Query("kode_sekolah") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("type") String type,
            @Query("tahun") String tahun,
            @Query("bulan") String bulan);

    @GET("laporantahun_datangpulang.php")
    Call<DataLaporan> getDataLaporantahun(
            @Query("kode_sekolah") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("type") String type,
            @Query("tahun") String tahun);

    @FormUrlEncoded
    @POST("ijin_datangpulang.php")
    Call<DataIjin> ijinDatangPulang(
            @Field("kode_sekolah") String kodes,
            @Field("id_pegawai") String uname,
            @Field("jenis") String jenis_,
            @Field("tgl_awal") String tgl_awal_,
            @Field("tgl_akhir") String tgl_akhir_,
            @Field("keterangan") String keterangan_);

    //new feature
    @GET("get_majors.php")
    Call<DataMajors> getDataMajors(
            @Query("kode_sekolah") String kode_sekolah
    );

    @GET("get_class.php")
    Call<DataClass> getDataClass(
            @Query("kode_sekolah") String kode_sekolah,
            @Query("id_unit") String id_unit
    );

    @GET("get_schedule.php")
    Call<DataSchedule> getDataSchedule(
            @Query("kode_sekolah") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("id_kelas") String id_kelas
    );

    @Multipart
    @POST("absen_pelajaran.php")
    Call<DefaultResponse> postDataAbsentLesson(
            @Part("kode_sekolah") RequestBody kode_sekolah,
            @Part("id_pegawai") RequestBody id_pegawai,
            @Part("nama_pegawai") RequestBody nama_pegawai,
            @Part("type") RequestBody type,
            @Part("lokasi") RequestBody lokasi,
            @Part("longi") RequestBody longi,
            @Part("lati") RequestBody lati,
            @Part MultipartBody.Part file,
            @Part("id_unit") RequestBody id_unit,
            @Part("id_kelas") RequestBody id_kelas,
            @Part("id_jadwal") RequestBody id_jadwal,
            @Part("id_pelajaran") RequestBody id_pelajaran,
            @Part("jam_pelajaran") RequestBody jam_pelajaran
    );

    @GET("laporan_pelajaran.php")
    Call<DataLaporanPelajaran> getDataLessonReport(
            @Query("kode_sekolah") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("type") String type,
            @Query("tahun") String tahun,
            @Query("bulan") String bulan
    );

    @GET("laporantahun_pelajaran.php")
    Call<DataLaporanTahunPelajaran> getDataLessonReportYear(
            @Query("kode_sekolah") String kode_sekolah,
            @Query("id_pegawai") String id_pegawai,
            @Query("type") String type,
            @Query("tahun") String tahun
    );

}