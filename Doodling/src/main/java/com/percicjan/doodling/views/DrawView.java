package com.percicjan.doodling.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by janpercic on 11. 01. 14.
 */

public class DrawView extends View {

    // Drawing path
    private Path drawPath;

    // Drawing and canvas paint
    private Paint drawPaint, canvasPaint;

    // Canvas
    private Canvas drawCanvas;

    // Canvas bitmap
    private Bitmap canvasBitmap;

    public DrawView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }



    /**
     * Get drawing area setup for interaction
     */
    private void setupDrawing(){
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // View given size
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvasBitmap.eraseColor(Color.TRANSPARENT);
        drawCanvas = new Canvas(canvasBitmap);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        // Draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Detect user touch
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }



    /**
     * Set brush color
     */
    public void setBrushColor(String newColor){
        invalidate();
        drawPaint.setColor(Color.parseColor(newColor));
    }



    /**
     * Set brush size
     */
    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        drawPaint.setStrokeWidth(pixelAmount);
    }



    /**
     *  Discard changes on canvas
     */
    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

}