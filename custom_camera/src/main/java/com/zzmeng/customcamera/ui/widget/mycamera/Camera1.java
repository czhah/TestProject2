package com.zzmeng.customcamera.ui.widget.mycamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.dosmono.customcamera.util.UIUtils;
import com.zzmeng.customcamera.ui.widget.camera.base.Constants;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.hardware.Camera.*;

public class Camera1 implements ICamera{

    private static final String TAG = "Camera1";

    private Context context;
    private Camera mCamera;
    private CameraInfo mBackCameraInfo;
    private CameraInfo mFrontCameraInfo;
    private int mBackCameraId;
    private int mFrontCameraId;
    private int mCameraId;
    private CameraInfo mCameraInfo;
    private SurfaceHolder mHolder;

    private float mAspectRatio = 16F / 9F;

    private int mDisplayOrientation = 0;
    private int mBackDisplayOrientation = 0;
    private int mFrontDisplayOrientation = 0;

    private int mViewHeight;
    private int mViewWidth;
    private boolean enableShutterSound;
    private int mFlashMode = CameraView.FLASH_MODE_OFF;

    private ICameraFocusCallback mFocusCallback;

    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);

    public Camera1(Context context) {
        this.context = context;
        //  获取相机的摄像头信息
        int numberOfCameras = getNumberOfCameras();
        Log.d(TAG, "numberOfCameras：" + numberOfCameras);
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo cameraInfo = new CameraInfo();
            getCameraInfo(i, cameraInfo);
            Log.d(TAG, "CameraInfo facing:"+cameraInfo.facing + ",  orientation:" + cameraInfo.orientation + ",  canDisableShutterSound:" + cameraInfo.canDisableShutterSound + ",  CameraId:" + i);
            if (i == CameraInfo.CAMERA_FACING_BACK) {
                //  后置摄像头
                mCameraId = i;
                mBackCameraId = i;
                mBackCameraInfo = cameraInfo;
                mBackDisplayOrientation = getCameraDisplayOrientation(mBackCameraInfo);
            } else if (i == CameraInfo.CAMERA_FACING_FRONT) {
                mFrontCameraId = i;
                mFrontCameraInfo = cameraInfo;
                mFrontDisplayOrientation = getCameraDisplayOrientation(mFrontCameraInfo);
            }
        }
    }

    private int getCameraDisplayOrientation(CameraInfo cameraInfo) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @Override
    public void startCamera(SurfaceHolder holder) {
        openCamera();
        setCameraParameters();
        setUpPreview(holder);
    }

    private void openCamera() {
        //  打开摄像头
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if(mCameraId == mBackCameraId) {
                mDisplayOrientation = mBackDisplayOrientation;
                mCamera = Camera.open(mBackCameraId);
                mCameraInfo = mBackCameraInfo;
            } else {
                mDisplayOrientation = mFrontDisplayOrientation;
                mCamera = Camera.open(mFrontCameraId);
                mCameraInfo = mFrontCameraInfo;
            }
        }
    }

    private void setUpPreview(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mHolder = holder;
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
//                mCamera.cancelAutoFocus();
                mCamera.setAutoFocusMoveCallback(new AutoFocusMoveCallback() {
                    @Override
                    public void onAutoFocusMoving(boolean start, Camera camera) {
                        if(mFocusCallback != null) {
                            mFocusCallback.onAutoFocusMoving(start);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "setUpPreview:"+e.toString());
            }
        }
    }

    private void setCameraParameters() {
        int longSide = mViewHeight;
        int shortSide = mViewWidth;
        if (mViewWidth > mViewHeight) {
            longSide = mViewWidth;
            shortSide = mViewHeight;
        }

        Log.w(TAG, "user longSide:" + longSide + ",  shortSide:" + shortSide + ",  mAspectRatio" + mAspectRatio);
        if (mCamera != null) {
            //  设置预览尺寸
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size previewSize : previewSizes) {
                //  控制预览的尺寸和屏幕尺寸一致且，宽高不超过屏幕宽高
                Log.d(TAG, "support previewSize  width:" + previewSize.width + ",  height:" + previewSize.height + ",  " + ((float) previewSize.width / (float) previewSize.height));
                if (previewSize.width <= longSide && previewSize.height <= shortSide && (float) previewSize.width / (float) previewSize.height >= mAspectRatio) {
                    Log.w(TAG, "user previewSize  width:" + previewSize.width + ",  height:" + previewSize.height + ",  " + ((float) previewSize.width / (float) previewSize.height));
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    break;
                }
            }
            //  设置图片尺寸
            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            for (Camera.Size pictureSize : pictureSizes) {
                Log.d(TAG, "support PictureSize  width:" + pictureSize.width + ",  height:" + pictureSize.height + ",  " + ((float) pictureSize.width / (float) pictureSize.height));
                if (pictureSize.width <= longSide && pictureSize.height <= shortSide && (float) pictureSize.width / (float) pictureSize.height >= mAspectRatio) {
                    Log.w(TAG, "user pictureSize  width:" + pictureSize.width + ",  height:" + pictureSize.height + ",  " + ((float) pictureSize.width / (float) pictureSize.height));
                    parameters.setPictureSize(pictureSize.width, pictureSize.height);
                    break;
                }
            }
            if(isSupportPictureFormat(parameters, ImageFormat.JPEG)) {
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.setJpegQuality(100);
            }

            //  处理图片的角度
            parameters.setRotation(calcCameraRotation(mDisplayOrientation));

            //  处理自动聚焦
            if (isSupportAutoFocus(parameters)) {
                Log.d(TAG, "设置自动对焦");
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //  处理拍照声音
            if(mCameraInfo.canDisableShutterSound && !enableShutterSound) {
                Log.d(TAG, "CameraId："+mCameraId+" 关闭拍照声音");
                mCamera.enableShutterSound(false);
            } else {
                Log.d(TAG, "CameraId："+mCameraId+" 打开拍照声音");
                mCamera.enableShutterSound(true);
            }
            //  处理闪光灯(自动、打开、常亮、关闭)
            if(isSupportFlashMode(parameters)) {
                setFlashMode(parameters);
            }
            mCamera.setParameters(parameters);
            //  处理预览的角度
            mCamera.setDisplayOrientation(mDisplayOrientation);
        }
    }

    private boolean isSupportPictureFormat(Camera.Parameters parameters, int format) {
        List<Integer> supportedPictureFormats = parameters.getSupportedPictureFormats();
        for(int supportFormat: supportedPictureFormats) {
            if(supportFormat == format) {
                return true;
            }
        }
        return false;
    }

    private void setFlashMode(Camera.Parameters parameters) {
        if(mFlashMode == CameraView.FLASH_MODE_ON) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        } else if(mFlashMode == CameraView.FLASH_MODE_OFF) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        } else if(mFlashMode == CameraView.FLASH_MODE_AUTO) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        } else if(mFlashMode == CameraView.FLASH_MODE_TORCH) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
    }

    private boolean isSupportFlashMode(Camera.Parameters parameters) {
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if(!supportedFlashModes.isEmpty()) {
            if(mFlashMode == CameraView.FLASH_MODE_ON && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                Log.d(TAG, "CameraId " + mCameraId + " 支持打开闪光灯");
                return true;
            } else if(mFlashMode == CameraView.FLASH_MODE_OFF && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                Log.d(TAG, "CameraId " + mCameraId + " 支持关闭闪光灯");
                return true;
            } else if(mFlashMode == CameraView.FLASH_MODE_AUTO && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                Log.d(TAG, "CameraId " + mCameraId + " 支持闪光灯自动调节");
                return true;
            } else if(mFlashMode == CameraView.FLASH_MODE_TORCH && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                Log.d(TAG, "CameraId " + mCameraId + " 支持闪光灯常亮");
                return true;
            }
        }
        Log.d(TAG, "CameraId " + mCameraId + " 不支持设置闪光灯设置："+mFlashMode);
        return false;
    }

    /**
     * 是否支持自动聚焦
     * @param parameters
     * @return
     */
    private boolean isSupportAutoFocus(Camera.Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        for(String focusMode : supportedFocusModes) {
            if(Camera.Parameters.FOCUS_MODE_AUTO.equals(focusMode)) {
                Log.d(TAG, "CameraId " + mCameraId + " 支持自动聚焦");
                return true;
            }
        }
        Log.d(TAG, "CameraId " + mCameraId + " 不支持自动聚焦");
        return false;
    }

    private int calcCameraRotation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (mCameraInfo.orientation + screenOrientationDegrees) % 360;
        } else {  // back-facing
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            return (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
    }

    /**
     * Test if the supplied orientation is in landscape.
     *
     * @param orientationDegrees Orientation in degrees (0,90,180,270)
     * @return True if in landscape, false if portrait
     */
    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == Constants.LANDSCAPE_90 ||
                orientationDegrees == Constants.LANDSCAPE_270);
    }

    @Override
    public void stopCamera() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }catch (IOException e){
                Log.e(TAG, "stopCamera error:"+e.toString());
            }
        }
    }

    @Override
    public void destroyCamera() {
        this.context = null;
        this.mFocusCallback = null;
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(null);
                mHolder = null;
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }catch (IOException e){
                Log.e(TAG, "destroyCamera error:"+e.toString());
            }
        }
    }

    @Override
    public void takePicture(final IPictureCallback callback) {
        //  处理拍照
        if (mCamera != null) {
            try {
                if (!isPictureCaptureInProgress.getAndSet(true)) {
                    mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            //  拍照的数据
                            isPictureCaptureInProgress.set(false);
                            if(callback != null) {
                                callback.captureResult(BitmapFactory.decodeByteArray(data, 0, data.length));
                            }
                        }
                    });
                }
            }catch (Exception e) {
                Log.e(TAG, "takePicture error:"+e.toString());
            }
        }
    }

    @Override
    public void setPreviewSize(int width, int height) {
        this.mViewWidth = width;
        this.mViewHeight = height;
        this.mAspectRatio = mViewHeight / mViewWidth;
    }

    /**
     * 是否支持前置摄像头
     * @return
     */
    @Override
    public boolean isSupportFrontCamera() {
        return mFrontCameraInfo != null;
    }

    /**
     * 是否支持后置摄像头
     * @return
     */
    @Override
    public boolean isSupportBackCamera() {
        return mBackCameraInfo != null;
    }

    /**
     * 转换摄像头
     */
    @Override
    public boolean switchCameraId() {
        if(mCameraId == mBackCameraId && isSupportFrontCamera()) {
            mCameraId = mFrontCameraId;
            stopCamera();
            startCamera(mHolder);
            return true;
        } else if(mCameraId == mFrontCameraId && isSupportBackCamera()) {
            mCameraId = mBackCameraId;
            stopCamera();
            startCamera(mHolder);
            return true;
        }
        return false;
    }

    @Override
    public void handleFocus(final float x, final float y) {
        if(mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Area> focusAreas = parameters.getFocusAreas();
            if(focusAreas != null && !focusAreas.isEmpty()) {
                Rect focusRect = calculateTapArea(x, y, UIUtils.INSTANCE.getScreenWidth(context), UIUtils.INSTANCE.getScreenHeight(context));
                focusAreas.add(new Camera.Area(focusRect, 800));
                parameters.setFocusAreas(focusAreas);

                mCamera.cancelAutoFocus();

                final String currentFocusMode = parameters.getFocusMode();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(parameters);
                mCamera.autoFocus(new AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if(success) {
                            Camera.Parameters params = camera.getParameters();
                            params.setFocusMode(currentFocusMode);
                            camera.setParameters(params);
                            mCamera.cancelAutoFocus();

                            if(mFocusCallback != null) {
                                mFocusCallback.onAutoFocusMoving(false);
                            }
                        } else {
                            handleFocus(x, y);
                        }
                    }
                });
            } else {
                if(mFocusCallback != null) {
                    mFocusCallback.onAutoFocusMoving(false);
                }
            }
        }
    }

    private static Rect calculateTapArea(float x, float y, int width, int height) {
        int areaSize = 300;
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);
//        Log.i("CJT", "FocusArea centerX = " + centerX + " , centerY = " + centerY);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        return new Rect(Math.round(left), Math.round(top), Math.round(left + areaSize), Math.round(top + areaSize));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    @Override
    public void setFocusCallback(ICameraFocusCallback callback) {
        this.mFocusCallback = callback;
    }
}
