## Documentation for App AdminSekolah Presensi (Android Studio)

[Documentation](https://adminsekolah.net)

<img src="https://miro.medium.com/max/720/1*1efY4vE8NIr92fNImYK1pQ.jpeg">

[Retrofit call rest-api/get_ponpes.php](#Retrofit-call-get-ponpes)

[Retrofit call rest-api/get_data_user.php](#Retrofit-call-get-data-user)

[Retrofit call rest-api/get_data_laporan.php](#Retrofit-call-get-data-laporan)

[Retrofit call rest-api/get_data_laporantahun.php](#Retrofit-call-get-data-laporantahun)

[Retrofit call rest-api/fileUpload_coba1.php](#Retrofit-call-fileUpload-coba1)

[Retrofit call rest-api/submitIjin_coba.php](#Retrofit-call-submitIjin-coba)

### Retrofit call get ponpes

`LoginPonpesActivity.java`

```java
private void executeLanjut() {
        String kodes = _kodesText.getText().toString();

        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataPonpes> call = apiService.checkPonpes(kodes);
            call.enqueue(new Callback<DataPonpes>() {

                @Override
                public void onResponse(Call<DataPonpes> call, Response<DataPonpes> response) {
                    if (response.isSuccessful()) {
                        mDataPonpes = response.body();
                        Log.i("RETROFIT", mDataPonpes.toString());
                        if (mDataPonpes.getCorrect()) {
                            gotoLoginActivity(mDataPonpes);
                        } else {
                            onLanjutFailed();
                        }
                    } else {
                        StyleableToast.makeText(LoginPonpesActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                    }
                }

                @Override
                public void onFailure(Call<DataPonpes> call, Throwable t) {
                    Log.d("RETROFIT", "failed to fetch data from API" + t);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, String.valueOf(e));
        }
    }
```

### Retrofit call get data user

`LoginActivity.java`

```java
private void executeLogin() {
        String kodes = mDataPonpes.getKodes();
        String uname = _usernameText.getText().toString();
        pass = _pinText.getText().toString();

        try {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataUser> call = apiService.checkLogin(kodes, uname, pass);
            call.enqueue(new Callback<DataUser>() {
                @Override
                public void onResponse(Call<DataUser> call, Response<DataUser> response) {
                    if (response.isSuccessful()) {
                        mDataUser = response.body();
                        Log.i("RETROFIT", mDataUser.toString());
                        if (mDataUser.getCorrect()) {
                            gotoHomeActivity(mDataUser);
                        } else {
                            onLoginFailed();
                        }
                    } else {
                        StyleableToast.makeText(LoginActivity.this, "Terjadi Gangguan Koneksi Ke Server", Toast.LENGTH_SHORT, R.style.mytoast_danger).show();
                    }
                }

                @Override
                public void onFailure(Call<DataUser> call, Throwable t) {
                    Log.d("RETROFIT", "failed to fetch data from API" + t);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, String.valueOf(e));
        }
    }
```

### Retrofit call get data laporan

`LaporanFragment.kt`

```kotlin
private fun loadKehadiran() {
        .
        ..
        ...

        kodes = Ponpes.getKodes()
        idpegawai = User.getUsername()
        try {
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.getDataLaporan(
                "$kodes",
                "$idpegawai",
                "BULANAN",
                yearsSelect,
                monthSelect
            )

            call.enqueue(object : Callback<DataLaporan?> {
                override fun onResponse(
                    call: Call<DataLaporan?>,
                    response: Response<DataLaporan?>
                ) {
                    if (response.isSuccessful) {
                        val persentase = response.body()?.percentase

                        progress_value?.text = "$persentase %"
                        progress_hari?.text = response.body()?.percentase_hari
                        progres_bar?.progress = persentase?.let { it / 100 }!!

                        num_hadir?.text = response.body()?.hadir + " Hari"
                        num_ijin?.text = response.body()?.izin_cuti + " Hari"
                        num_alpha?.text = response.body()?.alpa.toString() + " Hari"
                        num_terlambat?.text = response.body()?.terlambat + " Jam"
                        presentase_hadir_tahun_ini?.text = response.body()?.hadir_tahun_ini

                        detailKehadiranTahun = response.body()!!
                        dataKehadiran = response.body()?.rekap

                        adapter = ListRekapKehadiranAdapter(
                            requireContext(),
                            dataKehadiran!!,
                            this@LaporanFragment
                        )
                        rv_kehadiran!!.adapter = adapter

                        if (dataKehadiran!!.size <= 0) {
                            header_rv.visibility = View.GONE
                            iv_not_found.visibility = View.VISIBLE
                            tv_not_found.visibility = View.VISIBLE
                        } else {
                            header_rv.visibility = View.VISIBLE
                            iv_not_found.visibility = View.GONE
                            tv_not_found.visibility = View.GONE
                        }
                    }
                }

                override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {

                }
            })
        } catch (e: Exception) {
            Log.d("Token e", e.toString())
        }
    }
```

### Retrofit call get data laporantahun

`KehadiranTahunanActivity.kt`

```kotlin
private fun getDataLaporanTahunan(){
        val kodes: String = dataPonpes.toString()
        val idpegawai: String = dataUser.toString()

        try {
            val apiService = ApiClient.getClient().create(ApiInterface::class.java)
            val call = apiService.getDataLaporantahun(
                kodes,
                idpegawai,
                "TAHUNAN",
                yearSelect,
            )

            call.enqueue(object : Callback<DataLaporan?> {
                override fun onResponse(
                    call: Call<DataLaporan?>,
                    response: Response<DataLaporan?>
                ) {
                    if (response.isSuccessful) {
                        val persentase = response.body()?.percentase

                        tahun.text = yearSelect.toString()
                        progress_value_tahunan.text = "$persentase%"
                        progress_hari_tahunan.text = response.body()?.percentase_hari
                        progres_bar_tahunan.progress = persentase?.let { it / 100 }!!

                        num_hadir_tahunan.text = response.body()?.hadir + " Hari"
                        num_ijin_tahunan.text = response.body()?.izin_cuti + " Hari"
                        num_alpha_tahunan.text = response.body()?.alpa.toString() + " Hari"
                        num_terlambat_tahunan.text = response.body()?.terlambat + " Jam"
                    }
                }

                override fun onFailure(call: Call<DataLaporan?>, t: Throwable) {

                }
            })
        } catch (e: Exception) {
            Log.d("Token e", e.toString())
        }
    }
```

### Retrofit call fileUpload coba1

`UploadImage.kt`

```kotlin
private fun uploadFile(): String? {
            var responseString: String? = null
            val httpclient: HttpClient = DefaultHttpClient()
            var domains= datPonpes?.get(SessionManager.KEY_DOMAIN_PONPES).toString()

            val httppost = if(domains=="demo") HttpPost(ApiClient.FILE_UPLOAD_URL_DEMO)
            else HttpPost(ApiClient.FILE_UPLOAD_URL)

            try {
                val entity = AndroidMultiPartEntity { num: Long -> publishProgress((num / totalSize.toFloat() * 100).toInt()) }
                if (compressedImage != null) {
                    entity.addPart("image", FileBody(compressedImage))
                    Log.e(
                            "UPLOAD_IMAGE",
                            "path foto pas upload : " + compressedImage!!.absolutePath
                    )
                } else {
                    //   Toast.makeText(UploadImage.this, "foto tidak disertakan", Toast.LENGTH_SHORT).show();
                }

                // Extra parameters if you want to pass to server
                entity.addPart(
                        "device_id",
                        StringBody(session!!.deviceData.deviceId)
                )
                entity.addPart(
                        "imei",
                        StringBody(session!!.deviceData.imei)
                )
                entity.addPart(
                        "id_pegawai",
                        StringBody(datUser!![SessionManager.KEY_USERNAME].toString())
                )
                entity.addPart(
                        "kode_sekolah",
                        StringBody(datPonpes!![SessionManager.KEY_KODES].toString())
                )
                entity.addPart(
                        "domain",
                        StringBody(datPonpes!![SessionManager.KEY_DOMAIN_PONPES].toString())
                )
                entity.addPart(
                        "longi",
                        StringBody(locationModel!!.longitude.toString())
                )
                Log.i("UPLOAD", "Longitude:" + locationModel!!.longitude)
                entity.addPart(
                        "lati",
                        StringBody(locationModel!!.latitude.toString())
                )
                Log.i("UPLOAD", "latitude:" + locationModel!!.latitude)
                entity.addPart(
                        "type",
                        StringBody(TYPE)
                )
                entity.addPart(
                        "keterangan",
                        StringBody(inputKeterangan!!.text.toString())
                )
                entity.addPart("lokasi", StringBody(lokasiUser!!.text.toString()))
                totalSize = entity.contentLength
                httppost.entity = entity

                // Making server call
                val response = httpclient.execute(httppost)
                val r_entity = response.entity
                val statusCode = response.statusLine.statusCode
                responseString = if (statusCode == 200) {
                    // Server response
                    EntityUtils.toString(r_entity)
                } else {
                    ("Error occurred! Http Status Code: "
                            + statusCode)
                }
            } catch (e: ClientProtocolException) {
                responseString = e.toString()
            } catch (e: IOException) {
                responseString = e.toString()
            }
            return responseString
        }

        override fun onPostExecute(result: String?) {
            Log.e("CLOCKING", "result: $result")
            val pesan = "Berhasil Absen"
            super.onPostExecute(result)
            toAfterAbsen()
        }

        override fun doInBackground(vararg p0: Void?): String? {
            return uploadFile()
        }
    }
```

### Retrofit call submitIjin coba

`IjinActivity.java`

```java
private void executeSimpan() {
        String kodes = kodess;
        String uname = userName;
        String jenis_ = jenis.getText().toString();
        String tgl_awal_ = tgl_awal.getText().toString();
        String tgl_akhir_ = tgl_akhir.getText().toString();
        String keterangan_ = keterangan.getText().toString();

        try {
            apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataIjin> call = apiService.submitIjincoba(kodes, uname, jenis_, tgl_awal_, tgl_akhir_, keterangan_);
            call.enqueue(new Callback<DataIjin>() {

                @Override
                public void onResponse(Call<DataIjin> call, Response<DataIjin> response) {
                    if (response.isSuccessful()) {
                        DataIjin ijin = response.body();
                        Log.i("RETROFIT", ijin.toString());
                        if (ijin.getCorrect()) {
                            onSimpanSuccess("" + ijin.getMessage() + "");
                        } else {
                            onSimpanFailed("" + ijin.getMessage() + "");
                        }
                    } else
                        Toast.makeText(IjinActivity.this, "Terjadi gangguan koneksi ke server", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<DataIjin> call, Throwable t) {
                    Log.d("RETROFIT", "failed to fetch data from API" + t);
                }

            });
        } catch (Exception e) {
            Log.d(TAG, String.valueOf(e));
        }
    }
```

---

## API Doc

**Noted:**
| Parameter | Value |
| ------------ | --------------- |
| `parameter=` | **`text Bold`** |

#### API call rest-api/get_ponpes.php

<pre><code>
https://m.adminsekolah.net/rest-api/get_ponpes.php?kode_sekolah=<b>2020123</b>
</code></pre>

#### Example of API response

```json
{
  "is_correct": true,
  "kode_sekolah": "2020123",
  "nama_pesantren": "Demo Admin Sekolah",
  "alamat_pesantren": "Kediri",
  "domain": "demo",
  "logo": "demo.adminsekolah.net/uploads/school/DEMO_ADMINSEKOLAH16.png",
  "waktu_indonesia": "WIB",
  "db": "adminsek_demo.",
  "message": "Data login anda valid"
}
```

---

#### API call rest-api/get_data_user.php

<pre><code>
https://m.adminsekolah.net/rest-api/get_data_user.php?kode_sekolah=<b>2020123</b>&nip=<b>00040032</b>&password=<b>123456</b>
</code></pre>

#### Example of API response

```json
{
  "is_correct": true,
  "username": "2499",
  "jabatan": "Karyawan SD",
  "nama": "Widya Putri Tsafina S.Pd",
  "nip": "00040032",
  "role_id": "PEGAWAI",
  "phone": "+628123364001",
  "email": "widyaputri@gmail.com",
  "waktu_indonesia": "WIB",
  "max_datang": "07:00",
  "max_pulang": "16:00",
  "validasi": "LOCK",
  "photo": "http://demo.ujipresensi.my.id/img/avatar_user.png",
  "lokasi": "Kantor Pusat Admin Sekolah",
  "longitude": "112.0471677",
  "latitude": "-7.8132744",
  "jarak_radius": "100",
  "message": "Data login anda valid"
}
```

---

#### API call rest-api/get_data_laporan.php

<pre><code>
https://m.adminsekolah.net/rest-api/get_data_laporan.php?kode_sekolah=<b>2020123</b>&id_pegawai=<b>2499</b>&type=<b>BULANAN</b>&tahun=<b>2022</b>&bulan=<b>10</b>
</code></pre>

#### Example of API response

```json
{
  "hadir": "1",
  "izin_cuti": "0",
  "alpa": 0,
  "terlambat": "0",
  "percentase": "4.54",
  "percentase_hari": "1/22 Hari",
  "hadir_tahun_ini": "365",
  "rekap": [
    {
      "hari": "2022-12-31",
      "status": "Hadir",
      "detail": {
        "jam_datang": "07:00:07",
        "jam_pulang": "16:00:16",
        "lokasi": "Kantor Pusat Admin Sekolah",
        "catatan_absen": "Noted"
      }
    }
  ]
}
```

---

#### API call get data laporantahun

<pre><code>
https://m.adminsekolah.net/rest-api/get_data_laporantahun.php?kode_sekolah=<b>2020123</b>&id_pegawai=<b>2499</b>&type=<b>TAHUNAN</b>&tahun=<b>2022</b>
</code></pre>

#### Example of API response

```json
{
  "hadir": "1",
  "izin_cuti": "0",
  "alpa": 0,
  "terlambat": "0",
  "percentase": "4.54",
  "percentase_hari": "1/264 Hari"
}
```

---

#### API call rest-api/fileUpload_coba1.php

<pre><code>
https://m.adminsekolah.net/rest-api/fileUpload_coba1.php?id_pegawai=<b>2499</b>&kode_sekolah=<b>2020123</b>&type=<b>DATANG</b>&lokasi=<b>Kantor Pusat Admin Sekolah</b>&longi=<b>112.0471677</b>&lati=<b>-7.8132744</b>&keterangan=<b>Noted</b>&image=<b>indoweb.jpeg`</b>
</code></pre>

| Parameter |        Value         |
| :-------: | :------------------: |
|  `type`   | `DATANG` or `PULANG` |

#### Example of API response

```json
{
  "longi": "112.0471677",
  "lati": "-7.8132744",
  "lokasi": "Kantor Pusat Admin Sekolah",
  "type": "DATANG",
  "keterangan": "Noted",
  "id_pegawai": "2499",
  "kode_sekolah": "2020123",
  "database": "adminsek_demo.",
  "folder": "demo",
  "waktu_indonesia": "WIB",
  "date": "2022-12-31",
  "time": "07:00:07",
  "file_name": "2499_indoweb.jpeg",
  "message": "Could not move the file prod!",
  "error": true,
  "file_path": "absensi/2499_indoweb.jpeg",
  "image": "",
  "validasi": "Y",
  "nama_pegawai": "Widya Putri Tsafina S.Pd",
  "status_absen": "DATANG",
  "query": "INSERT INTO . . .",
  "sql": true
}
```

---

#### API call rest-api/submitIjin_coba.php

<pre><code>
https://m.adminsekolah.net/rest-api/submitIjin_coba.php?kode_sekolah=<b>2020123</b>&id_pegawai=<b>2499</b>&jenis=<b>CUTI</b>&tgl_awal=<b>2022-12-30</b>&tgl_akhir=<b>2022-12-31</b>&keterangan=<b>Noted</b>
</code></pre>

| Parameter |                 Value                 |
| :-------: | :-----------------------------------: |
|  `jenis`  | `CUTI` or `SAKIT` or `KEPERLUAN LAIN` |

#### Example of API response

<table>
<tr>
<td> Status </td> <td style="text-align: center;"> Response </td>
</tr>
<tr>
<td> true </td>
<td>

```json
{
  "is_correct": true,
  "message": "Tambah Presensi Ijin Berhasil"
}
```

</td>
</tr>
<tr>
<td> false </td>
<td>

```json
{
  "is_correct": false,
  "message": "Tambah Presensi Ijin Gagal. Silahkan Periksa Data Anda Terlebih Dahulu"
}
```

</td>
</tr>
</table>

---

Indoweb.id &copy; 2022 All rights reserved.
