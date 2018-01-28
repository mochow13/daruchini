package com.life.mottakin.daruchiniocr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

import static java.lang.Thread.sleep;

public class daruchiniMainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE=1;
    static final int REQUEST_EXTERNAL_STORAGE_RESULT=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daruchini_main);
        takePhoto();
    }

    public void takePhoto() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            doOperation();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Would you allow Daruchini to user external storage?",
                            Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE_RESULT);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_EXTERNAL_STORAGE_RESULT) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doOperation();
            } else {
                Toast.makeText(this,
                        "External storage permission is denied - cannot save images locally.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    private void doOperation() {

        ImageButton cameraButton=findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = getFileInfo();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(cameraIntent,REQUEST_IMAGE_CAPTURE);
            }
        });

        ImageButton galleryButton=findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Image captured and saved, now show the image in a different activity
            Intent showPhotoIntent = new Intent(daruchiniMainActivity.this,showCapturedImageActivity.class);
            showPhotoIntent.putExtra("imageFilename","test.png"); // sending file name
            daruchiniMainActivity.this.startActivity(showPhotoIntent);
        }

    }

    private File getFileInfo() {

        String root=Environment.getExternalStorageDirectory().toString();
        File daruchiniDirectory=new File(root+"/daruchini-ocr");

        if(!daruchiniDirectory.exists()) daruchiniDirectory.mkdirs();

        File image = new File(daruchiniDirectory,"test.png");
        return image;
    }
}
