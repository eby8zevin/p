package id.indoweb.elazis.presensi.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataUser {

    @SerializedName("is_correct")
    @Expose
    private Boolean isCorrect;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("jabatan")
    @Expose
    private String jabatan;

    @SerializedName("nama")
    @Expose
    private String nama;

    @SerializedName("nip")
    @Expose
    private String nip;

    @SerializedName("role_id")
    @Expose
    private String roleId;

    @SerializedName("phone")
    @Expose
    private String phone;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("max_datang")
    @Expose
    private String max_datang;

    @SerializedName("max_pulang")
    @Expose
    private String max_pulang;

    @SerializedName("validasi")
    @Expose
    private String validasi;

    @SerializedName("photo")
    @Expose
    private String photo;

    @SerializedName("area")
    @Expose
    private List<Area> area;

    @SerializedName("jarak_radius")
    @Expose
    private String jarak_radius;

    public DataUser() {
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public List<Area> getArea() {
        return area;
    }

    public void setArea(List<Area> area) {
        this.area = area;
    }

    public String getValidasi() {
        return validasi;
    }

    public void setValidasi(String validasi) {
        this.validasi = validasi;
    }

    public String getMaxDatang() {
        return max_datang;
    }

    public void setMaxDatang(String max_datang) {
        this.max_datang = max_datang;
    }

    public String getMaxPulang() {
        return max_pulang;
    }

    public void setMaxPulang(String max_pulang) {
        this.max_pulang = max_pulang;
    }

    public String getJarak_radius() {
        return jarak_radius;
    }

    public void setJarak_radius(String jarak_radius) {
        this.jarak_radius = jarak_radius;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }
}