package com.life.mottakin.daruchiniocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;

public class showCapturedImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_show_captured_image);

        ImageView imageView = (ImageView) findViewById(R.id.showImage);
        Intent currIntent = getIntent();

        if(currIntent.hasExtra("imageFilename")) {
            String filename = currIntent.getExtras().getString("imageFilename");
            System.out.println(filename);
            String root= Environment.getExternalStorageDirectory().toString()+"/daruchini-ocr/";
            File file = new File(root+filename);
            System.out.println(file.length());
            Bitmap imageToShow = BitmapFactory.decodeFile(file.getAbsolutePath());
            System.out.println(imageToShow.getByteCount());
            // Ba-dum-tsss
            imageView.setImageBitmap(imageToShow);
        }
    }
}
