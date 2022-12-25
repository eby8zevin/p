package com.ekosp.indoweb.adminsekolah.model.data_laporan_pelajaran;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataMajors {

    @SerializedName("is_correct")
    @Expose
    private Boolean is_correct;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("unit")
    @Expose
    public List<Unit> units;

    public DataMajors() {

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

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public static class Unit {

        @SerializedName("id_unit")
        @Expose
        private String id_unit;

        @SerializedName("nama_unit")
        @Expose
        private String nama_unit;

        public Unit() {

        }

        public String getId_unit() {
            return id_unit;
        }

        public void setId_unit(String id_unit) {
            this.id_unit = id_unit;
        }

        public String getNama_unit() {
            return nama_unit;
        }

        public void setNama_unit(String nama_unit) {
            this.nama_unit = nama_unit;
        }
    }
}