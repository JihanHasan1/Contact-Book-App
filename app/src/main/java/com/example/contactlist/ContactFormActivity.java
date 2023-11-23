package com.example.contactlist;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ContactFormActivity extends AppCompatActivity {

    private ImageView imgView;
    private EditText etName, etEmail, etPhoneHome, etPhoneOffice;
    private Button btnSave, btnCancel, btnView;
    KeyValueDB KeyValueDB;
    private String imageString="";
    private String existingKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_form);

        imgView = findViewById(R.id.imgView);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneHome = findViewById(R.id.etPhoneHome);
        etPhoneOffice = findViewById(R.id.etPhoneOffice);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnView = findViewById(R.id.btnView);

        Intent i = getIntent();
        if(i.hasExtra("CONTACT_KEY")){
            existingKey = i.getStringExtra("CONTACT_KEY");
            KeyValueDB db = new KeyValueDB(ContactFormActivity.this);
            String value = db.getValueByKey(existingKey);
            String values[] = value.split("---");

            etName.setText(values[0]);
            etEmail.setText(values[1]);
            etPhoneHome.setText(values[2]);
            etPhoneOffice.setText(values[3]);

            imageString = values[4];
            byte[] bytes= Base64.decode(imageString,Base64.DEFAULT);
            // Initialize bitmap
            Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            // set bitmap on imageView
            imgView.setImageBitmap(bitmap);

            btnSave.setText("Update");

            db.close();
        }

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                String phoneHome = etPhoneHome.getText().toString();
                String phoneOffice = etPhoneOffice.getText().toString();
                String image = imageString;

                int count=0;
                if(name.equals("")){
                    etName.setError("Insert Name !");
                    count++;
                }
                if (email.equals("")) {
                    etEmail.setError("Insert Email !");
                    count++;
                }
                if (phoneHome.equals("")) {
                    etPhoneHome.setError("Insert Number !");
                    count++;
                }
                if (phoneOffice.equals("")) {
                    etPhoneOffice.setError("Insert Number !");
                    count++;
                }
                if (image.equals("")) {
                    Toast.makeText(ContactFormActivity.this, "Insert Photo", Toast.LENGTH_SHORT).show();
                    count++;
                }
                if(count == 0){
                    if(isValid(email)){
                        if(isValidMobileNo(phoneHome)){
                            if(isValidMobileNo(phoneOffice)){
                                String value = name+"---"+email+"---"+phoneHome+"---"+phoneOffice+"---"+image+"---";
                                KeyValueDB db = new KeyValueDB(ContactFormActivity.this);
                                if(existingKey.length()==0){
                                    String key = name + System.currentTimeMillis();
                                    existingKey = key;

                                    db.insertKeyValue(key, value);
                                    Toast.makeText(ContactFormActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();

                                }else{
                                    db.updateValueByKey(existingKey, value);
                                    Toast.makeText(ContactFormActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();

                                }
                                db.close();
                                String[] keys = {"action", "id", "semester", "key", "event"};
                                String[] values = {"backup", "2020160405", "20231", existingKey, value}; //Main ID: 2020160034
                                httpRequest(keys, values);

                                Intent i = new Intent(ContactFormActivity.this, ContactListActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                etPhoneOffice.setError("Invalid Number !");
                                Toast.makeText(ContactFormActivity.this, "Invalid Phone (Office) !", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            etPhoneHome.setError("Invalid Number !");
                            Toast.makeText(ContactFormActivity.this, "Invalid Phone (Home) !", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        etEmail.setError("Invalid Email !");
                        Toast.makeText(ContactFormActivity.this, "Invalid Email !", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            public boolean isValid(String email)
            {
                String validity = "^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$";

                Pattern pat = Pattern.compile(validity);
                if (email == null)
                    return true;
                return pat.matcher(email).matches();
            }

            public boolean isValidMobileNo(String validity)
            {
                return validity.matches("^(?:\\+88|88)?(01[3-9]\\d{8})$");
            }

        });


        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ContactFormActivity.this, ContactListActivity.class);
                startActivity(i);
                finish();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void httpRequest(final String keys[], final String values[]){
        new AsyncTask<Void,Void,String>(){

            @Override
            protected String doInBackground(Void... voids) {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
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
                }
            }
        }.execute();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        // start activity result
        //noinspection deprecation
        startActivityForResult(Intent.createChooser(intent,"Select Image"),100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==100 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectImage(); // when permission is granted call method
        } else
        {
            // when permission is denied
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100 && resultCode==RESULT_OK && data!=null)
        {
            Uri uri=data.getData(); // when result is ok, initialize uri
            new ImagePickerTask(uri).execute();
        }
    }

    //AsyncTask Implementation for Multithreading
    private class ImagePickerTask extends AsyncTask<Void, Void, Bitmap> {
        private final Uri uri;

        public ImagePickerTask(Uri uri) {
            this.uri = uri;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmapImage;
                bitmapImage = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream stream=new ByteArrayOutputStream(); // initialize byte stream
                bitmapImage.compress(Bitmap.CompressFormat.JPEG,100,stream); // compress Bitmap
                byte[] bytes=stream.toByteArray(); // Initialize byte array
                imageString= Base64.encodeToString(bytes, Base64.DEFAULT); // get base64 encoded string
                return bitmapImage;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imgView.setImageBitmap(bitmap);
            }
        }
    }
    private int getImageOrientation(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ExifInterface exifInterface = new ExifInterface(inputStream);

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}