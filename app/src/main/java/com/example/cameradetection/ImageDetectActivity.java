package com.example.cameradetection;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ImageDetectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detect);

        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bmp != null) {
            saveImage(getResizedBitmap(bmp, 600, 799));
        }

    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();

        return resizedBitmap;
    }

    private void saveImage(Bitmap bmp) {

        File dir = new File(Environment.DIRECTORY_PICTURES + File.separator + "Avtobaxolash");
        if (!dir.exists()) {
            dir.mkdir();
            Log.d("TAG", "saveImage: mavjud");
        }
        FileOutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + System.currentTimeMillis() + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "Avtobaxolash");
                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//                Log.d("upload_uri", "saveImage: " + contentValues.getoa);
                fos = (FileOutputStream) resolver.openOutputStream(Objects.requireNonNull(uri));
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos);
                ImageView iv = findViewById(R.id.iv2);

                File file= new File(uri.getPath());
//                File compressedFile = FileUtil.from(ImageDetectActivity.this, uri);
//                iv.setImageURI(compressedFile.);

            File d =   new  Compressor().compress(ImageDetectActivity.this,file,  )

            } else {
                File file = new File(dir, "Image_" + System.currentTimeMillis() + ".jpg");
                new SingleMediaScanner(ImageDetectActivity.this, file);

                fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "picture not saved" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}