package com.example.ada.voyage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;

public class AppBarActivity extends AuthenticationActivity  {

    EditText et;
    Button bt;

    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;

    Button bt2;

    String store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity_event);


        et = (EditText) findViewById(R.id.event_title);
        bt = (Button) findViewById(R.id.submitt);
        store = et.getText().toString();

        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);



        bt2 = (Button) findViewById(R.id.submitt);

        bt2.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "등록되었습니다", Toast.LENGTH_SHORT).show();
                Intent i = new Intent (getApplicationContext(), AppBarSubActivity.class);
                i.putExtra("store", store);

                startActivity(i);
            }
        });

    }



}
