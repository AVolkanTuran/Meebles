package edu.fandm.volkanwill.meebles;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class PopulationPlotView extends View {

    private Paint plotPaint;
    private Paint dotPaint;
    private Path plotPath;

    private float dotX = 0f;
    private float dotY = 0f;

    private double growthRate = 1.0;
    private int maxPoints = 100;

    public PopulationPlotView(Context context) {
        super(context);
        init();
    }

    public PopulationPlotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        plotPaint = new Paint();
        plotPaint.setColor(Color.YELLOW);
        plotPaint.setStrokeWidth(5f);
        plotPaint.setStyle(Paint.Style.STROKE);
        plotPaint.setAntiAlias(true);

        dotPaint = new Paint();
        dotPaint.setColor(Color.CYAN);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        plotPath = new Path();
    }

    public void setGrowthRate(double rate){
        growthRate = rate;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float height = getHeight() - getPaddingTop() - getPaddingBottom();

        plotPath.reset();

        // Generate exponential curve
        double maxValue = Math.exp(growthRate * maxPoints * 0.05); // maximum y-value for normalization
        for(int i=0; i<=maxPoints; i++){
            float x = getPaddingLeft() + width * i / (float)maxPoints;

            // exponential: y = exp(growthRate * t)
            double yVal = Math.exp(growthRate * i * 0.05);
            float y = getPaddingTop() + height * (1f - (float)(yVal / maxValue));

            if(i==0) plotPath.moveTo(x,y);
            else plotPath.lineTo(x,y);
        }

        // Draw the curve
        canvas.drawPath(plotPath, plotPaint);

        // Draw moving dot
        canvas.drawCircle(dotX, dotY, 12f, dotPaint);
    }

    public void startDotAnimation(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "dotProgress", 0f, 1f);
        animator.setDuration(4000); // 4 seconds to move along plot
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.start();
    }

    public void setDotProgress(float progress){
        int index = (int)(progress * maxPoints);
        if(index > maxPoints) index = maxPoints;

        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float height = getHeight() - getPaddingTop() - getPaddingBottom();

        double maxValue = Math.exp(growthRate * maxPoints * 0.05);
        double yVal = Math.exp(growthRate * index * 0.05);
        dotX = getPaddingLeft() + width * index / (float)maxPoints;
        dotY = getPaddingTop() + height * (1f - (float)(yVal / maxValue));

        invalidate();
    }
}