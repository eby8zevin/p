package com.ekosp.indoweb.epesantren.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.util.Log;

import com.ekosp.indoweb.epesantren.LoginActivity;
import com.ekosp.indoweb.epesantren.LoginPonpesActivity;
import com.ekosp.indoweb.epesantren.model.Area;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.ekosp.indoweb.epesantren.model.DataUser;
import com.ekosp.indoweb.epesantren.model.DeviceData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

public class SessionManager {
    SharedPreferences pref;
    Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "EpesantrenPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String IS_LANJUT = "IsLanjut";
    public static final String KEY_NAME = "name";
    public static final String KEY_NIP = "nip";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_VALIDASI = "validasi";
    public static final String KEY_MAX_DATANG = "max_datang";
    public static final String KEY_MAX_PULANG = "max_pulang";
    public static final String KEY_LOKASI = "lokasi";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_CURRENT_LONGITUDE = "current_longitude";
    public static final String KEY_CURRENT_LATITUDE = "current_latitude";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_LOGO = "logo";
    public static final String KEY_WAKTU_INDONESIA = "waktu_indonesia";
    public static final String KEY_NAMA_PONPES = "nama_pesantren";
    public static final String KEY_ALAMAT_PONPES = "alamat_pesantren";
    public static final String KEY_DOMAIN_PONPES = "domain";
    public static final String KEY_RADIUS_LOKASI = "radius_lokasi";
    public static final String KEY_IMEI = "imei";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_KODES = "kode_sekolah";
    public static final String KEY_JABATAN = "jabatan";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PHOTO = "photo";
    private final String KEY_LOGINTIME = "0";
    private final String KEY_LANJUTTIME = "0";

    public static final String KEY_ROLE_ID = "role_id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_AREA = "area";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(DataUser datUser) {

        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USERNAME, datUser.getUsername());
        //editor.putString(KEY_LOKASI, datUser.getLokasi());
        editor.putString(KEY_NAME, datUser.getNama());
        editor.putString(KEY_NIP, datUser.getNip());
        editor.putString(KEY_VALIDASI, datUser.getValidasi());
        editor.putString(KEY_EMAIL, datUser.getEmail());
        editor.putString(KEY_PHOTO, datUser.getPhoto());
        editor.putString(KEY_PHONE, datUser.getPhone());
        editor.putString(KEY_MAX_DATANG, datUser.getMaxDatang());
        editor.putString(KEY_MAX_PULANG, datUser.getMaxPulang());
        editor.putString(KEY_JABATAN, datUser.getJabatan());
        editor.putString(KEY_RADIUS_LOKASI, datUser.getJarak_radius());
        //editor.putString(KEY_LONGITUDE, String.valueOf(datUser.getLongitude()));
        //editor.putString(KEY_LATITUDE, String.valueOf(datUser.getLatitude()));
        editor.putLong(KEY_LOGINTIME, Utils.getCurrentTimestamp());

        editor.putString(KEY_ROLE_ID, datUser.getRoleId());
        editor.putString(KEY_MESSAGE, datUser.getMessage());
        editor.putString(KEY_AREA, new Gson().toJson(datUser.getArea())).apply();
        // commit changes
        editor.commit();
    }

    /**
     * Create lanjut session
     */
    public void createLanjutSession(DataPonpes datPonpes) {

        editor.putBoolean(IS_LANJUT, true);
        editor.putString(KEY_KODES, datPonpes.getKodes());
        editor.putString(KEY_LOGO, datPonpes.getLogo());
        editor.putString(KEY_NAMA_PONPES, datPonpes.getNamaPonpes());
        editor.putString(KEY_ALAMAT_PONPES, datPonpes.getAlamatPonpes());
        editor.putString(KEY_DOMAIN_PONPES, datPonpes.getDomain());
        editor.putString(KEY_WAKTU_INDONESIA, datPonpes.getwaktu_indonesia());
        editor.putLong(KEY_LANJUTTIME, Utils.getCurrentTimestamp());

        editor.putString(KEY_MESSAGE, datPonpes.getMessage());
        // commit changes
        editor.commit();
    }

    /**
     * Save current user location
     */
    public void saveCurrentLocation(Location loc) {
        Log.e("current_location", loc.toString());
        editor.putString(KEY_CURRENT_LONGITUDE, String.valueOf(loc.getLongitude()));
        editor.putString(KEY_CURRENT_LATITUDE, String.valueOf(loc.getLatitude()));
        // commit changes
        editor.commit();
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

    /**
     * Get saved username
     */
    public String getCurentLocation() {
        return pref.getString(KEY_NIP, null);
    }


    /**
     * Delete saved username
     */
    public void deleteSavedUsername() {
        editor.putString(KEY_NIP, "");
        // commit changes
        editor.commit();
    }

    /**
     * Delete saved username
     */
    public void deleteSavedKodes() {
        editor.putString(KEY_KODES, "");
        // commit changes
        editor.commit();
    }

    /**
     * Get saved username
     */
    public String getSavedUsername() {
        return pref.getString(KEY_NIP, null);
    }

    /**
     * Get saved kode_sekolah
     */
    public String getSavedKodes() {
        return pref.getString(KEY_KODES, null);
    }

    /**
     * Create login session
     */
    public void setSessionLogout() {
        editor.putBoolean(IS_LOGIN, false);
        editor.putString(KEY_NAME, "");
        editor.putString(KEY_NIP, "");
        editor.putString(KEY_EMAIL, "");
        editor.putLong(KEY_LOGINTIME, 0);
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    public void checkLanjut() {
        if (!this.isLanjut()) {
            Intent i = new Intent(_context, LoginPonpesActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    public long durasiLogin() {
        long awalLogin = pref.getLong(KEY_LOGINTIME, 0);
        long milisNow = Utils.getCurrentTimestamp();
        long durasi = milisNow - awalLogin;

        return durasi;
    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getDataUserPreference() {
        HashMap<String, String> data = new HashMap<String, String>();

        data.put(KEY_USERNAME, pref.getString(KEY_USERNAME, ""));
        data.put(KEY_NAME, pref.getString(KEY_NAME, ""));
        data.put(KEY_NIP, pref.getString(KEY_NIP, ""));
        data.put(KEY_VALIDASI, pref.getString(KEY_VALIDASI, "Y"));
        data.put(KEY_MAX_DATANG, pref.getString(KEY_MAX_DATANG, ""));
        data.put(KEY_MAX_PULANG, pref.getString(KEY_MAX_PULANG, ""));
        data.put(KEY_LOKASI, pref.getString(KEY_LOKASI, ""));
        data.put(KEY_LONGITUDE, pref.getString(KEY_LONGITUDE, ""));
        data.put(KEY_LATITUDE, pref.getString(KEY_LATITUDE, ""));
        data.put(KEY_JABATAN, pref.getString(KEY_LATITUDE, ""));
        data.put(KEY_RADIUS_LOKASI, pref.getString(KEY_RADIUS_LOKASI, ""));
        data.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));
        data.put(KEY_PHOTO, pref.getString(KEY_PHOTO, ""));
        data.put(KEY_PHONE, pref.getString(KEY_PHONE, ""));

        data.put(IS_LOGIN, String.valueOf(pref.getBoolean(IS_LOGIN, Boolean.parseBoolean(""))));
        data.put(KEY_ROLE_ID, pref.getString(KEY_ROLE_ID, ""));
        data.put(KEY_MESSAGE, pref.getString(KEY_MESSAGE, ""));
        data.put(KEY_AREA, pref.getString(KEY_AREA, ""));

        return data;
    }

    public HashMap<String, String> getDataPonpesPreference() {
        HashMap<String, String> data = new HashMap<String, String>();

        data.put(KEY_KODES, pref.getString(KEY_KODES, ""));
        data.put(KEY_LOGO, pref.getString(KEY_LOGO, ""));
        data.put(KEY_NAMA_PONPES, pref.getString(KEY_NAMA_PONPES, ""));
        data.put(KEY_ALAMAT_PONPES, pref.getString(KEY_ALAMAT_PONPES, ""));
        data.put(KEY_DOMAIN_PONPES, pref.getString(KEY_DOMAIN_PONPES, ""));
        data.put(KEY_WAKTU_INDONESIA, pref.getString(KEY_WAKTU_INDONESIA, ""));

        data.put(IS_LANJUT, String.valueOf(pref.getBoolean(IS_LANJUT, Boolean.parseBoolean(""))));
        data.put(KEY_MESSAGE, pref.getString(KEY_MESSAGE, ""));

        return data;
    }

    public DataUser getSessionDataUser() {
        DataUser data = new DataUser();

        data.setUsername(pref.getString(KEY_USERNAME, ""));
        data.setNama(pref.getString(KEY_NAME, ""));
        data.setNip(pref.getString(KEY_NIP, ""));
        data.setValidasi(pref.getString(KEY_VALIDASI, "Y"));
        data.setMaxDatang(pref.getString(KEY_MAX_DATANG, ""));
        data.setMaxPulang(pref.getString(KEY_MAX_PULANG, ""));
        //data.setLokasi(pref.getString(KEY_LOKASI, ""));
        data.setJabatan(pref.getString(KEY_JABATAN, ""));
        //data.setLongitude(Double.parseDouble(pref.getString(KEY_LONGITUDE, "1.1")));
        //data.setLatitude(Double.parseDouble(pref.getString(KEY_LATITUDE, "1.1")));
        data.setJarak_radius(pref.getString(KEY_RADIUS_LOKASI, ""));
        data.setEmail(pref.getString(KEY_EMAIL, ""));
        data.setPhoto(pref.getString(KEY_PHOTO, ""));
        data.setPhone(pref.getString(KEY_PHONE, ""));

        data.setCorrect(pref.getBoolean(IS_LOGIN, Boolean.parseBoolean("")));
        data.setRoleId(pref.getString(KEY_ROLE_ID, ""));
        data.setMessage(pref.getString(KEY_MESSAGE, ""));
        data.setArea(new Gson().fromJson(pref.getString(KEY_AREA, ""), new TypeToken<List<Area>>() {
        }.getType()));

        return data;
    }

    public DataPonpes getSessionDataPonpes() {
        DataPonpes data = new DataPonpes();

        data.setNamaPonpes(pref.getString(KEY_NAMA_PONPES, ""));
        data.setAlamatPonpes(pref.getString(KEY_ALAMAT_PONPES, ""));
        data.setDomain(pref.getString(KEY_DOMAIN_PONPES, ""));
        data.setKodes(pref.getString(KEY_KODES, ""));
        data.setLogo(pref.getString(KEY_LOGO, ""));
        data.setwaktu_indonesia(pref.getString(KEY_WAKTU_INDONESIA, ""));

        data.setCorrect(pref.getBoolean(IS_LANJUT, Boolean.parseBoolean("")));
        data.setMessage(pref.getString(KEY_MESSAGE, ""));

        return data;
    }

    /**
     * Clear session details
     */
    public void logoutUser(String username, String kodes) {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // presistance eksisting username
        editor.putString(KEY_NIP, username);
        editor.putString(KEY_KODES, kodes);
        editor.commit();
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isLanjut() {
        return pref.getBoolean(IS_LANJUT, false);
    }
}