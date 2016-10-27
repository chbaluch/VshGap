/*
 * This is the source code of VshGap for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Cells;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.vshgap.android.AndroidUtilities;
import org.vshgap.messenger.R;
import org.vshgap.ui.Components.LayoutHelper;

public class DrawerActionCell extends FrameLayout {

    private TextView textView;

    public DrawerActionCell(Context context) {
        super(context);

        textView = new TextView(context);
        textView.setTextColor(0xff444444);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        textView.setCompoundDrawablePadding(AndroidUtilities.dp(34));
        addView(textView);
        LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        layoutParams.gravity = Gravity.LEFT;
        layoutParams.leftMargin = AndroidUtilities.dp(14);
        layoutParams.rightMargin = AndroidUtilities.dp(16);
        textView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
        updateTheme();
    }

    public void setTextAndIcon(String text, int resId) {
        textView.setText(text);
        //textView.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
        textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(resId), null, null, null);
    }

    private void updateTheme(){
        textView.setTextColor(AndroidUtilities.getIntDef("drawerOptionColor", 0xff444444));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AndroidUtilities.getIntDef("drawerOptionSize", 15));
        Drawable[] drawables = textView.getCompoundDrawables();
        if(drawables[0].getConstantState().equals(getResources().getDrawable(R.drawable.menu_themes).getConstantState())){
            return;
        }
        int color = AndroidUtilities.getIntDef("drawerIconColor", 0xff737373);
        if(drawables[0] != null)drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
