package com.zzmeng.customcamera.ui.widget.camera;

import android.content.Context;
import android.view.*;
import androidx.core.view.ViewCompat;
import com.zzmeng.customcamera.R;
import com.zzmeng.customcamera.ui.widget.camera.base.PreviewImpl;

/**
 * Create by chenzhuang on 2019/3/4 0004 下午 2:50
 */
public class SurfaceViewPreview extends PreviewImpl {

    final SurfaceView mSurfaceView;

    SurfaceViewPreview(Context context, ViewGroup parent) {
        final View view = View.inflate(context, R.layout.surface_view, parent);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        final SurfaceHolder holder = mSurfaceView.getHolder();
        //noinspection deprecation
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder h) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
                setSize(width, height);
                if (!ViewCompat.isInLayout(mSurfaceView)) {
                    dispatchSurfaceChanged();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder h) {
                setSize(0, 0);
            }
        });
    }

    @Override
    public Surface getSurface() {
        return getSurfaceHolder().getSurface();
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceView.getHolder();
    }

    @Override
    public View getView() {
        return mSurfaceView;
    }

    @Override
    public Class getOutputClass() {
        return SurfaceHolder.class;
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
    }

    @Override
    public boolean isReady() {
        return getWidth() != 0 && getHeight() != 0;
    }
}
