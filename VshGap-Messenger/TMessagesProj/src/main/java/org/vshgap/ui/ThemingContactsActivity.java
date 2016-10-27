/*
 * This is the source code of VshGap for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.vshgap.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.vshgap.android.AndroidUtilities;
import org.vshgap.android.LocaleController;
import org.vshgap.messenger.ApplicationLoader;
import org.vshgap.messenger.R;
import org.vshgap.ui.ActionBar.ActionBar;
import org.vshgap.ui.ActionBar.BaseFragment;
import org.vshgap.ui.Adapters.BaseFragmentAdapter;
import org.vshgap.ui.Cells.HeaderCell;
import org.vshgap.ui.Cells.ShadowSectionCell;
import org.vshgap.ui.Cells.TextColorCell;
import org.vshgap.ui.Cells.TextSettingsCell;
import org.vshgap.ui.Components.AvatarDrawable;
import org.vshgap.ui.Components.ColorSelectorDialog;
import org.vshgap.ui.Components.NumberPicker;

import static org.vshgap.ui.Components.ColorSelectorDialog.OnColorChangedListener;

public class ThemingContactsActivity extends BaseFragment {

    private ListView listView;
    private ListAdapter listAdapter;

    private int headerSection2Row;
    private int headerColorRow;
    private int headerTitleColorRow;
    private int headerIconsColorRow;

    private int rowsSectionRow;
    private int rowsSection2Row;
    private int rowColorRow;
    private int avatarRadiusRow;
    private int nameColorRow;
    private int nameSizeRow;
    private int statusColorRow;
    private int statusSizeRow;
    private int onlineColorRow;

    private int rowCount;

    public final static int CENTER = 0;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;
        headerSection2Row = rowCount++;
        headerColorRow = rowCount++;
        headerTitleColorRow = rowCount++;
        headerIconsColorRow = rowCount++;

        rowsSectionRow = rowCount++;
        rowsSection2Row = rowCount++;
        rowColorRow = rowCount++;
        avatarRadiusRow  = rowCount++;
        nameColorRow = rowCount++;
        nameSizeRow = rowCount++;
        statusColorRow = rowCount++;
        statusSizeRow = rowCount++;
        onlineColorRow = rowCount++;

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();

    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        if (fragmentView == null) {

            actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);

            if (AndroidUtilities.isTablet()) {
                actionBar.setOccupyStatusBar(false);
            }
            actionBar.setTitle(LocaleController.getString("ContactsScreen", R.string.ContactsScreen));

            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            listAdapter = new ListAdapter(context);

            fragmentView = new FrameLayout(context);
            FrameLayout frameLayout = (FrameLayout) fragmentView;

            listView = new ListView(context);
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setVerticalScrollBarEnabled(false);
            AndroidUtilities.setListViewEdgeEffectColor(listView, AvatarDrawable.getProfileBackColorForId(5));
            frameLayout.addView(listView);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP;
            listView.setLayoutParams(layoutParams);
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                    SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);

                    if (i == headerColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }

                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("contactsHeaderColor", color);
                            }

                        },themePrefs.getInt("contactsHeaderColor", AndroidUtilities.getIntColor("themeColor")), CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == headerTitleColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( "contactsHeaderTitleColor", color);
                            }
                        },themePrefs.getInt( "contactsHeaderTitleColor", 0xffffffff), CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == headerIconsColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( "contactsHeaderIconsColor", color);
                            }
                        },themePrefs.getInt( "contactsHeaderIconsColor", 0xffffffff), CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == rowColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("contactsRowColor", color);
                            }

                        },themePrefs.getInt("contactsRowColor", 0xffffffff), CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == nameColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("contactsNameColor", color);
                            }

                        },themePrefs.getInt("contactsNameColor", 0xff000000), CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == statusColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("contactsStatusColor", color);
                            }

                        },themePrefs.getInt("contactsStatusColor", 0xffa8a8a8), CENTER, 0, false);
                        colorDialog.show();
                    }  else if (i == onlineColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("contactsOnlineColor", color);
                            }
                        },themePrefs.getInt("contactsOnlineColor", AndroidUtilities.getIntDarkerColor("themeColor",0x15)), CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == avatarRadiusRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarRadius", R.string.AvatarRadius));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt("contactsAvatarRadius", 32);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(32);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != currentValue) {
                                    commitInt("contactsAvatarRadius", numberPicker.getValue());
                                }
                            }
                        });
                        showAlertDialog(builder);
                    } else if (i == nameSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("NameSize", R.string.NameSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt("contactsNameSize", 17);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != currentValue) {
                                    commitInt("contactsNameSize", numberPicker.getValue());
                                }
                            }
                        });
                        showAlertDialog(builder);
                    } else if (i == statusSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("StatusSize", R.string.StatusSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt("contactsStatusSize", 14);
                        numberPicker.setMinValue(10);
                        numberPicker.setMaxValue(20);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(numberPicker.getValue() != currentValue){
                                    commitInt("contactsStatusSize", numberPicker.getValue());
                                }
                            }
                        });
                        showAlertDialog(builder);
                    }
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (getParentActivity() == null) {
                        return false;
                    }
                    if (i == headerColorRow) {
                        resetInt("contactsHeaderColor");
                    } else if (i == headerTitleColorRow) {
                        resetInt("contactsHeaderTitleColor");
                    } else if (i == headerIconsColorRow) {
                        resetInt("contactsHeaderIconsColor");
                    } else if (i == rowColorRow) {
                        resetInt("contactsRowColor");
                    } else if (i == avatarRadiusRow) {
                        resetInt("contactsAvatarSize");
                    } else if (i == nameColorRow) {
                        resetInt("contactsNameColor");
                    } else if (i == nameSizeRow) {
                        resetInt("contactsNameSize");
                    } else if (i == statusColorRow) {
                        resetInt("contactsStatusColor");
                    } else if (i == statusSizeRow) {
                        resetInt("contactsStatusSize");
                    } else if (i == onlineColorRow) {
                        resetInt("contactsOnlineColor");
                    }
                    return true;
                }
            });

            frameLayout.addView(actionBar);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    private void resetInt(String key){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
        if (listView != null) {
            listView.invalidateViews();
        }
    }

    private void commitInt(String key, int value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
        if (listView != null) {
            listView.invalidateViews();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    //needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return false;
            }
        });
        listView.setAdapter(listAdapter);
        //actionBar.setBackgroundColor(AndroidUtilities.getIntColor("themeColor"));
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return  i == headerColorRow || i == headerTitleColorRow || i == headerIconsColorRow || i == rowColorRow || i == avatarRadiusRow || i == nameColorRow || i == nameSizeRow || i == statusColorRow || i == statusSizeRow ||
                    i == onlineColorRow ;
        }

        @Override
        public int getCount() {
            return rowCount;
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
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
            if (type == 0) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            }
            else if (type == 1) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                if (i == headerSection2Row) {
                    ((HeaderCell) view).setText(LocaleController.getString("Header", R.string.Header));
                } else if (i == rowsSection2Row) {
                    ((HeaderCell) view).setText(LocaleController.getString("ContactsList", R.string.ContactsList));
                }
            }
            else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == avatarRadiusRow) {
                    int size = themePrefs.getInt("contactsAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
                    textCell.setTextAndValue(LocaleController.getString("AvatarRadius", R.string.AvatarRadius), String.format("%d", size), true);
                } else if (i == nameSizeRow) {
                    int size = themePrefs.getInt("contactsNameSize", AndroidUtilities.isTablet() ? 19 : 17);
                    textCell.setTextAndValue(LocaleController.getString("NameSize", R.string.NameSize), String.format("%d", size), true);
                } else if (i == statusSizeRow) {
                    int size = themePrefs.getInt("contactsStatusSize", AndroidUtilities.isTablet() ? 16 : 14);
                    textCell.setTextAndValue(LocaleController.getString("StatusSize", R.string.StatusSize), String.format("%d", size), true);
                }

            }
            else if (type == 3){
                if (view == null) {
                    view = new TextColorCell(mContext);
                }

                TextColorCell textCell = (TextColorCell) view;

                if (i == headerColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("HeaderColor", R.string.HeaderColor), themePrefs.getInt("contactsHeaderColor", AndroidUtilities.getIntColor("themeColor")), true);
                } else if (i == headerTitleColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("HeaderTitleColor", R.string.HeaderTitleColor), themePrefs.getInt("contactsHeaderTitleColor", 0xffffffff), true);
                } else if (i == headerIconsColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("HeaderIconsColor", R.string.HeaderIconsColor), themePrefs.getInt("contactsHeaderIconsColor", 0xffffffff), true);
                } else if (i == rowColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("RowColor", R.string.RowColor), themePrefs.getInt("contactsRowColor", 0xffffffff), true);
                } else if (i == nameColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("NameColor", R.string.NameColor), themePrefs.getInt("contactsNameColor", 0xff000000), true);
                } else if (i == statusColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("StatusColor", R.string.StatusColor), themePrefs.getInt("contactsStatusColor", 0xffa8a8a8), true);
                } else if (i == onlineColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("OnlineColor", R.string.OnlineColor), themePrefs.getInt("contactsOnlineColor", AndroidUtilities.getIntDarkerColor("themeColor",0x15)), true);
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if ( i == rowsSectionRow ) {
                return 0;
            }
            else if ( i == headerSection2Row || i == rowsSection2Row ) {
                return 1;
            }
            else if ( i == avatarRadiusRow || i == nameSizeRow ||  i == statusSizeRow ) {
                return 2;
            }

            else if ( i == headerColorRow || i == headerTitleColorRow || i == headerIconsColorRow || i == rowColorRow || i == nameColorRow || i == statusColorRow || i == onlineColorRow) {
                return 3;
            }
            else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
