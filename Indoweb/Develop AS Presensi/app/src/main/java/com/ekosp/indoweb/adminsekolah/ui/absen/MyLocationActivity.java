package com.ekosp.indoweb.adminsekolah.ui.absen;

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

import com.ekosp.indoweb.adminsekolah.R;
import com.ekosp.indoweb.adminsekolah.databinding.MyLocationDemoBinding;
import com.ekosp.indoweb.adminsekolah.helper.GlobalVar;
import com.ekosp.indoweb.adminsekolah.helper.NewGPSTracker;
import com.ekosp.indoweb.adminsekolah.helper.SessionManager;
import com.ekosp.indoweb.adminsekolah.model.DataSekolah;
import com.ekosp.indoweb.adminsekolah.model.DataUser;
import com.ekosp.indoweb.adminsekolah.model.LocationModel;
import com.ekosp.indoweb.adminsekolah.ui.MainActivity;
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

    private static final String TAG = "MyLocationActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private boolean validationLocation = true;

    private final ArrayList<LatLng> latLng = new ArrayList<>();
    private int radius_location; // default in meter
    private float distance;

    private SessionManager session;
    private DataSekolah.SekolahData sekolahData;
    private DataUser.UserData userData;

    private Geocoder geocoder;
    private GoogleMap mMap;
    private List<Address> addresses;
    private LocationModel locationModel;
    private Location destLocation;
    private String TYPE;
    private String destLocationName;

    Circle circle;
    Location mLastLocation;
    NewGPSTracker tracker;

    private MyLocationDemoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MyLocationDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        tracker = new NewGPSTracker(this);
        geocoder = new Geocoder(this);

        getSession();
        setDestLocation();
        setStatusUser();
        customDialog();
        binding.layoutConfirm.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent != null) TYPE = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI);

        binding.btnBack.setOnClickListener(v -> back());
        binding.infoToolbarTitle.setText(String.format("Absen %s", TYPE));

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
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

        for (int i = 0; i < latLng.size(); i++) {
            CircleOptions circleoptions = new CircleOptions()
                    .center(latLng.get(i))
                    .radius(radius_location)
                    .strokeWidth(2)
                    .strokeColor(this.getResources().getColor(R.color.red));
            circle = mMap.addCircle(circleoptions.center(latLng.get(i)).radius(radius_location));

            mMap.addMarker(new MarkerOptions()
                    .position(latLng.get(i))
                    .title("LOKASI ABSENSI")
                    .snippet(userData.getArea().get(i).getLokasi()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(circleoptions.getCenter()), 15.0f));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng.get(i)));
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Jarak dari lokasi Absen: " + getReadableDistance(distance), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Jarak dari lokasi Absen: " + getReadableDistance(distance), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
        mLastLocation = location;
        distance = mLastLocation.distanceTo(destLocation);
        if (locationModel == null) {
            if (mLastLocation != null)
                locationModel = new LocationModel(0.0, 0.0);
        }
        Objects.requireNonNull(locationModel).setLatitude(mLastLocation.getLatitude());
        locationModel.setLongitude(mLastLocation.getLongitude());

        mMap.setOnMarkerClickListener(marker -> {
            binding.layoutConfirm.setVisibility(View.VISIBLE);
            binding.btnLocConfirm.setVisibility(View.VISIBLE);

            try {
                addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                String address = addresses.get(0).getAddressLine(0);
                //binding.tvMapAddress.setText(String.format("%s %s ,%s", marker.getSnippet(), dataPonpes.getNamaPonpes(), address));
                Log.i(TAG, address);

                SpannableStringBuilder textMap = new SpannableStringBuilder()
                        .append("Anda melakukan Absensi ")
                        .append(TYPE, new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        .append(" di ")
                        .append(marker.getSnippet(), new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        .append(" ")
                        .append(sekolahData.getNama_sekolah());
                binding.tvMapAddress.setText(textMap);

            } catch (IOException e) {
                e.printStackTrace();
            }

            destLocation.setLatitude(marker.getPosition().latitude);
            destLocation.setLongitude(marker.getPosition().longitude);

            distance = mLastLocation.distanceTo(destLocation);
            destLocationName = marker.getSnippet();
            binding.btnLocConfirm.setOnClickListener(v -> check(distance, destLocationName));
            return false;
        });

        mMap.setOnMapClickListener(latLng1 -> {
            binding.layoutConfirm.setVisibility(View.VISIBLE);
            binding.tvMapAddress.setText("Pilih Titik Lokasi \nAbsensi Dulu Ya...");
            binding.btnLocConfirm.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (locationModel == null) {
            locationModel = new LocationModel(0.0, 0.0);
        }

        if (mPermissionDenied) {
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(true).show(getSupportFragmentManager(), "dialog");
            mPermissionDenied = false;
        }
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
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);

            if (locationManager != null) {
                mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mLastLocation != null) {
                    distance = mLastLocation.distanceTo(destLocation);
                    if (locationModel == null) {
                        locationModel = new LocationModel(0.0, 0.0);
                    }
                    locationModel.setLatitude(mLastLocation.getLatitude());
                    locationModel.setLongitude(mLastLocation.getLongitude());
                }
            }
        }
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

    private void check(Float distance, String destLocation) {
        if (isLocationModelEmpty())
            Toast.makeText(this, "Data real lokasi Anda tidak valid\nPastikan GPS Anda aktif", Toast.LENGTH_SHORT).show();
        else if (distance < radius_location) {
            Toast.makeText(this, "Anda di dalam radius lokasi " + TYPE + "\nJarak Anda: " + getReadableDistance(distance), Toast.LENGTH_LONG).show();

            Intent i = new Intent(this, UploadImage.class);
            i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE);
            i.putExtra(GlobalVar.PARAM_LAST_LOCATION, locationModel);
            i.putExtra(GlobalVar.DESTINATION_LOCATION, destLocation);
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else if (!validationLocation) {
            Toast.makeText(this, "Presensi Anda UNLOCK " + TYPE + "\nJarak Anda: " + getReadableDistance(distance), Toast.LENGTH_LONG).show();

            Intent i = new Intent(this, UploadImage.class);
            i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE);
            i.putExtra(GlobalVar.PARAM_LAST_LOCATION, locationModel);
            i.putExtra(GlobalVar.DESTINATION_LOCATION, destLocation);
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            Toast.makeText(this, "Anda masih di luar radius " + TYPE + "\nJarak Absen Anda: " + getReadableDistance(distance), Toast.LENGTH_LONG).show();
        }
    }

    private String getReadableDistance(float size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"Meter", "KM", "MM", "GM", "TM"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1000, digitGroups)) + " " + units[digitGroups];
    }

    private void setDestLocation() {
        destLocation = new Location(LocationManager.GPS_PROVIDER);
        for (int i = 0; i < userData.getArea().size(); i++) {
            destLocation.setLatitude(userData.getArea().get(i).getLatitude());
            destLocation.setLongitude(userData.getArea().get(i).getLongitude());

            destLocationName = userData.getArea().get(i).getLokasi();
            latLng.add(new LatLng(userData.getArea().get(i).getLatitude(), userData.getArea().get(i).getLongitude()));
        }
    }

    private void setStatusUser() {
        radius_location = Integer.parseInt(userData.getJarak_radius().equals("") ? "0" : userData.getJarak_radius());
        if (userData.getValidasi().equalsIgnoreCase("y")) {
            validationLocation = true;
        } else {
            validationLocation = false;
        }

        if (userData.getValidasi().equalsIgnoreCase("LOCK")) {
            validationLocation = true;
        } else {
            validationLocation = false;
        }
    }

    private void back() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void getSession() {
        userData = session.getSessionDataUser();
        sekolahData = session.getSessionDataSekolah();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationModel == null) {
            locationModel = new LocationModel(0.0, 0.0);
        }
    }
}