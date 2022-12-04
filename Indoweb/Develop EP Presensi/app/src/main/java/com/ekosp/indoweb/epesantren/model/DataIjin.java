package com.ekosp.indoweb.epesantren.model;

/**
 * Created by Eko Setyo Purnomo on 08-Apr-18.
 * Find me on https://ekosp.com
 * Or email me directly to ekosetyopurnomo@gmail.com
 */

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