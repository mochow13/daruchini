package com.life.mottakin.daruchiniocr;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import catalano.imaging.FastBitmap;
import catalano.imaging.Filters.WolfJolionThreshold;

/**
 * Created by mottakin on 4/14/18.
 */

// Previously I was doing all these work inside the onClick of SCAN button which caused
// 'skipped frames' error because the UI thread was doing much work
// To avoid this we need to run separate worker threads using runnable() or AsyncTask
// AsyncTask example: https://stackoverflow.com/questions/9671546/asynctask-android-example

// Using Catalano Framework: https://github.com/DiegoCatalano/Catalano-Framework
// Imported the Catalano.Android.Imaging folder as a module and added the source codes of Math, Statistics, Core to the
// module's java folder
// Sample usage: https://www.codeproject.com/Articles/656059/Catalano-Framework

public class imageProcessor extends AsyncTask <Bitmap,Void,Bitmap> {

    // We need to refer the showCapturedImageActivity to show the output image
    // Previously I took the reference of the corresponding imageView but it did not work
    // Now trying to refer the activity itself - still memory issues
    // Weak referencing the parent activity of this class to tackle memory leak
    // Reference: https://medium.com/@ankit.sinhal/avoid-memory-leaks-reference-afd0e9dbb213

    private final WeakReference<showCapturedImageActivity> showActivity;

    // Weak referencing the view objects as well as activity is crucial to avoid memory leaks
    // Reference: (Do not leak Views) https://www.youtube.com/watch?v=BkbHeFHn8JY
    // Also: https://medium.com/google-developer-experts/finally-understanding-how-references-work-in-android-and-java-26a0d9c92f83

    public imageProcessor(showCapturedImageActivity ShowActivity) {
        this.showActivity = new WeakReference<>(ShowActivity);
    }

    private Bitmap scanImageWithCatalano(Bitmap imageToScan) {

        Log.d("i", "reached");

        imageToScan = convertToMutable(imageToScan);

        Log.d("After mutable", "done conversion");

        FastBitmap fb = new FastBitmap(imageToScan);
        fb.indicateGrayscale(true);

        Log.d("done:", "the grayscaled image is converted to fastBitmap");

        WolfJolionThreshold wolf = new WolfJolionThreshold();
        wolf.applyInPlace(fb);

        return fb.toBitmap();
    }

    private Bitmap convertToGrayScale(final Bitmap imageToScan) {

        Log.d("scanImage:","Filtering...");

        Mat inputMat = new Mat(imageToScan.getHeight(), imageToScan.getWidth(), CvType.CV_8U);

        Utils.bitmapToMat(imageToScan,inputMat);
        Imgproc.cvtColor(inputMat,inputMat,Imgproc.COLOR_RGB2GRAY);

        Log.d("scanImage:", "Thresholding...");

        // The blocksize will always have to be odd
        // Reference: https://stackoverflow.com/questions/27268636/assertion-failed-blocksize-2-1-blocksize-1-in-cvadaptivethreshold
        // Imgproc.adaptiveThreshold(inputMat,outputMat,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,15,10);

        Utils.matToBitmap(inputMat,imageToScan);

        return imageToScan;
    }

    // Was facing "Bitmap needs to be mutable" error for FastBitmap in Catalano Framework
    // Reference: https://stackoverflow.com/questions/4349075/bitmapfactory-decoderesource-returns-a-mutable-bitmap-in-android-2-2-and-an-immu/9194259#9194259

    /**
     * Converts a immutable bitmap to a mutable bitmap. This operation doesn't allocates
     * more memory that there is already allocated.
     *
     * @param imgIn - Source image. It will be released, and should not be used more
     * @return a copy of imgIn, but muttable.
     */
    public static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }


    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {

        Bitmap imageToScan = bitmaps[0];
        imageToScan = convertToGrayScale(imageToScan);
        imageToScan = scanImageWithCatalano(imageToScan);

        return imageToScan;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        super.onPostExecute(bitmap);
        final showCapturedImageActivity showImage = showActivity.get();

        Glide.with(showImage)
                .load(bitmap)
                .apply(new RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(showImage.imageView);
    }

    // TODO: Check out the Catalano codes for the thresholding - much memory is used

}
