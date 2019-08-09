package com.zzmeng.customcamera.ui.widget.cut.handle;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import com.zzmeng.customcamera.ui.widget.cut.edge.Edge;


/**
 * 操控裁剪框的辅助类:操控裁剪框的缩放
 */
class CropWindowScaleHelper {


    private Edge mHorizontalEdge;
    private Edge mVerticalEdge;


    CropWindowScaleHelper(Edge horizontalEdge, Edge verticalEdge) {
        mHorizontalEdge = horizontalEdge;
        mVerticalEdge = verticalEdge;
    }


    /**
     * 随着手指的移动而改变裁剪框的大小
     *
     * @param imageRect 用来表示图片边界的矩形
     */
    void updateCropWindow(@NonNull MotionEvent event,
                          @NonNull PointF touchOffsetOutput,
                          @NonNull RectF imageRect, int cropType) {
        float x = event.getX();
        float y = event.getY();
        x += touchOffsetOutput.x;
        y += touchOffsetOutput.y;
        if (mHorizontalEdge != null)
            mHorizontalEdge.updateCoordinate(x, y, imageRect);

        if (mVerticalEdge != null)
            mVerticalEdge.updateCoordinate(x, y, imageRect);
    }


}
