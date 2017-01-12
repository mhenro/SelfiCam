package ru.mhenro.selficam;

/**
 * Created by mhenr on 14.11.2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context context;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        this.context = context;

        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        /* get maximum picture size available */
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
        for (int i = 0; i < sizes.size(); i++) {
            if (sizes.get(i).width > size.width)
                size = sizes.get(i);
        }
        params.setPictureSize(size.width, size.height);

        /* checking camera's features */
        boolean hasFlash = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (hasFlash && MainActivity.camType != MainActivity.CAM_TYPE.CAM_TYPE_FRONT) {
            switch (MainActivity.flashMode) {
                case FLASH_MODE_AUTO:
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    break;
                case FLASH_MODE_ON:
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    break;
                case FLASH_MODE_OFF:
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    break;
            }
        }
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.setExposureCompensation(0);
        params.setPictureFormat(ImageFormat.JPEG);
        params.setJpegQuality(100);
        params.setRotation(90);
        camera.setParameters(params);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            Log.d(TAG, "surfaceCreated");
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        setCameraDisplayOrientation(0);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void setCameraDisplayOrientation(int cameraId) {
        /* get rotation of the screen */
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
               // Log.d(TAG, "ROTATION_0");
                degrees = 0;
                break;
            case Surface.ROTATION_90:
               // Log.d(TAG, "ROTATION_90");
                degrees = 90;
                break;
            case Surface.ROTATION_180:
              //  Log.d(TAG, "ROTATION_180");
                degrees = 180;
                break;
            case Surface.ROTATION_270:
              //  Log.d(TAG, "ROTATION_270");
                degrees = 270;
                break;
        }

        int result = 0;

        //get camera info
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        /* back camera */
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else {
            /* передняя камера */
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        }
        try {
            result = result % 360;
            mCamera.setDisplayOrientation(result);
        } catch (Exception e) {
            Log.d(TAG, "Set display orientation failed: " + e.getMessage());
        }
    }
}