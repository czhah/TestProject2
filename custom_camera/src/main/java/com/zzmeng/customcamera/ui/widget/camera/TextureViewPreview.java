package com.zzmeng.customcamera.ui.widget.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.view.*;
import com.zzmeng.customcamera.R;
import com.zzmeng.customcamera.ui.widget.camera.base.PreviewImpl;

/**
 * Create by chenzhuang on 2018/4/25 0025 下午 5:30
 */
public class TextureViewPreview extends PreviewImpl {

    private final TextureView mTextureView;

    private int mDisplayOrientation;

    TextureViewPreview(Context context, ViewGroup parent) {
        final View view = View.inflate(context, R.layout.view_textureview, parent);
        mTextureView = (TextureView) view.findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setSize(width, height);
                configureTransform();
                dispatchSurfaceChanged();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                setSize(width, height);
                configureTransform();
                dispatchSurfaceChanged();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                setSize(0, 0);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    focusOnTouch((int) event.getX(), (int) event.getY());
                return false;
            }
        });
    }

    private void focusOnTouch(int x, int y) {
        Rect rect = new Rect(x - 100, y - 100, x + 100, y + 100);
        int left = rect.left * 2000 / mTextureView.getWidth() - 1000;
        int top = rect.top * 2000 / mTextureView.getHeight() - 1000;
        int right = rect.right * 2000 / mTextureView.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / mTextureView.getHeight() - 1000;
        // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        focusOnRect(new Rect(left, top, right, bottom));
    }


    // This method is called only from Camera2.
    @TargetApi(15)
    @Override
    public void setBufferSize(int width, int height) {
        mTextureView.getSurfaceTexture().setDefaultBufferSize(width, height);
    }

    @Override
    public Surface getSurface() {
        return new Surface(mTextureView.getSurfaceTexture());
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mTextureView.getSurfaceTexture();
    }

    @Override
    public View getView() {
        return mTextureView;
    }

    @Override
    public Class getOutputClass() {
        return SurfaceTexture.class;
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        configureTransform();
    }

    @Override
    public boolean isReady() {
        return mTextureView.getSurfaceTexture() != null;
    }

    /**
     * Configures the transform matrix for TextureView based on {@link #mDisplayOrientation} and
     * the surface size.
     */
    public void configureTransform() {
        Matrix matrix = new Matrix();
        if (mDisplayOrientation % 180 == 90) {
            final int width = getWidth();
            final int height = getHeight();
            // Rotate the camera preview when the screen is landscape.
            matrix.setPolyToPoly(
                    new float[]{
                            0.f, 0.f, // top left
                            width, 0.f, // top right
                            0.f, height, // bottom left
                            width, height, // bottom right
                    }, 0,
                    mDisplayOrientation == 90 ?
                            // Clockwise
                            new float[]{
                                    0.f, height, // top left
                                    0.f, 0.f, // top right
                                    width, height, // bottom left
                                    width, 0.f, // bottom right
                            } : // mDisplayOrientation == 270
                            // Counter-clockwise
                            new float[]{
                                    width, 0.f, // top left
                                    width, height, // top right
                                    0.f, 0.f, // bottom left
                                    0.f, height, // bottom right
                            }, 0,
                    4);
        } else if (mDisplayOrientation == 180) {
            matrix.postRotate(180, getWidth() / 2, getHeight() / 2);
        }
        mTextureView.setTransform(matrix);
    }
}
