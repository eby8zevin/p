package com.ekosp.indoweb.adminsekolah.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataIjin {

    @SerializedName("is_correct")
    @Expose
    private Boolean isCorrect;

    @SerializedName("message")
    @Expose
    private String message;

    public DataIjin() {
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

    @Override
    public String toString() {
        return "DataIjin{" +
                "isCorrect=" + isCorrect +
                ",message='" + message + '}';
    }
}