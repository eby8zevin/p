package com.ekosp.indoweb.epesantren.model;

/**
 * Created by Eko Setyo Purnomo on 08-Apr-18.
 * Find me on https://ekosp.com
 * Or email me directly to ekosetyopurnomo@gmail.com
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataPonpes {

    @SerializedName("is_correct")
    @Expose
    private Boolean isCorrect;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("nama_pesantren")
    @Expose
    private String nama_pesantren;
    @SerializedName("alamat_pesantren")
    @Expose
    private String alamat_pesantren;
    @SerializedName("domain")
    @Expose
    private String domain;
    @SerializedName("kode_sekolah")
    @Expose
    private String kode_sekolah;
    @SerializedName("logo")
    @Expose
    private String logo;
    @SerializedName("waktu_indonesia")
    @Expose
    private String waktu_indonesia;


    public DataPonpes() {
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

    public String getNamaPonpes() {
        return nama_pesantren;
    }

    public void setNamaPonpes(String nama_pesantren) {
        this.nama_pesantren = nama_pesantren;
    }

    public String getAlamatPonpes() {
        return alamat_pesantren;
    }

    public void setAlamatPonpes(String alamat_pesantren) {
        this.alamat_pesantren = alamat_pesantren;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKodes() {
        return kode_sekolah;
    }

    public void setKodes(String kode_sekolah) {
        this.kode_sekolah = kode_sekolah;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getwaktu_indonesia() {
        return waktu_indonesia;
    }

    public void setwaktu_indonesia(String waktu_indonesia) {
        this.waktu_indonesia = waktu_indonesia;
    }

    @Override
    public String toString() {
        return "DataPonpes{" +
                "isCorrect=" + isCorrect +
                ", message='" + message + '\'' +
                ", nama_pesantren='" + nama_pesantren + '\'' +
                ", alamat_pesantren='" + alamat_pesantren + '\'' +
                ", domain='" + domain + '\'' +
                ", kode_sekolah='" + kode_sekolah + '\'' +
                ", logo='" + logo + '\'' +
                ", waktu_indonesia='" + waktu_indonesia + '\'' +
                '}';
    }
}