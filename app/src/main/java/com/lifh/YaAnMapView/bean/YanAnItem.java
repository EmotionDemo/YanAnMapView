package com.lifh.YaAnMapView.bean;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * @author lifenghua
 * @description TODO
 * @date 2020/10/09 15:39
 */
public class YanAnItem {
    private Path path;
    private int drawColor;
    private int id;
    private String xianchengName;

    public YanAnItem(Path path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXianchengName() {
        return xianchengName;
    }

    public void setXianchengName(String xianchengName) {
        this.xianchengName = xianchengName;
    }

    /**
     * 绘制地图
     *
     * @param canvas
     * @param paint
     * @param isSelect
     */
    public void draw(Canvas canvas, Paint paint, boolean isSelect) {
        if (isSelect) {
            //画阴影图层
            paint.setStrokeWidth(8);
            paint.setShadowLayer(8, 0, 0, 0xffffff);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.BLUE);
            canvas.drawPath(path, paint);
            //画区域path
            paint.clearShadowLayer();
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(drawColor);
            canvas.drawPath(path, paint);
        } else {
            //画线条
            paint.clearShadowLayer();
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xFFD0E8F4);
            canvas.drawPath(path, paint);
            //画区域
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(drawColor);
            canvas.drawPath(path, paint);
        }

    }


    /**
     * 判断当前点击坐标是否在path范围内
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isTouch(int x, int y) {
        Region region = new Region();
        RectF rect = new RectF();
        path.computeBounds(rect, true);
        region.setPath(path, new Region((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom));
        boolean isContains = region.contains((int) x, (int) y);
        if (isContains) {
            return true;
        }
        return false;
    }

    public void setDrawColor(int drawColor) {
        this.drawColor = drawColor;
    }
}
