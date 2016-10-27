/*
 * This is the source code of VshGap for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import org.vshgap.android.AndroidUtilities;
import org.vshgap.android.AnimationCompat.ViewProxy;
import org.vshgap.android.ContactsController;
import org.vshgap.android.LocaleController;
import org.vshgap.android.MessagesController;
import org.vshgap.messenger.ApplicationLoader;
import org.vshgap.messenger.R;
import org.vshgap.messenger.TLRPC;
import org.vshgap.ui.Cells.DividerCell;
import org.vshgap.ui.Cells.GreySectionCell;
import org.vshgap.ui.Cells.LetterSectionCell;
import org.vshgap.ui.Cells.TextCell;
import org.vshgap.ui.Cells.UserCell;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsAdapter extends BaseSectionsAdapter {

    private Context mContext;
    private boolean onlyUsers;
    private boolean needPhonebook;
    private HashMap<Integer, TLRPC.User> ignoreUsers;
    private HashMap<Integer, ?> checkedMap;
    private boolean scrolling;
    private boolean isAdmin;

    public ContactsAdapter(Context context, boolean arg1, boolean arg2, HashMap<Integer, TLRPC.User> arg3, boolean arg4) {
        mContext = context;
        onlyUsers = arg1;
        needPhonebook = arg2;
        ignoreUsers = arg3;
        isAdmin = arg4;
    }

    public void setCheckedMap(HashMap<Integer, ?> map) {
        checkedMap = map;
    }

    public void setIsScrolling(boolean value) {
        scrolling = value;
    }

    @Override
    public Object getItem(int section, int position) {
        if (onlyUsers && !isAdmin) {
            if (section < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
                if (position < arr.size()) {
                    return MessagesController.getInstance().getUser(arr.get(position).user_id);
                }
            }
            return null;
        } else {
            if (section == 0) {
                return null;
            } else {
                if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                    ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
                    if (position < arr.size()) {
                        return MessagesController.getInstance().getUser(arr.get(position).user_id);
                    }
                    return null;
                }
            }
        }
        if (needPhonebook) {
            return ContactsController.getInstance().phoneBookContacts.get(position);
        }
        return null;
    }

    @Override
    public boolean isRowEnabled(int section, int row) {
        if (onlyUsers && !isAdmin) {
            ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
            return row < arr.size();
        } else {
            if (section == 0) {
                if (needPhonebook || isAdmin) {
                    if (row == 1) {
                        return false;
                    }
                } else {
                    if (row == 3) {
                        return false;
                    }
                }
                return true;
            } else if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
                return row < arr.size();
            }
        }
        return true;
    }

    @Override
    public int getSectionCount() {
        int count = ContactsController.getInstance().sortedUsersSectionsArray.size();
        if (!onlyUsers) {
            count++;
        }
        if (isAdmin) {
            count++;
        }
        if (needPhonebook) {
            count++;
        }
        return count;
    }

    @Override
    public int getCountForSection(int section) {
        if (onlyUsers && !isAdmin) {
            if (section < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
                int count = arr.size();
                if (section != (ContactsController.getInstance().sortedUsersSectionsArray.size() - 1) || needPhonebook) {
                    count++;
                }
                return count;
            }
        } else {
            if (section == 0) {
                if (needPhonebook || isAdmin) {
                    return 2;
                } else {
                    return 4;
                }
            } else if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
                int count = arr.size();
                if (section - 1 != (ContactsController.getInstance().sortedUsersSectionsArray.size() - 1) || needPhonebook) {
                    count++;
                }
                return count;
            }
        }
        if (needPhonebook) {
            return ContactsController.getInstance().phoneBookContacts.size();
        }
        return 0;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new LetterSectionCell(mContext);
        }
        if (onlyUsers && !isAdmin) {
            if (section < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ((LetterSectionCell) convertView).setLetter(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
            } else {
                ((LetterSectionCell) convertView).setLetter("");
            }
        } else {
            if (section == 0) {
                ((LetterSectionCell) convertView).setLetter("");
            } else if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ((LetterSectionCell) convertView).setLetter(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
            } else {
                ((LetterSectionCell) convertView).setLetter("");
            }
        }
        ((LetterSectionCell) convertView).setLetterColor(AndroidUtilities.getIntDef("contactsNameColor", 0xff808080)); //Plus
        return convertView;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(section, position);
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int cColorGrey = themePrefs.getInt("contactsNameColor", 0xff737373);
        int cColorBlack = themePrefs.getInt("contactsNameColor", 0xff000000);
        if (type == 4) {
            if (convertView == null) {
                convertView = new DividerCell(mContext);
                convertView.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 28 : 72), 0, AndroidUtilities.dp(LocaleController.isRTL ? 72 : 28), 0);
                convertView.setTag("contactsRowColor"); //Plus
            }
        } else if (type == 3) {
            if (convertView == null) {
                convertView = new GreySectionCell(mContext);
                ((GreySectionCell) convertView).setText(LocaleController.getString("Contacts", R.string.Contacts).toUpperCase());
                //((GreySectionCell) convertView).setText(String.format(Locale.US, " %d " + LocaleController.getString("Contacts", R.string.Contacts).toUpperCase(), arr0.size()));
                ((GreySectionCell) convertView).setBackgroundColor(themePrefs.getInt("contactsRowColor", 0xffffffff));
                ((GreySectionCell) convertView).setTextColor(cColorGrey);
            }
        } else if (type == 2) {
            if (convertView == null) {
                convertView = new TextCell(mContext);
            }
            TextCell actionCell = (TextCell) convertView;
            actionCell.setTextColor(cColorBlack);
            if (needPhonebook) {
                //actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite);
                Drawable invite = mContext.getResources().getDrawable(R.drawable.menu_invite);
                invite.setColorFilter(cColorGrey, PorterDuff.Mode.SRC_IN);
                actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), invite);
            } else if (isAdmin) {
                //actionCell.setTextAndIcon(LocaleController.getString("InviteToGroupByLink", R.string.InviteToGroupByLink), R.drawable.menu_invite);
                Drawable invite = mContext.getResources().getDrawable(R.drawable.menu_invite);
                invite.setColorFilter(cColorGrey, PorterDuff.Mode.SRC_IN);
                actionCell.setTextAndIcon(LocaleController.getString("InviteToGroupByLink", R.string.InviteToGroupByLink), invite);
            } else {
                if (position == 0) {
                    //actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup);
                    Drawable newGroup = mContext.getResources().getDrawable(R.drawable.menu_newgroup);
                    newGroup.setColorFilter(cColorGrey, PorterDuff.Mode.SRC_IN);
                    actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), newGroup);
                } else if (position == 1) {
                    //actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret);
                    Drawable secret = mContext.getResources().getDrawable(R.drawable.menu_secret);
                    secret.setColorFilter(cColorGrey, PorterDuff.Mode.SRC_IN);
                    actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), secret);
                } else if (position == 2) {
                    //actionCell.setTextAndIcon(LocaleController.getString("NewBroadcastList", R.string.NewBroadcastList), R.drawable.menu_broadcast);
                    Drawable broadcast = mContext.getResources().getDrawable(R.drawable.menu_broadcast);
                    broadcast.setColorFilter(cColorGrey, PorterDuff.Mode.SRC_IN);
                    actionCell.setTextAndIcon(LocaleController.getString("NewBroadcastList", R.string.NewBroadcastList), broadcast);
                }
            }
        } else if (type == 1) {
            if (convertView == null) {
                convertView = new TextCell(mContext);
                ((TextCell) convertView).setTextColor(cColorBlack);
                ((TextCell) convertView).setTextSize(themePrefs.getInt("contactsNameSize", 16));
            }
            ContactsController.Contact contact = ContactsController.getInstance().phoneBookContacts.get(position);
            if (contact.first_name != null && contact.last_name != null) {
                ((TextCell) convertView).setText(contact.first_name + " " + contact.last_name);
            } else if (contact.first_name != null && contact.last_name == null) {
                ((TextCell) convertView).setText(contact.first_name);
            } else {
                ((TextCell) convertView).setText(contact.last_name);
            }
        } else if (type == 0) {
            if (convertView == null) {
                convertView = new UserCell(mContext, 58);
                //((UserCell) convertView).setStatusColors(0xffa8a8a8, 0xff3b84c0);
                ((UserCell) convertView).setStatusColors(themePrefs.getInt("contactsStatusColor", 0xffa8a8a8), themePrefs.getInt("contactsOnlineColor", AndroidUtilities.getIntDarkerColor("themeColor", 0x15)));
                ((UserCell) convertView).setNameColor(cColorBlack);
                ((UserCell) convertView).setAvatarRadius(themePrefs.getInt("contactsAvatarRadius", 32));
            }

            ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - (onlyUsers && !isAdmin ? 0 : 1)));
            TLRPC.User user = MessagesController.getInstance().getUser(arr.get(position).user_id);
            ((UserCell)convertView).setData(user, null, null, 0);
            if (checkedMap != null) {
                ((UserCell) convertView).setChecked(checkedMap.containsKey(user.id), !scrolling  && Build.VERSION.SDK_INT > 10);
            }
            if (ignoreUsers != null) {
                if (ignoreUsers.containsKey(user.id)) {
                    ViewProxy.setAlpha(convertView, 0.5f);
                } else {
                    ViewProxy.setAlpha(convertView, 1.0f);
                }
            }
        }
        parent.setBackgroundColor(themePrefs.getInt("contactsRowColor", 0xffffffff)); //Plus
        return convertView;
    }

    @Override
    public int getItemViewType(int section, int position) {
        if (onlyUsers && !isAdmin) {
            ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
            return position < arr.size() ? 0 : 4;
        } else {
            if (section == 0) {
                if (needPhonebook || isAdmin) {
                    if (position == 1) {
                        return 3;
                    }
                } else {
                    if (position == 3) {
                        return 3;
                    }
                }
                return 2;
            } else if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
                return position < arr.size() ? 0 : 4;
            }
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }
}
