package com.yigitluleci.travelbook.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.yigitluleci.travelbook.R;
import com.yigitluleci.travelbook.adapter.CustomAdapter;
import com.yigitluleci.travelbook.model.Place;
import com.yigitluleci.travelbook.view.MapsActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase database;
    ArrayList<Place> placeList = new ArrayList<>();
    ListView listView;
    CustomAdapter customAdapter;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Addplace menüsünü ekliyoruz
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_place,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Add Place'e basıldığında yapılacak işlem
        //Yeni konum ekelemek için new değeriyle MapsActivity'yi açıyoruz 
        if(item.getItemId()== R.id.add_place){
            Intent intent  = new Intent(this, MapsActivity.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Main ekranı geldiğinde çalıştırılıcak kısımlar
        //getData() fonksiyonu ile database'deki verileri listview şeklinde ana ekrana yazdırıyoruz
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        getData();
    }

    public void getData(){
        //Oluşturduğumuz customAdapter sınıfından obje oluşturuyoruz yukarıda oluşturduğumuz placeList ArrayListini gönderiyoruz
        customAdapter = new CustomAdapter(this,placeList);
        //Database ile işlem yapılacağından try-catch yapısı içinde verilerimizi çekmeye başlıyoruz
        try {
            //Daha önceden oluşturulmadıysa Places tablosunu oluşturuyoruz, database nesnesini yukarıda tanımlamıştık
            database = this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM places",null);
            //konum adı, enlem ve boylam bilgilerinin index numaralarını değişkenlere kaydediyoruz
            int nameIx = cursor.getColumnIndex("name");
            int latitudeIx = cursor.getColumnIndex("latitude");
            int longitudeIx = cursor.getColumnIndex("longitude");

            while (cursor.moveToNext()){
                //while döngüsü ile database'deki tüm satırları okuyarak değişkenlere aktarıyoruz
                String nameFromDatabase = cursor.getString(nameIx);
                String latitudeFromDatabase = cursor.getString(latitudeIx);
                String longitudeFromDatabase = cursor.getString(longitudeIx);

                Double latitude = Double.parseDouble(latitudeFromDatabase);
                Double longitude = Double.parseDouble(longitudeFromDatabase);
                //place sınıfından nesne oluşturup çektiğimiz verileri bu nesne üzerinden placeList'e ekliyoruz
                Place place = new Place(nameFromDatabase,latitude,longitude);
                //verilerin çekilip çekilmediğini logcat üzerinden kontrol etmek amacıyla yazdığım kod
                System.out.println(place.name);

                placeList.add(place);
            }
            customAdapter.notifyDataSetChanged();
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        //custom adappterımızı listview'a set ediyoruz
        listView.setAdapter(customAdapter);
        //listView'da bir item'a tıkladığımızda yapılacak işlemler
        //oluşturduğumuz place listesindeki verileri info(old) ile mapsactivity classına gönderiyoruz
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placeList.get(i));
                startActivity(intent);
            }
        });


    }

}
