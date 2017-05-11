package com.matrixxun.zoominlist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

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
        listView.getHeaderView().setImageResource(R.drawable.splash01);
        listView.getHeaderView().setScaleType(ImageView.ScaleType.CENTER_CROP);
        listView.setShadow(R.drawable.shadow_bottom);
    }

}
