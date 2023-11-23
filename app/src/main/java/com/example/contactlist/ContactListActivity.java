package com.example.contactlist;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {
    private ListView lvContacts;
    private ArrayList<Contact> Contacts;
    private CustomContactAdapter adapter;

    private Button btnCreate, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        Contacts = new ArrayList<>();
        lvContacts = findViewById(R.id.listContacts);
        btnCreate = findViewById(R.id.btnCreate);

        //loadData();
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ContactListActivity.this, ContactFormActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                Intent i = new Intent(ContactListActivity.this, ContactFormActivity.class);
                i.putExtra("CONTACT_KEY", Contacts.get(position).key);
                startActivity(i);
                finish();
            }
        });
        lvContacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String message = "Do you want to delete Contact - "+Contacts.get(position).name +" ?";
                showDialog(message, "Delete Contact", Contacts.get(position).key);
                return true;
            }
        });
        adapter = new CustomContactAdapter(this, Contacts);
        lvContacts.setAdapter(adapter);
    }
    private void showDialog(String message, String title, String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                KeyValueDB db = new KeyValueDB(getApplicationContext());
                db.deleteDataByKey(key);

                String[] keys = {"action", "id", "semester", "key"};
                String[] values = {"remove", "2020160405", "20231", key};
                httpRequest(keys, values);

                dialog.cancel();
                loadData();
                adapter.notifyDataSetChanged();
            }

        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void loadData(){
        Contacts.clear();
        KeyValueDB db = new KeyValueDB(ContactListActivity.this);
        Cursor rows = db.execute("SELECT * FROM key_value_pairs");
        if (rows.getCount() == 0) {
            String[] keys = {"action", "id", "semester"};
            String[] values = {"restore", "2020160405", "20231"}; //Main ID: 2020160034
            httpRequest(keys, values);
        }
        else {
            while (rows.moveToNext()) {
                String key = rows.getString(0);
                String ContactData = rows.getString(1);
                String[] fieldValues = ContactData.split("---");

                String name = fieldValues[0];
                String email = fieldValues[1];
                String phoneHome = fieldValues[2];
                String phoneOffice = fieldValues[3];
                String imageString = fieldValues[4];

                Contact e = new Contact(key, name, email, phoneHome, phoneOffice, imageString);
                Contacts.add(e);
            }
            adapter.notifyDataSetChanged();

        }
        db.close();

    }

    public void onRestart(){
        super.onRestart();
        loadData();
        String[] keys = {"action", "id", "semester"};
        String[] values = {"restore", "2020160405", "20231"}; //Main ID: 2020160034
        httpRequest(keys, values);
    }

    public void onStart() {
        super.onStart();
        loadData();
    }
    @SuppressLint("StaticFieldLeak")
    private void httpRequest(final String keys[], final String values[]){
        new AsyncTask<Void,Void,String>(){

            @Override
            protected String doInBackground(Void... voids) {
                List<NameValuePair> params=new ArrayList<NameValuePair>();
                for (int i=0; i<keys.length; i++){
                    params.add(new BasicNameValuePair(keys[i],values[i]));
                }
                String url= "https://muthosoft.com/univ/cse489/index.php";
                String data="";
                try {
                    data = JSONParser.getInstance().makeHttpRequest(url,"POST",params);
                    return data;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected void onPostExecute(String data){
                if(data!=null){
                    System.out.println(data);
                    updateEventListByServerData(data);
                }
            }
        }.execute();
    }
    public void updateEventListByServerData(String data){
        try{
            JSONObject jo = new JSONObject(data);
            if(jo.has("events")){
                Contacts.clear();
                JSONArray ja = jo.getJSONArray("events");
                KeyValueDB db = new KeyValueDB(ContactListActivity.this);

                for(int i=0; i<ja.length(); i++){
                    JSONObject event = ja.getJSONObject(i);
                    String eventKey = event.getString("e_key");
                    String eventValue = event.getString("e_value");

                    db.updateValueByKey(eventKey, eventValue); //Insert data into SQLite from server
                }
                db.close();
                loadData();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
