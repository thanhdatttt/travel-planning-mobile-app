package com.example.travelplanning.ui.admin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.travelplanning.R;
import java.util.ArrayList;
import java.util.List;

public class SimpleLineGraphView extends View {
    private List<Float> dataPoints = new ArrayList<>();
    private Paint linePaint, fillPaint, axisPaint, textPaint;
    private Path linePath, fillPath;
    private int lineColor = Color.parseColor("#4CAF50");

    // Tăng padding để có chỗ ghi số
    private float paddingLeft = 40f;
    private float paddingBottom = 40f;
    private float paddingTop = 20f;

    public SimpleLineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        linePath = new Path();
        fillPath = new Path();

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // SỬA: Phải dùng ContextCompat để lấy màu từ Resources
        axisPaint.setColor(ContextCompat.getColor(context, R.color.gray));
        axisPaint.setStrokeWidth(3f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setData(List<Float> data, int color) {
        this.dataPoints = data;
        this.lineColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataPoints == null || dataPoints.size() < 2) return;

        float width = getWidth();
        float height = getHeight();
        float chartBottom = height - paddingBottom;
        float chartLeft = paddingLeft;
        float chartWidth = width - chartLeft - 20; // Trừ lề phải 20
        float chartHeight = chartBottom - paddingTop;

        float max = 1;
        for (float f : dataPoints) if (f > max) max = f;

        // Vẽ Trục X và Y
        canvas.drawLine(chartLeft, paddingTop, chartLeft, chartBottom, axisPaint);
        canvas.drawLine(chartLeft, chartBottom, width, chartBottom, axisPaint);

        // Vẽ Labels cho trục Y (Ví dụ: Min, Mid, Max)
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("0", chartLeft - 10, chartBottom, textPaint);
        canvas.drawText(String.valueOf((int)max), chartLeft - 10, paddingTop + 20, textPaint);

        // Tính toán đường vẽ
        float xStep = chartWidth / (dataPoints.size() - 1);
        linePath.reset();
        fillPath.reset();

        for (int i = 0; i < dataPoints.size(); i++) {
            float x = chartLeft + (i * xStep);
            float y = chartBottom - (dataPoints.get(i) / max * chartHeight);

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, chartBottom);
                fillPath.lineTo(x, y);
            } else {
                float prevX = chartLeft + ((i - 1) * xStep);
                float prevY = chartBottom - (dataPoints.get(i - 1) / max * chartHeight);

                linePath.cubicTo((prevX + x) / 2, prevY, (prevX + x) / 2, y, x, y);
                fillPath.cubicTo((prevX + x) / 2, prevY, (prevX + x) / 2, y, x, y);
            }

            if (i == 0 || i == dataPoints.size() / 2 || i == dataPoints.size() - 1) {
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("D" + (i + 1), x, height - 10, textPaint);
            }

            // Đóng vùng Fill khi đến điểm cuối
            if (i == dataPoints.size() - 1) {
                fillPath.lineTo(x, chartBottom);
                fillPath.close();
            }
        }

        // 4. Đổ Gradient và vẽ lên Canvas
        linePaint.setColor(lineColor);
        fillPaint.setShader(new LinearGradient(0, paddingTop, 0, chartBottom, lineColor, Color.TRANSPARENT, Shader.TileMode.CLAMP));
        fillPaint.setAlpha(60);

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }
}