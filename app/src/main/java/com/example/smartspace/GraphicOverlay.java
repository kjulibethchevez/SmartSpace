package com.example.smartspace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.objects.DetectedObject;

import java.util.List;

public class GraphicOverlay extends View {

    private List<DetectedObject> objects;

    private Paint boxPaint;

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6);
    }

    public void setObjects(List<DetectedObject> objects) {
        this.objects = objects;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (objects == null) return;

        for (DetectedObject object : objects) {
            Rect bounds = object.getBoundingBox();
            canvas.drawRect(bounds, boxPaint);
        }
    }
}