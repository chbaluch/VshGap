/*
 * This is the source code of VshGap for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import org.vshgap.android.AndroidUtilities;
import org.vshgap.android.ContactsController;
import org.vshgap.android.Emoji;
import org.vshgap.android.ImageReceiver;
import org.vshgap.android.LocaleController;
import org.vshgap.android.MessageObject;
import org.vshgap.android.MessagesController;
import org.vshgap.messenger.ApplicationLoader;
import org.vshgap.messenger.FileLoader;
import org.vshgap.messenger.FileLog;
import org.vshgap.messenger.R;
import org.vshgap.messenger.TLRPC;
import org.vshgap.ui.Components.AvatarDrawable;
import org.vshgap.ui.Components.ResourceLoader;
import org.vshgap.ui.Components.StaticLayoutEx;

public class ChatBaseCell extends BaseCell {

    public interface ChatBaseCellDelegate {
        void didPressedUserAvatar(ChatBaseCell cell, TLRPC.User user);
        void didPressedCancelSendButton(ChatBaseCell cell);
        void didLongPressed(ChatBaseCell cell);
        void didPressReplyMessage(ChatBaseCell cell, int id);
        void didPressUrl(String url);
        boolean canPerformActions();
    }

    protected class MyPath extends Path {

        private StaticLayout currentLayout;
        private int currentLine;
        private float lastTop = -1;

        public void setCurrentLayout(StaticLayout layout, int start) {
            currentLayout = layout;
            currentLine = layout.getLineForOffset(start);
            lastTop = -1;
        }

        @Override
        public void addRect(float left, float top, float right, float bottom, Direction dir) {
            if (lastTop == -1) {
                lastTop = top;
            } else if (lastTop != top) {
                lastTop = top;
                currentLine++;
            }
            float lineRight = currentLayout.getLineRight(currentLine);
            float lineLeft = currentLayout.getLineLeft(currentLine);
            if (left >= lineRight) {
                return;
            }
            if (right > lineRight) {
                right = lineRight;
            }
            if (left < lineLeft) {
                left = lineLeft;
            }
            super.addRect(left, top, right, bottom, dir);
        }
    }

    protected ClickableSpan pressedLink;
    protected boolean linkPreviewPressed;
    protected MyPath urlPath = new MyPath();
    protected static Paint urlPaint;

    public boolean isChat = false;
    protected boolean isPressed = false;
    protected boolean forwardName = false;
    protected boolean isHighlighted = false;
    protected boolean media = false;
    protected boolean isCheckPressed = true;
    private boolean wasLayout = false;
    protected boolean isAvatarVisible = false;
    protected boolean drawBackground = true;
    protected MessageObject currentMessageObject;

    private static TextPaint timePaintIn;
    private static TextPaint timePaintOut;
    private static TextPaint timeMediaPaint;
    private static TextPaint namePaint;
    private static TextPaint forwardNamePaint;
    protected static TextPaint replyNamePaint;
    protected static TextPaint replyTextPaint;
    protected static Paint replyLinePaint;

    protected int backgroundWidth = 100;

    protected int layoutWidth;
    protected int layoutHeight;

    private ImageReceiver avatarImage;
    private AvatarDrawable avatarDrawable;
    private boolean avatarPressed = false;
    private boolean forwardNamePressed = false;

    private StaticLayout replyNameLayout;
    private StaticLayout replyTextLayout;
    private ImageReceiver replyImageReceiver;
    private int replyStartX;
    private int replyStartY;
    protected int replyNameWidth;
    private float replyNameOffset;
    protected int replyTextWidth;
    private float replyTextOffset;
    private boolean needReplyImage = false;
    private boolean replyPressed = false;
    private TLRPC.FileLocation currentReplyPhoto;

    private StaticLayout nameLayout;
    protected int nameWidth;
    private float nameOffsetX = 0;
    protected boolean drawName = false;

    private StaticLayout forwardedNameLayout;
    protected int forwardedNameWidth;
    protected boolean drawForwardedName = false;
    private int forwardNameX;
    private int forwardNameY;
    private float forwardNameOffsetX = 0;

    private StaticLayout timeLayout;
    protected int timeWidth;
    private int timeX;
    private TextPaint currentTimePaint;
    private String currentTimeString;
    protected boolean drawTime = true;

    private TLRPC.User currentUser;
    private TLRPC.FileLocation currentPhoto;
    private String currentNameString;

    private TLRPC.User currentForwardUser;
    private String currentForwardNameString;

    protected ChatBaseCellDelegate delegate;

    protected int namesOffset = 0;

    private int last_send_state = 0;
    private int last_delete_date = 0;

    private int leftBound = 52;//52
    private int avatarSize = AndroidUtilities.dp(42);
    protected boolean avatarAlignTop = false;
    private int avatarLeft = AndroidUtilities.dp(6);

    public ChatBaseCell(Context context) {
        super(context);
        if (timePaintIn == null) {
            timePaintIn = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintIn.setTextSize(AndroidUtilities.dp(12));
            timePaintIn.setColor(0xffa1aab3);

            timePaintOut = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintOut.setTextSize(AndroidUtilities.dp(12));
            timePaintOut.setColor(0xff70b15c);

            timeMediaPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timeMediaPaint.setTextSize(AndroidUtilities.dp(12));
            timeMediaPaint.setColor(0xffffffff);

            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(AndroidUtilities.dp(15));

            forwardNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            forwardNamePaint.setTextSize(AndroidUtilities.dp(14));

            replyNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            replyNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            replyNamePaint.setTextSize(AndroidUtilities.dp(14));

            replyTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            replyTextPaint.setTextSize(AndroidUtilities.dp(14));
            replyTextPaint.linkColor = 0xff316f9f;
            //replyTextPaint.linkColor = AndroidUtilities.getIntTColor("chatLLinkColor");

            replyLinePaint = new Paint();
        }
        avatarImage = new ImageReceiver(this);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        avatarDrawable = new AvatarDrawable();
        replyImageReceiver = new ImageReceiver(this);
        //Chat Photo
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int radius = AndroidUtilities.dp(themePrefs.getInt("chatAvatarRadius", 32));
        avatarImage.setRoundRadius(radius);
        avatarDrawable.setRadius(radius);
        avatarSize = AndroidUtilities.dp(themePrefs.getInt("chatAvatarSize", 42));
        avatarLeft  = AndroidUtilities.dp(themePrefs.getInt("chatAvatarMarginLeft", 6));
        avatarAlignTop = themePrefs.getBoolean("chatAvatarAlignTop", false);
        //setBubbles(themePrefs.getString("chatBubbleStyle", ImageListActivity.getBubbleName(0)));
    }

    private void updateTheme(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        int lColor = AndroidUtilities.getDefBubbleColor();
        int dColor = AndroidUtilities.setDarkColor(defColor, 0x15);
        int rBubbleColor = themePrefs.getInt("chatRBubbleColor", lColor);
        int rBubbleSColor = AndroidUtilities.setDarkColor(rBubbleColor, 0x15);
        int lBubbleColor = themePrefs.getInt("chatLBubbleColor", 0xffffffff);
        int lBubbleSColor = AndroidUtilities.setDarkColor(lBubbleColor, 0x15);

        timePaintOut.setColor(themePrefs.getInt("chatRTimeColor", dColor));
        timePaintOut.setTextSize(AndroidUtilities.dp(themePrefs.getInt("chatTimeSize", 12)));
        timePaintIn.setColor(themePrefs.getInt("chatLTimeColor", 0xffa1aab3));
        timePaintIn.setTextSize(AndroidUtilities.dp(themePrefs.getInt("chatTimeSize", 12)));

        int linkColor = themePrefs.getInt("chatLLinkColor", defColor);
        int bColor = AndroidUtilities.getIntAlphaColor("chatLBubbleColor", 0xffffffff, 0.9f);
        if(currentMessageObject.isOut()){
            bColor = AndroidUtilities.getIntAlphaColor("chatRBubbleColor", lColor, 0.9f);
            linkColor = themePrefs.getInt("chatRLinkColor", defColor);
        }
        replyTextPaint.linkColor = linkColor;
        //ResourceLoader.loadRecources(getContext());
        /*if(ResourceLoader.mediaBackgroundDrawable == null){
            ResourceLoader.mediaBackgroundDrawable = getResources().getDrawable(R.drawable.phototime);
            ResourceLoader.checkDrawable = getResources().getDrawable(R.drawable.msg_check);
            ResourceLoader.halfCheckDrawable = getResources().getDrawable(R.drawable.msg_halfcheck);
            ResourceLoader.clockDrawable = getResources().getDrawable(R.drawable.msg_clock);
            ResourceLoader.checkMediaDrawable = getResources().getDrawable(R.drawable.msg_check_w);
            ResourceLoader.halfCheckMediaDrawable = getResources().getDrawable(R.drawable.msg_halfcheck_w);
            ResourceLoader.clockMediaDrawable = getResources().getDrawable(R.drawable.msg_clock_photo);
            //ResourceLoader.videoIconDrawable = getResources().getDrawable(R.drawable.ic_video);
            ResourceLoader.docMenuInDrawable = getResources().getDrawable(R.drawable.doc_actions_b);
            ResourceLoader.docMenuOutDrawable = getResources().getDrawable(R.drawable.doc_actions_g);
        }*/

        ResourceLoader.mediaBackgroundDrawable.setColorFilter(bColor, PorterDuff.Mode.SRC_IN);

        ResourceLoader.backgroundDrawableOut.setColorFilter(rBubbleColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.backgroundMediaDrawableOut.setColorFilter(rBubbleColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.backgroundDrawableOutSelected.setColorFilter(rBubbleSColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.backgroundMediaDrawableOutSelected.setColorFilter(rBubbleSColor, PorterDuff.Mode.SRC_IN);

        ResourceLoader.backgroundDrawableIn.setColorFilter(lBubbleColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.backgroundMediaDrawableIn.setColorFilter(lBubbleColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.backgroundDrawableInSelected.setColorFilter(lBubbleSColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.backgroundMediaDrawableInSelected.setColorFilter(lBubbleSColor, PorterDuff.Mode.SRC_IN);

        int checksColor = themePrefs.getInt("chatChecksColor", defColor);
        ResourceLoader.checkDrawable.setColorFilter(checksColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.halfCheckDrawable.setColorFilter(checksColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.clockDrawable.setColorFilter(checksColor, PorterDuff.Mode.SRC_IN);
        ResourceLoader.checkMediaDrawable.setColorFilter(checksColor, PorterDuff.Mode.MULTIPLY);
        ResourceLoader.halfCheckMediaDrawable.setColorFilter(checksColor, PorterDuff.Mode.MULTIPLY);
        ResourceLoader.halfCheckMediaDrawable.setColorFilter(checksColor, PorterDuff.Mode.MULTIPLY);
    }



    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        avatarImage.onDetachedFromWindow();
        replyImageReceiver.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        avatarImage.onAttachedToWindow();
        replyImageReceiver.onAttachedToWindow();
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        invalidate();
    }

    protected void resetPressedLink() {
        if (pressedLink != null) {
            pressedLink = null;
        }
        linkPreviewPressed = false;
        invalidate();
    }

    public void setDelegate(ChatBaseCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void setHighlighted(boolean value) {
        if (isHighlighted == value) {
            return;
        }
        isHighlighted = value;
        invalidate();
    }

    public void setCheckPressed(boolean value, boolean pressed) {
        isCheckPressed = value;
        isPressed = pressed;
        invalidate();
    }

    protected boolean isUserDataChanged() {
        if (currentMessageObject == null || currentUser == null) {
            return false;
        }
        if (last_send_state != currentMessageObject.messageOwner.send_state) {
            return true;
        }
        if (last_delete_date != currentMessageObject.messageOwner.destroyTime) {
            return true;
        }

        TLRPC.User newUser = MessagesController.getInstance().getUser(currentMessageObject.messageOwner.from_id);
        TLRPC.FileLocation newPhoto = null;

        if (isAvatarVisible && newUser != null && newUser.photo != null) {
            newPhoto = newUser.photo.photo_small;
        }

        if (replyTextLayout == null && currentMessageObject.replyMessageObject != null) {
            return true;
        }

        if (currentPhoto == null && newPhoto != null || currentPhoto != null && newPhoto == null || currentPhoto != null && newPhoto != null && (currentPhoto.local_id != newPhoto.local_id || currentPhoto.volume_id != newPhoto.volume_id)) {
            return true;
        }

        TLRPC.FileLocation newReplyPhoto = null;

        if (currentMessageObject.replyMessageObject != null) {
            TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(currentMessageObject.replyMessageObject.photoThumbs, 80);
            if (photoSize != null && currentMessageObject.replyMessageObject.type != 13) {
                newReplyPhoto = photoSize.location;
            }
        }

        if (currentReplyPhoto == null && newReplyPhoto != null) {
            return true;
        }

        String newNameString = null;
        if (drawName && isChat && newUser != null && !currentMessageObject.isOut()) {
            newNameString = ContactsController.formatName(newUser.first_name, newUser.last_name);
        }

        if (currentNameString == null && newNameString != null || currentNameString != null && newNameString == null || currentNameString != null && newNameString != null && !currentNameString.equals(newNameString)) {
            return true;
        }

        newUser = MessagesController.getInstance().getUser(currentMessageObject.messageOwner.fwd_from_id);
        newNameString = null;
        if (newUser != null && drawForwardedName && currentMessageObject.messageOwner.fwd_from_id != 0) {
            newNameString = ContactsController.formatName(newUser.first_name, newUser.last_name);
        }
        return currentForwardNameString == null && newNameString != null || currentForwardNameString != null && newNameString == null || currentForwardNameString != null && newNameString != null && !currentForwardNameString.equals(newNameString);
    }

    protected void measureTime(MessageObject messageObject) {
        if (!media) {
            if (messageObject.isOut()) {
                currentTimePaint = timePaintOut;
            } else {
                currentTimePaint = timePaintIn;
            }
        } else {
            currentTimePaint = timeMediaPaint;
        }
        currentTimeString = LocaleController.formatterDay.format((long) (messageObject.messageOwner.date) * 1000);
        timeWidth = (int)Math.ceil(currentTimePaint.measureText(currentTimeString));
    }

    public void setMessageObject(MessageObject messageObject) {
        currentMessageObject = messageObject;
        last_send_state = messageObject.messageOwner.send_state;
        last_delete_date = messageObject.messageOwner.destroyTime;
        isPressed = false;
        isCheckPressed = true;
        isAvatarVisible = false;
        wasLayout = false;
        replyNameLayout = null;
        replyTextLayout = null;
        replyNameWidth = 0;
        replyTextWidth = 0;
        currentReplyPhoto = null;

        currentUser = MessagesController.getInstance().getUser(messageObject.messageOwner.from_id);
        if (isChat && !messageObject.isOut()) {
            isAvatarVisible = true;
            if (currentUser != null) {
                if (currentUser.photo != null) {
                    currentPhoto = currentUser.photo.photo_small;
                } else {
                    currentPhoto = null;
                }
                avatarDrawable.setInfo(currentUser);
            } else {
                currentPhoto = null;
                avatarDrawable.setInfo(messageObject.messageOwner.from_id, null, null, false);
            }
            avatarImage.setImage(currentPhoto, "50_50", avatarDrawable, false);
        }
           /*
        if (!media) {
            if (currentMessageObject.isOut()) {
                currentTimePaint = timePaintOut;
            } else {
                currentTimePaint = timePaintIn;
            }
        } else {
            currentTimePaint = timeMediaPaint;
        }*/
        if (currentMessageObject.isOut()) {
            currentTimePaint = timePaintOut;
        } else {
            currentTimePaint = timePaintIn;
        }

        currentTimeString = LocaleController.formatterDay.format((long) (currentMessageObject.messageOwner.date) * 1000);
        timeWidth = (int)Math.ceil(currentTimePaint.measureText(currentTimeString));

        namesOffset = 0;

        if (drawName && isChat && currentUser != null && !currentMessageObject.isOut()) {
            currentNameString = ContactsController.formatName(currentUser.first_name, currentUser.last_name);
            nameWidth = getMaxNameWidth();

            CharSequence nameStringFinal = TextUtils.ellipsize(currentNameString.replace("\n", " "), namePaint, nameWidth - AndroidUtilities.dp(12), TextUtils.TruncateAt.END);
            nameLayout = new StaticLayout(nameStringFinal, namePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (nameLayout.getLineCount() > 0) {
                nameWidth = (int)Math.ceil(nameLayout.getLineWidth(0));
                namesOffset += AndroidUtilities.dp(19);
                nameOffsetX = nameLayout.getLineLeft(0);
            } else {
                nameWidth = 0;
            }
        } else {
            currentNameString = null;
            nameLayout = null;
            nameWidth = 0;
        }

        if (drawForwardedName && messageObject.isForwarded()) {
            currentForwardUser = MessagesController.getInstance().getUser(messageObject.messageOwner.fwd_from_id);
            if (currentForwardUser != null) {
                currentForwardNameString = ContactsController.formatName(currentForwardUser.first_name, currentForwardUser.last_name);

                forwardedNameWidth = getMaxNameWidth();

                CharSequence str = TextUtils.ellipsize(currentForwardNameString.replace("\n", " "), forwardNamePaint, forwardedNameWidth - AndroidUtilities.dp(40), TextUtils.TruncateAt.END);
                str = AndroidUtilities.replaceTags(String.format("%s\n%s <b>%s</b>", LocaleController.getString("ForwardedMessage", R.string.ForwardedMessage), LocaleController.getString("From", R.string.From), str));
                forwardedNameLayout = StaticLayoutEx.createStaticLayout(str, forwardNamePaint, forwardedNameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TextUtils.TruncateAt.END, forwardedNameWidth, 2);
                if (forwardedNameLayout.getLineCount() > 1) {
                    forwardedNameWidth = Math.max((int) Math.ceil(forwardedNameLayout.getLineWidth(0)), (int) Math.ceil(forwardedNameLayout.getLineWidth(1)));
                    namesOffset += AndroidUtilities.dp(36);
                    forwardNameOffsetX = Math.min(forwardedNameLayout.getLineLeft(0), forwardedNameLayout.getLineLeft(1));
                } else {
                    forwardedNameWidth = 0;
                }
            } else {
                currentForwardNameString = null;
                forwardedNameLayout = null;
                forwardedNameWidth = 0;
            }
        } else {
            currentForwardNameString = null;
            forwardedNameLayout = null;
            forwardedNameWidth = 0;
        }

        if (messageObject.isReply()) {
            namesOffset += AndroidUtilities.dp(42);
            if (messageObject.contentType == 2 || messageObject.contentType == 3) {
                namesOffset += AndroidUtilities.dp(4);
            } else if (messageObject.contentType == 1) {
                if (messageObject.type == 13) {
                    namesOffset -= AndroidUtilities.dp(42);
                } else {
                    namesOffset += AndroidUtilities.dp(5);
                }
            }

            int maxWidth;
            if (messageObject.type == 13) {
                int width;
                if (AndroidUtilities.isTablet()) {
                    if (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        width = AndroidUtilities.displaySize.x;
                    } else {
                    int leftWidth = AndroidUtilities.displaySize.x / 100 * 35;
                    if (leftWidth < AndroidUtilities.dp(320)) {
                        leftWidth = AndroidUtilities.dp(320);
                    }
                    width = AndroidUtilities.displaySize.x - leftWidth;
                    }
                } else {
                    width = AndroidUtilities.displaySize.x;
                }
                if (messageObject.isOut()) {
                    maxWidth = width - backgroundWidth - AndroidUtilities.dp(60);
                } else {
                    maxWidth = width - backgroundWidth - AndroidUtilities.dp(56 + (isChat ? 61 : 0));
                }
            } else {
                maxWidth = getMaxNameWidth() - AndroidUtilities.dp(22);
            }
            if (!media && messageObject.contentType != 0) {
                maxWidth -= AndroidUtilities.dp(8);
            }

            CharSequence stringFinalName = null;
            CharSequence stringFinalText = null;
            if (messageObject.replyMessageObject != null) {
                TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.replyMessageObject.photoThumbs, 80);
                if (photoSize == null || messageObject.replyMessageObject.type == 13 || messageObject.type == 13 && !AndroidUtilities.isTablet()) {
                    replyImageReceiver.setImageBitmap((Drawable) null);
                    needReplyImage = false;
                } else {
                    currentReplyPhoto = photoSize.location;
                    replyImageReceiver.setImage(photoSize.location, "50_50", null, true);
                    needReplyImage = true;
                    maxWidth -= AndroidUtilities.dp(44);
                }

                TLRPC.User user = MessagesController.getInstance().getUser(messageObject.replyMessageObject.messageOwner.from_id);
                if (user != null) {
                    stringFinalName = TextUtils.ellipsize(ContactsController.formatName(user.first_name, user.last_name).replace("\n", " "), replyNamePaint, maxWidth - AndroidUtilities.dp(8), TextUtils.TruncateAt.END);
                }
                if (messageObject.replyMessageObject.messageText != null && messageObject.replyMessageObject.messageText.length() > 0) {
                    String mess = messageObject.replyMessageObject.messageText.toString();
                    if (mess.length() > 150) {
                        mess = mess.substring(0, 150);
                    }
                    mess = mess.replace("\n", " ");
                    stringFinalText = Emoji.replaceEmoji(mess, replyTextPaint.getFontMetricsInt(), AndroidUtilities.dp(14));
                    stringFinalText = TextUtils.ellipsize(stringFinalText, replyTextPaint, maxWidth - AndroidUtilities.dp(8), TextUtils.TruncateAt.END);
                }
            }
            if (stringFinalName == null) {
                stringFinalName = LocaleController.getString("Loading", R.string.Loading);
            }
            try {
            replyNameLayout = new StaticLayout(stringFinalName, replyNamePaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (replyNameLayout.getLineCount() > 0) {
                replyNameWidth = (int)Math.ceil(replyNameLayout.getLineWidth(0)) + AndroidUtilities.dp(12 + (needReplyImage ? 44 : 0));
                replyNameOffset = replyNameLayout.getLineLeft(0);
            }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            try {
            if (stringFinalText != null) {
                replyTextLayout = new StaticLayout(stringFinalText, replyTextPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (replyTextLayout.getLineCount() > 0) {
                    replyTextWidth = (int) Math.ceil(replyTextLayout.getLineWidth(0)) + AndroidUtilities.dp(12 + (needReplyImage ? 44 : 0));
                    replyTextOffset = replyTextLayout.getLineLeft(0);
                }
            }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        requestLayout();
    }

    public final MessageObject getMessageObject() {
        return currentMessageObject;
    }

    protected int getMaxNameWidth() {
        return backgroundWidth - AndroidUtilities.dp(8);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (delegate == null || delegate.canPerformActions()) {
                if (isAvatarVisible && avatarImage.isInsideImage(x, y)) {
                    avatarPressed = true;
                    result = true;
                } else if (drawForwardedName && forwardedNameLayout != null) {
                    if (x >= forwardNameX && x <= forwardNameX + forwardedNameWidth && y >= forwardNameY && y <= forwardNameY + AndroidUtilities.dp(32)) {
                        forwardNamePressed = true;
                        result = true;
                    }
                } else if (currentMessageObject.isReply()) {
                    if (x >= replyStartX && x <= replyStartX + Math.max(replyNameWidth, replyTextWidth) && y >= replyStartY && y <= replyStartY + AndroidUtilities.dp(35)) {
                        replyPressed = true;
                        result = true;
                    }
                }
                if (result) {
                    startCheckLongPress();
                }
            }
        } else {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                cancelCheckLongPress();
            }
            if (avatarPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    avatarPressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (delegate != null) {
                        delegate.didPressedUserAvatar(this, currentUser);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    avatarPressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (isAvatarVisible && !avatarImage.isInsideImage(x, y)) {
                        avatarPressed = false;
                    }
                }
            } else if (forwardNamePressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    forwardNamePressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (delegate != null) {
                        delegate.didPressedUserAvatar(this, currentForwardUser);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    forwardNamePressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= forwardNameX && x <= forwardNameX + forwardedNameWidth && y >= forwardNameY && y <= forwardNameY + AndroidUtilities.dp(32))) {
                        forwardNamePressed = false;
                    }
                }
            } else if (replyPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    replyPressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (delegate != null) {
                        delegate.didPressReplyMessage(this, currentMessageObject.messageOwner.reply_to_msg_id);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    replyPressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= replyStartX && x <= replyStartX + Math.max(replyNameWidth, replyTextWidth) && y >= replyStartY && y <= replyStartY + AndroidUtilities.dp(35))) {
                        replyPressed = false;
                    }
                }
            }
        }
        return result;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (currentMessageObject == null) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }

        if (changed || !wasLayout) {
            layoutWidth = getMeasuredWidth();
            layoutHeight = getMeasuredHeight();

            timeLayout = new StaticLayout(currentTimeString, currentTimePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (!media) {
                if (!currentMessageObject.isOut()) {
                    timeX = backgroundWidth - AndroidUtilities.dp(9) - timeWidth + (isChat ? AndroidUtilities.dp(leftBound) : 0);
                } else {
                    timeX = layoutWidth - timeWidth - AndroidUtilities.dp(38.5f);
                }
            } else {
                if (!currentMessageObject.isOut()) {
                    timeX = backgroundWidth - AndroidUtilities.dp(4) - timeWidth + (isChat ? AndroidUtilities.dp(leftBound) : 0);
                } else {
                    timeX = layoutWidth - timeWidth - AndroidUtilities.dp(42.0f);
                }
            }

            if (isAvatarVisible) {
                //avatarImage.setImageCoords(AndroidUtilities.dp(6), layoutHeight - AndroidUtilities.dp(45), AndroidUtilities.dp(42), AndroidUtilities.dp(42));
                avatarImage.setImageCoords(avatarLeft, avatarAlignTop ? AndroidUtilities.dp(3) : layoutHeight - AndroidUtilities.dp(3) - avatarSize, avatarSize, avatarSize);
            }

            wasLayout = true;
        }
    }

    protected void onAfterBackgroundDraw(Canvas canvas) {

    }

    @Override
    protected void onLongPress() {
        if (delegate != null) {
            delegate.didLongPressed(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (currentMessageObject == null) {
            return;
        }

        if (!wasLayout) {
            requestLayout();
            return;
        }

        if (isAvatarVisible) {
            avatarImage.draw(canvas);
        }
        updateTheme();
        Drawable currentBackgroundDrawable = null;
        if (currentMessageObject.isOut()) {
            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed || isHighlighted) {
                if (!media) {
                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableOutSelected;
                } else {
                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOutSelected;
                }
            } else {
                if (!media) {
                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableOut;
                } else {
                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOut;
                }
            }
            setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - (!media ? 0 : AndroidUtilities.dp(9)), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - AndroidUtilities.dp(2));
        } else {
            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed || isHighlighted) {
                if (!media) {
                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableInSelected;
                } else {
                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableInSelected;
                }
            } else {
                if (!media) {
                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableIn;
                } else {
                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableIn;
                }
            }
            if (isChat) {
                setDrawableBounds(currentBackgroundDrawable, AndroidUtilities.dp(leftBound + (!media ? 0 : 9)), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - AndroidUtilities.dp(2));
            } else {
                setDrawableBounds(currentBackgroundDrawable, (!media ? 0 : AndroidUtilities.dp(9)), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - AndroidUtilities.dp(2));
            }
        }
        if (drawBackground) {
            currentBackgroundDrawable.draw(canvas);
        }

        onAfterBackgroundDraw(canvas);
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        if (drawName && nameLayout != null) {
            canvas.save();
            canvas.translate(currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(19) - nameOffsetX, AndroidUtilities.dp(10));
            if(AndroidUtilities.getBoolPref("chatMemberColorCheck")){
                namePaint.setColor(themePrefs.getInt("chatMemberColor", AndroidUtilities.getIntDarkerColor("themeColor", 0x15)));
            }else{
                namePaint.setColor(AvatarDrawable.getNameColorForId(currentUser.id));
            }
            nameLayout.draw(canvas);
            canvas.restore();
        }

        if (drawForwardedName && forwardedNameLayout != null) {
            forwardNameY = AndroidUtilities.dp(10 + (drawName ? 19 : 0));
            int defColor = themePrefs.getInt("themeColor",AndroidUtilities.defColor);
            if (currentMessageObject.isOut()) {
                //forwardNamePaint.setColor(0xff4a923c);
                forwardNamePaint.setColor(themePrefs.getInt("chatForwardColor", AndroidUtilities.setDarkColor(defColor, 0x15)));
                forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(10);
            } else {
                //forwardNamePaint.setColor(0xff006fc8);
                forwardNamePaint.setColor(themePrefs.getInt("chatForwardColor", defColor));
                forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(19);
            }
            canvas.save();
            canvas.translate(forwardNameX - forwardNameOffsetX, forwardNameY);
            forwardedNameLayout.draw(canvas);
            canvas.restore();
        }

        if (currentMessageObject.isReply()) {
            if (currentMessageObject.type == 13) {
                replyLinePaint.setColor(0xffffffff);
                replyNamePaint.setColor(0xffffffff);
                replyTextPaint.setColor(0xffffffff);
                int backWidth;
                if (currentMessageObject.isOut()) {
                    backWidth = currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(32);
                    replyStartX = currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(9) - backWidth;
                } else {
                    backWidth = getWidth() - currentBackgroundDrawable.getBounds().right - AndroidUtilities.dp(32);
                    replyStartX = currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(23);
                }
                Drawable back;
                if (ApplicationLoader.isCustomTheme()) {
                    back = ResourceLoader.backgroundBlack;
                } else {
                    back = ResourceLoader.backgroundBlue;
                }
                replyStartY = layoutHeight - AndroidUtilities.dp(58);
                back.setBounds(replyStartX - AndroidUtilities.dp(7), replyStartY - AndroidUtilities.dp(6), replyStartX - AndroidUtilities.dp(7) + backWidth, replyStartY + AndroidUtilities.dp(41));
                back.draw(canvas);
            } else {
                int color = themePrefs.getInt("chatForwardColor", AndroidUtilities.getIntDarkerColor("themeColor", 0x15));
                if (currentMessageObject.isOut()) {
                    replyLinePaint.setColor(color);//0xff8dc97a);
                    replyNamePaint.setColor(color);//0xff61a349);
                    int outColor = themePrefs.getInt("chatRTextColor", 0xff000000);
                    if (currentMessageObject.replyMessageObject != null && currentMessageObject.replyMessageObject.type == 0) {
                        replyTextPaint.setColor(outColor);//0xff000000);
                    } else {
                        replyTextPaint.setColor(color);//0xff70b15c);
                    }
                    replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11);
                } else {
                    replyLinePaint.setColor(color);//0xff6c9fd2);
                    replyNamePaint.setColor(color);//0xff377aae);
                    int inColor = themePrefs.getInt("chatLTextColor", 0xff000000);
                    if (currentMessageObject.replyMessageObject != null && currentMessageObject.replyMessageObject.type == 0) {
                        replyTextPaint.setColor(inColor);//0xff000000);
                    } else {
                        replyTextPaint.setColor(color);//0xff999999);
                    }
                    if (currentMessageObject.contentType == 1 && media) {
                        replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11);
                    } else {
                        replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(20);
                    }
                }
                replyStartY = AndroidUtilities.dp(12 + (drawForwardedName && forwardedNameLayout != null ? 36 : 0) + (drawName && nameLayout != null ? 20 : 0));
            }
            canvas.drawRect(replyStartX, replyStartY, replyStartX + AndroidUtilities.dp(2), replyStartY + AndroidUtilities.dp(35), replyLinePaint);
            if (needReplyImage) {
                replyImageReceiver.setImageCoords(replyStartX + AndroidUtilities.dp(10), replyStartY, AndroidUtilities.dp(35), AndroidUtilities.dp(35));
                replyImageReceiver.draw(canvas);
            }
            if (replyNameLayout != null) {
                canvas.save();
                canvas.translate(replyStartX - replyNameOffset + AndroidUtilities.dp(10 + (needReplyImage ? 44 : 0)), replyStartY);
                replyNameLayout.draw(canvas);
                canvas.restore();
            }
            if (replyTextLayout != null) {
                canvas.save();
                canvas.translate(replyStartX - replyTextOffset + AndroidUtilities.dp(10 + (needReplyImage ? 44 : 0)), replyStartY + AndroidUtilities.dp(19));
                replyTextLayout.draw(canvas);
                canvas.restore();
            }
        }

        if (drawTime || !media) {
            if (media) {
                setDrawableBounds(ResourceLoader.mediaBackgroundDrawable, timeX - AndroidUtilities.dp(3), layoutHeight - AndroidUtilities.dp(27.5f), timeWidth + AndroidUtilities.dp(6 + (currentMessageObject.isOut() ? 20 : 0)), AndroidUtilities.dp(16.5f));
                ResourceLoader.mediaBackgroundDrawable.draw(canvas);

                canvas.save();
                canvas.translate(timeX, layoutHeight - AndroidUtilities.dp(12.0f) - timeLayout.getHeight());
                timeLayout.draw(canvas);
                canvas.restore();
            } else {
                canvas.save();
                canvas.translate(timeX, layoutHeight - AndroidUtilities.dp(6.5f) - timeLayout.getHeight());
                timeLayout.draw(canvas);
                canvas.restore();
            }

            if (currentMessageObject.isOut()) {
                boolean drawCheck1 = false;
                boolean drawCheck2 = false;
                boolean drawClock = false;
                boolean drawError = false;
                boolean isBroadcast = (int)(currentMessageObject.getDialogId() >> 32) == 1;

                if (currentMessageObject.isSending()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = true;
                    drawError = false;
                } else if (currentMessageObject.isSendError()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = false;
                    drawError = true;
                } else if (currentMessageObject.isSent()) {
                    if (!currentMessageObject.isUnread()) {
                        drawCheck1 = true;
                        drawCheck2 = true;
                    } else {
                        drawCheck1 = false;
                        drawCheck2 = true;
                    }
                    drawClock = false;
                    drawError = false;
                }

                if (drawClock) {
                    //ResourceLoader.clockDrawable = ResourceLoader.clockMediaDrawable;
                    if (!media) {
                        setDrawableBounds(ResourceLoader.clockDrawable, layoutWidth - AndroidUtilities.dp(18.5f) - ResourceLoader.clockDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.5f) - ResourceLoader.clockDrawable.getIntrinsicHeight());
                        ResourceLoader.clockDrawable.draw(canvas);
                    } else {
                        setDrawableBounds(ResourceLoader.clockMediaDrawable, layoutWidth - AndroidUtilities.dp(22.0f) - ResourceLoader.clockMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.clockMediaDrawable.getIntrinsicHeight());
                        ResourceLoader.clockMediaDrawable.draw(canvas);
                    }
                }
                if (isBroadcast) {
                    if (drawCheck1 || drawCheck2) {
                        if (!media) {
                            setDrawableBounds(ResourceLoader.broadcastDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.broadcastDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.broadcastDrawable.getIntrinsicHeight());
                            ResourceLoader.broadcastDrawable.draw(canvas);
                        } else {
                            setDrawableBounds(ResourceLoader.broadcastMediaDrawable, layoutWidth - AndroidUtilities.dp(24.0f) - ResourceLoader.broadcastMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.broadcastMediaDrawable.getIntrinsicHeight());
                            ResourceLoader.broadcastMediaDrawable.draw(canvas);
                        }
                    }
                } else {
                    if (drawCheck2) {
                        //ResourceLoader.checkDrawable = ResourceLoader.checkMediaDrawable;
                        if (!media) {
                            if (drawCheck1) {
                                setDrawableBounds(ResourceLoader.checkDrawable, layoutWidth - AndroidUtilities.dp(22.5f) - ResourceLoader.checkDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.checkDrawable.getIntrinsicHeight());
                            } else {
                                setDrawableBounds(ResourceLoader.checkDrawable, layoutWidth - AndroidUtilities.dp(18.5f) - ResourceLoader.checkDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.checkDrawable.getIntrinsicHeight());
                            }
                            ResourceLoader.checkDrawable.draw(canvas);
                        } else {
                            if (drawCheck1) {
                                setDrawableBounds(ResourceLoader.checkMediaDrawable, layoutWidth - AndroidUtilities.dp(26.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicHeight());
                            } else {
                                setDrawableBounds(ResourceLoader.checkMediaDrawable, layoutWidth - AndroidUtilities.dp(22.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicHeight());
                            }
                            ResourceLoader.checkMediaDrawable.draw(canvas);
                        }
                    }
                    if (drawCheck1) {
                        //ResourceLoader.halfCheckDrawable = ResourceLoader.halfCheckMediaDrawable;
                        if (!media) {
                            setDrawableBounds(ResourceLoader.halfCheckDrawable, layoutWidth - AndroidUtilities.dp(18) - ResourceLoader.halfCheckDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.halfCheckDrawable.getIntrinsicHeight());
                            ResourceLoader.halfCheckDrawable.draw(canvas);
                        } else {
                            setDrawableBounds(ResourceLoader.halfCheckMediaDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.halfCheckMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.halfCheckMediaDrawable.getIntrinsicHeight());
                            ResourceLoader.halfCheckMediaDrawable.draw(canvas);
                        }
                    }
                }
                if (drawError) {
                    if (!media) {
                        setDrawableBounds(ResourceLoader.errorDrawable, layoutWidth - AndroidUtilities.dp(18) - ResourceLoader.errorDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(6.5f) - ResourceLoader.errorDrawable.getIntrinsicHeight());
                        ResourceLoader.errorDrawable.draw(canvas);
                    } else {
                        setDrawableBounds(ResourceLoader.errorDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.errorDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(12.5f) - ResourceLoader.errorDrawable.getIntrinsicHeight());
                        ResourceLoader.errorDrawable.draw(canvas);
                    }
                }
            }
        }
    }
}
