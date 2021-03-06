/*
 * This is the source code of VshGap for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Cells;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.vshgap.android.AndroidUtilities;
import org.vshgap.android.LocaleController;
import org.vshgap.ui.Components.LayoutHelper;

public class TextCell extends FrameLayout {

    private TextView textView;
    private TextView valueTextView;
    private ImageView imageView;
    private ImageView valueImageView;

    public TextCell(Context context) {
        super(context);

        textView = new TextView(context);
        textView.setTextColor(0xff212121);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(textView);
        LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? 16 : 71);
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? 71 : 16);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        textView.setLayoutParams(layoutParams);

        valueTextView = new TextView(context);
        //valueTextView.setTextColor(0xff2f8cc9);
        valueTextView.setTextColor(AndroidUtilities.getIntColor("themeColor"));
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        valueTextView.setLines(1);
        valueTextView.setMaxLines(1);
        valueTextView.setSingleLine(true);
        valueTextView.setGravity((LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL);
        addView(valueTextView);
        layoutParams = (LayoutParams) valueTextView.getLayoutParams();
        layoutParams.width = LayoutHelper.WRAP_CONTENT;
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0);
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT;
        valueTextView.setLayoutParams(layoutParams);

        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        addView(imageView);
        layoutParams = (LayoutParams) imageView.getLayoutParams();
        layoutParams.width = LayoutHelper.WRAP_CONTENT;
        layoutParams.height = LayoutHelper.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? 0 : 16);
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? 16 : 0);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL;
        imageView.setLayoutParams(layoutParams);

        valueImageView = new ImageView(context);
        valueImageView.setScaleType(ImageView.ScaleType.CENTER);
        addView(valueImageView);
        layoutParams = (LayoutParams) valueImageView.getLayoutParams();
        layoutParams.width = LayoutHelper.WRAP_CONTENT;
        layoutParams.height = LayoutHelper.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? 24 : 0);
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? 0 : 24);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
        valueImageView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setText(String text) {
        textView.setText(text);
        imageView.setVisibility(INVISIBLE);
        valueTextView.setVisibility(INVISIBLE);
        valueImageView.setVisibility(INVISIBLE);
    }

    public void setTextAndIcon(String text, int resId) {
        textView.setText(text);
        imageView.setImageResource(resId);
        imageView.setVisibility(VISIBLE);
        valueTextView.setVisibility(INVISIBLE);
        valueImageView.setVisibility(INVISIBLE);
    }

    public void setTextSize(int size) {
        textView.setTextSize(size);
    }

    public void setTextAndIcon(String text, Drawable drawable) {
        textView.setText(text);
        imageView.setImageDrawable(drawable);
        imageView.setVisibility(VISIBLE);
        valueTextView.setVisibility(GONE);
        valueImageView.setVisibility(GONE);
    }

    public void setValueColor(int color) {
        valueTextView.setTextColor(color);
    }

    public void setTextAndValue(String text, String value) {
        textView.setText(text);
        valueTextView.setText(value);
        valueTextView.setVisibility(VISIBLE);
        imageView.setVisibility(INVISIBLE);
        valueImageView.setVisibility(INVISIBLE);
    }

    public void setTextAndValueDrawable(String text, Drawable drawable) {
        textView.setText(text);
        valueImageView.setVisibility(VISIBLE);
        valueImageView.setImageDrawable(drawable);
        valueTextView.setVisibility(INVISIBLE);
        imageView.setVisibility(INVISIBLE);
    }
}
