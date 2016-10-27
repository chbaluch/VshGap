/*
 * This is the source code of VshGap for Android v. 2.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.vshgap.ui.Cells;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.vshgap.messenger.TLRPC;
import org.vshgap.ui.Components.BackupImageView;
import org.vshgap.ui.Components.LayoutHelper;

public class StickerEmojiCell extends FrameLayout {

    private BackupImageView imageView;
    private TLRPC.Document sticker;

    public StickerEmojiCell(Context context) {
        super(context);

        imageView = new BackupImageView(context);
        imageView.setAspectFit(true);
        addView(imageView, LayoutHelper.createFrame(66, 66, Gravity.CENTER));
    }

    @Override
    public void setPressed(boolean pressed) {
        if (imageView.getImageReceiver().getPressed() != pressed) {
            imageView.getImageReceiver().setPressed(pressed);
            imageView.invalidate();
        }
        super.setPressed(pressed);
    }

    public TLRPC.Document getSticker() {
        return sticker;
    }

    public void setSticker(TLRPC.Document document) {
        if (document != null) {
            sticker = document;
            document.thumb.location.ext = "webp";
            imageView.setImage(document.thumb.location, null, (Drawable) null);
        }
    }
}
