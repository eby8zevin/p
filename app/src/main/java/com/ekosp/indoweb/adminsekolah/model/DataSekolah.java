package com.ekosp.indoweb.adminsekolah.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataSekolah {

    @SerializedName("is_correct")
    @Expose
    private Boolean isCorrect;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private SekolahData data;

    public DataSekolah() {
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SekolahData getData() {
        return data;
    }

    public void setData(SekolahData data) {
        this.data = data;
    }

    public static class SekolahData {

        @SerializedName("kode_sekolah")
        @Expose
        private String kode_sekolah;

        @SerializedName("nama_sekolah")
        @Expose
        private String nama_sekolah;

        @SerializedName("alamat_sekolah")
        @Expose
        private String alamat_sekolah;

        @SerializedName("domain")
        @Expose
        private String domain;

        @SerializedName("logo")
        @Expose
        private String logo;

        @SerializedName("waktu_indonesia")
        @Expose
        private String waktu_indonesia;

        public SekolahData() {
        }

        public String getKode_sekolah() {
            return kode_sekolah;
        }

        public void setKode_sekolah(String kode_sekolah) {
            this.kode_sekolah = kode_sekolah;
        }

        public String getNama_sekolah() {
            return nama_sekolah;
        }

        public void setNama_sekolah(String nama_sekolah) {
            this.nama_sekolah = nama_sekolah;
        }

        public String getAlamat_sekolah() {
            return alamat_sekolah;
        }

        public void setAlamat_sekolah(String alamat_sekolah) {
            this.alamat_sekolah = alamat_sekolah;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getWaktu_indonesia() {
            return waktu_indonesia;
        }

        public void setWaktu_indonesia(String waktu_indonesia) {
            this.waktu_indonesia = waktu_indonesia;
        }
    }
}