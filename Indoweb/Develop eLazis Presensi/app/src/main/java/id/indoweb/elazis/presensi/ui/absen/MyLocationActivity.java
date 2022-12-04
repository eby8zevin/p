package id.indoweb.elazis.presensi.ui.absen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import id.indoweb.elazis.presensi.ui.MainActivity;
import id.indoweb.elazis.presensi.R;
import id.indoweb.elazis.presensi.databinding.MyLocationDemoBinding;
import id.indoweb.elazis.presensi.helper.GlobalVar;
import id.indoweb.elazis.presensi.helper.NewGPSTracker;
import id.indoweb.elazis.presensi.helper.SessionManager;
import id.indoweb.elazis.presensi.model.DataPonpes;
import id.indoweb.elazis.presensi.model.DataUser;
import id.indoweb.elazis.presensi.model.LocationModel;

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
    private boolean validationLocation = true;

    private final ArrayList<LatLng> latLng = new ArrayList<>();
    private int radius_location; // default in meter
    private float distance;

    private SessionManager session;
    private DataPonpes dataPonpes;
    private DataUser dataUser;

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
        radius_location = Integer.parseInt(dataUser.getJarak_radius());

        Intent intent = getIntent();
        if (intent != null) TYPE = intent.getStringExtra(GlobalVar.PARAM_TYPE_ABSENSI);

        binding.btnBack.setOnClickListener(v -> back());
        binding.infoToolbarTitle.setText(String.format("Absen %s", TYPE));
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
                    .snippet(dataUser.getArea().get(i).getLokasi()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(circleoptions.getCenter()), 15.0f));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng.get(i)));
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }

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
            try {
                addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                String address = addresses.get(0).getAddressLine(0);
                binding.tvMapAddress.setText(String.format("%s %s ,%s", marker.getSnippet(), dataPonpes.getNamaPonpes(), address));
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
    }

    private void check(Float distance, String destLocation) {
        if (isLocationModelEmpty())
            Toast.makeText(this, "Data real lokasi Anda tidak valid\nPastikan GPS Anda aktif", Toast.LENGTH_SHORT).show();
        else if (distance < radius_location) {
            Toast.makeText(this, "Anda di dalam radius lokasi " + TYPE + "\nJarak Anda: " + getReadableDistance(distance), Toast.LENGTH_LONG).show();

            Intent i = new Intent(this, UploadImage.class);
            i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE);
            i.putExtra(GlobalVar.PARAM_LAST_LOCATION, locationModel);
            i.putExtra("LOKASI_TUJUAN", destLocation);
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else if (!validationLocation) {
            Toast.makeText(this, "Presensi Anda UNLOCK " + TYPE + "\nJarak Anda: " + getReadableDistance(distance), Toast.LENGTH_LONG).show();

            Intent i = new Intent(this, UploadImage.class);
            i.putExtra(GlobalVar.PARAM_TYPE_ABSENSI, TYPE);
            i.putExtra(GlobalVar.PARAM_LAST_LOCATION, locationModel);
            i.putExtra("LOKASI_TUJUAN", destLocation);
            startActivity(i);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            Toast.makeText(this, "Anda masih di luar radius " + TYPE + "\nJarak Absen Anda: " + getReadableDistance(distance), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationModel == null) {
            locationModel = new LocationModel(0.0, 0.0);
        }
        getSession();
        System.out.println("cek: " + dataUser.getArea().size());

        destLocation = new Location(LocationManager.GPS_PROVIDER);
        for (int i = 0; i < dataUser.getArea().size(); i++) {
            destLocation.setLatitude(dataUser.getArea().get(i).getLatitude());
            destLocation.setLongitude(dataUser.getArea().get(i).getLongitude());

            destLocationName = dataUser.getArea().get(i).getLokasi();
            latLng.add(new LatLng(dataUser.getArea().get(i).getLatitude(), dataUser.getArea().get(i).getLongitude()));
        }

        radius_location = Integer.parseInt(dataUser.getJarak_radius().equals("") ? "0" : dataUser.getJarak_radius());
        if (dataUser.getValidasi().equalsIgnoreCase("y")) {
            validationLocation = true;
        } else {
            validationLocation = false;
        }

        if (dataUser.getValidasi().equalsIgnoreCase("LOCK")) {
            validationLocation = true;
        } else {
            validationLocation = false;
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    private void getSession() {
        dataUser = session.getSessionDataUser();
        dataPonpes = session.getSessionDataPonpes();
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

    private void back() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}