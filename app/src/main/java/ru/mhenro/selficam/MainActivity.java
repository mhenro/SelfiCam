package ru.mhenro.selficam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {

    public static boolean IS_TEST = false;


    final String TAG = MainActivity.class.getName();
    private Camera mCamera;
    private CameraPreview mPreview;

    private Button btnCamNumber;
    private Button btnTake;
    private Button btnFlash;

  //  private InterstitialAd admob;

    public enum CAM_TYPE {
        CAM_TYPE_BACK,
        CAM_TYPE_FRONT
    }
    static CAM_TYPE camType = CAM_TYPE.CAM_TYPE_BACK;

    public enum FLASH_MODE {
        FLASH_MODE_ON,
        FLASH_MODE_OFF,
        FLASH_MODE_AUTO
    }
    static FLASH_MODE flashMode = FLASH_MODE.FLASH_MODE_AUTO;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Toast toast = Toast.makeText(getApplicationContext(), "Error creating media file, check storage permissions", Toast.LENGTH_LONG);
                toast.show();
                Log.d(TAG, "Error creating media file, check storage permissions: "/* +
                        e.getMessage()*/);
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                /* set image for available in the gallery */
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(pictureFile);
                mediaScanIntent.setData(contentUri);
                getApplicationContext().sendBroadcast(mediaScanIntent);

                Toast toast = Toast.makeText(getApplicationContext(), "Image has been saved successfully!", Toast.LENGTH_LONG);
                toast.show();
            } catch (FileNotFoundException e) {
                Toast toast = Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_LONG);
                toast.show();
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Toast toast = Toast.makeText(getApplicationContext(), "Error accessing file", Toast.LENGTH_LONG);
                toast.show();
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    private void loadAD() {
        AdRequest adRequest = null;
        if (IS_TEST) {
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("samsung-galaxy_nexus-014E280D10015012")
                    .addTestDevice("6905290230CFC28C53B6DF5A84FE269F")
                    .build();
        } else {
            adRequest = new AdRequest.Builder().build();
        }
        if (adRequest != null) {
       //     admob.loadAd(adRequest);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        /* create admob fullscreen banner */
        //admob = new InterstitialAd(this);
        //admob.setAdUnitId("ca-app-pub-4322047714383216/1904994886");
        //admob.setAdListener(new AdListener() {
        //    @Override
        //    public void onAdClosed() {
        //      super.onAdClosed();
        //        loadAD();
        //    }
        //});
        //loadAD();
/*
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                //Toast toast = Toast.makeText(getApplicationContext(), "seconds remaining: " + millisUntilFinished / 1000, Toast.LENGTH_SHORT);
                //toast.show();
            }

            public void onFinish() {
         //       Log.d("TIMER", "finish...");
                if (admob.isLoaded()) {
            //      Log.d("TIMER", "loaded!");
                    admob.show();
                }
                this.start();
            }
        }.start();
*/


        /* create an instance of Camera */
        mCamera = getCameraInstance();

        /* create our Preview view and set it as the content of our activity */
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        /* init buttons */
        btnCamNumber = (Button)findViewById(R.id.btnCamNumber);
        btnTake = (Button)findViewById(R.id.btnTake);
        btnFlash = (Button)findViewById(R.id.btnFlash);

        btnCamNumber.setText(R.string.btnCamNumber_back);
        btnFlash.setText(R.string.btnFlash_auto);

        final Context context = this;

        btnCamNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(camType) {
                    case CAM_TYPE_BACK:
                        camType = CAM_TYPE.CAM_TYPE_FRONT;
                        btnCamNumber.setText(R.string.btnCamNumber_front);
                        break;
                    case CAM_TYPE_FRONT:
                        camType = CAM_TYPE.CAM_TYPE_BACK;
                        btnCamNumber.setText(R.string.btnCamNumber_back);
                        break;
                }
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
                mPreview = null;

                /* create an instance of Camera */
                mCamera = getCameraInstance();

                /* create our Preview view and set it as the content of our activity */
                mPreview = new CameraPreview(context, mCamera);
                FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
                preview.removeAllViews();
                preview.addView(mPreview);
            }
        });

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onKeyDown(0, null);
            }
        });

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(flashMode) {
                    case FLASH_MODE_AUTO:
                        flashMode = FLASH_MODE.FLASH_MODE_ON;
                        btnFlash.setText(R.string.btnFlash_on);
                        break;
                    case FLASH_MODE_ON:
                        flashMode = FLASH_MODE.FLASH_MODE_OFF;
                        btnFlash.setText(R.string.btnFlash_off);
                        break;
                    case FLASH_MODE_OFF:
                        flashMode = FLASH_MODE.FLASH_MODE_AUTO;
                        btnFlash.setText(R.string.btnFlash_auto);
                        break;
                }

                Camera.Parameters params = mCamera.getParameters();
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
                mCamera.setParameters(params);
            }
        });
    }

    /* a safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            switch(camType) {
                case CAM_TYPE_BACK:
                    c = Camera.open(0);
                    break;
                case CAM_TYPE_FRONT:
                    c = Camera.open(1);
                    break;
            }
        }
        catch (Exception e){
            Log.e("Error:", "Can't open the camera: " + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    /* create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SelfiCam");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d("SelfiCam", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }

        switch(camType) {
            case CAM_TYPE_BACK:
                btnCamNumber.setText(R.string.btnCamNumber_back);
                break;
            case CAM_TYPE_FRONT:
                btnCamNumber.setText(R.string.btnCamNumber_front);
                break;
        }

        switch(flashMode) {
            case FLASH_MODE_AUTO:
                btnFlash.setText(R.string.btnFlash_auto);
                break;
            case FLASH_MODE_ON:
                btnFlash.setText(R.string.btnFlash_on);
                break;
            case FLASH_MODE_OFF:
                btnFlash.setText(R.string.btnFlash_off);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return false;
        }

        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
            //    Log.d(TAG, "try to focus");
              //  if (success) {
              //      Log.d(TAG, "focused successfully!");
                    mCamera.takePicture(null, null, mPicture);
                    mCamera.startPreview();
          //      }
            }
        });

        return false;
    }
}
