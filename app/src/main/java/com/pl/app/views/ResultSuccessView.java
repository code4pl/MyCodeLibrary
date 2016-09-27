package com.pl.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.pl.app.R;
import com.pl.app.utils.CommonUtil;

/**
 * Created by Alex Pang on 2016/9/26.
 * Custom view to explain a success result of loading or other action
 */

public class ResultSuccessView extends View {
    private Paint paint;
    private float strokeWidth;
    private RectF rectF;
    private int startAngle1 = 45;
    private int sweepAngle;
    private Path path;
    private int strokeColor;
    private float xCoor;

    public ResultSuccessView(Context context) {
        this(context, null);
    }

    public ResultSuccessView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResultSuccessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float default_stroke_width = CommonUtil.dip2px(context, 4);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SuccessView, defStyleAttr, 0);
        strokeWidth = attributes.getDimension(
                R.styleable.SuccessView_stroke_color, default_stroke_width);
        strokeColor = attributes.getColor(
                R.styleable.SuccessView_stroke_color, Color.WHITE);
        initPainters();
    }

    protected void initPainters() {
        rectF = new RectF();
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        rectF.set(strokeWidth / 2f, strokeWidth / 2f,
                MeasureSpec.getSize(widthMeasureSpec) - strokeWidth / 2f,
                MeasureSpec.getSize(heightMeasureSpec) - strokeWidth / 2f);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        float width = rectF.width();
        xCoor = width * 0.28f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startAngle2 = startAngle1+90;
        float startAngle3 = startAngle1+180;
        float startAngle4 = startAngle1+270;
        paint.setColor(strokeColor);
        if (sweepAngle <= 90) {
            canvas.drawArc(rectF, -startAngle1, -sweepAngle, false, paint);
            canvas.drawArc(rectF, -startAngle2, -sweepAngle, false, paint);
            canvas.drawArc(rectF, -startAngle3, -sweepAngle, false, paint);
            canvas.drawArc(rectF, -startAngle4, -sweepAngle, false, paint);
            sweepAngle += 2;
            startAngle1 += 10;
        }
        float width = rectF.width();
        if (sweepAngle > 60) {
            float height = rectF.height();
            path.moveTo(width*0.28f, height*0.52f);
            xCoor += 18;
            float yCoor;
            if (xCoor <= width*0.43f) {
                // 第一段直线公式 y=x+0.24
                yCoor = xCoor + height * 0.24f;
                path.lineTo(xCoor, yCoor);
            } else if (xCoor <= width*0.76f) {
                // 第二段直线公式 y=-0.94x+107.42
                yCoor = -0.94f*xCoor + height*1.07f;
                path.lineTo(width*0.43f, height*0.67f);
                path.lineTo(xCoor, yCoor);
            } else {
                path.lineTo(width*0.43f, height*0.66f);
                path.lineTo(width*0.76f, height*0.36f);
            }
            canvas.drawArc(rectF, 0, 360, false, paint);
            canvas.drawPath(path, paint);
        }

        if (sweepAngle < 90 || xCoor < width*0.8f)
            invalidate();
    }
}
