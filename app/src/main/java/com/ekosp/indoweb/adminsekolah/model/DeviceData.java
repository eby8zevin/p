package com.ekosp.indoweb.adminsekolah.model;

/**
 * Created by Eko S. Purnomo on 12/26/2019.
 * Email me at ekosetyopurnomo@gmail.com
 * Visit me on ekosp.com
 */
public class DeviceData {

    private String imei;
    private String deviceId;

    public DeviceData(String imei, String deviceId) {
        this.imei = imei;
        this.deviceId = deviceId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
