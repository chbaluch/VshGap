/*
 * This is the source code of VshGap for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.vshgap.ui.Adapters;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.vshgap.android.LocaleController;
import org.vshgap.android.MessagesController;
import org.vshgap.messenger.ApplicationLoader;
import org.vshgap.messenger.FileLog;
import org.vshgap.messenger.R;
import org.vshgap.messenger.UserConfig;
import org.vshgap.ui.Cells.DividerCell;
import org.vshgap.ui.Cells.DrawerActionCell;
import org.vshgap.ui.Cells.DrawerProfileCell;
import org.vshgap.ui.Cells.EmptyCell;
import org.vshgap.ui.Cells.TextInfoCell;

import java.util.Locale;

public class DrawerLayoutAdapter extends BaseAdapter {

    private Context mContext;
    private int versionType = 4;
    private int versionRow = 11;
    private int contactsRow = 6;
    private int settingsRow = 9;
    private int themingRow = 8;
    private int communityRow = 10;
    private int themesRow = 7;

    //private int rowCount = 0;

    public DrawerLayoutAdapter(Context context) {
        mContext = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return !(i == 0 || i == 1 || i == 5);
    }

    @Override
    public int getCount() {
        //return UserConfig.isClientActivated() ? 10 : 0;
        return UserConfig.isClientActivated() ? 12 : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);
        if (type == 0) {
            if (view == null) {
                view = new DrawerProfileCell(mContext);
            }
            ((DrawerProfileCell) view).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
        } else if (type == 1) {
            if (view == null) {
                view = new EmptyCell(mContext, 8);
            }
        } else if (type == 2) {
            if (view == null) {
                view = new DividerCell(mContext);
                view.setTag("drawerListColor");
            }
        } else if (type == 3) {
            if (view == null) {
                view = new DrawerActionCell(mContext);
            }
            DrawerActionCell actionCell = (DrawerActionCell) view;
            if (i == 2) {
                actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup);
            } else if (i == 3) {
                actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret);
            } else if (i == 4) {
                actionCell.setTextAndIcon(LocaleController.getString("NewBroadcastList", R.string.NewBroadcastList), R.drawable.menu_broadcast);
            } else if (i == contactsRow) {
                actionCell.setTextAndIcon(LocaleController.getString("Contacts", R.string.Contacts), R.drawable.menu_contacts);
            }/* else if (i == 7) {
                actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite);
            }*/ else if (i == themesRow) {
                actionCell.setTextAndIcon(LocaleController.getString("Themes", R.string.Themes), R.drawable.menu_themes);
            } else if (i == themingRow) {
                actionCell.setTextAndIcon(LocaleController.getString("Theming", R.string.Theming), R.drawable.menu_theming);
            } else if (i == settingsRow) {
                actionCell.setTextAndIcon(LocaleController.getString("Settings", R.string.Settings), R.drawable.menu_settings);
            } else if (i == communityRow) {
                actionCell.setTextAndIcon(LocaleController.getString("Community", R.string.Community), R.drawable.menu_forum);
            } /*else if (i == 10) {
                actionCell.setTextAndIcon(LocaleController.getString("VshGapFaq", R.string.VshGapFaq), R.drawable.menu_help);
            }*/
        }  else if (type == versionType) {
            view = new TextInfoCell(mContext);
            if (i == versionRow) {
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    ((TextInfoCell) view).setText(String.format(Locale.US, LocaleController.getString("VshGapForAndroid", R.string.VshGapForAndroid)+" v%s (%d)", pInfo.versionName, pInfo.versionCode));
                    //((TextInfoCell) view).setTextColor(AndroidUtilities.getIntDef("drawerVersionColor",0xffa3a3a3));
                    //((TextInfoCell) view).setTextSize(AndroidUtilities.getIntDef("drawerVersionSize",13));
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
        }
        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        } else if (i == 5) {
            return 2;
        }
        //new
        else if (i == versionRow) {
            return versionType;
        }
        //
        return 3;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public boolean isEmpty() {
        return !UserConfig.isClientActivated();
    }
}
