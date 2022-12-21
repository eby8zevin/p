package com.ekosp.indoweb.adminsekolah.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Eko.Purnomo on 2019-04-27.
 * about me visit https://ekosp.com
 * or contact me at ekosetyopurnomo@gmail.com
 */

public class DataVersion {

    @SerializedName("versionCode")
    @Expose
    private Integer versionCode;
    @SerializedName("versionName")
    @Expose
    private String versionName;
    @SerializedName("releaseDate")
    @Expose
    private String releaseDate;

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

}
