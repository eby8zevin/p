package com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataClass {

    @SerializedName("is_correct")
    @Expose
    private String is_correct;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("kelas")
    @Expose
    private List<Kelas> kelas;

    public DataClass() {
    }

    public String getIs_correct() {
        return is_correct;
    }

    public void setIs_correct(String is_correct) {
        this.is_correct = is_correct;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Kelas> getKelas() {
        return kelas;
    }

    public void setKelas(List<Kelas> kelas) {
        this.kelas = kelas;
    }

    public static class Kelas {

        @SerializedName("id_kelas")
        @Expose
        private String id_kelas;

        @SerializedName("nama_kelas")
        @Expose
        private String nama_kelas;

        public String getId_kelas() {
            return id_kelas;
        }

        public void setId_kelas(String id_kelas) {
            this.id_kelas = id_kelas;
        }

        public String getNama_kelas() {
            return nama_kelas;
        }

        public void setNama_kelas(String nama_kelas) {
            this.nama_kelas = nama_kelas;
        }
    }
}