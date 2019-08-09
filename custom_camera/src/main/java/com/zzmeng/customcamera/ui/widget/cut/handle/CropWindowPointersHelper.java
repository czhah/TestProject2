package com.zzmeng.customcamera.ui.widget.cut.handle;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import com.zzmeng.customcamera.ui.widget.cut.edge.Edge;

/**
 * Create by chenzhuang on 2018/10/11 0011 下午 4:13
 */
public class CropWindowPointersHelper extends CropWindowScaleHelper {

    private static final float ZOOM_FACTOR = 1.4F;

    CropWindowPointersHelper() {
        super(null, null);
    }

    @Override
    void updateCropWindow(@NonNull MotionEvent event, @NonNull PointF touchOffsetOutput, @NonNull RectF imageRect, int cropType) {
        float x = Math.abs(event.getX(1) - event.getX(0));
        float y = Math.abs(event.getY(1) - event.getY(0));
        //  处理缩放
        if (touchOffsetOutput.x != 0 && touchOffsetOutput.y != 0) {
            float offsetX = (x - touchOffsetOutput.x) / 2 * ZOOM_FACTOR;
            float offsetY = (y - touchOffsetOutput.y) / 2 * ZOOM_FACTOR;

            if (cropType == 1) {
                float tan = 0;
                if (offsetX >= 0 && offsetY >= 0) {
                    tan = (float) Math.sqrt((int) offsetX * (int) offsetX + (int) offsetY * (int) offsetY);
                    final float lastLeft = Edge.LEFT.getCoordinate();
                    final float lastRight = Edge.RIGHT.getCoordinate();
                    final float lastTop = Edge.TOP.getCoordinate();
                    final float lastBottom = Edge.BOTTOM.getCoordinate();
                    Edge.LEFT.offset(-tan);
                    Edge.RIGHT.offset(tan);
                    Edge.TOP.offset(-tan);
                    Edge.BOTTOM.offset(tan);

                    if (Edge.LEFT.isOutsideMargin(imageRect) || Edge.RIGHT.isOutsideMargin(imageRect) ||
                            Edge.TOP.isOutsideMargin(imageRect) || Edge.BOTTOM.isOutsideMargin(imageRect)) {
                        Edge.LEFT.initCoordinate(lastLeft);
                        Edge.RIGHT.initCoordinate(lastRight);
                        Edge.TOP.initCoordinate(lastTop);
                        Edge.BOTTOM.initCoordinate(lastBottom);
                    }
                } else if (offsetX <= 0 && offsetY <= 0) {
                    offsetX = Math.abs(offsetX);
                    offsetY = Math.abs(offsetY);
                    tan = -((float) Math.sqrt((int) offsetX * (int) offsetX + (int) offsetY * (int) offsetY)) / 3;

                    Edge.offsetWidth(tan);
                    Edge.offsetHeight(tan);
                }


                return;
            }

            if (offsetX >= 0) {
                Edge.LEFT.offset(-offsetX);
                Edge.RIGHT.offset(offsetX);

                //  判断最大边界
                if (Edge.LEFT.isOutsideMargin(imageRect)) {
                    //重新指定左边的值为初始值
                    Edge.LEFT.initCoordinate(imageRect.left);
                }

                if (Edge.RIGHT.isOutsideMargin(imageRect)) {
                    Edge.RIGHT.initCoordinate(imageRect.right);
                }
            } else {
                Edge.offsetWidth(offsetX);
            }

            if (offsetY >= 0) {
                Edge.TOP.offset(-offsetY);
                Edge.BOTTOM.offset(offsetY);
                if (Edge.TOP.isOutsideMargin(imageRect)) {
                    Edge.TOP.initCoordinate(imageRect.top);
                }
                if (Edge.BOTTOM.isOutsideMargin(imageRect)) {
                    Edge.BOTTOM.initCoordinate(imageRect.bottom);
                }
            } else {
                Edge.offsetHeight(offsetY);
            }
        }

        touchOffsetOutput.x = x;
        touchOffsetOutput.y = y;
    }
}
