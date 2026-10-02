package com.example.cuoi_ky;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class DonutChartView extends View {

    private Paint paint;
    private RectF rectF;
    private List<PieSegment> segments = new ArrayList<>();
    private float strokeWidth = 40f;

    public static class PieSegment {
        public float percent;
        public int color;
        public PieSegment(float percent, int color) {
            this.percent = percent;
            this.color = color;
        }
    }

    public DonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);
        rectF = new RectF();
    }

    public void setSegments(List<PieSegment> segments) {
        this.segments = segments;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (segments.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2 - strokeWidth;
        
        rectF.set(width/2 - radius, height/2 - radius, width/2 + radius, height/2 + radius);
        paint.setStrokeWidth(strokeWidth);

        float startAngle = -90;
        for (PieSegment segment : segments) {
            float sweepAngle = segment.percent * 360f / 100f;
            paint.setColor(segment.color);
            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);
            startAngle += sweepAngle;
        }
    }
}
