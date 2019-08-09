package com.zzmeng.customcamera.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.util.AndroidException;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.dosmono.customcamera.util.BitmapUtils;
import com.dosmono.customcamera.util.UIUtils;
import com.zzmeng.customcamera.R;
import com.zzmeng.customcamera.ui.widget.camera.CameraView;
import com.zzmeng.customcamera.ui.widget.camera.base.AspectRatio;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class CustomCameraActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PARAMS1 = "camera_path"; //  图片地址
    public static final String PARAMS2 = "camera_crop"; //  是否剪裁

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.mipmap.ic_camera_flash_off,
            R.mipmap.ic_camera_flash_on,
    };

    private CameraView mCameraView;
    private ImageView mIvFlash;
    private ImageView mIvCamera;

    private Context mContext;
    private Handler mBackgroundHandler;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mCurrentFlash;

    private String path;
    private boolean isCut;

    private MyHandler mUiHandler = new MyHandler(this);

    static class MyHandler extends Handler {
        WeakReference<CustomCameraActivity> weakReference;

        public MyHandler(CustomCameraActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (weakReference == null || weakReference.get() == null) {
                return;
            }
            CustomCameraActivity activity = weakReference.get();
            Bundle data = msg.getData();
            if (data != null && data.getBoolean("PATH", false)) {
                activity.showPreview();
            } else {
//                activity.showMessage("识别失败，请重试!");
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);

        mContext = this;
        mScreenWidth = UIUtils.INSTANCE.getScreenWidth(this);
        mScreenHeight = UIUtils.INSTANCE.getScreenHeight(this);

        mCameraView = findViewById(R.id.main_cameraView);
        if (mCameraView != null) {
            int gcd = UIUtils.INSTANCE.mGCD(mScreenHeight, mScreenWidth);
            mCameraView.addCallback(mCallback);
            mCameraView.setAspectRatio(AspectRatio.of(mScreenHeight / gcd, mScreenWidth / gcd));
        }

        mIvFlash = findViewById(R.id.main_iv_flash);
        mIvCamera = findViewById(R.id.main_iv_camera);

        mIvFlash.setOnClickListener(this);
        mIvCamera.setOnClickListener(this);

        findViewById(R.id.main_iv_take).setOnClickListener(this);
        findViewById(R.id.main_iv_finish).setOnClickListener(this);

        initData(savedInstanceState);
    }


    public void initData(Bundle bundle) {
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra(PARAMS1);
            isCut = intent.getBooleanExtra(PARAMS2, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraView != null) {
            mCameraView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.stop();
    }

    /**
     * 显示预览
     */
    private void showPreview() {
        Disposable disposable = Observable.just(true).delay(3, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                Intent intent = new Intent(CustomCameraActivity.this, CustomPreViewActivity.class);
                intent.putExtra(CustomPreViewActivity.PARAMS1, path);
                intent.putExtra(CustomPreViewActivity.PARAMS2, isCut);
                startActivityForResult(intent, 0);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            //  选择成功，返回
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.main_iv_flash) {
            //  开启/关闭闪光灯
            if (mCameraView != null) {
                mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                mIvFlash.setImageDrawable(ContextCompat.getDrawable(mContext, FLASH_ICONS[mCurrentFlash]));
                mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
            }
        } else if (id == R.id.main_iv_camera) {
            //  前后置摄像头
            if (mCameraView != null) {
                int facing = mCameraView.getFacing();
                mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                        CameraView.FACING_BACK : CameraView.FACING_FRONT);
            }
        } else if (id == R.id.main_iv_take) {
            //  拍照
            if (mCameraView != null) {
                mCameraView.takePicture();
//                mCameraView.stop();
            }
        } else if (id == R.id.main_iv_finish) {
            //  退出
            finish();
        }
    }

    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            getBackgroundHandler().post(new ImageSaver(data, mUiHandler, mScreenWidth, mScreenHeight, path));
        }
    };

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private static class ImageSaver implements Runnable {

        private byte[] bytes;
        private final Handler mUiHandler;
        private final int mWidth;
        private final int mHeight;
        private String savePath;

        ImageSaver(byte[] data, Handler handler, int width, int height, String path) {
            bytes = data;
            mUiHandler = handler;
            mWidth = width;
            mHeight = height;
            savePath = path;
        }

        @Override
        public void run() {
            Message msg = Message.obtain();
            //  写入文件中
            Bitmap bitmap = BitmapUtils.INSTANCE.getBitmapFromBytes(bytes, mWidth, mHeight);
            bytes = null;
            if (bitmap != null) {
                if (BitmapUtils.INSTANCE.bitmap2Path(bitmap, savePath)) {
                    Bundle data = new Bundle();
                    data.putBoolean("PATH", true);
                    msg.setData(data);
                }
                bitmap.recycle();
                bitmap = null;
            }
            mUiHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }
}
