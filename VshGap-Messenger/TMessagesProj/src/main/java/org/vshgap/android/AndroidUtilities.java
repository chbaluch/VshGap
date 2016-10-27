/*
 * This is the source code of VshGap for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.StateSet;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.vshgap.android.AnimationCompat.AnimatorListenerAdapterProxy;
import org.vshgap.android.AnimationCompat.AnimatorSetProxy;
import org.vshgap.android.AnimationCompat.ObjectAnimatorProxy;
import org.vshgap.android.AnimationCompat.ViewProxy;
import org.vshgap.messenger.ApplicationLoader;
import org.vshgap.messenger.ConnectionsManager;
import org.vshgap.messenger.FileLog;
import org.vshgap.messenger.R;
import org.vshgap.messenger.TLRPC;
import org.vshgap.messenger.UserConfig;
import org.vshgap.ui.Components.ForegroundDetector;
import org.vshgap.ui.Components.NumberPicker;
import org.vshgap.ui.Components.TypefaceSpan;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;

public class AndroidUtilities {

    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<>();
    private static int prevOrientation = -10;
    private static boolean waitingForSms = false;
    private static final Object smsLock = new Object();

    public static int statusBarHeight = 0;
    public static float density = 1;
    public static Point displaySize = new Point();
    public static Integer photoSize = null;
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static int leftBaseline;
    public static boolean usingHardwareInput;
    private static Boolean isTablet = null;

    public static final String THEME_PREFS = "theme";
    public static final int THEME_PREFS_MODE = Activity.MODE_PRIVATE;

    public static final int defColor = 0xff27ae60;//0xff58BCD5;//0xff43C3DB;//0xff2f8cc9;58BCD5//0xff55abd2
    public static int themeColor = getIntColor("themeColor");

    public static boolean needRestart = false;

    static {
        density = ApplicationLoader.applicationContext.getResources().getDisplayMetrics().density;
        leftBaseline = isTablet() ? 80 : 72;
        checkDisplaySize();
    }

    public static void lockOrientation(Activity activity) {
        if (activity == null || prevOrientation != -10 || Build.VERSION.SDK_INT < 9) {
            return;
        }
        try {
            prevOrientation = activity.getRequestedOrientation();
            WindowManager manager = (WindowManager)activity.getSystemService(Activity.WINDOW_SERVICE);
            if (manager != null && manager.getDefaultDisplay() != null) {
                int rotation = manager.getDefaultDisplay().getRotation();
                int orientation = activity.getResources().getConfiguration().orientation;
                int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
                int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;
                if (Build.VERSION.SDK_INT < 9) {
                    SCREEN_ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    SCREEN_ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                }

                if (rotation == Surface.ROTATION_270) {
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    }
                } else if (rotation == Surface.ROTATION_90) {
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                } else if (rotation == Surface.ROTATION_0) {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                } else {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    } else {
                        activity.setRequestedOrientation(SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public static void unlockOrientation(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < 9) {
            return;
        }
        try {
            if (prevOrientation != -10) {
                activity.setRequestedOrientation(prevOrientation);
                prevOrientation = -10;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public static Typeface getTypeface(String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(ApplicationLoader.applicationContext.getAssets(), assetPath);
                    typefaceCache.put(assetPath, t);
                } catch (Exception e) {
                    FileLog.e("Typefaces", "Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }

    public static boolean isWaitingForSms() {
        boolean value = false;
        synchronized (smsLock) {
            value = waitingForSms;
        }
        return value;
    }

    public static void setWaitingForSms(boolean value) {
        synchronized (smsLock) {
            waitingForSms = value;
        }
    }

    public static void showKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static boolean isKeyboardShowed(View view) {
        if (view == null) {
            return false;
        }
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inputManager.isActive(view);
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static File getCacheDir() {
        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        if (state == null || state.startsWith(Environment.MEDIA_MOUNTED)) {
            try {
                File file = ApplicationLoader.applicationContext.getExternalCacheDir();
                if (file != null) {
                    return file;
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
        try {
            File file = ApplicationLoader.applicationContext.getCacheDir();
            if (file != null) {
                return file;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return new File("");
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int)Math.ceil(density * value);
    }

    public static int compare(int lhs, int rhs) {
        if (lhs == rhs) {
            return 0;
        } else if (lhs > rhs) {
            return 1;
        }
        return -1;
    }

    public static float dpf2(float value) {
        if (value == 0) {
            return 0;
        }
        return density * value;
    }

    public static void checkDisplaySize() {
        try {
            Configuration configuration = ApplicationLoader.applicationContext.getResources().getConfiguration();
            usingHardwareInput = configuration.keyboard != Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            WindowManager manager = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    if (android.os.Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                    FileLog.e("tmessages", "display size = " + displaySize.x + " " + displaySize.y + " " + displayMetrics.xdpi + "x" + displayMetrics.ydpi);
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        /*
        keyboardHidden
        public static final int KEYBOARDHIDDEN_NO = 1
        Constant for keyboardHidden, value corresponding to the keysexposed resource qualifier.

        public static final int KEYBOARDHIDDEN_UNDEFINED = 0
        Constant for keyboardHidden: a value indicating that no value has been set.

        public static final int KEYBOARDHIDDEN_YES = 2
        Constant for keyboardHidden, value corresponding to the keyshidden resource qualifier.

        hardKeyboardHidden
        public static final int HARDKEYBOARDHIDDEN_NO = 1
        Constant for hardKeyboardHidden, value corresponding to the physical keyboard being exposed.

        public static final int HARDKEYBOARDHIDDEN_UNDEFINED = 0
        Constant for hardKeyboardHidden: a value indicating that no value has been set.

        public static final int HARDKEYBOARDHIDDEN_YES = 2
        Constant for hardKeyboardHidden, value corresponding to the physical keyboard being hidden.

        keyboard
        public static final int KEYBOARD_12KEY = 3
        Constant for keyboard, value corresponding to the 12key resource qualifier.

        public static final int KEYBOARD_NOKEYS = 1
        Constant for keyboard, value corresponding to the nokeys resource qualifier.

        public static final int KEYBOARD_QWERTY = 2
        Constant for keyboard, value corresponding to the qwerty resource qualifier.
         */
    }

    public static float getPixelsInCM(float cm, boolean isX) {
        return (cm / 2.54f) * (isX ? displayMetrics.xdpi : displayMetrics.ydpi);
    }

    public static long makeBroadcastId(int id) {
        return 0x0000000100000000L | ((long)id & 0x00000000FFFFFFFFL);
    }

    public static int getMyLayerVersion(int layer) {
        return layer & 0xffff;
    }

    public static int getPeerLayerVersion(int layer) {
        return (layer >> 16) & 0xffff;
    }

    public static int setMyLayerVersion(int layer, int version) {
        return layer & 0xffff0000 | version;
    }

    public static int setPeerLayerVersion(int layer, int version) {
        return layer & 0x0000ffff | (version << 16);
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            ApplicationLoader.applicationHandler.post(runnable);
        } else {
            ApplicationLoader.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        ApplicationLoader.applicationHandler.removeCallbacks(runnable);
    }

    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = ApplicationLoader.applicationContext.getResources().getBoolean(R.bool.isTablet);
        }
        return isTablet;
    }

    public static boolean isSmallTablet() {
        float minSide = Math.min(displaySize.x, displaySize.y) / density;
        return minSide <= 700;
    }

    public static int getMinTabletSide() {
        if (!isSmallTablet()) {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int leftSide = smallSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return smallSide - leftSide;
        } else {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int maxSide = Math.max(displaySize.x, displaySize.y);
            int leftSide = maxSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return Math.min(smallSide, maxSide - leftSide);
        }
    }

    public static int getPhotoSize() {
        if (photoSize == null) {
            if (Build.VERSION.SDK_INT >= 16) {
                photoSize = 1280;
            } else {
                photoSize = 800;
            }
        }
        return photoSize;
    }

    public static String formatTTLString(int ttl) {
        if (ttl < 60) {
            return LocaleController.formatPluralString("Seconds", ttl);
        } else if (ttl < 60 * 60) {
            return LocaleController.formatPluralString("Minutes", ttl / 60);
        } else if (ttl < 60 * 60 * 24) {
            return LocaleController.formatPluralString("Hours", ttl / 60 / 60);
        } else if (ttl < 60 * 60 * 24 * 7) {
            return LocaleController.formatPluralString("Days", ttl / 60 / 60 / 24);
        } else {
            int days = ttl / 60 / 60 / 24;
            if (ttl % 7 == 0) {
                return LocaleController.formatPluralString("Weeks", days / 7);
            } else {
                return String.format("%s %s", LocaleController.formatPluralString("Weeks", days / 7), LocaleController.formatPluralString("Days", days % 7));
            }
        }
    }

    public static AlertDialog.Builder buildTTLAlert(final Context context, final TLRPC.EncryptedChat encryptedChat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("MessageLifetime", R.string.MessageLifetime));
        final NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(20);
        if (encryptedChat.ttl > 0 && encryptedChat.ttl < 16) {
            numberPicker.setValue(encryptedChat.ttl);
        } else if (encryptedChat.ttl == 30) {
            numberPicker.setValue(16);
        } else if (encryptedChat.ttl == 60) {
            numberPicker.setValue(17);
        } else if (encryptedChat.ttl == 60 * 60) {
            numberPicker.setValue(18);
        } else if (encryptedChat.ttl == 60 * 60 * 24) {
            numberPicker.setValue(19);
        } else if (encryptedChat.ttl == 60 * 60 * 24 * 7) {
            numberPicker.setValue(20);
        } else if (encryptedChat.ttl == 0) {
            numberPicker.setValue(0);
        }
        numberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                if (value == 0) {
                    return LocaleController.getString("ShortMessageLifetimeForever", R.string.ShortMessageLifetimeForever);
                } else if (value >= 1 && value < 16) {
                    return AndroidUtilities.formatTTLString(value);
                } else if (value == 16) {
                    return AndroidUtilities.formatTTLString(30);
                } else if (value == 17) {
                    return AndroidUtilities.formatTTLString(60);
                } else if (value == 18) {
                    return AndroidUtilities.formatTTLString(60 * 60);
                } else if (value == 19) {
                    return AndroidUtilities.formatTTLString(60 * 60 * 24);
                } else if (value == 20) {
                    return AndroidUtilities.formatTTLString(60 * 60 * 24 * 7);
                }
                return "";
            }
        });
        builder.setView(numberPicker);
        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int oldValue = encryptedChat.ttl;
                which = numberPicker.getValue();
                if (which >= 0 && which < 16) {
                    encryptedChat.ttl = which;
                } else if (which == 16) {
                    encryptedChat.ttl = 30;
                } else if (which == 17) {
                    encryptedChat.ttl = 60;
                } else if (which == 18) {
                    encryptedChat.ttl = 60 * 60;
                } else if (which == 19) {
                    encryptedChat.ttl = 60 * 60 * 24;
                } else if (which == 20) {
                    encryptedChat.ttl = 60 * 60 * 24 * 7;
                }
                if (oldValue != encryptedChat.ttl) {
                    SecretChatHelper.getInstance().sendTTLMessage(encryptedChat, null);
                    MessagesStorage.getInstance().updateEncryptedChatTTL(encryptedChat);
                }
            }
        });
        return builder;
    }

    public static void clearCursorDrawable(EditText editText) {
        if (editText == null || Build.VERSION.SDK_INT < 12) {
            return;
        }
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.setInt(editText, 0);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public static void setProgressBarAnimationDuration(ProgressBar progressBar, int duration) {
        if (progressBar == null) {
            return;
        }
        try {
            Field mCursorDrawableRes = ProgressBar.class.getDeclaredField("mDuration");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.setInt(progressBar, duration);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public static int getViewInset(View view) {
        if (view == null || Build.VERSION.SDK_INT < 21) {
            return 0;
        }
        try {
            Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            mAttachInfoField.setAccessible(true);
            Object mAttachInfo = mAttachInfoField.get(view);
            if (mAttachInfo != null) {
                Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                mStableInsetsField.setAccessible(true);
                Rect insets = (Rect)mStableInsetsField.get(mAttachInfo);
                return insets.bottom;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return 0;
    }

    public static int getCurrentActionBarHeight() {
        if (isTablet()) {
            return dp(64);
        } else if (ApplicationLoader.applicationContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return dp(48);
        } else {
            return dp(56);
        }
    }

    public static Point getRealScreenSize() {
        Point size = new Point();
        try {
            WindowManager windowManager = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                windowManager.getDefaultDisplay().getRealSize(size);
            } else {
                try {
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    size.set((Integer) mGetRawW.invoke(windowManager.getDefaultDisplay()), (Integer) mGetRawH.invoke(windowManager.getDefaultDisplay()));
                } catch (Exception e) {
                    size.set(windowManager.getDefaultDisplay().getWidth(), windowManager.getDefaultDisplay().getHeight());
                    FileLog.e("tmessages", e);
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return size;
    }

    public static void setListViewEdgeEffectColor(AbsListView listView, int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                Field field = AbsListView.class.getDeclaredField("mEdgeGlowTop");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowTop = (EdgeEffect) field.get(listView);
                if (mEdgeGlowTop != null) {
                    mEdgeGlowTop.setColor(color);
                }

                field = AbsListView.class.getDeclaredField("mEdgeGlowBottom");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowBottom = (EdgeEffect) field.get(listView);
                if (mEdgeGlowBottom != null) {
                    mEdgeGlowBottom.setColor(color);
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

    public static void clearDrawableAnimation(View view) {
        if (Build.VERSION.SDK_INT < 21 || view == null) {
            return;
        }
        Drawable drawable = null;
        if (view instanceof ListView) {
            drawable = ((ListView) view).getSelector();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
            }
        } else {
            drawable = view.getBackground();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
                drawable.jumpToCurrentState();
            }
        }
    }

    public static Spannable replaceTags(String str) {
        try {
            int start = -1;
            int startColor = -1;
            int end = -1;
            StringBuilder stringBuilder = new StringBuilder(str);
            while ((start = stringBuilder.indexOf("<br>")) != -1) {
                stringBuilder.replace(start, start + 4, "\n");
            }
            while ((start = stringBuilder.indexOf("<br/>")) != -1) {
                stringBuilder.replace(start, start + 5, "\n");
            }
        ArrayList<Integer> bolds = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();
            while ((start = stringBuilder.indexOf("<b>")) != -1 || (startColor = stringBuilder.indexOf("<c")) != -1) {
                if (start != -1) {
                    stringBuilder.replace(start, start + 3, "");
                    end = stringBuilder.indexOf("</b>");
                    if (end == -1) {
                        end = stringBuilder.indexOf("<b>");
                    }
                    stringBuilder.replace(end, end + 4, "");
            bolds.add(start);
            bolds.add(end);
                } else if (startColor != -1) {
                    stringBuilder.replace(startColor, startColor + 2, "");
                    end = stringBuilder.indexOf(">", startColor);
                    int color = Color.parseColor(stringBuilder.substring(startColor, end));
                    stringBuilder.replace(startColor, end + 1, "");
                    end = stringBuilder.indexOf("</c>");
                    stringBuilder.replace(end, end + 4, "");
                    colors.add(startColor);
                    colors.add(end);
                    colors.add(color);
                }
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(stringBuilder);
        for (int a = 0; a < bolds.size() / 2; a++) {
                spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), bolds.get(a * 2), bolds.get(a * 2 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            for (int a = 0; a < colors.size() / 3; a++) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(colors.get(a * 3 + 2)), colors.get(a * 3), colors.get(a * 3 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spannableStringBuilder;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return new SpannableStringBuilder(str);
    }

    public static boolean needShowPasscode(boolean reset) {
        boolean wasInBackground;
        if (Build.VERSION.SDK_INT >= 14) {
            wasInBackground = ForegroundDetector.getInstance().isWasInBackground(reset);
            if (reset) {
                ForegroundDetector.getInstance().resetBackgroundVar();
            }
        } else {
            wasInBackground = UserConfig.lastPauseTime != 0;
        }
        return UserConfig.passcodeHash.length() > 0 && wasInBackground &&
                (UserConfig.appLocked || UserConfig.autoLockIn != 0 && UserConfig.lastPauseTime != 0 && !UserConfig.appLocked && (UserConfig.lastPauseTime + UserConfig.autoLockIn) <= ConnectionsManager.getInstance().getCurrentTime());
    }

    public static void shakeTextView(final TextView textView, final float x, final int num) {
        if (num == 6) {
            ViewProxy.setTranslationX(textView, 0);
            textView.clearAnimation();
            return;
        }
        AnimatorSetProxy animatorSetProxy = new AnimatorSetProxy();
        animatorSetProxy.playTogether(ObjectAnimatorProxy.ofFloat(textView, "translationX", AndroidUtilities.dp(x)));
        animatorSetProxy.setDuration(50);
        animatorSetProxy.addListener(new AnimatorListenerAdapterProxy() {
            @Override
            public void onAnimationEnd(Object animation) {
                shakeTextView(textView, num == 5 ? 0 : -x, num + 1);
            }
        });
        animatorSetProxy.start();
    }



    /*public static String ellipsize(String text, int maxLines, int maxWidth, TextPaint paint) {
        if (text == null || paint == null) {
            return null;
        }
        int count;
        int offset = 0;
        StringBuilder result = null;
        TextView
        for (int a = 0; a < maxLines; a++) {
            count = paint.breakText(text, true, maxWidth, null);
            if (a != maxLines - 1) {
                if (result == null) {
                    result = new StringBuilder(count * maxLines + 1);
                }
                boolean foundSpace = false;
                for (int c = count - 1; c >= offset; c--) {
                    if (text.charAt(c) == ' ') {
                        foundSpace = true;
                        result.append(text.substring(offset, c - 1));
                        offset = c - 1;
                    }
                }
                if (!foundSpace) {
                    offset = count;
                }
                text = text.substring(0, offset);
            } else if (maxLines == 1) {
                return text.substring(0, count);
            } else {
                result.append(text.substring(0, count));
            }
        }
        return result.toString();
    }*/

    /*public static void turnOffHardwareAcceleration(Window window) {
        if (window == null || Build.MODEL == null || Build.VERSION.SDK_INT < 11) {
            return;
        }
        if (Build.MODEL.contains("GT-S5301") ||
                Build.MODEL.contains("GT-S5303") ||
                Build.MODEL.contains("GT-B5330") ||
                Build.MODEL.contains("GT-S5302") ||
                Build.MODEL.contains("GT-S6012B") ||
                Build.MODEL.contains("MegaFon_SP-AI")) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
    }*/
    //PLUS
    public static int getIntColor(String key){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(THEME_PREFS, THEME_PREFS_MODE);
        return themePrefs.getInt(key, defColor);//Def color is Teal
    }

    public static int getIntTColor(String key){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(THEME_PREFS, THEME_PREFS_MODE);
        int def = themePrefs.getInt("themeColor", defColor);
        return themePrefs.getInt(key, def);//Def color is theme color
    }

    public static int getIntDef(String key, int def){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(THEME_PREFS, THEME_PREFS_MODE);
        return themePrefs.getInt(key, def);
    }

    public static int getIntAlphaColor(String key, int def, float factor){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(THEME_PREFS, THEME_PREFS_MODE);
        int color = themePrefs.getInt(key, def);
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int getIntDarkerColor(String key, int factor){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(THEME_PREFS, THEME_PREFS_MODE);
        int color = themePrefs.getInt(key, defColor);
        return setDarkColor(color, factor);
    }

    public static int setDarkColor(int color, int factor){
        int red = Color.red(color) - factor;
        int green = Color.green(color) - factor;
        int blue = Color.blue(color) - factor;
        if(factor < 0){
            red = (red > 0xff) ? 0xff : red;
            green = (green > 0xff) ? 0xff : green;
            blue = (blue > 0xff) ? 0xff : blue;
            if(red == 0xff && green == 0xff && blue == 0xff){
                red = factor;
                green = factor;
                blue = factor;
            }
        }
        if(factor > 0){
            red = (red < 0) ? 0 : red;
            green = (green < 0) ? 0 : green;
            blue = (blue < 0) ? 0 : blue;
            if(red == 0 && green == 0 && blue == 0){
                red = factor;
                green = factor;
                blue = factor;
            }
        }
        return Color.argb(0xff, red, green, blue);
    }

    public static void setIntColor(String key, int value){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(THEME_PREFS, THEME_PREFS_MODE);
        SharedPreferences.Editor e = themePrefs.edit();
        e.putInt(key, value);
        e.commit();
    }

    public static void setBoolPref(Context context, String key, Boolean b){
        SharedPreferences sharedPref = context.getSharedPreferences(THEME_PREFS, 0);
        SharedPreferences.Editor e = sharedPref.edit();
        e.putBoolean(key, b);
        e.commit();
    }

    public static void setStringPref(Context context, String key, String s){
        SharedPreferences sharedPref = context.getSharedPreferences(THEME_PREFS, 0);
        SharedPreferences.Editor e = sharedPref.edit();
        e.putString(key, s);
        e.commit();
    }

    public static boolean getBoolPref(String key){
        boolean s = false;
        if (ApplicationLoader.applicationContext.getSharedPreferences(THEME_PREFS, 0).getBoolean(key, false)) s=true;
        return s;
    }

    public static boolean getBoolMain(String key){
        boolean s = false;
        if (ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE).getBoolean(key, false)) s=true;
        return s;
    }

    public static int getIntPref(Context context,String key){
        int i=0;
        if(key.contains("picker")){
            int intColor = context.getSharedPreferences(THEME_PREFS, 0).getInt(key, Color.WHITE );
            i=intColor;
        }
        return i;
    }
/*
    public static int getIntColorDef(Context context, String key, int def){
        int i=def;
        if(context.getSharedPreferences(THEME_PREFS, 0).getBoolean(key, false)){
            i = context.getSharedPreferences(THEME_PREFS, 0).getInt(key.replace("_check", "_picker"), def);
        }
        return i;
    }

    public static void setTVTextColor(Context ctx, TextView tv, String key, int def){
        if(tv==null)return;
        if(getBoolPref(ctx, key))
            def = getIntPref(ctx, key.replace("_check", "_picker"));
        tv.setTextColor(def);
    }

    public static int getSizePref(Context context,String key, int def){
        if(key.contains("picker"))return context.getSharedPreferences(THEME_PREFS, 0).getInt(key, def);
        return def;
    }

    public static void paintActionBarHeader(Activity a, ActionBar ab, String hdCheck, String gdMode){
        if(ab==null)return;
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#54759E")));
        if(getBoolPref(a, hdCheck)){
            int i = getIntPref(a, hdCheck.replace("_check", "_picker"));
            Drawable d = new ColorDrawable(i);
            try{
                ab.setBackgroundDrawable(d);
                d = paintGradient(a,i,gdMode.replace("mode","color_picker"),gdMode);
                if(d!=null)ab.setBackgroundDrawable(d);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }*/

    public static Drawable paintGradient(Context c, int mainColor, String gColor, String g){
        GradientDrawable gd = null;
        int[] a ={mainColor,getIntPref(c,gColor)};
        int gType = Integer.parseInt(c.getSharedPreferences(THEME_PREFS, 0).getString(g, "0"));
        if(gType==2) gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,a);
        if(gType==1) gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM ,a);
        return gd;
    }

    public static Drawable paintDrawable(Context c, int resId, int resIdW, String color){
        Drawable d = c.getResources().getDrawable(resId);
        if(color.contains("_check")){
            if(getBoolPref(color)){
                d = c.getResources().getDrawable(resIdW);
                d.setColorFilter(getIntPref(c, color.replace("_check", "_picker")), PorterDuff.Mode.MULTIPLY);
            }
        }
        return d;
    }

    public static int getDefBubbleColor(){
        int color = 0xffb2dfdb;//0xff80cbc4;
        if(getIntColor("themeColor") != 0xff27ae60){
            color = AndroidUtilities.getIntDarkerColor("themeColor", -0x50);
        }
        return color;
    }

/*
    static void modifyXMLfile(File preffile,String sname){
        try {
            File file = preffile;
            //Log.e("modifyXMLfile",preffile.getAbsolutePath());
            //Log.e("modifyXMLfile",preffile.exists()+"");
            List<String> lines = new ArrayList<String>();
            // first, read the file and store the changes
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line = in.readLine();
            while (line != null) {
                if (!line.contains(sname))lines.add(line);
                //Log.e("modifyXMLfile",line);
                line = in.readLine();
            }
            in.close();
            // now, write the file again with the changes
            PrintWriter out = new PrintWriter(file);
            for (String l : lines)
                out.println(l);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
