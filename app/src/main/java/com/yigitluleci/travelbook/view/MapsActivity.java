package com.yigitluleci.travelbook.view;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yigitluleci.travelbook.R;
import com.yigitluleci.travelbook.model.Place;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    SQLiteDatabase database;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intentToMain = new Intent(this,MainActivity.class);
        startActivity(intentToMain);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        //MainActivity classından gelen info "new" değerini taşıyorsa yapılacak işlemler
        if(info.matches("new")){

            //Konum alma servisini çağırıyoruz
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    //Konum değişirse ne yapılacak
                    //Kamera bulunan konuma getirilir ve zoom yapılır
                    //Harita açıldığında bir kere güncellenecek şekilde değiştirilir, harita üzerinde rahatça gezinilir
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.yigitluleci.travelbook", MODE_PRIVATE);
                    boolean trackBoolean = sharedPreferences.getBoolean("trackBoolean",false);
                    if(!trackBoolean){

                        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();
                    }



                }
            };
            //Eğer konum alma servisinze izin verilmezse kullanıcının bilinen son konumu üzerinden işlem yapılır
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                if(lastLocation != null){
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }
            }
        } else{
            //Eğer Main'den gelen info "old" değerini taşıyorsa map temizlenir
            //Main'den gelen place nesnesinin taşıdığı değerler aktarılır
            //Gelen bilgiler dahilinde map üzerinde yeni marker oluşturulur, tıklanınca adres gözükür ve map o bölgeye zoom yapar
            mMap.clear();
            Place place =(Place) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(place.latitude,place.longitude);
            String placeName = place.name;

            mMap.addMarker(new MarkerOptions().position(latLng).title(placeName) );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0){
            if(requestCode==1){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                    Intent intent = getIntent();
                    String info = intent.getStringExtra("info");

                    if(info.matches("new")){
                        Location lastLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                        if(lastLocation != null){
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }
                    }else{
                        //sqlite data && intent data
                        mMap.clear();
                        Place place =(Place) intent.getSerializableExtra("place");
                        LatLng latLng = new LatLng(place.latitude,place.longitude);
                        String placeName = place.name;

                        mMap.addMarker(new MarkerOptions().position(latLng).title(placeName) );
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    }
                }
            }
        }

    }
    //Map üzerinde basılı tutulunca ne olur
    @Override
    public void onMapLongClick(LatLng latLng) {
        
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        //Basılı tutulduğunda adres konumunu addressList listesinin içine yazdırıyoruz
        //address değişkenine gerçek adresi yazdırıyoruz
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList != null && addressList.size()>0){
                if(addressList.get(0).getThoroughfare() != null){
                    address += addressList.get(0).getThoroughfare();

                    if(addressList.get(0).getSubThoroughfare() != null){
                        address +="";
                        address += addressList.get(0).getSubThoroughfare();
                    }
                }
            }else
            {
                address ="New Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Mapi temizleyip tıklanılan bölgede yeni marker oluşturuyoruz 
        mMap.clear();
        mMap.addMarker(new MarkerOptions().title(address).position(latLng));
        //enlem ve boylam bilgisini double cinsinden çekiyoruz
        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;
        //Database'e kaydetmek için Place sınıfından yeni bir nesne oluşturuyoruz
        final Place place = new Place(address,latitude,longitude);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setCancelable(false);
        //Yanlışlıkla tıklamalara önlem olarak Alert veriyoruz
        alertDialog.setTitle("Are You Sure?");
        alertDialog.setMessage(place.name);
        //Evete tıklanırsa
        alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Database işlemleri için try catch yapısını kuruyoruz
                try {
                    //Places tablosu yoksa oluşturuyoruz varsa açıyoruz
                    database = MapsActivity.this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
                    database.execSQL("CREATE TABLE IF NOT EXISTS places (id INTEGER PRIMARY KEY,name VARCHAR,latitude VARCHAR,longitude VARCHAR)");
                    
                    String toCompile = "INSERT INTO places (name,latitude,longitude) VALUES(?,?,?)";
                    //Oluşturduğumuz sınıf sayesinde kolayca verilerimizi Database'e yazdırıyoruz
                    SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
                    sqLiteStatement.bindString(1,place.name);
                    sqLiteStatement.bindString(2,String.valueOf(place.latitude));
                    sqLiteStatement.bindString(3,String.valueOf(place.longitude));
                    sqLiteStatement.execute();
                    //Saved toast mesajı yazdırıyoruz
                    Toast.makeText(getApplicationContext(),"Saved!",Toast.LENGTH_LONG).show();


                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        });
        //Hayıra basılırsa 
        alertDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Canceled toast mesajını yazdırıyoruz
                Toast.makeText(getApplicationContext(),"Canceled!",Toast.LENGTH_LONG).show();
            }
        });
        //Verilen cevaba göre Toast'u ekranda gösteriyoruz
        alertDialog.show();

    }
}
