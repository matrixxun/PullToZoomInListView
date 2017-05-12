package com.matrixxun.zoominlist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.matrixxun.starry.PullToZoomListView;

public class MainActivity extends AppCompatActivity {
    private PullToZoomListView listView;
    private String[] adapterData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (PullToZoomListView)findViewById(R.id.list_view);
        adapterData = new String[] { "Activity","Service","Content Provider","Intent","BroadcastReceiver",
                "ADT","Sqlite3","HttpClient","DDMS","Android Studio","Fragment","Loader",
                "ADT","Sqlite3","HttpClient","DDMS","Android Studio","Fragment","Loader"};

        listView.setAdapter(new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_list_item_1, adapterData));
        listView.getHeaderView().setImageResource(R.drawable.splash);
        listView.getHeaderView().setScaleType(ImageView.ScaleType.CENTER_CROP);
        listView.setShadow(R.drawable.shadow_bottom);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this,"click index:"+position,Toast.LENGTH_SHORT).show();
            }
        });
    }

}
