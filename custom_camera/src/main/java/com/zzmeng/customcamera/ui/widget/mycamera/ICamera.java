package com.zzmeng.customcamera.ui.widget.mycamera;

import android.view.SurfaceHolder;

public interface ICamera {

    void startCamera(SurfaceHolder holder);

    void stopCamera();

    void destroyCamera();

    void takePicture(IPictureCallback callback);

    void setPreviewSize(int width, int height);

    boolean isSupportFrontCamera();

    boolean isSupportBackCamera();

    boolean switchCameraId();

    void handleFocus(float x, float y, int count);

    void setFocusCallback(ICameraFocusCallback callback);

}
