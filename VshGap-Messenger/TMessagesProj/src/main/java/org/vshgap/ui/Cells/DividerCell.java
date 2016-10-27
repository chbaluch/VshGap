/*
 * This is the source code of VshGap for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import org.vshgap.android.AndroidUtilities;

public class DividerCell extends BaseCell {

    private static Paint paint;

    public DividerCell(Context context) {
        super(context);
        if (paint == null) {
            paint = new Paint();
            paint.setColor(0xffd9d9d9);
            paint.setStrokeWidth(1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(16) + 1);
        String key = getTag() != null ? getTag().toString() : null;
        if(key != null){
            int color = AndroidUtilities.getIntDef(key, 0xffffffff);
            paint.setColor(color == 0xffffffff ? 0xffd9d9d9 : color);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(getPaddingLeft(), AndroidUtilities.dp(8), getWidth() - getPaddingRight(), AndroidUtilities.dp(8), paint);
    }
}
