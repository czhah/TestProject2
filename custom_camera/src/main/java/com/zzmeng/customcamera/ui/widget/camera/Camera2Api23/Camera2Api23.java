package com.zzmeng.customcamera.ui.widget.camera.Camera2Api23;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.params.StreamConfigurationMap;
import com.zzmeng.customcamera.ui.widget.camera.api21.Camera2;
import com.zzmeng.customcamera.ui.widget.camera.base.PreviewImpl;
import com.zzmeng.customcamera.ui.widget.camera.base.Size;
import com.zzmeng.customcamera.ui.widget.camera.base.SizeMap;

/**
 * Create by chenzhuang on 2018/4/25 0025 下午 5:25
 */
public class Camera2Api23 extends Camera2 {

    public Camera2Api23(Callback callback, PreviewImpl preview, Context context) {
        super(callback, preview, context);
    }

    @Override
    protected void collectPictureSizes(SizeMap sizes, StreamConfigurationMap map) {
        // Try to get hi-res output sizes
        android.util.Size[] outputSizes = map.getHighResolutionOutputSizes(ImageFormat.JPEG);
        if (outputSizes != null) {
            for (android.util.Size size : map.getHighResolutionOutputSizes(ImageFormat.JPEG)) {
                sizes.add(new Size(size.getWidth(), size.getHeight()));
            }
        }
        if (sizes.isEmpty()) {
            super.collectPictureSizes(sizes, map);
        }
    }

}
