package com.example.ada.voyage.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ada.voyage.R;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/** Created by: Jaebin Yang */

public class SettingsActivity extends AppCompatActivity {
    StorageReference storageRef;
    String newResource;
    ArrayList<SettingItem> settings;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference calendarRef;
    DatabaseReference blogRef;
    FirebaseAuth mAuth;
    JSONArray calendar;
    JSONArray blog;
    //CalendarDBHelper calendarDBhelper;

    String personId;
    ProgressDialog progressdialog;
    String postID, postTitle, postImg1, postImg2, postImg3, postImg4, postImg5, postDate, postDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /** Create an action bar that allows the user to go back to the previous activity/fragment. */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Settings");
        //calendarDBhelper = new CalendarDBHelper(this, null, null, 1);


        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://voyage-9903f.appspot.com");


        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser =  mAuth.getCurrentUser();
        personId = (String) currentUser.getUid();
        myRef= database.getReference("/users/" + personId);

        /** Two options in settings: Save to Remote Database, and Sync from Remote Database */
        settings = new ArrayList<SettingItem>();
        SettingItem si;
        si = new SettingItem(R.drawable.settings_upload_data, "Save to Remote Database");
        settings.add(si);
        si = new SettingItem(R.drawable.settings_sync_data, "Sync from Remote Database");
        settings.add(si);


        MyListAdapter myAdapter = new MyListAdapter(this,
                R.layout.settings_list_item, settings);

        ListView myList;
        myList = (ListView) findViewById(R.id.settingsList);
        myList.setAdapter(myAdapter);

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /** If the user chooses to save to remote database, ask for confirmation to the user,
             * then overwrite the data stored in the Firebase with the current data stored in the application.
             * On the other hand, if the user chooses to sync data from remote database,
             * after asking for confirmation to the user, overwrite the data in the local database
             * with the data stored in the Firebase. */
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position == 0) { /** User has chosen the Save to Remote Database option */
                    AlertDialog.Builder save = new AlertDialog.Builder(SettingsActivity.this);
                    save.setTitle("Save to Remote Database");
                    save.setMessage("Previously saved data will be overwritten. Will you continue?");
                    save.setNegativeButton("No", null);
                    save.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            saveToFirebase();
                        }
                    });
                    save.show();
                }
                else if (position == 1) { /** User has chosen the Sync from Remote Database option */
                    AlertDialog.Builder sync = new AlertDialog.Builder(SettingsActivity.this);
                    sync.setTitle("Sync from Remote Database");
                    sync.setMessage("Your current data will not be saved. Will you continue?");
                    sync.setNegativeButton("No", null);
                    sync.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            retrieveData();
                        }
                    });
                    sync.show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /** If the user chooses to go back, simply finish the activity */
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void retrieveData() {
        if(isConnected(getBaseContext())) {
            progressdialog = new ProgressDialog(SettingsActivity.this);
            progressdialog.setMessage("Sync in Progress...");
            progressdialog.show();
            /** Delete all the data stored in the local database, since we are going to
             * overwrite it with the data from the Firebase.
             */
            //calendarDBhelper.deleteAll();
            calendarRef = database.getReference("/users/" + personId + "/events");
            calendarRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    /** Return the data as a DataSnapshot object, and then change it to an iterable object
                     * (dataSnapshot.getChildren()). Read through the iterable, and add the data to the
                     * local database.
                     */
                    for (DataSnapshot event : dataSnapshot.getChildren()) {
                        String title, location, startDate, startTime, endDate, endTime, allDay, description;
                        String eventID = event.child("id").getValue(String.class);
                        int id = Integer.parseInt(eventID);
                        title = event.child("title").getValue(String.class);
                        location = event.child("location").getValue(String.class);
                        startDate = event.child("startDate").getValue(String.class);
                        startTime = event.child("startTime").getValue(String.class);
                        endDate = event.child("endDate").getValue(String.class);
                        endTime = event.child("endTime").getValue(String.class);
                        allDay = event.child("allDay").getValue(String.class);
                        description = event.child("description").getValue(String.class);
                       // Schedule e = new Schedule(id, title, location, startDate, startTime, endDate, endTime,
                               // allDay, description);
                       // calendarDBhelper.addEvent(e);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            blogRef = database.getReference("/users/" + personId + "/posts");
            blogRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot post : dataSnapshot.getChildren()) {
                        String postID, title, date, img1, img2, img3, img4, img5, description;
                        postID = post.child("id").getValue(String.class);
                        int id = Integer.parseInt(postID);
                        title = post.child("title").getValue(String.class);
                        date = post.child("date").getValue(String.class);
                        img1 = post.child("Image 1").getValue(String.class);
                        img2 = post.child("Image 2").getValue(String.class);
                        img3 = post.child("Image 3").getValue(String.class);
                        img4 = post.child("Image 4").getValue(String.class);
                        img5 = post.child("Image 5").getValue(String.class);
                        description = post.child("description").getValue(String.class);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            progressdialog.dismiss();
            Toast.makeText(SettingsActivity.this,
                    "Sync Successful", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(SettingsActivity.this,
                    "Please check your internet connection.", Toast.LENGTH_LONG).show();
        }
    }


    public void saveToFirebase() {
        if(isConnected(getBaseContext())) {
            Toast.makeText(SettingsActivity.this,
                    "Saving Data...", Toast.LENGTH_LONG).show();
            String eventID, eventTitle, eventLocation, eventStartDate, eventStartTime, eventEndDate;
            String eventEndTime, eventAllDay, eventDescription;
            calendarRef = database.getReference("/users/" + personId + "/events");
            blogRef = database.getReference("/users/" + personId + "/posts");
            /** Remove all the data in the user's designated Firebase section, since we are going to
             * overwrite it with the data stored in the local database.
             * Change the data in the SQLite Database to a JSON array before saving it to Firebase.
             */
            calendarRef.removeValue();
            for (int i = 0; i < calendar.length(); i++) {
                try {
                    JSONObject calendarRow = calendar.getJSONObject(i);
                    eventID = calendarRow.getString("_id");
                    eventTitle = calendarRow.getString("title");
                    eventLocation = calendarRow.getString("location");
                    eventStartDate = calendarRow.getString("startDate");
                    eventStartTime = calendarRow.getString("startTime");
                    eventEndDate = calendarRow.getString("endDate");
                    eventEndTime = calendarRow.getString("endTime");
                    eventAllDay = calendarRow.getString("allDay");
                    eventDescription = calendarRow.getString("description");

                    DatabaseReference eventId = calendarRef.child(eventID);
                    DatabaseReference id = eventId.child("id");
                    id.setValue(eventID);
                    DatabaseReference title = eventId.child("title");
                    title.setValue(eventTitle);
                    DatabaseReference location = eventId.child("location");
                    location.setValue(eventLocation);
                    DatabaseReference startDate = eventId.child("startDate");
                    startDate.setValue(eventStartDate);
                    DatabaseReference startTime = eventId.child("startTime");
                    startTime.setValue(eventStartTime);
                    DatabaseReference endDate = eventId.child("endDate");
                    endDate.setValue(eventEndDate);
                    DatabaseReference endTime = eventId.child("endTime");
                    endTime.setValue(eventEndTime);
                    DatabaseReference allDay = eventId.child("allDay");
                    allDay.setValue(eventAllDay);
                    DatabaseReference description = eventId.child("description");
                    description.setValue(eventDescription);
                } catch (org.json.JSONException e) {
                    Toast.makeText(SettingsActivity.this,
                            "Error! Failed to save Data on Remote Database", Toast.LENGTH_SHORT).show();
                }
            }


            blogRef.removeValue();
            for (int i = 0; i < blog.length(); i++) {
                try {
                    JSONObject blogRow = blog.getJSONObject(i);
                    postID = blogRow.getString("_id");
                    postTitle = blogRow.getString("title");
                    postDate = blogRow.getString("date");
                    postImg1 = blogRow.getString("firstImg");
                    postImg2 = blogRow.getString("secondImg");
                    postImg3 = blogRow.getString("thirdImg");
                    postImg4 = blogRow.getString("fourthImg");
                    postImg5 = blogRow.getString("fifthImg");
                    postDescription = blogRow.getString("description");


                    DatabaseReference id = blogRef.child(postID);
                    DatabaseReference postId = id.child("id");
                    postId.setValue(postID);
                    DatabaseReference title = id.child("title");
                    title.setValue(postTitle);
                    DatabaseReference date = id.child("date");
                    date.setValue(postDate);
                    DatabaseReference image1 = id.child("Image 1");
                    if (!(postImg1.equals("Exception: No Image Applied") || postImg1.startsWith("https://firebasestorage.googleapis.com"))) {
                        Uri uri = Uri.parse(postImg1);
                        StorageReference path = storageRef.child("Photos/" + postTitle + uri.getLastPathSegment());
                        UploadTask task = path.putFile(uri);
                        try {
                            Thread.sleep(4000);
                        } catch (Exception e) {

                        }
                        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                /** Ignore the red underlines in the code below;
                                 * it is a bug in the Android Studio program
                                 * (which will hopefully be resolved in future updates */

                                @SuppressWarnings("VisibleForTests") Uri uri = taskSnapshot.getMetadata().getDownloadUrl();
                                if (uri.toString().startsWith("https://firebasestorage.googleapis.com")) {
                                    newResource = uri.toString();
                                }
                            }
                        });
                        image1.setValue(newResource);
                    }
                    else {
                        image1.setValue(postImg1);
                    }
                    DatabaseReference image2 = id.child("Image 2");
                    if (!(postImg2.equals("Exception: No Image Applied") || postImg2.startsWith("https://firebasestorage.googleapis.com"))) {
                        Uri uri = Uri.parse(postImg2);
                        StorageReference path = storageRef.child("Photos/" + postTitle + uri.getLastPathSegment());
                        UploadTask task = path.putFile(uri);
                        try {
                            Thread.sleep(4000);
                        } catch (Exception e) {

                        }
                        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                /** Ignore the red underlines in the code below;
                                 * it is a bug in the Android Studio program
                                 * (which will hopefully be resolved in future updates */

                                @SuppressWarnings("VisibleForTests") Uri uri = taskSnapshot.getMetadata().getDownloadUrl();
                                if (uri.toString().startsWith("https://firebasestorage.googleapis.com")) {
                                    newResource = uri.toString();
                                }
                            }
                        });
                        image2.setValue(newResource);
                    } else {
                        image2.setValue(postImg2);
                    }
                    DatabaseReference image3 = id.child("Image 3");
                    if (!(postImg3.equals("Exception: No Image Applied") || postImg3.startsWith("https://firebasestorage.googleapis.com"))) {
                        Uri uri = Uri.parse(postImg3);
                        StorageReference path = storageRef.child("Photos/" + postTitle + uri.getLastPathSegment());
                        UploadTask task = path.putFile(uri);
                        try {
                            Thread.sleep(4000);
                        } catch (Exception e) {

                        }
                        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                /** Ignore the red underlines in the code below;
                                 * it is a bug in the Android Studio program
                                 * (which will hopefully be resolved in future updates */

                                @SuppressWarnings("VisibleForTests") Uri uri = taskSnapshot.getMetadata().getDownloadUrl();
                                if (uri.toString().startsWith("https://firebasestorage.googleapis.com")) {
                                    newResource = uri.toString();
                                }
                            }
                        });
                        image3.setValue(newResource);
                    } else {
                        image3.setValue(postImg3);
                    }
                    DatabaseReference image4 = id.child("Image 4");
                    if (!(postImg4.equals("Exception: No Image Applied") || postImg4.startsWith("https://firebasestorage.googleapis.com"))) {
                        Uri uri = Uri.parse(postImg4);
                        StorageReference path = storageRef.child("Photos/" + postTitle + uri.getLastPathSegment());
                        UploadTask task = path.putFile(uri);
                        try {
                            Thread.sleep(4000);
                        } catch (Exception e) {

                        }
                        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                /** Ignore the red underlines in the code below;
                                 * it is a bug in the Android Studio program
                                 * (which will hopefully be resolved in future updates */

                                @SuppressWarnings("VisibleForTests") Uri uri = taskSnapshot.getMetadata().getDownloadUrl();
                                if (uri.toString().startsWith("https://firebasestorage.googleapis.com")) {
                                    newResource = uri.toString();
                                }
                            }
                        });
                        image4.setValue(newResource);
                    } else {
                        image4.setValue(postImg4);
                    }
                    DatabaseReference image5 = id.child("Image 5");
                    if (!(postImg5.equals("Exception: No Image Applied") || postImg5.startsWith("https://firebasestorage.googleapis.com"))) {
                        Uri uri = Uri.parse(postImg5);
                        StorageReference path = storageRef.child("Photos/" + postTitle + uri.getLastPathSegment());
                        UploadTask task = path.putFile(uri);
                        try {
                            Thread.sleep(4000);
                        } catch (Exception e) {

                        }
                        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                /** Ignore the red underlines in the code below;
                                 * it is a bug in the Android Studio program
                                 * (which will hopefully be resolved in future updates */

                                @SuppressWarnings("VisibleForTests") Uri uri = taskSnapshot.getMetadata().getDownloadUrl();
                                if (uri.toString().startsWith("https://firebasestorage.googleapis.com")) {
                                    newResource = uri.toString();
                                }
                            }
                        });
                        image5.setValue(newResource);
                    } else {
                        image5.setValue(postImg5);
                    }
                    DatabaseReference description = id.child("description");
                    description.setValue(postDescription);
                } catch (org.json.JSONException e) {
                    Toast.makeText(SettingsActivity.this,
                            "Error! Failed to save Data on Remote Database", Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(SettingsActivity.this,
                    "Data Successfully Saved", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(SettingsActivity.this,
                    "Please check your internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    class SettingItem {
        int icon;
        String name;

        SettingItem(int icon, String name) {
            this.icon = icon;
            this.name = name;
        }
    }


    class MyListAdapter extends BaseAdapter {
        Context c;
        LayoutInflater inflater;
        ArrayList<SettingItem> settings;
        int layout;

        public MyListAdapter(Context context, int l, ArrayList<SettingItem> s) {
            c = context;
            settings = s;
            layout = l;
            inflater = LayoutInflater.from(c);
        }

        public int getCount() {
            return settings.size();
        }

        public Object getItem(int position) {
            return settings.get(position).name;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            /** View Settings layout */
            final int pos = position;
            if (convertView == null) {
                convertView = inflater.inflate(layout, parent, false);
            }
            ImageView img = (ImageView) convertView.findViewById(R.id.img);
            img.setImageResource(settings.get(pos).icon);

            TextView txt = (TextView) convertView.findViewById(R.id.text);
            txt.setText(settings.get(pos).name);

            return convertView;
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }
}

