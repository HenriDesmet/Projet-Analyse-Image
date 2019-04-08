package com.example.henri.projetinfoimage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.bitmap;
import static android.R.attr.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cz.msebera.android.httpclient.Header;

public class MainMenuPhoto extends AppCompatActivity implements View.OnClickListener {
    ImageView imageView;
    private static final int PHOTO_LIB_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_photo);

        Button btnCamera = (Button) findViewById(R.id.btnCamera);
        Button btnGallery = (Button) findViewById(R.id.btnGallery);
        Button btnAnalyse = (Button) findViewById(R.id.btnAnalyse);
        imageView = (ImageView) findViewById(R.id.imageView);

        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnAnalyse.setOnClickListener(this);

    }

    public void onClick(View v){
        switch(v.getId()) {
            case R.id.btnCamera:
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, CAMERA_REQUEST);
            break;

            case R.id.btnGallery:
                startPhotoLibraryActivity();
                break;

            case R.id.btnAnalyse:
                search();
                break;
        }
    }


    protected void startPhotoLibraryActivity(){
        Intent photoLibIntent = new Intent();
        photoLibIntent.setType("image/*");
        photoLibIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(photoLibIntent,PHOTO_LIB_REQUEST);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     if (requestCode==CAMERA_REQUEST && resultCode==RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap miniature = (Bitmap)extras.get("data");
            imageView.setImageBitmap(miniature);
        }
     else if(requestCode==PHOTO_LIB_REQUEST && resultCode==RESULT_OK){
         Uri selectedImageUri = data.getData();
         try{
             Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
             Bitmap resizedBitmap = getResizedBitmap(bitmap, 300, 400);
             imageView.setImageBitmap(resizedBitmap);
         }
         catch (FileNotFoundException e){

         }
         catch (IOException e ){

         }
     }


    }
    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }


    private File bitmapToFile(Bitmap bitmap, String name) {
        File imageFile = new File(getApplicationContext().getFilesDir().getPath() + "/" + name + ".jpg");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(MainMenuPhoto.class.getSimpleName(), "Error writing bitmap", e);
        }
        return imageFile;
    }

    private CharSequence ImageToBase64 (Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        CharSequence encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    private void search(){
        final Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        CharSequence base64String = ImageToBase64(bitmap);
        CharSequence newBase64String = "data:image/jpeg;base64,/9j/" + base64String;
        RequestParams param = new RequestParams();
        try {
           // param.put("base64", newBase64String);
          //  System.out.println(newBase64String);
            File file = bitmapToFile(bitmap, "random");
            param.put("file", file, "image/jpg");
           // param.put("base64", newBase64String);

         // } catch (NullPointerException e){
        } catch (FileNotFoundException e){
            Toast.makeText(MainMenuPhoto.this, "Sélectionnez une image ...", Toast.LENGTH_SHORT).show();
            return;
        }

        // POST IMG
        HttpUtils.post("image_research/", param, new JsonHttpResponseHandler() {
            @Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                 Toast.makeText(MainMenuPhoto.this, "No API response ...", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("API_response", "POST response:" + response);
                try {
                    String search_id = (String) new JSONObject(response.toString()).get("location");
                    Log.d("API_response", "POST id de la ressource: " + search_id);

                    // GET IMG
                    HttpUtils.get(search_id, new RequestParams(), new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse){
                            Log.e("msg", "erreur");
                            Toast.makeText(MainMenuPhoto.this, "l'API n'est pas accessible ...", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.e("API_response", "GET response" + response.toString());
                            Intent intent = new Intent(getBaseContext(), ResultsActivity.class);
                            intent.putExtra("response", response.toString());
                            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
                            byte[] byteArray = bStream.toByteArray();
                            intent.putExtra("image",byteArray);
                            startActivity(intent);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }
    private void oldSearch(){
        RequestParams param = new RequestParams();

        try {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            File file = bitmapToFile(bitmap, "random");
            param.put("img", file, "image/jpg");
        } catch (NullPointerException e){
            Toast.makeText(MainMenuPhoto.this, "Sélectionnez une image ...", Toast.LENGTH_SHORT).show();
            return;
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        // POST IMG
        HttpUtils.post("img_searcheslists", param, new JsonHttpResponseHandler() {
            @Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
               // Toast.makeText(MainMenuPhoto.this, "l'API n'est pas accessible ...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), ResultsActivity.class);
                startActivity(intent);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("API_response", "POST response:" + response);
                try {
                    String search_id = (String) new JSONObject(response.toString()).get("id");
                    Log.d("API_response", "POST id de la ressource: " + search_id);

                    // GET IMG
                    HttpUtils.get("img_searcheslists/" + search_id, new RequestParams(), new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse){
                            Log.e("msg", "erreur");
                            Toast.makeText(MainMenuPhoto.this, "l'API n'est pas accessible ...", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.e("API_response", "GET response" + response.toString());
                            Intent intent = new Intent(getBaseContext(), ResultsActivity.class);
                            intent.putExtra("response", response.toString());
                            startActivity(intent);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /* static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    } */


}
