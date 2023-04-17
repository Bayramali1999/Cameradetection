package com.example.cameradetection;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

public class MyCameraView extends JavaCameraView implements Camera.PictureCallback {
    OnPictureTakenListener takePicture;

    public MyCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void takePicture(OnPictureTakenListener listener) {
        mCamera.setPreviewCallback(this);
        mCamera.takePicture(null, null, this);
        takePicture = listener;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        this.disableView();
        Bitmap originalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
        takePicture.onTaken(originalImage);
    }
}


