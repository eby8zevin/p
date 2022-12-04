package com.ekosp.indoweb.epesantren.locator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ekosp.indoweb.epesantren.MainActivity;
import com.ekosp.indoweb.epesantren.R;
import com.ekosp.indoweb.epesantren.databinding.MyLocationDemoBinding;
import com.ekosp.indoweb.epesantren.helper.GlobalVar;
import com.ekosp.indoweb.epesantren.helper.NewGPSTracker;
import com.ekosp.indoweb.epesantren.helper.SessionManager;
import com.ekosp.indoweb.epesantren.model.DataPonpes;
import com.ekosp.indoweb.epesantren.model.DataUser;
import com.ekosp.indoweb.epesantren.model.LocationModel;
import com.ekosp.indoweb.epesantren.upload.UploadImage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyLocationActivity extends AppCompatActivity implements
        OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private final ArrayList<LatLng> latlngs = new ArrayList<>();
    private static final String TAG = MyLocationActivity.class.getSimpleName();
    private int radius_lokasi; // default dalam meter
    private float jarak;

    private SessionManager session;
    private DataUser dataUser;
    private DataPonpes dataPonpes;
    private boolean validasiLokasi = true;
    private Location _lokasi_tujuan;
    private String TYPE;
    private String nama_lokasi_tujuan;
    Circle circle;
    private List<Address> addresses;

    private LocationModel locationModel;

    Location mLastLocation;

    // GPSTracker gps;
    NewGPSTracker tracker;
    private Geocoder geocoder;

    private MyLocationDemoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MyLocationDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        tracker = new NewGPSTracker(this);
        geocoder = new Geocoder(this);

        customDialog();
        getDataLocation();

        Intent intent = getIntent();
        if (intent != null) TYPE = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI);

        binding.infoHdrAbsen.setText(String.format("Absen %s", TYPE));
        binding.back.setOnClickListener(view -> backToHome());
        binding.frameAtas3.setVisibility(View.GONE);
    }

    private void customDialog() {
        SpannableString titleDialog = new SpannableString("Pilih Titik Lokasi \nAbsensi Dulu Ya...");
        titleDialog.setSpan(
                new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0,
                titleDialog.length(),
                0
        );

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(titleDialog);
        alertDialog.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.cancel());

        alertDialog.create().show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        mMap = map;

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        for (int i = 0; i < latlngs.size(); i++) {
            CircleOptions circleoptions = new CircleOptions()
                    .center(latlngs.get(i))
                    .radius(radius_lokasi)
                    .strokeWidth(2)
                    .strokeColor(this.getResources().getColor(R.color.red));
            circle = mMap.addCircle(circleoptions.center(latlngs.get(i)).radius(radius_lokasi));

            mMap.addMarker(new MarkerOptions()
                    .position(latlngs.get(i))
                    .title("LOKASI ABSENSI")
                    .snippet(dataUser.getArea().get(i).getLokasi()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(circleoptions.getCenter()), 15.0f));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlngs.get(i)));
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
        //To setup location manager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //To request location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);

        if (locationManager != null) {
            mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mLastLocation != null) {
                jarak = mLastLocation.distanceTo(_lokasi_tujuan);
                if (locationModel == null) {
                    locationModel = new LocationModel(0.0, 0.0);
                }
                locationModel.setLatitude(mLastLocation.getLatitude());
                locationModel.setLongitude(mLastLocation.getLongitude());
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Jarak dari lokasi absen: " + getReadableDistance(jarak), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Jarak dari lokasi absen: " + getReadableDistance(jarak), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (locationModel == null) {
            locationModel = new LocationModel(0.0, 0.0);
        }

        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void backToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onLocationChanged(Location location) {

        //To hold location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //opening position with some zoom level in the map
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
        mLastLocation = location;
        jarak = mLastLocation.distanceTo(_lokasi_tujuan);
        if (locationModel == null) {
            if (mLastLocation != null)
                locationModel = new LocationModel(0.0, 0.0);
        }
        Objects.requireNonNull(locationModel).setLatitude(mLastLocation.getLatitude());
        locationModel.setLongitude(mLastLocation.getLongitude());

        mMap.setOnMarkerClickListener(marker -> {
            try {
                addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                String address = addresses.get(0).getAddressLine(0);
                Log.i(TAG, address);

                SpannableStringBuilder textMap = new SpannableStringBuilder()
                        .append("Anda melakukan Absensi ")
                        .append(TYPE, new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        .append(" di ")
                        .append(marker.getSnippet(), new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        .append(" ")
                        .append(dataPonpes.getNamaPonpes());

                binding.btmTxtMap.setText(textMap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            _lokasi_tujuan.setLatitude(marker.getPosition().latitude);
            _lokasi_tujuan.setLongitude(marker.getPosition().longitude);

            jarak = mLastLocation.distanceTo(_lokasi_tujuan);
            nama_lokasi_tujuan = marker.getSnippet();

            binding.frameAtas3.setVisibility(View.VISIBLE);
            binding.btnCheckAbsensi.setVisibility(View.VISIBLE);
            binding.btnCheckAbsensi.setOnClickListener(v -> check(jarak, nama_lokasi_tujuan));
            return false;
        });

        mMap.setOnMapClickListener(latLng1 -> {
            binding.frameAtas3.setVisibility(View.VISIBLE);
            binding.btnCheckAbsensi.setVisibility(View.GONE);
            binding.btmTxtMap.setText("Pilih Titik Lokasi \nAbsensi Dulu Ya...");
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void check(float jarak, String nama_lokasi_tujuan) {
        Log.i(TAG, "check");
        if (isLocationModelEmpty())
            Toast.makeText(this, "Data real lokasi Anda tidak valid\nPastikan GPS anda aktif", Toast.LENGTH_SHORT).show();
        else if (jarak < radius_lokasi) {
            Toast.makeText(this, "Anda di dalam radius lokasi " + TYPE + "\nJarak anda: " + getReadableDistance(jarak), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, UploadImage.class);
            intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE);
            intent.putExtra(GlobalVar.PARAM_LAST_LOCATION, locationModel);
            intent.putExtra("LOKASI_TUJUAN", nama_lokasi_tujuan);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else if (!validasiLokasi) {
            Toast.makeText(this, "Presensi Anda UNLOCK " + TYPE + "\nJarak anda: " + getReadableDistance(jarak), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, UploadImage.class);
            intent.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE);
            intent.putExtra(GlobalVar.PARAM_LAST_LOCATION, locationModel);
            intent.putExtra("LOKASI_TUJUAN", nama_lokasi_tujuan);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            Toast.makeText(this, "Anda masih di luar radius " + TYPE + "\nJarak absen anda: " + getReadableDistance(jarak), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationModel == null) {
            locationModel = new LocationModel(0.0, 0.0);
        }
    }

    private void getDataLocation() {
        dataUser = session.getSessionDataUser();
        dataPonpes = session.getSessionDataPonpes();

        _lokasi_tujuan = new Location(LocationManager.GPS_PROVIDER);
        for (int i = 0; i < dataUser.getArea().size(); i++) {
            _lokasi_tujuan.setLatitude(dataUser.getArea().get(i).getLatitude());
            _lokasi_tujuan.setLongitude(dataUser.getArea().get(i).getLongitude());

            nama_lokasi_tujuan = dataUser.getArea().get(i).getLokasi();
            latlngs.add(new LatLng(dataUser.getArea().get(i).getLatitude(), dataUser.getArea().get(i).getLongitude()));
        }

        radius_lokasi = Integer.parseInt(dataUser.getJarak_radius().equals("") ? "0" : dataUser.getJarak_radius());
        if (dataUser.getValidasi().equalsIgnoreCase("y")) {
            validasiLokasi = true;
        } else {
            validasiLokasi = false;
        }

        if (dataUser.getValidasi().equalsIgnoreCase("LOCK")) {
            validasiLokasi = true;
        } else {
            validasiLokasi = false;
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private String getReadableDistance(float size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"Meter", "KM", "MM", "GM", "TM"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1000, digitGroups)) + " " + units[digitGroups];
    }

    private boolean isLocationModelEmpty() {
        if (mLastLocation != null) {
            if (locationModel == null) {
                locationModel = new LocationModel(0.0, 0.0);
            }
            if (locationModel.getLatitude() == null)
                locationModel.setLatitude(mLastLocation.getLatitude());
            if (locationModel.getLongitude() == null)
                locationModel.setLongitude(mLastLocation.getLongitude());
        } else {
            return true;
        }

        return false;
    }
}