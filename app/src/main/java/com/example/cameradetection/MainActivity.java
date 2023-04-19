package com.example.cameradetection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private MyCameraView cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback;
    private Mat mRgba;
    private int cameraWidth, cameraHeight;
    private int x1, x2, x3, x4, y1, y2, y3, y4;
    private final int smallRectangleWidth = 130;
    private final int smallRectangleHeight = 130;
    int smallx1, smally1, smallx2, smally2;

    int counter = 0;

    private final String[] PERMISSION = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Display display = getWindowManager().getDefaultDisplay();
//        android.graphics.Point size = new android.graphics.Point();
//        display.getSize(size);
//
//        int dWidth = size.x;
//        int dHeight = size.y;

//        CameraBridgeViewBase.giveScreenSize(dWidth, dHeight);

        if (!hasPermission(this, PERMISSION)) {
            requestPermissions(PERMISSION, 100);
        }
        cameraBridgeViewBase = findViewById(R.id.camera);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case SUCCESS:
                        cameraBridgeViewBase.enableView();
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //permissioon granted
        }
    }

    private boolean hasPermission(Context context, String[] permission) {

        if (context != null && permission != null) {
            for (String permiss : permission) {
                if (ContextCompat.checkSelfPermission(context, permiss) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(width, height, CvType.CV_8UC4);

        cameraWidth = width;
        cameraHeight = height;
        x1 = 0;
        x2 = cameraWidth;
        x3 = 0;
        x4 = cameraWidth;
        y1 = 0;
        y2 = 0;
        y3 = cameraHeight;
        y4 = cameraHeight;
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat imgCanny = new Mat();
        Mat mrgba2 = new Mat();
        Mat finalImg = new Mat();

        mRgba = inputFrame.rgba();
        mRgba.copyTo(mrgba2);
        mRgba.copyTo(finalImg);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(mRgba, mRgba, new Size(5, 5), 1);
        Imgproc.Canny(mRgba, imgCanny, 10, 70);

        Imgproc.rectangle(finalImg, new Point(x1, y1), new Point(x1 + smallRectangleWidth, y1 + smallRectangleHeight), new Scalar(255, 0, 0, 1), 5);
        Imgproc.rectangle(finalImg, new Point(x2, y2), new Point(x2 - smallRectangleWidth, y2 + smallRectangleHeight), new Scalar(255, 0, 0, 1), 5);
        Imgproc.rectangle(finalImg, new Point(x3, y3), new Point(x3 + smallRectangleWidth, y3 - smallRectangleHeight), new Scalar(255, 0, 0, 1), 5);
        Imgproc.rectangle(finalImg, new Point(x4, y4), new Point(x4 - smallRectangleWidth, y4 - smallRectangleHeight), new Scalar(255, 0, 0, 1), 5);

        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.findContours(imgCanny, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        boolean rectangle_1 = false, rectangle_2 = false, rectangle_3 = false, rectangle_4 = false;
        for (MatOfPoint contour : contours) {

            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.01 * Imgproc.arcLength(curve, true), true);
            int numberVertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(contour);

            if (Math.abs(contourArea) < 600 ) {
                continue;
            }

            if (numberVertices == 4) {
                Rect r = Imgproc.boundingRect(contour);
                int area = (int) r.area();
                if ((r.x >= x1 && r.x + r.width <= smallRectangleHeight) && (r.y >= y1 && r.y + r.height <= smallRectangleWidth)) {
                    Imgproc.rectangle(finalImg, new Point(x1, y1), new Point(smallRectangleWidth, smallRectangleHeight), new Scalar(0, 255, 0, 0), 5);
                    Imgproc.rectangle(finalImg, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0, 0), 5);
                    smallx1 = r.x + (r.width) / 2;
                    smally1 = r.y + (r.height) / 2;

                    rectangle_1 = true;
                }
                if ((r.x >= x2 - smallRectangleHeight && r.x + r.width <= x2) && (r.y >= y2 && r.y + r.width <= y2 + smallRectangleWidth)) {
                    Imgproc.rectangle(finalImg, new Point(x2, y2), new Point(x2 - smallRectangleWidth, y2 + smallRectangleHeight), new Scalar(0, 255, 0, 0), 5);
                    Imgproc.rectangle(finalImg, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0, 0), 5);
                    rectangle_2 = true;
                }
                if ((r.x >= x3 && r.x + r.width <= x3 + smallRectangleWidth) && (r.y >= y3 - smallRectangleWidth && r.y + r.height <= y3)) {
                    Imgproc.rectangle(finalImg, new Point(x3, y3), new Point(smallRectangleWidth, y3 - smallRectangleHeight), new Scalar(0, 255, 0, 0), 5);
                    Imgproc.rectangle(finalImg, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0, 0), 5);
                    rectangle_3 = true;
                }
                if ((r.x >= x4 - smallRectangleHeight && r.x + r.width <= x4) && (r.y >= y4 - smallRectangleWidth && r.y + r.height <= y4)) {
                    Imgproc.rectangle(finalImg, new Point(x4, y4), new Point(x4 - smallRectangleWidth, y4 - smallRectangleHeight), new Scalar(0, 255, 0, 0), 5);
                    Imgproc.rectangle(finalImg, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0, 0), 5);
                    smallx2 = r.x + (r.width) / 2;
                    smally2 = r.y + (r.height) / 2;
                    rectangle_4 = true;
                }
            }
        }

        if (rectangle_4 && rectangle_3 && rectangle_2 && rectangle_1 && counter == 0) {

//            MediaActionSound mediaActionSound = new MediaActionSound();
//            mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
//            Imgproc.rectangle(mrgba2, new Point(smallx1, smally1), new Point(smallx2, smally2), new Scalar(0, 255, 0, 1), 5);

            counter++;
            Bitmap bitmap1 = Bitmap.createBitmap(mrgba2.width(), mrgba2.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mrgba2, bitmap1);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), null, true);
            cropImage(rotatedBitmap, rotatedBitmap);
//
//            try {
//                String filename = "abitmap.png";
//                FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
//                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                stream.close();
//                bitmap1.recycle();
//                Intent in1 = new Intent(this, ImageDetectActivity.class);
//                in1.putExtra("image", filename);
//                startActivity(in1);
//                cameraBridgeViewBase.disableView();
//                cameraBridgeViewBase.setVisibility(GONE);
//                finish();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
        return finalImg;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (cameraBridgeViewBase != null) {
//            cameraBridgeViewBase.disableView();
//            cameraBridgeViewBase.setVisibility(GONE);
//        }
//    }

    public float getRatioX(float cameraWidth, float bitmapWidth) {
        return bitmapWidth / cameraWidth;
    }

    public float getRatioY(float cameraHeight, float bitmapHeight) {
        return bitmapHeight / cameraHeight;
    }

    private void cropImage(Bitmap bitmap, Bitmap rotatedBitmap) {
        float ratioX = getRatioX(cameraWidth, rotatedBitmap.getWidth());
        float ratioY = getRatioY(cameraHeight, rotatedBitmap.getHeight());
        Bitmap cropedimage = Bitmap.createBitmap(bitmap, (int) (smallx1 * ratioX), (int) (smally1 * ratioY), (int) ((smallx2 - smallx1) * ratioX), (int) ((smally2 - smally1) * ratioY), null, true);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotated = Bitmap.createBitmap(cropedimage, 0, 0, cropedimage.getWidth(), cropedimage.getHeight(), matrix, true);

        try {
            String filename = "abitmap.png";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            rotated.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            rotated.recycle();
            Intent in1 = new Intent(this, ImageDetectActivity.class);
            in1.putExtra("image", filename);
            startActivity(in1);
            finish();
//            cameraBridgeViewBase.disableView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

