/*
 * This is the source code of VshGap for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Cells;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.vshgap.android.AndroidUtilities;
import org.vshgap.ui.Components.LayoutHelper;

public class LoadingCell extends FrameLayout {

    public LoadingCell(Context context) {
        super(context);

        ProgressBar progressBar = new ProgressBar(context);
        addView(progressBar);
        LayoutParams layoutParams = (FrameLayout.LayoutParams) progressBar.getLayoutParams();
        layoutParams.width = LayoutHelper.WRAP_CONTENT;
        layoutParams.height = LayoutHelper.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(54), MeasureSpec.EXACTLY));
    }
}
