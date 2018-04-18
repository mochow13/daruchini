package com.life.mottakin.daruchiniocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class showCapturedImageActivity extends AppCompatActivity {

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch(status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.d("OpenCV:","OpenCV loaded successfully *sigh*");
                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }

            super.onManagerConnected(status);
        }
    };

    // To show image in an efficient way, instead of going through the hassles
    // I am just using Glide - a library recommended by Google
    // Reference: https://github.com/bumptech/glide

    ImageView imageView;
    Button scanButton;
    Bitmap imageToScan;

    final int maxWidth = 1206;
    final int maxHeight = 1504;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We need to check if OpenCV is initiated
        // Reference: https://stackoverflow.com/questions/35090838/no-implementation-found-for-long-org-opencv-core-mat-n-mat-error-using-opencv

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        // Go full screen to show the image
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_show_captured_image);

        imageView = findViewById(R.id.showImage);
        final Intent currIntent = getIntent();

        if(currIntent.hasExtra("imageFilename")) {
            String filename = currIntent.getExtras().getString("imageFilename");
            System.out.println(filename);

            String root = Environment.getExternalStorageDirectory().toString()+"/daruchini-ocr/";
            final File file = new File(root+filename);

            scanButton = findViewById(R.id.scanButton);

            // Without skipMemoryCache and diskCacheStrategy, loads previous image
            // These options are important for Glide's performance

            Glide.with(showCapturedImageActivity.this)
                    .load(Uri.fromFile(file))
                    .apply(new RequestOptions()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(imageView);

            new RescaleImage(this).execute(root+filename);
        }
    }

    private void doScanning() {
        // Sending the activity reference to the imageProcessor
        final imageProcessor ip = new imageProcessor(this);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip.execute(imageToScan);
            }
        });
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

    private class RescaleImage extends AsyncTask <String,Void,Void> {

        WeakReference<showCapturedImageActivity> weakActivityReference;

        RescaleImage(showCapturedImageActivity weakActivityReference) {
            this.weakActivityReference = new WeakReference<>(weakActivityReference);
        }

        @Override
        protected Void doInBackground(String... strings) {

            File file = new File(strings[0]);
            imageToScan = BitmapFactory.decodeFile(file.getAbsolutePath());

            int width = imageToScan.getWidth();
            int height = imageToScan.getHeight();

            float ratioBitmap = (float)width/(float)height;
            float ratioMax = (float)maxWidth/(float)maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;

            if(ratioMax>ratioBitmap) {
                finalWidth = (int)((float)maxHeight*ratioBitmap);
            }
            else {
                finalHeight = (int)((float)maxWidth/ratioBitmap);
            }

            imageToScan = Bitmap.createScaledBitmap(imageToScan,finalWidth,finalHeight,true);

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            super.onPostExecute(params);
            showCapturedImageActivity sc = weakActivityReference.get();
            sc.doScanning();
        }
    }

    // TODO: Show this scanned image with only one button to save, probably in a new activity
    // TODO: Add functionality to save the scanned image
    // TODO: Not sure if I should keep OpenCV if Catalano works - looks like it is working
    // TODO: Need to check and tune parameter for Thresholding - not quite sure how they are working
    // TODO: Try other thresholding techniques
}