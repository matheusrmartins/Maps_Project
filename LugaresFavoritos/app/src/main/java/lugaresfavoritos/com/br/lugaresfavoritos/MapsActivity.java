package lugaresfavoritos.com.br.lugaresfavoritos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, android.location.LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;
    private ProgressDialog progressDialog;

    private int localizacao = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        localizacao = intent.getIntExtra("localizacao", -1);

        if (localizacao != -1 && localizacao != 0){
            locationManager.removeUpdates(this);

            mMap.addMarker(new MarkerOptions().position(MainActivity.localizacoes.get(localizacao)).title(MainActivity.lugares.get(localizacao)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.localizacoes.get(localizacao), 17));

           // progressDialog.dismiss();
        } else {
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Buscando a localização...");
            progressDialog.show();
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String marcador = new Date().toString();
        try{
            List<Address> listaLocais = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            if (listaLocais != null && listaLocais.size() > 0){
                marcador = listaLocais.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = openOrCreateDatabase("banco.db", Context.MODE_PRIVATE, null);

        db.execSQL("insert into locais (endereco, latitude, longitude) values (\""+marcador+"\", "+point.latitude+", "+point.longitude+")");

        MainActivity.arrayAdapter.notifyDataSetChanged();

        mMap.addMarker(new MarkerOptions()
        .position(point)
        .title(marcador)
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    @Override
    public void onLocationChanged(Location localizacaoUsuario) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(localizacaoUsuario.getLatitude(), localizacaoUsuario.getLongitude()), 17));
        progressDialog.dismiss();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
