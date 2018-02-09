package com.life.mottakin.daruchiniocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

            final Bitmap imageToShow = BitmapFactory.decodeFile(file.getAbsolutePath());
            System.out.println(imageToShow.getByteCount());

            // Ba-dum-tsss
            imageView.setImageBitmap(imageToShow);

//            Button sendButton = findViewById(R.id.sendButton);
//            sendButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    // Reference: https://stackoverflow.com/questions/20322528/uploading-images-to-server-android
//                    // HttpClient mentioned in the link is removed from Android 6.0 onwards
//                    // The alternate is HttpURLConnection - https://developer.android.com/reference/java/net/HttpURLConnection.html
//                    // But actually, I have to use Volley
//
////                    sendFile(imageToShow);
//                }
//            });
        }
    }

    private void sendFile(final Bitmap imageToShow) {

        // Separate thread is needed to do the data sending/receiving operation
        // Main thread does not allow this
        // Reference: https://stackoverflow.com/a/43813062

        HandlerThread handlerThread = new HandlerThread("URLConnection");
        handlerThread.start();
        Handler mainHandler = new Handler(handlerThread.getLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {

                Log.d("Raha: ","Before IO call");

                URL url = null;
                try {
                    url = new URL("http://10.42.0.246:10000");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    Log.d("Raha", "Connection opened.");

                    try {
                        urlConnection.setDoOutput(true);
                        urlConnection.setChunkedStreamingMode(0);

                        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
//                        writeStream(out);

                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        imageToShow.compress(Bitmap.CompressFormat.PNG, 100, byteStream);

                        byteStream.writeTo(out);

//                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                        readStream(in);

                    } finally {
                        urlConnection.disconnect();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        mainHandler.post(myRunnable);
        finish();
    }
}
