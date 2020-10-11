package com.lifh.YaAnMapView.mview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.PathParser;
import androidx.core.view.GestureDetectorCompat;

import com.lifh.YaAnMapView.R;
import com.lifh.YaAnMapView.bean.YanAnItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author lifenghua
 * @description TODO
 * @date 2020/10/09 15:30
 */
public class YanAnMapView extends View {

    private static final String TAG = YanAnMapView.class.getName();

    private List<YanAnItem> yanAnItems = new ArrayList<>();

    //被点击的区域
    private YanAnItem selectedItem;

    //缩放倍数
    private float scale = 1.0f;

    private Context mContext;

    /**
     * 整个地图的最大矩形边界
     */
    private RectF mMaxRect;

    private Paint mPaint;

    private int[] colors = new int[]{0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1, 0xFFF0E68C};

    private GestureDetectorCompat gestureDetectorCompat;

    /**
     * 添加所点击的县城的省份
     */
    public interface OnXianChengClickListener {
        void onSelectXiancheng(String xianchengName, int xianchengId);
    }

    private OnXianChengClickListener onXianChengClickListener;

    public void setOnXianChengClickListener(OnXianChengClickListener onXianChengClickListener) {
        this.onXianChengClickListener = onXianChengClickListener;
    }


    public YanAnMapView(Context context) {
        super(context);
    }

    public YanAnMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public YanAnMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 获取xml资源并解析dom
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    Thread thread = new Thread() {
        @Override
        public void run() {
            InputStream inputStream = mContext.getResources().openRawResource(R.raw.yananmap);
            //采用Dom解析器解析xml
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(inputStream);
                Element rootelement = doc.getDocumentElement();
                NodeList items = rootelement.getElementsByTagName("path");
                // 以下四个变量用来保存地图四个边界，用于确定缩放比例(适配屏幕)
                float left = -1;
                float right = -1;
                float top = -1;
                float bottom = -1;
                for (int i = 0; i < items.getLength(); i++) {

                    Element element = (Element) items.item(i);
                    String pathData = element.getAttribute("android:pathData");
                    String xianchengName = element.getAttribute("name");
                    int xianchengId = Integer.parseInt(element.getAttribute("id"));
                    Path path = PathParser.createPathFromPathData(pathData);
                    YanAnItem item = new YanAnItem(path);
                    item.setId(xianchengId);
                    item.setXianchengName(xianchengName);
                    Log.d("----id", String.valueOf(xianchengId));
                    Log.d("-----name", xianchengName);

                    RectF rectF = new RectF();
                    // 计算当前path区域的矩形边界
                    path.computeBounds(rectF, true);
                    // 判断边界，最终获得的就是整个地图的最大矩形边界
                    left = left < 0 ? rectF.left : Math.min(left, rectF.left);
                    right = Math.max(right, rectF.right);
                    top = top < 0 ? rectF.top : Math.min(top, rectF.top);
                    bottom = Math.max(bottom, rectF.bottom);

                    yanAnItems.add(item);
                }
                mMaxRect = new RectF(left, top, right, bottom);
                handler.sendEmptyMessage(1);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        mContext = context;
        mPaint = new Paint();
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        gestureDetectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG, "onDown x:" + e.getX() + ";Y:" + e.getY());
                handleTouch(e.getX(), e.getY());
                return true;
            }

        });
        thread.start();
    }

    /**
     * 处理事件
     *
     * @param x
     * @param y
     */
    private void handleTouch(float x, float y) {
        YanAnItem yanAnItem = null;
        if (yanAnItems != null) {
            for (YanAnItem item : yanAnItems) {
                boolean isTouched = item.isTouch((int) (x / scale), (int) (y / scale));
                if (isTouched) {
                    if (onXianChengClickListener != null) {
                         onXianChengClickListener.onSelectXiancheng(item.getXianchengName(), item.getId());
                    }
                    yanAnItem = item;
                    break;
                }
            }
            if (yanAnItem != null) {
                selectedItem = yanAnItem;
                postInvalidate();
            }
        }

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mMaxRect !=null){
            double mapWidth = mMaxRect.width();
            scale = (float) (width /mapWidth);
        }

        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (yanAnItems == null || yanAnItems.size() == 0) {
                return;
            }
            int colorNum = yanAnItems.size();
            int color = 0xFF239BD7;
            //赋予颜色
            for (int i = 0; i < colorNum; i++) {
                int flag = i % 4;
                switch (flag) {
                    case 1:
                        color = colors[0];
                        break;
                    case 2:
                        color = colors[1];
                        break;
                    case 3:
                        color = colors[2];
                        break;
                    default:
                        color = colors[3];
                }
                yanAnItems.get(i).setDrawColor(color);
            }
            postInvalidate();
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        //将画布缩放1.5倍
        canvas.scale(scale, scale);
        if (yanAnItems != null) {
            for (YanAnItem yanAnItem : yanAnItems) {
                if (yanAnItem != selectedItem) {
                    yanAnItem.draw(canvas, mPaint, false);
                }
            }
            if (selectedItem != null) {
                selectedItem.draw(canvas, mPaint, true);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }
}
