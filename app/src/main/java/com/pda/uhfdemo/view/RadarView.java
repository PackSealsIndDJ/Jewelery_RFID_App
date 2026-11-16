package com.pda.uhfdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.pda.uhfdemo.R;

public class RadarView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int rssi;
    private int previousRssi;
    private boolean hasReading;
    private boolean hasPreviousReading;
    private float markerRadius = 0.9f;

    public RadarView(Context context) {
        super(context);
        init();
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setContentDescription("RFID proximity radar");
    }

    public void reset() {
        hasReading = false;
        hasPreviousReading = false;
        invalidate();
    }

    public void setRssi(int value) {
        previousRssi = rssi;
        hasPreviousReading = hasReading;
        rssi = value;
        hasReading = true;
        markerRadius = 0.9f - clamp((value + 90f) / 45f, 0f, 1f) * 0.72f;
        invalidate();
    }

    public String getDistanceLabel() {
        if (!hasReading || rssi >= 0) {
            return getContext().getString(R.string.radar_distance_unknown);
        }
        double meters = Math.pow(10d, (-50d - rssi) / 20d);
        meters = Math.max(0.1d, Math.min(30d, meters));
        if (meters >= 10d) {
            return getContext().getString(R.string.radar_distance_meters_rounded, meters);
        }
        return getContext().getString(R.string.radar_distance_meters, meters);
    }

    public String getTrendLabel() {
        if (!hasReading) {
            return getContext().getString(R.string.radar_waiting);
        }
        if (!hasPreviousReading || Math.abs(rssi - previousRssi) < 2) {
            return getContext().getString(R.string.radar_steady);
        }
        return getContext().getString(rssi > previousRssi
                ? R.string.radar_closer : R.string.radar_farther);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.max(0f, Math.min(getWidth(), getHeight()) * 0.43f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.rgb(85, 125, 145));
        for (int ring = 1; ring <= 3; ring++) {
            canvas.drawCircle(cx, cy, radius * ring / 3f, paint);
        }
        canvas.drawLine(cx - radius, cy, cx + radius, cy, paint);
        canvas.drawLine(cx, cy - radius, cx, cy + radius, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(0, 150, 136));
        canvas.drawCircle(cx, cy, 7f, paint);
        if (hasReading) {
            float markerY = cy - radius * markerRadius;
            paint.setColor(rssi > -60 ? Color.rgb(46, 125, 50) : Color.rgb(251, 140, 0));
            canvas.drawCircle(cx, markerY, 12f, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(cx, markerY, 4f, paint);
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
