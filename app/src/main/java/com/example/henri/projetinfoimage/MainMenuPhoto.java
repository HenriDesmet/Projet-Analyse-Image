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
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import static android.R.attr.data;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainMenuPhoto extends AppCompatActivity implements View.OnClickListener {
    ImageView imageView;
    Uri selectedImageUri;
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
        System.out.println(v.getId());
        switch(v.getId()) {
            case R.id.btnCamera:
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, CAMERA_REQUEST);
            break;

            case R.id.btnGallery:
                startPhotoLibraryActivity();
                break;

            case R.id.btnAnalyse:
        }
    }


    protected void startPhotoLibraryActivity(){
        Intent photoLibIntent = new Intent();
        photoLibIntent.setType("image/*");
        photoLibIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(photoLibIntent,PHOTO_LIB_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     if (requestCode==CAMERA_REQUEST && resultCode==RESULT_OK) {
            System.out.println(data.getExtras());
            Bundle extras = data.getExtras();
            Bitmap miniature = (Bitmap)extras.get("data");
            imageView.setImageBitmap(miniature);
           /* Log.e("tag", "on passe dans le result");
            System.out.println(intent.getExtras());
            System.out.println(intent.getData());
            selectedImageUri = intent.getData();
            System.out.println(selectedImageUri);
            setImageViewContentFromCamera(selectedImageUri); */
        }
     else if(requestCode==PHOTO_LIB_REQUEST && resultCode==RESULT_OK){
         Uri selectedImageUri = data.getData();
         setImageViewContent(selectedImageUri);
     }


    }

  /*  public void setImageViewContentFromCamera(Uri selectedImageUri) {

        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            try {
                String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                Cursor cur = getContentResolver().query(selectedImageUri, orientationColumn, null, null, null);
                int orientation = -1;
                if (cur != null && cur.moveToFirst()) {
                    orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                Bitmap sourceBitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(selectedImageUri), null, null);
                ExifInterface exif = new ExifInterface(selectedImageUri.getPath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Bitmap adjustedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
                imageView.setImageBitmap(adjustedBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } */
  public void setImageViewContent(Uri selectedImageUri){
      if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
          try {
              Bitmap srcBmp = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(selectedImageUri), null, null);
              //imageView.setImageBitmap(srcBmp);
              if(true){ //todo check if landscape or not trouver comment savoir si landscape ou non
                  Matrix matrix = new Matrix();
                  matrix.postRotate(90);
                  Bitmap rotated = Bitmap.createBitmap(srcBmp, 0, 0, srcBmp.getWidth(), srcBmp.getHeight(),
                          matrix, true);
                  imageView.setImageBitmap(rotated);
              }
              else{
                  imageView.setImageBitmap(srcBmp);
              }

          } catch (FileNotFoundException e) {
              e.printStackTrace();
          }
      }
  }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                   // showDialog("External storage", context,Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }


    /* static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    } */


}
