package com.zzmeng.customcamera.ui.widget.camera2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import androidx.core.content.ContextCompat;
import com.dosmono.customcamera.util.UIUtils;
import com.zzmeng.customcamera.R;
import com.zzmeng.customcamera.ui.widget.camera.base.Constants;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 如何注册相机相关的权限
 * 如何配置相机特性要求
 * 如何获取摄像头的个数
 * 如何开启摄像头
 * 如何关闭摄像头
 * <p>
 * 如何获取相机支持的参数
 * 如何配置预览尺寸
 * 如何配置预览的 Surface
 * 如何开启和关闭预览
 * 设备方向的概念
 * 局部坐标系的概念
 * 屏幕方向的概念
 * 摄像头传感器方向的概念
 * 如何矫正预览画面的方向
 * 如何适配预览画面的比例
 * 如何获取预览数据
 * 如何切换前后置摄像头
 */
public class CameraView extends RelativeLayout {


    private Camera.CameraInfo backCameraInfo;
    private Camera.CameraInfo frontCameraInfo;
    private int backCameraId;
    private int frontCameraId;
    private int mCameraId;
    private Camera.CameraInfo mCameraInfo;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private int mScreenHeight;
    private int mScreenWidth;
    private float mAspectRatio = 16F / 9F;
    private int mDisplayOrientation = 0;
    private int mBackDisplayOrientation = 0;
    private int mFrontDisplayOrientation = 0;
    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = LayoutInflater.from(context).inflate(R.layout.surface_view, this);
        mSurfaceView = view.findViewById(R.id.surface_view);
        final SurfaceHolder holder = mSurfaceView.getHolder();
        //noinspection deprecation
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder h) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
                setUpPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder h) {

            }
        });

        initCamera();
    }

    private void initCamera() {
        //  获取相机的摄像头信息
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.d("cz", "numberOfCameras：" + numberOfCameras);
        for (int i = 0; i < numberOfCameras; i++) {

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (i == Camera.CameraInfo.CAMERA_FACING_BACK) {
                //  后置摄像头
                backCameraId = i;
                backCameraInfo = cameraInfo;
                mBackDisplayOrientation = getCameraDisplayOrientation(backCameraInfo);
                Log.d("cz", "backCameraInfo  facing:" + backCameraInfo.facing + ",  orientation:" + backCameraInfo.orientation + ",  canDisableShutterSound:" + backCameraInfo.canDisableShutterSound + ",  backCameraId:" + backCameraId);
            } else if (i == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontCameraId = i;
                frontCameraInfo = cameraInfo;
                mFrontDisplayOrientation = getCameraDisplayOrientation(frontCameraInfo);
                Log.d("cz", "frontCameraInfo  facing:" + frontCameraInfo.facing + ",  orientation:" + frontCameraInfo.orientation + ",  canDisableShutterSound:" + frontCameraInfo.canDisableShutterSound + ",  frontCameraId:" + frontCameraId);
            }
        }

        mScreenHeight = UIUtils.INSTANCE.getScreenHeight(getContext());
        mScreenWidth = UIUtils.INSTANCE.getScreenWidth(getContext());
        Log.d("cz", "mScreenWidth:" + mScreenWidth + ",  mScreenHeight:" + mScreenHeight + ", mAspectRatio:" + mAspectRatio);
    }

    public void start() {
        openCamera();
        setCameraParameters();
        setUpPreview();
    }

    private void setUpPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setCameraParameters() {
        int longSide = mScreenHeight;
        int shortSide = mScreenWidth;
        if (mScreenWidth > mScreenHeight) {
            longSide = mScreenWidth;
            shortSide = mScreenHeight;
        }
        if (mCamera != null) {
            //  设置预览尺寸
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size previewSize : previewSizes) {
                //  控制预览的尺寸和屏幕尺寸一致且，宽高不超过屏幕宽高
                Log.d("cz", "previewSize  width:" + previewSize.width + ",  height:" + previewSize.height + ",  " + ((float) previewSize.width / (float) previewSize.height));
                if (previewSize.width <= longSide && previewSize.height <= shortSide && (float) previewSize.width / (float) previewSize.height == mAspectRatio) {
                    Log.w("cz", "previewSize  width:" + previewSize.width + ",  height:" + previewSize.height + ",  " + ((float) previewSize.width / (float) previewSize.height));
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    break;
                }
            }
            //  设置图片尺寸
            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            for (Camera.Size pictureSize : pictureSizes) {
                Log.d("cz", "pictureSize  width:" + pictureSize.width + ",  height:" + pictureSize.height + ",  " + ((float) pictureSize.width / (float) pictureSize.height));
                if (pictureSize.width <= longSide && pictureSize.height <= shortSide && (float) pictureSize.width / (float) pictureSize.height == mAspectRatio) {
                    Log.w("cz", "pictureSize  width:" + pictureSize.width + ",  height:" + pictureSize.height + ",  " + ((float) pictureSize.width / (float) pictureSize.height));
                    parameters.setPictureSize(pictureSize.width, pictureSize.height);
                    break;
                }
            }


            //  处理预览的角度
            mCamera.setDisplayOrientation(mDisplayOrientation);

            //  处理自动聚焦


            //  处理闪光灯


            mCamera.setParameters(parameters);
            //  处理图片的角度
            parameters.setRotation(calcCameraRotation(mDisplayOrientation));
        }
    }

    public void stop() {
        stopPreview();
        releaseCamera();
    }

    private void openCamera() {
        releaseCamera();
        //  前置摄像头
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (hasBackFacing()) {
                mDisplayOrientation = mBackDisplayOrientation;
                mCamera = Camera.open(backCameraId);
                mCameraId = backCameraId;
                mCameraInfo = backCameraInfo;
            } else if (hasFrontFacing()) {
                mDisplayOrientation = mFrontDisplayOrientation;
                mCamera = Camera.open(frontCameraId);
                mCameraId = frontCameraId;
                mCameraInfo = frontCameraInfo;
            } else {
                throw new RuntimeException("没有摄像头");
            }
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void takePicture() {
        //  处理拍照
        if (mCamera != null) {
            if (isPictureCaptureInProgress.getAndSet(true)) {
                mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        //  拍照的数据
                        isPictureCaptureInProgress.set(false);

                    }
                });
            }
        }
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

    private int getCameraDisplayOrientation(Camera.CameraInfo cameraInfo) {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
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
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private boolean hasBackFacing() {
        return backCameraInfo != null;
    }

    private boolean hasFrontFacing() {
        return frontCameraInfo != null;
    }
}
