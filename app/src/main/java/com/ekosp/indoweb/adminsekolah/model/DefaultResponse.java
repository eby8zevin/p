package com.ekosp.indoweb.adminsekolah.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DefaultResponse {

    @SerializedName("is_correct")
    @Expose
    private Boolean is_correct;

    @SerializedName("message")
    @Expose
    private String message;

    public DefaultResponse() {
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
}