/*
 * This is the source code of VshGap for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.vshgap.ui.ActionBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.vshgap.android.AndroidUtilities;
import org.vshgap.messenger.ConnectionsManager;
import org.vshgap.messenger.FileLog;
import org.vshgap.messenger.R;
import org.vshgap.messenger.Utilities;

public class BaseFragment {
    private boolean isFinished = false;
    protected AlertDialog visibleDialog = null;

    protected View fragmentView;
    protected ActionBarLayout parentLayout;
    protected ActionBar actionBar;
    protected int classGuid = 0;
    protected Bundle arguments;
    protected boolean swipeBackEnabled = true;
    protected boolean hasOwnBackground = false;

    public BaseFragment() {
        classGuid = ConnectionsManager.getInstance().generateClassGuid();
    }

    public BaseFragment(Bundle args) {
        arguments = args;
        classGuid = ConnectionsManager.getInstance().generateClassGuid();
    }

    public View createView(Context context, LayoutInflater inflater) {
        return null;
    }

    public Bundle getArguments() {
        return arguments;
    }

    protected void setParentLayout(ActionBarLayout layout) {
        if (parentLayout != layout) {
            parentLayout = layout;
            if (fragmentView != null) {
                ViewGroup parent = (ViewGroup) fragmentView.getParent();
                if (parent != null) {
                    try {
                        parent.removeView(fragmentView);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
                fragmentView = null;
            }
            if (actionBar != null) {
                ViewGroup parent = (ViewGroup) actionBar.getParent();
                if (parent != null) {
                    try {
                        parent.removeView(actionBar);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }
            if (parentLayout != null) {
                actionBar = new ActionBar(parentLayout.getContext());
                actionBar.parentFragment = this;
                //actionBar.setBackgroundColor(0xff54759e);
                actionBar.setBackgroundResource(R.color.header); //Plus
                actionBar.setItemsBackground(R.drawable.bar_selector);
            }
        }
    }

    public void finishFragment() {
        finishFragment(true);
    }

    public void finishFragment(boolean animated) {
        if (isFinished || parentLayout == null) {
            return;
        }
        parentLayout.closeLastFragment(animated);
    }

    public void removeSelfFromStack() {
        if (isFinished || parentLayout == null) {
            return;
        }
        parentLayout.removeFragmentFromStack(this);
    }

    public boolean onFragmentCreate() {
        return true;
    }

    public void onFragmentDestroy() {
        ConnectionsManager.getInstance().cancelRpcsForClassGuid(classGuid);
        isFinished = true;
        if (actionBar != null) {
            actionBar.setEnabled(false);
        }
    }

    public void onResume() {
        if(AndroidUtilities.needRestart){
            AndroidUtilities.needRestart = false;
            Utilities.restartApp();
        }
    }

    public void onPause() {
        if (actionBar != null) {
            actionBar.onPause();
        }
        try {
            if (visibleDialog != null && visibleDialog.isShowing()) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {

    }

    public boolean onBackPressed() {
        return true;
    }

    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {

    }

    public void saveSelfArgs(Bundle args) {

    }

    public void restoreSelfArgs(Bundle args) {

    }

    public boolean presentFragment(BaseFragment fragment) {
        return parentLayout != null && parentLayout.presentFragment(fragment);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast) {
        return parentLayout != null && parentLayout.presentFragment(fragment, removeLast);
    }

    public boolean presentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation) {
        return parentLayout != null && parentLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true);
    }

    public Activity getParentActivity() {
        if (parentLayout != null) {
            return parentLayout.parentActivity;
        }
        return null;
    }

    public void startActivityForResult(final Intent intent, final int requestCode) {
        if (parentLayout != null) {
            parentLayout.startActivityForResult(intent, requestCode);
        }
    }

    public void onBeginSlide() {
        try {
            if (visibleDialog != null && visibleDialog.isShowing()) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        if (actionBar != null) {
            actionBar.onPause();
        }
    }

    public void onOpenAnimationEnd() {

    }

    public void onLowMemory() {

    }

    public boolean needAddActionBar() {
        return true;
    }

    public AlertDialog showAlertDialog(AlertDialog.Builder builder) {
        if (parentLayout == null || parentLayout.animationInProgress || parentLayout.startedTracking || parentLayout.checkTransitionAnimation()) {
            return null;
        }
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            visibleDialog = builder.show();
            //Plus
            int color = AndroidUtilities.getIntColor("themeColor");
            int id = visibleDialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
            TextView tv = (TextView) visibleDialog.findViewById(id);
            tv.setTextColor(color);
            id = visibleDialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = visibleDialog.findViewById(id);
            if(divider != null)divider.setBackgroundColor(color);
            Button btn = visibleDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            if(btn != null)btn.setTextColor(color);
            btn = visibleDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if(btn != null)btn.setTextColor(color);
            btn = visibleDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            if(btn != null)btn.setTextColor(color);
            //
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    visibleDialog = null;
                    onDialogDismiss();
                }
            });
            return visibleDialog;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return null;
    }

    protected void onDialogDismiss() {

    }
}