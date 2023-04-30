package com.example.cameradetection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private int x1, x2, x3, x4, y1, y2, y3, y4;
    Point p1 = null; //upper left; minX && minY
    Point p2 = null; //upper right; maxX && minY
    Point p3 = null; //lower right; maxX && maxY
    Point p4 = null;
    int counter = 0;

    private final String[] PERMISSION = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (!hasPermission(this, PERMISSION)) {
            requestPermissions(PERMISSION, 100);
        }
        setCameraStart();
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

    private void setCameraStart() {
        cameraBridgeViewBase = findViewById(R.id.camera);
        cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            setCameraStart();
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

        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        int screen_width, screen_height;
        Toolbar toolbar = findViewById(R.id.toolbar);
        screen_width = size.x;
        screen_height = size.y - toolbar.getHeight();

        float scaledWidth = height * CameraBridgeViewBase.mScale1;
        float scaledHeight = width * CameraBridgeViewBase.mScale2;
        //todo up done correctly

        y1 = (int) ((int) (scaledWidth - screen_width) / (2 * CameraBridgeViewBase.mScale1));
        x1 = (int) ((int) (scaledHeight - screen_height) / (2 * CameraBridgeViewBase.mScale2));

        if (x1 > 0) {
            x4 = width - x1;
        } else {
            x4 = width;
            x1 = 0;
        }
        if (y1 > 0) {
            y4 = height - y1;
        } else {
            y4 = height;
            y1 = 0;
        }


        int xDistance = x4 - x1;
        int yDistance = y4 - y1;
//
        x2 = (int) (0.20 * xDistance) + x1;
        y2 = (int) (0.21 * yDistance) + y1;
        x3 = (int) (0.70 * xDistance) + x1;
        y3 = (int) (0.79 * yDistance) + y1;
        x4 = (int) (0.90 * xDistance) + x1;
        y4 = yDistance + y1;
//    x2 = (int) (0.115 * xDistance) + x1;
//        y2 = (int) (0.21 * yDistance) + y1;
//        x3 = (int) (0.54 * xDistance) + x1;
//        y3 = (int) (0.79 * yDistance) + y1;
//        x4 = (int) (0.69 * xDistance) + x1;
//        y4 = yDistance + y1;

        Log.d("TAG", "onCameraViewStarted: screenH:" + (width - (2 * x1)) + " screenW:" + (height - (2 * y1)));
        Log.d("TAG", "onCameraViewStarted: x1:" + x1 + " x2:" + x2 + " x3:" + x3 + " x4:" + x4);
        Log.d("TAG", "onCameraViewStarted: y1:" + y1 + " y2:" + y2 + " y3:" + y3 + " y4:" + y4);

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


        Imgproc.rectangle(finalImg, new Point(x1, y1), new Point(x2, y2), new Scalar(255, 0, 0, 1), 5);
        Imgproc.rectangle(finalImg, new Point(x2, y3), new Point(x1, y4), new Scalar(255, 0, 0, 1), 5);
        Imgproc.rectangle(finalImg, new Point(x3, y3), new Point(x4, y4), new Scalar(255, 0, 0, 1), 5);
        Imgproc.rectangle(finalImg, new Point(x4, y1), new Point(x3, y2), new Scalar(255, 0, 0, 1), 5);

        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.findContours(imgCanny, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        boolean rectangle_1 = false, rectangle_2 = false, rectangle_3 = false, rectangle_4 = false;
        p1 = null;
        p2 = null;
        p3 = null;
        p4 = null;
        for (MatOfPoint contour : contours) {

            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.01 * Imgproc.arcLength(curve, true), true);
            int numberVertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(contour);

            if (Math.abs(contourArea) < 400) {
                continue;
            }

            if (numberVertices >= 4 && numberVertices <= 6) {
                Rect r = Imgproc.boundingRect(contour);
//top right
                if (r.x >= x1 && r.x + r.width <= x2) {
                    if (r.y >= y1 && r.y + r.height <= y2) {
                        Imgproc.rectangle(finalImg, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 255, 0, 0), 5);
//                        Imgproc.rectangle(finalImg, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0, 0), 5);
                        float smallx1 = r.x + (r.width) / 2f;
                        float smally1 = r.y + (r.height) / 2f;
                        p1 = new Point(smallx1, smally1);
                        rectangle_1 = true;
                    }
                }
                //bottom left
                if (r.x >= x3 && r.x + r.width <= x4) {
                    if (r.y >= y3 && r.y + r.width <= y4) {
                        Imgproc.rectangle(finalImg, new Point(x3, y3), new Point(x4, y4), new Scalar(0, 255, 0, 0), 5);
//                        Imgproc.rectangle(finalImg, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0, 0), 5);
                        float smallx2 = r.x + (r.width) / 2f;
                        float smally2 = r.y + (r.height) / 2f;
                        p4 = new Point(smallx2, smally2);
                        rectangle_2 = true;
                    }
                }
                //top left
                if (r.x >= x1 && r.x + r.width <= x2) {
                    if (r.y >= y3 && r.y + r.height <= y4) {
                        Imgproc.rectangle(finalImg, new Point(x2, y3), new Point(x1, y4), new Scalar(0, 255, 0, 0), 5);
//                        Imgproc.rectangle(finalImg, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0, 0), 5);
                        float smallx3 = r.x + (r.width) / 2f;
                        float smally3 = r.y + (r.height) / 2f;
                        p3 = new Point(smallx3, smally3);
                        rectangle_3 = true;
                    }
                }
                //bottom right
                if (r.x >= x3 && r.x + r.width <= x4) {
                    if (r.y >= y1 && r.y + r.height <= y2) {
                        Imgproc.rectangle(finalImg, new Point(x3, y2), new Point(x4, y1), new Scalar(0, 255, 0, 0), 5);
//                        Imgproc.rectangle(finalImg, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 255, 0, 0), 5);
                        float smallx4 = r.x + (r.width) / 2f;
                        float smally4 = r.y + (r.height) / 2f;
                        p2 = new Point(smallx4, smally4);
                        rectangle_4 = true;
                    }
                }
            }
        }

        if (rectangle_4 && rectangle_3 && rectangle_2 && rectangle_1 && counter == 0) {
//            MediaActionSound mediaActionSound = new MediaActionSound();
//            mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
            if (p1 != null && p2 != null && p3 != null && p4 != null) {
                counter++;
                editFrame(mrgba2);
            }
        }
        return finalImg;
    }

    private void editFrame(Mat mrgba2) {
        Mat imgWrap = mrgba2.clone();
        float w = imgWrap.width(), h = imgWrap.height();
        Mat src = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst = new Mat(4, 1, CvType.CV_32FC2);

        src.put(0, 0, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
        dst.put(0, 0, 0.0, 0.0, w, 0, 0, h, w, h);
        Mat matrix = Imgproc.getPerspectiveTransform(src, dst);
        Imgproc.warpPerspective(mrgba2, imgWrap, matrix, new Size(w, h));
        Bitmap bitmap1 = Bitmap.createBitmap((int) w, (int) h, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgWrap, bitmap1);
        Matrix matrixs = new Matrix();
        matrixs.postRotate(90);
        Bitmap rotated = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrixs, true);
        showEditedPicture(rotated);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
        }
    }

    private void showEditedPicture(Bitmap rotated) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}