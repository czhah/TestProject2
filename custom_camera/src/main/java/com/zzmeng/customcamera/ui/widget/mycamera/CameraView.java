package com.zzmeng.customcamera.ui.widget.mycamera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import com.dosmono.customcamera.util.UIUtils;

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
 * 如何点击聚焦
 */
public class CameraView extends RelativeLayout implements ICameraFocusCallback {

    public static final int FLASH_MODE_ON = 0;
    public static final int FLASH_MODE_OFF = FLASH_MODE_ON + 1;
    public static final int FLASH_MODE_AUTO = FLASH_MODE_ON + 2;
    public static final int FLASH_MODE_TORCH = FLASH_MODE_ON + 3;

    private SurfaceView mSurfaceView;
    private ICamera mCameraImp;
    private FocusView mFocusView;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mWidth;
    private int mHeight;
    private SensorManager mSensorManger;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = UIUtils.INSTANCE.getScreenWidth(context);
        mScreenHeight = UIUtils.INSTANCE.getScreenHeight(context);
        mSurfaceView = new SurfaceView(context);
        LayoutParams mSurfaceViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mSurfaceViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        mSurfaceView.setLayoutParams(mSurfaceViewParams);
        final SurfaceHolder holder = mSurfaceView.getHolder();
        //noinspection deprecation
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder h) {
                Log.d("Camera1", "=====surfaceCreated");
                if(mCameraImp != null) {
                    mCameraImp.startCamera(holder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
//                setUpPreview();
                Log.d("Camera1", "=====surfaceChanged w:"+width+", h:"+height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder h) {
                Log.d("Camera1", "=====surfaceDestroyed");
                if(mCameraImp != null) {
                    mCameraImp.stopCamera();
                }
            }
        });
        mCameraImp = new Camera1(context);
        mCameraImp.setFocusCallback(this);

        mFocusView = new FocusView(context, mScreenWidth / 4);
        LayoutParams mFocusViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mFocusView.setVisibility(View.INVISIBLE);

        addView(mSurfaceView, mSurfaceViewParams);
        addView(mFocusView, mFocusViewParams);

        //  传感器
        mSensorManger = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        if(mCameraImp != null) {
            mCameraImp.setPreviewSize(mWidth, mHeight);
        }
        Log.d("Camera1", "==onMeasure width:"+mWidth+", height:"+mHeight);
    }

    public void onResume() {
        Log.d("Camera1", "onResume");
        if(mSensorManger != null) {
            mSensorManger.registerListener(mSensorEventListener, mSensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void onPause() {
        Log.d("Camera1", "onPause");
        if(mSensorManger != null) {
            mSensorManger.unregisterListener(mSensorEventListener);
        }
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor == null) {
                return;
            }

            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }
            float[] values = event.values;
            int angle = CameraUtils.INSTANCE.getSensorAngle(values[0], values[1]);
            Log.d("Camera1", "==SensorEventListener x:"+values[0] + " y:"+values[1]+"  angle:"+angle);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    /**
     * 拍照
     */
    public void takePicture() {
        if(mCameraImp != null) {
            mCameraImp.takePicture(new IPictureCallback() {
                @Override
                public void captureResult(Bitmap bitmap) {

                }
            });
        }
    }

    /**
     * 切换摄像头
     * @return
     */
    public boolean switchCamera() {
        return mCameraImp != null && mCameraImp.switchCameraId();
    }

    @Override
    public void onAutoFocusMoving(boolean start) {
        Log.d("cz", "==onAutoFocusMoving:"+start);
        if(!start) {
            mFocusView.setVisibility(View.INVISIBLE);
        } else {
//            setupFocusViewAnim(100, 180);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mCameraImp != null) {
                    mCameraImp.handleFocus(x, y, 0);
                    setupFocusViewAnim(x, y);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void setupFocusViewAnim(float x, float y) {
        mFocusView.setVisibility(VISIBLE);
        if (x < (float) mFocusView.getWidth() / 2) {
            x = mFocusView.getWidth() / 2;
        }
        if (x > mScreenWidth - (float) mFocusView.getWidth() / 2) {
            x = mScreenWidth - mFocusView.getWidth() / 2;
        }
        if (y < (float) mFocusView.getWidth() / 2) {
            y = mFocusView.getWidth() / 2;
        }

        mFocusView.setX(x - (float) mFocusView.getWidth() / 2);
        mFocusView.setY(y - (float) mFocusView.getHeight() / 2);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFocusView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFocusView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFocusView, "alpha", 1f, 0.3f, 1f, 0.3f, 1f, 0.3f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mCameraImp != null) {
            mCameraImp.destroyCamera();
        }
    }
}
