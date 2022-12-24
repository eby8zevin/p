package com.ekosp.indoweb.adminsekolah.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ekosp.indoweb.adminsekolah.model.Area;
import com.ekosp.indoweb.adminsekolah.model.DataSekolah;
import com.ekosp.indoweb.adminsekolah.model.DataUser;
import com.ekosp.indoweb.adminsekolah.model.DeviceData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

public class SessionManager {
    SharedPreferences pref;
    Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "AdminSekolah";

    //School
    private static final String IS_LANJUT = "IsLanjut";
    public static final String KEY_KODES = "kode_sekolah";
    public static final String KEY_NAMA_PONPES = "nama_pesantren";
    public static final String KEY_ALAMAT_PONPES = "alamat_pesantren";
    public static final String KEY_DOMAIN_PONPES = "domain";
    public static final String KEY_LOGO = "logo";
    public static final String KEY_WAKTU_INDONESIA = "waktu_indonesia";

    //User
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_NIP = "nip";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_JABATAN = "jabatan";
    public static final String KEY_ROLE_ID = "role_id";
    public static final String KEY_VALIDASI = "validasi";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_AREA = "area";
    public static final String KEY_RADIUS_LOKASI = "radius_lokasi";
    public static final String KEY_MODE_ABSEN = "mode_absen";

    public static final String KEY_LOKASI = "lokasi";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";

    public static final String KEY_IMEI = "imei";
    public static final String KEY_DEVICE_ID = "device_id";

    public static final String KEY_DATANG = "jam_datang";
    public static final String KEY_PULANG = "jam_pulang";

    public static final String KEY_PWD = "password";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLanjutSession(DataSekolah.SekolahData data) {

        editor.putString(KEY_KODES, data.getKode_sekolah());
        editor.putString(KEY_NAMA_PONPES, data.getNama_sekolah());
        editor.putString(KEY_ALAMAT_PONPES, data.getAlamat_sekolah());
        editor.putString(KEY_DOMAIN_PONPES, data.getDomain());
        editor.putString(KEY_LOGO, data.getLogo());
        editor.putString(KEY_WAKTU_INDONESIA, data.getWaktu_indonesia());

        // commit changes
        editor.commit();
    }

    public DataSekolah.SekolahData getSessionDataSekolah() {
        DataSekolah.SekolahData data = new DataSekolah.SekolahData();

        data.setKode_sekolah(pref.getString(KEY_KODES, ""));
        data.setNama_sekolah(pref.getString(KEY_NAMA_PONPES, ""));
        data.setAlamat_sekolah(pref.getString(KEY_ALAMAT_PONPES, ""));
        data.setDomain(pref.getString(KEY_DOMAIN_PONPES, ""));
        data.setLogo(pref.getString(KEY_LOGO, ""));
        data.setWaktu_indonesia(pref.getString(KEY_WAKTU_INDONESIA, ""));

        return data;
    }

    // Get stored session data
    public HashMap<String, String> getDataSekolahPreference() {
        HashMap<String, String> data = new HashMap<>();

        data.put(KEY_KODES, pref.getString(KEY_KODES, ""));
        data.put(KEY_NAMA_PONPES, pref.getString(KEY_NAMA_PONPES, ""));
        data.put(KEY_ALAMAT_PONPES, pref.getString(KEY_ALAMAT_PONPES, ""));
        data.put(KEY_DOMAIN_PONPES, pref.getString(KEY_DOMAIN_PONPES, ""));
        data.put(KEY_LOGO, pref.getString(KEY_LOGO, ""));
        data.put(KEY_WAKTU_INDONESIA, pref.getString(KEY_WAKTU_INDONESIA, ""));

        return data;
    }

    public void createLoginSession(DataUser.UserData userData) {

        editor.putString(KEY_USERNAME, userData.getUsername());
        editor.putString(KEY_NIP, userData.getNip());
        editor.putString(KEY_NAME, userData.getNama());
        editor.putString(KEY_PHONE, userData.getPhone());
        editor.putString(KEY_EMAIL, userData.getEmail());
        editor.putString(KEY_JABATAN, userData.getJabatan());
        editor.putString(KEY_ROLE_ID, userData.getRoleId());
        editor.putString(KEY_VALIDASI, userData.getValidasi());
        editor.putString(KEY_PHOTO, userData.getPhoto());
        editor.putString(KEY_AREA, new Gson().toJson(userData.getArea())).apply();
        editor.putString(KEY_RADIUS_LOKASI, userData.getJarak_radius());
        editor.putString(KEY_MODE_ABSEN, userData.getMode_absen());
        // commit changes
        editor.commit();
    }

    public DataUser.UserData getSessionDataUser() {
        DataUser.UserData userData = new DataUser.UserData();

        userData.setUsername(pref.getString(KEY_USERNAME, ""));
        userData.setNip(pref.getString(KEY_NIP, ""));
        userData.setNama(pref.getString(KEY_NAME, ""));
        userData.setPhone(pref.getString(KEY_PHONE, ""));
        userData.setEmail(pref.getString(KEY_EMAIL, ""));
        userData.setJabatan(pref.getString(KEY_JABATAN, ""));
        userData.setRoleId(pref.getString(KEY_ROLE_ID, ""));
        userData.setValidasi(pref.getString(KEY_VALIDASI, ""));
        userData.setPhoto(pref.getString(KEY_PHOTO, ""));
        userData.setArea(new Gson().fromJson(pref.getString(KEY_AREA, ""), new TypeToken<List<Area>>() {
        }.getType()));
        userData.setJarak_radius(pref.getString(KEY_RADIUS_LOKASI, ""));
        userData.setMode_absen(pref.getString(KEY_MODE_ABSEN, ""));

        userData.setMax_datang(pref.getString(KEY_DATANG, ""));
        userData.setMax_pulang(pref.getString(KEY_PULANG, ""));

        return userData;
    }

    // Get stored session data
    public HashMap<String, String> getDataUserPreference() {
        HashMap<String, String> data = new HashMap<>();

        data.put(KEY_USERNAME, pref.getString(KEY_USERNAME, ""));
        data.put(KEY_NIP, pref.getString(KEY_NIP, ""));
        data.put(KEY_NAME, pref.getString(KEY_NAME, ""));
        data.put(KEY_PHONE, pref.getString(KEY_PHONE, ""));
        data.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));
        data.put(KEY_JABATAN, pref.getString(KEY_LATITUDE, ""));
        data.put(KEY_ROLE_ID, pref.getString(KEY_ROLE_ID, ""));
        data.put(KEY_VALIDASI, pref.getString(KEY_VALIDASI, ""));
        data.put(KEY_PHOTO, pref.getString(KEY_PHOTO, ""));
        data.put(KEY_AREA, pref.getString(KEY_AREA, ""));
        data.put(KEY_RADIUS_LOKASI, pref.getString(KEY_RADIUS_LOKASI, ""));
        data.put(KEY_MODE_ABSEN, pref.getString(KEY_MODE_ABSEN, ""));

        data.put(KEY_LOKASI, pref.getString(KEY_LOKASI, ""));
        data.put(KEY_LONGITUDE, pref.getString(KEY_LONGITUDE, ""));
        data.put(KEY_LATITUDE, pref.getString(KEY_LATITUDE, ""));

        data.put(KEY_DATANG, pref.getString(KEY_DATANG, ""));
        data.put(KEY_PULANG, pref.getString(KEY_PULANG, ""));

        return data;
    }

    public void saveDeviceData(DeviceData data) {
        editor.putString(KEY_IMEI, data.getImei());
        editor.putString(KEY_DEVICE_ID, data.getDeviceId());
        // commit changes
        editor.commit();
    }

    public DeviceData getDeviceData() {
        return new DeviceData(
                pref.getString(KEY_IMEI, "00000"),
                pref.getString(KEY_DEVICE_ID, "00000")
        );
    }

    public String getSavedKodes() {
        return pref.getString(KEY_KODES, null);
    }

    public void deleteSavedKodes() {
        editor.putString(KEY_KODES, "");
        // commit changes
        editor.commit();
    }

    public String getSavedNIP() {
        return pref.getString(KEY_NIP, null);
    }

    public void deleteSavedNIP() {
        editor.putString(KEY_NIP, "");
        // commit changes
        editor.commit();
    }

    public void logoutUser(String codeSchool, String nip) {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // existing username
        editor.putString(KEY_KODES, codeSchool);
        editor.putString(KEY_NIP, nip);
        editor.commit();
    }

    public void savedPwd(String pwd) {
        editor.putString(KEY_PWD, pwd);
        editor.apply();
    }

    public String getSavedPwd() {
        return pref.getString(KEY_PWD, null);
    }
}