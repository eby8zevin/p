package com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataSchedule {

    @SerializedName("is_correct")
    @Expose
    private Boolean is_correct;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("jadwal")
    @Expose
    private List<Jadwal> jadwal;

    public DataSchedule() {

    }

    public Boolean getIs_correct() {
        return is_correct;
    }

    public void setIs_correct(Boolean is_correct) {
        this.is_correct = is_correct;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Jadwal> getJadwal() {
        return jadwal;
    }

    public void setJadwal(List<Jadwal> jadwal) {
        this.jadwal = jadwal;
    }

    public static class Jadwal {

        @SerializedName("jadwal_id")
        @Expose
        private String jadwal_id;

        @SerializedName("id_hari")
        @Expose
        private String id_hari;

        @SerializedName("hari")
        @Expose
        private String hari;

        @SerializedName("kelas")
        @Expose
        private String kelas;

        @SerializedName("id_nama_pelajaran")
        @Expose
        private String id_nama_pelajaran;

        @SerializedName("nama_pelajaran")
        @Expose
        private String nama_pelajaran;

        @SerializedName("waktu")
        @Expose
        private String waktu;

        @SerializedName("guru")
        @Expose
        private String guru;

        public Jadwal() {

        }

        public String getJadwal_id() {
            return jadwal_id;
        }

        public void setJadwal_id(String jadwal_id) {
            this.jadwal_id = jadwal_id;
        }

        public String getId_hari() {
            return id_hari;
        }

        public void setId_hari(String id_hari) {
            this.id_hari = id_hari;
        }

        public String getHari() {
            return hari;
        }

        public void setHari(String hari) {
            this.hari = hari;
        }

        public String getKelas() {
            return kelas;
        }

        public void setKelas(String kelas) {
            this.kelas = kelas;
        }

        public String getId_nama_pelajaran() {
            return id_nama_pelajaran;
        }

        public void setId_nama_pelajaran(String id_nama_pelajaran) {
            this.id_nama_pelajaran = id_nama_pelajaran;
        }

        public String getNama_pelajaran() {
            return nama_pelajaran;
        }

        public void setNama_pelajaran(String nama_pelajaran) {
            this.nama_pelajaran = nama_pelajaran;
        }

        public String getWaktu() {
            return waktu;
        }

        public void setWaktu(String waktu) {
            this.waktu = waktu;
        }

        public String getGuru() {
            return guru;
        }

        public void setGuru(String guru) {
            this.guru = guru;
        }
    }
}