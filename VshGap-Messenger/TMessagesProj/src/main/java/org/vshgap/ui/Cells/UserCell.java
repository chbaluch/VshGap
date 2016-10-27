/*
 * This is the source code of VshGap for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Cells;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.vshgap.android.AndroidUtilities;
import org.vshgap.android.ContactsController;
import org.vshgap.android.LocaleController;
import org.vshgap.android.MessagesController;
import org.vshgap.messenger.ApplicationLoader;
import org.vshgap.messenger.ConnectionsManager;
import org.vshgap.messenger.R;
import org.vshgap.messenger.TLRPC;
import org.vshgap.messenger.UserConfig;
import org.vshgap.ui.Components.AvatarDrawable;
import org.vshgap.ui.Components.BackupImageView;
import org.vshgap.ui.Components.CheckBox;
import org.vshgap.ui.Components.LayoutHelper;
import org.vshgap.ui.Components.SimpleTextView;

public class UserCell extends FrameLayout {

    private BackupImageView avatarImageView;
    private SimpleTextView nameTextView;
    private SimpleTextView statusTextView;
    private ImageView imageView;
    private CheckBox checkBox;

    private AvatarDrawable avatarDrawable;
    private TLRPC.User currentUser = null;

    private CharSequence currentName;
    private CharSequence currrntStatus;
    private int currentDrawable;

    private String lastName = null;
    private int lastStatus = 0;
    private TLRPC.FileLocation lastAvatar = null;

    private int statusColor = 0xffa8a8a8;
    private int statusOnlineColor = AndroidUtilities.getIntDarkerColor("themeColor",0x15);//0xff3b84c0;

    private int nameColor = 0xff000000;

    private Drawable curDrawable = null;

    private int radius = 32;

    public UserCell(Context context, int padding) {
        super(context);

        avatarDrawable = new AvatarDrawable();

        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(24));
        addView(avatarImageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 7 + padding, 8, LocaleController.isRTL ? 7 + padding : 0, 0));

        nameTextView = new SimpleTextView(context);
        nameTextView.setTextColor(0xff212121);
        nameTextView.setTextSize(17);
        nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 : (68 + padding), 11.5f, LocaleController.isRTL ? (68 + padding) : 28, 0));

        statusTextView = new SimpleTextView(context);
        statusTextView.setTextSize(14);
        statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 28 : (68 + padding), 34.5f, LocaleController.isRTL ? (68 + padding) : 28, 0));

        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setVisibility(GONE);
        addView(imageView, LayoutHelper.createFrame(LayoutParams.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, LocaleController.isRTL ? 0 : 16, 0, LocaleController.isRTL ? 16 : 0, 0));

        checkBox = new CheckBox(context, R.drawable.round_check2);
        checkBox.setVisibility(INVISIBLE);
        addView(checkBox, LayoutHelper.createFrame(22, 22, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 37 + padding, 38, LocaleController.isRTL ? 37 + padding : 0, 0));
    }

    public void setData(TLRPC.User user, CharSequence name, CharSequence status, int resId) {
        if (user == null) {
            currrntStatus = null;
            currentName = null;
            currentUser = null;
            nameTextView.setText("");
            statusTextView.setText("");
            avatarImageView.setImageDrawable(null);
            return;
        }
        currrntStatus = status;
        currentName = name;
        currentUser = user;
        currentDrawable = resId;
        update(0);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checkBox.getVisibility() != VISIBLE) {
            checkBox.setVisibility(VISIBLE);
        }
        checkBox.setChecked(checked, animated);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64), MeasureSpec.EXACTLY));
    }

    public void setStatusColors(int color, int onlineColor) {
        statusColor = color;
        statusOnlineColor = onlineColor;
    }

    public void update(int mask) {
        if (currentUser == null) {
            return;
        }
        TLRPC.FileLocation photo = null;
        String newName = null;
        if (currentUser.photo != null) {
            photo = currentUser.photo.photo_small;
        }

        if (mask != 0) {
            boolean continueUpdate = false;
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0) {
                if (lastAvatar != null && photo == null || lastAvatar == null && photo != null && lastAvatar != null && photo != null && (lastAvatar.volume_id != photo.volume_id || lastAvatar.local_id != photo.local_id)) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                int newStatus = 0;
                if (currentUser.status != null) {
                    newStatus = currentUser.status.expires;
                }
                if (newStatus != lastStatus) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && currentName == null && lastName != null && (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                newName = ContactsController.formatName(currentUser.first_name, currentUser.last_name);
                if (!newName.equals(lastName)) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate) {
                return;
            }
        }
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        avatarDrawable.setInfo(currentUser);
        if (currentUser.status != null) {
            lastStatus = currentUser.status.expires;
        } else {
            lastStatus = 0;
        }

        if (currentName != null) {
            lastName = null;
            nameTextView.setText(currentName);
        } else {
            lastName = newName == null ? ContactsController.formatName(currentUser.first_name, currentUser.last_name) : newName;
            nameTextView.setText(lastName);
            nameTextView.setTextColor(nameColor);
            nameTextView.setTextSize(themePrefs.getInt("contactsNameSize", 17));
        }
        if (currrntStatus != null) {
            statusTextView.setTextColor(statusColor);
            statusTextView.setText(currrntStatus);
        } else {
            if (currentUser.id == UserConfig.getClientUserId() || currentUser.status != null && currentUser.status.expires > ConnectionsManager.getInstance().getCurrentTime()) {
                statusTextView.setTextColor(statusOnlineColor);
                statusTextView.setText(LocaleController.getString("Online", R.string.Online));
            } else {
                statusTextView.setTextColor(statusColor);
                statusTextView.setText(LocaleController.formatUserStatus(currentUser));
            }
        }

        if (imageView.getVisibility() == VISIBLE && currentDrawable == 0 || imageView.getVisibility() == GONE && currentDrawable != 0) {
            imageView.setVisibility(currentDrawable == 0 ? GONE : VISIBLE);
            imageView.setImageResource(currentDrawable);
        }
        statusTextView.setTextSize(themePrefs.getInt("contactsStatusSize", 14));
        //imageView.setVisibility(currentDrawable == 0 ? INVISIBLE : VISIBLE);
        //imageView.setImageResource(currentDrawable);
        if(curDrawable != null)imageView.setImageDrawable(curDrawable);

        //int radius = AndroidUtilities.dp(themePrefs.getInt("contactsAvatarRadius", 32));
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(radius));
        avatarDrawable.setRadius(AndroidUtilities.dp(radius));

        avatarImageView.setImage(photo, "50_50", avatarDrawable);
    }

    public void setNameColor(int color) {
        nameColor = color;
    }

    public void setImageDrawable(Drawable drawable){
        curDrawable = drawable;
    }

    public void setAvatarRadius(int value){
        radius = value;
    }
}
