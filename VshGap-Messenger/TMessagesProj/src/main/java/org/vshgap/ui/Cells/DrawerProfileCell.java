/*
 * This is the source code of VshGap for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Cells;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.vshgap.PhoneFormat.PhoneFormat;
import org.vshgap.android.AndroidUtilities;
import org.vshgap.android.ContactsController;
import org.vshgap.android.MessageObject;
import org.vshgap.android.MessagesController;
import org.vshgap.messenger.ApplicationLoader;
import org.vshgap.messenger.R;
import org.vshgap.messenger.TLRPC;
import org.vshgap.messenger.UserConfig;
import org.vshgap.ui.Components.AvatarDrawable;
import org.vshgap.ui.Components.BackupImageView;
import org.vshgap.ui.Components.LayoutHelper;
import org.vshgap.ui.PhotoViewer;

public class DrawerProfileCell extends FrameLayout implements PhotoViewer.PhotoViewerProvider{

    private BackupImageView avatarImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private ImageView shadowView;
    private Rect srcRect = new Rect();
    private Rect destRect = new Rect();
    private Paint paint = new Paint();

    public DrawerProfileCell(Context context) {
        super(context);
        setBackgroundColor(0xff27ae60);

        shadowView = new ImageView(context);
        shadowView.setVisibility(INVISIBLE);
        shadowView.setScaleType(ImageView.ScaleType.FIT_XY);
        shadowView.setImageResource(R.drawable.bottom_shadow);
        addView(shadowView);
        LayoutParams layoutParams = (FrameLayout.LayoutParams) shadowView.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = AndroidUtilities.dp(70);
        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        shadowView.setLayoutParams(layoutParams);

        avatarImageView = new BackupImageView(context);
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(32));
        addView(avatarImageView);
        layoutParams = (LayoutParams) avatarImageView.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(64);
        layoutParams.height = AndroidUtilities.dp(64);
        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        layoutParams.leftMargin = AndroidUtilities.dp(16);
        layoutParams.bottomMargin = AndroidUtilities.dp(67);
        avatarImageView.setLayoutParams(layoutParams);
        final Activity activity = (Activity) context;
        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity == null) {
                    return;
                }
                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(activity);
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, DrawerProfileCell.this);
                }
            }
        });

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setGravity(Gravity.LEFT);
        addView(nameTextView);
        layoutParams = (FrameLayout.LayoutParams) nameTextView.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.WRAP_CONTENT;
        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        layoutParams.leftMargin = AndroidUtilities.dp(16);
        layoutParams.bottomMargin = AndroidUtilities.dp(28);
        layoutParams.rightMargin = AndroidUtilities.dp(16);
        nameTextView.setLayoutParams(layoutParams);

        phoneTextView = new TextView(context);
        phoneTextView.setTextColor(0xffc2e5ff);
        phoneTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        phoneTextView.setLines(1);
        phoneTextView.setMaxLines(1);
        phoneTextView.setSingleLine(true);
        phoneTextView.setGravity(Gravity.LEFT);
        addView(phoneTextView);
        layoutParams = (FrameLayout.LayoutParams) phoneTextView.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = LayoutHelper.WRAP_CONTENT;
        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        layoutParams.leftMargin = AndroidUtilities.dp(16);
        layoutParams.bottomMargin = AndroidUtilities.dp(9);
        layoutParams.rightMargin = AndroidUtilities.dp(16);
        phoneTextView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (Build.VERSION.SDK_INT >= 21) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148) + AndroidUtilities.statusBarHeight, MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148), MeasureSpec.EXACTLY));
        }
        updateTheme();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable backgroundDrawable = ApplicationLoader.getCachedWallpaper();
        if (ApplicationLoader.isCustomTheme() && backgroundDrawable != null && !AndroidUtilities.getBoolPref("drawerHeaderBGCheck")) {
            phoneTextView.setTextColor(0xffffffff);
            shadowView.setVisibility(VISIBLE);
            if (backgroundDrawable instanceof ColorDrawable) {
                backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                backgroundDrawable.draw(canvas);
            } else if (backgroundDrawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
                float scaleX = (float) getMeasuredWidth() / (float) bitmap.getWidth();
                float scaleY = (float) getMeasuredHeight() / (float) bitmap.getHeight();
                float scale = scaleX < scaleY ? scaleY : scaleX;
                int width = (int) (getMeasuredWidth() / scale);
                int height = (int) (getMeasuredHeight() / scale);
                int x = (bitmap.getWidth() - width) / 2;
                int y = (bitmap.getHeight() - height) / 2;
                srcRect.set(x, y, x + width, y + height);
                destRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                canvas.drawBitmap(bitmap, srcRect, destRect, paint);
            }
        } else {
            shadowView.setVisibility(INVISIBLE);
            phoneTextView.setTextColor(0xffc2e5ff);
            super.onDraw(canvas);
        }
        updateTheme();
    }

    public void setUser(TLRPC.User user) {
        if (user == null) {
            return;
        }
        TLRPC.FileLocation photo = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
        }
        nameTextView.setText(ContactsController.formatName(user.first_name, user.last_name));
        phoneTextView.setText(PhoneFormat.getInstance().format("+" + user.phone));
        AvatarDrawable avatarDrawable = new AvatarDrawable(user);
        avatarDrawable.setColor(0xff229955);
        avatarImageView.setImage(photo, "50_50", avatarDrawable);
    }

    @Override
    public void updatePhotoAtIndex(int index) {}

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user != null && user.photo != null && user.photo.photo_big != null) {
            TLRPC.FileLocation photoBig = user.photo.photo_big;
            if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int coords[] = new int[2];
                avatarImageView.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                object.parentView = avatarImageView;
                object.imageReceiver = avatarImageView.getImageReceiver();
                object.user_id = UserConfig.getClientUserId();
                object.thumb = object.imageReceiver.getBitmap();
                object.size = -1;
                object.radius = avatarImageView.getImageReceiver().getRoundRadius();
                return object;
            }
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) { }

    @Override
    public void willHidePhotoViewer() {
        avatarImageView.getImageReceiver().setVisible(true, true);
    }

    @Override
    public boolean isPhotoChecked(int index) { return false; }

    @Override
    public void setPhotoChecked(int index) { }

    @Override
    public void cancelButtonPressed() { }

    @Override
    public void sendButtonPressed(int index) { }

    @Override
    public int getSelectedCount() { return 0; }

    private void updateTheme(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int tColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        int dColor = AndroidUtilities.getIntDarkerColor("themeColor",-0x40);
        setBackgroundColor(themePrefs.getInt("drawerHeaderColor", tColor));
        nameTextView.setTextColor(themePrefs.getInt("drawerNameColor", 0xffffffff));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, themePrefs.getInt("drawerNameSize", 15));
        phoneTextView.setTextColor(themePrefs.getInt("drawerPhoneColor", dColor));
        phoneTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, themePrefs.getInt("drawerPhoneSize", 13));
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        TLRPC.FileLocation photo = null;
        if (user != null && user.photo != null && user.photo.photo_small != null ) {
            photo = user.photo.photo_small;
        }
        AvatarDrawable avatarDrawable = new AvatarDrawable(user);
        avatarDrawable.setColor(themePrefs.getInt("drawerAvatarColor", AndroidUtilities.getIntDarkerColor("themeColor", 0x15)));
        int radius = AndroidUtilities.dp(themePrefs.getInt("drawerAvatarRadius", 32));
        avatarDrawable.setRadius(radius);
        avatarImageView.getImageReceiver().setRoundRadius(radius);
        avatarImageView.setImage(photo, "50_50", avatarDrawable);
        if(AndroidUtilities.getBoolMain("hideMobile")){
            phoneTextView.setVisibility(GONE);
        }else{
            phoneTextView.setVisibility(VISIBLE);
        }
    }
}
