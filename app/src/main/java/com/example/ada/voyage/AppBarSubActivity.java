package com.example.ada.voyage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class AppBarSubActivity extends AppBarActivity {

    ListView lv;
   // ArrayList<String> arrayList;
   // ArrayAdapter<String> adapter;

    String store2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calendar);

        //arrayList = new ArrayList<String>();
        //adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
        lv = (ListView) findViewById(R.id.event_list);
        lv.setAdapter(adapter);

        store2 = getIntent().getStringExtra("store");

        arrayList.add(store2);
        adapter.notifyDataSetChanged();

        /**
        bt.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                arrayList.add(store);
                adapter.notifyDataSetChanged();
            }
        });*/

    }
}
