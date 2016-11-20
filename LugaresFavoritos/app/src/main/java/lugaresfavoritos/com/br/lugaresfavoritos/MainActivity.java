package lugaresfavoritos.com.br.lugaresfavoritos;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.Manifest;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> lugares;
    static ArrayList<LatLng> localizacoes;
    static ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listview = (ListView) findViewById(R.id.listview);
        lugares = new ArrayList<>();

        localizacoes = new ArrayList<>();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lugares);
        listview.setAdapter(arrayAdapter);


        SQLiteDatabase db = openOrCreateDatabase("banco.db", Context.MODE_PRIVATE, null);
        StringBuilder sqlClientes = new StringBuilder();
        sqlClientes.append("CREATE TABLE IF NOT EXISTS locais(");
        sqlClientes.append("_id INTEGER PRIMARY KEY, endereco VARCHAR(400), latitude DOUBLE, longitude DOUBLE);");

        db.execSQL(sqlClientes.toString());

        buscarFavoritos();

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    102);

        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent_maps = new Intent(getApplicationContext(), MapsActivity.class);
                intent_maps.putExtra("localizacao", position);
                startActivity(intent_maps);
            }
        });

    }

    void buscarFavoritos(){
        lugares.clear();
        localizacoes.clear();

        lugares.add("Abrir Mapa");

        localizacoes.add(new LatLng(0,0));

        SQLiteDatabase db = openOrCreateDatabase("banco.db", Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT * FROM locais", null);
        cursor.moveToFirst();

        try {

            for (int i = 0; i < cursor.getCount(); i++) {
                LatLng point = new LatLng(cursor.getDouble(cursor.getColumnIndex("latitude")),
                        cursor.getDouble(cursor.getColumnIndex("longitude")));
                lugares.add(cursor.getString(cursor.getColumnIndex("endereco")));
                localizacoes.add(point);
                cursor.moveToNext();
            }
        }
        catch (Exception e){
            Log.i("Erro ", e.getMessage());
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        buscarFavoritos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        buscarFavoritos();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 102:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //not granted
                    System.exit(0);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
