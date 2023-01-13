package id.indoweb.elazis.presensi.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

import id.indoweb.elazis.presensi.model.Area;
import id.indoweb.elazis.presensi.model.DataPonpes;
import id.indoweb.elazis.presensi.model.DataUser;
import id.indoweb.elazis.presensi.model.DeviceData;

public class SessionManager {
    SharedPreferences pref;
    Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "AdminSekolah";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String IS_LANJUT = "IsLanjut";
    public static final String KEY_NAME = "name";
    public static final String KEY_NIP = "nip";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_VALIDASI = "validasi";
    public static final String KEY_MAX_DATANG = "max_datang";
    public static final String KEY_MAX_PULANG = "max_pulang";
    /*public static final String KEY_LOKASI = "lokasi";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";*/
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

    public static final String KEY_ROLE_ID = "role_id";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_AREA = "area";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    // LoginPonpesActivity
    public void createLanjutSession(DataPonpes dataPonpes) {
        editor.putBoolean(IS_LANJUT, dataPonpes.getCorrect());
        editor.putString(KEY_MESSAGE, dataPonpes.getMessage());
        editor.putString(KEY_NAMA_PONPES, dataPonpes.getNamaPonpes());
        editor.putString(KEY_ALAMAT_PONPES, dataPonpes.getAlamatPonpes());
        editor.putString(KEY_DOMAIN_PONPES, dataPonpes.getDomain());
        editor.putString(KEY_KODES, dataPonpes.getKodes());
        editor.putString(KEY_LOGO, dataPonpes.getLogo());
        editor.putString(KEY_WAKTU_INDONESIA, dataPonpes.getwaktu_indonesia());

        editor.commit();
    }

    public String getSavedKodes() {
        return pref.getString(KEY_KODES, null);
    }

    public void deleteSavedKodes() {
        editor.putString(KEY_KODES, "");

        editor.commit();
    }

    public DataPonpes getSessionDataPonpes() {
        DataPonpes data = new DataPonpes();

        data.setCorrect(pref.getBoolean(IS_LANJUT, Boolean.parseBoolean("")));
        data.setMessage(pref.getString(KEY_MESSAGE, ""));
        data.setNamaPonpes(pref.getString(KEY_NAMA_PONPES, ""));
        data.setAlamatPonpes(pref.getString(KEY_ALAMAT_PONPES, ""));
        data.setDomain(pref.getString(KEY_DOMAIN_PONPES, ""));
        data.setKodes(pref.getString(KEY_KODES, ""));
        data.setLogo(pref.getString(KEY_LOGO, ""));
        data.setwaktu_indonesia(pref.getString(KEY_WAKTU_INDONESIA, ""));

        Log.i("SessionManager", data.toString());
        return data;
    }

    public HashMap<String, String> getDataPonpesPreference() {
        HashMap<String, String> data = new HashMap<>();

        //data.put(IS_LANJUT, pref.getString(IS_LANJUT, ""));
        data.put(KEY_MESSAGE, pref.getString(KEY_MESSAGE, ""));
        data.put(KEY_NAMA_PONPES, pref.getString(KEY_NAMA_PONPES, ""));
        data.put(KEY_ALAMAT_PONPES, pref.getString(KEY_ALAMAT_PONPES, ""));
        data.put(KEY_DOMAIN_PONPES, pref.getString(KEY_DOMAIN_PONPES, ""));
        data.put(KEY_KODES, pref.getString(KEY_KODES, ""));
        data.put(KEY_LOGO, pref.getString(KEY_LOGO, ""));
        data.put(KEY_WAKTU_INDONESIA, pref.getString(KEY_WAKTU_INDONESIA, ""));

        return data;
    }

    // LoginActivity
    public void createLoginSession(DataUser dataUser) {
        editor.putBoolean(IS_LOGIN, dataUser.getCorrect());
        editor.putString(KEY_USERNAME, dataUser.getUsername());
        editor.putString(KEY_NAME, dataUser.getNama());
        editor.putString(KEY_NIP, dataUser.getNip());
        editor.putString(KEY_ROLE_ID, dataUser.getRoleId());
        editor.putString(KEY_AREA, new Gson().toJson(dataUser.getArea())).apply();
        editor.putString(KEY_VALIDASI, dataUser.getValidasi());
        editor.putString(KEY_EMAIL, dataUser.getEmail());
        editor.putString(KEY_PHOTO, dataUser.getPhoto());
        editor.putString(KEY_MAX_DATANG, dataUser.getMaxDatang());
        editor.putString(KEY_MAX_PULANG, dataUser.getMaxPulang());
        editor.putString(KEY_RADIUS_LOKASI, dataUser.getJarak_radius());
        editor.putString(KEY_MESSAGE, dataUser.getMessage());
        editor.putString(KEY_JABATAN, dataUser.getJabatan());
        editor.putString(KEY_PHONE, dataUser.getPhone());

        editor.commit();
    }

    public String getSavedUsername() {
        return pref.getString(KEY_NIP, null);
    }

    public void deleteSavedUsername() {
        editor.putString(KEY_NIP, "");

        editor.commit();
    }

    public DataUser getSessionDataUser() {
        DataUser data = new DataUser();

        data.setCorrect(pref.getBoolean(IS_LOGIN, Boolean.parseBoolean("")));
        data.setUsername(pref.getString(KEY_USERNAME, ""));
        data.setNama(pref.getString(KEY_NAME, ""));
        data.setNip(pref.getString(KEY_NIP, ""));
        data.setRoleId(pref.getString(KEY_ROLE_ID, ""));
        data.setArea(new Gson().fromJson(pref.getString(KEY_AREA, ""), new TypeToken<List<Area>>() {
        }.getType()));
        data.setValidasi(pref.getString(KEY_VALIDASI, "Y"));
        data.setEmail(pref.getString(KEY_EMAIL, ""));
        data.setPhoto(pref.getString(KEY_PHOTO, ""));
        data.setMaxDatang(pref.getString(KEY_MAX_DATANG, ""));
        data.setMaxPulang(pref.getString(KEY_MAX_PULANG, ""));
        data.setJarak_radius(pref.getString(KEY_RADIUS_LOKASI, ""));
        data.setMessage(pref.getString(KEY_MESSAGE, ""));
        data.setJabatan(pref.getString(KEY_JABATAN, ""));
        data.setPhone(pref.getString(KEY_PHONE, ""));

        Log.i("SessionManager", data.toString());
        return data;
    }

    public HashMap<String, String> getDataUserPreference() {
        HashMap<String, String> data = new HashMap<>();

        //data.put((IS_LOGIN), pref.getString(IS_LOGIN, ""));
        data.put(KEY_USERNAME, pref.getString(KEY_USERNAME, ""));
        data.put(KEY_NAME, pref.getString(KEY_NAME, ""));
        data.put(KEY_NIP, pref.getString(KEY_NIP, ""));
        data.put(KEY_ROLE_ID, pref.getString(KEY_ROLE_ID, ""));
        data.put(KEY_AREA, pref.getString(KEY_AREA, ""));
        data.put(KEY_VALIDASI, pref.getString(KEY_VALIDASI, "Y"));
        data.put(KEY_EMAIL, pref.getString(KEY_EMAIL, ""));
        data.put(KEY_PHOTO, pref.getString(KEY_PHOTO, ""));
        data.put(KEY_MAX_DATANG, pref.getString(KEY_MAX_DATANG, ""));
        data.put(KEY_MAX_PULANG, pref.getString(KEY_MAX_PULANG, ""));
        data.put(KEY_RADIUS_LOKASI, pref.getString(KEY_RADIUS_LOKASI, ""));
        data.put(KEY_MESSAGE, pref.getString(KEY_MESSAGE, ""));
        data.put(KEY_JABATAN, pref.getString(KEY_JABATAN, ""));
        data.put(KEY_PHONE, pref.getString(KEY_PHONE, ""));

        return data;
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    // MainActivity
    public void saveDeviceData(DeviceData data) {
        editor.putString(KEY_IMEI, data.getImei());
        editor.putString(KEY_DEVICE_ID, data.getDeviceId());

        editor.commit();
    }

    public DeviceData getDeviceData() {
        return new DeviceData(
                pref.getString(KEY_IMEI, "00000"),
                pref.getString(KEY_DEVICE_ID, "00000")
        );
    }

    public void logoutUser(String username, String kodes) {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // existing username
        editor.putString(KEY_NIP, username);
        editor.putString(KEY_KODES, kodes);
        editor.commit();
    }

    public void savePwd(String pwd) {
        editor.putString("PASSWORD", pwd);
        editor.commit();
    }

    public String getPwd() {
        return pref.getString("PASSWORD", null);
    }
}