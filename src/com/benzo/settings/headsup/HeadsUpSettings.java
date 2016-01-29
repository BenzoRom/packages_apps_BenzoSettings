/*
 * Copyright (C) 2018 Benzo Rom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.benzo.settings.headsup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceViewHolder;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.benzo.settings.preference.PackageListAdapter;
import com.benzo.settings.preference.PackageListAdapter.PackageItem;

import com.android.internal.logging.nano.MetricsProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadsUpSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener, OnPreferenceChangeListener {

    private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";
    private static final String PREF_HEADS_UP_SNOOZE_TIME = "heads_up_snooze_time";
    private static final int DIALOG_BLACKLIST_APPS = 0;
    private static final int DIALOG_WHITELIST_APPS = 1;

    private ListPreference mHeadsUpTimeOut;
    private ListPreference mHeadsUpSnoozeTime;

    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private PreferenceGroup mBlacklistPrefList;
    private PreferenceGroup mWhitelistPrefList;
    private Preference mAddBlacklistPref;
    private Preference mAddWhitelistPref;

    private String mBlacklistPackageList;
    private String mWhitelistPackageList;
    private Map<String, Package> mBlacklistPackages;
    private Map<String, Package> mWhitelistPackages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.heads_up_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        Resources systemUiResources;
        try {
            systemUiResources = getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            return;
        }

        // Black/whitelist
        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());
        mBlacklistPrefList = (PreferenceGroup) findPreference("blacklist_applications");
        mBlacklistPrefList.setOrderingAsAdded(false);
        mWhitelistPrefList = (PreferenceGroup) findPreference("whitelist_applications");
        mWhitelistPrefList.setOrderingAsAdded(false);
        mBlacklistPackages = new HashMap<String, Package>();
        mWhitelistPackages = new HashMap<String, Package>();
        mAddBlacklistPref = findPreference("add_blacklist_packages");
        mAddWhitelistPref = findPreference("add_whitelist_packages");
        mAddBlacklistPref.setOnPreferenceClickListener(this);
        mAddWhitelistPref.setOnPreferenceClickListener(this);

        // Timeout
        int defaultTimeOut = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_notification_decay", null, null));
        mHeadsUpTimeOut = (ListPreference) findPreference(PREF_HEADS_UP_TIME_OUT);
        mHeadsUpTimeOut.setOnPreferenceChangeListener(this);
        int headsUpTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_TIMEOUT, defaultTimeOut);
        mHeadsUpTimeOut.setValue(String.valueOf(headsUpTimeOut));
        updateHeadsUpTimeOutSummary(headsUpTimeOut);

        // Snooze
        int defaultSnooze = systemUiResources.getInteger(systemUiResources.getIdentifier(
                    "com.android.systemui:integer/heads_up_default_snooze_length_ms", null, null));
        mHeadsUpSnoozeTime = (ListPreference) findPreference(PREF_HEADS_UP_SNOOZE_TIME);
        mHeadsUpSnoozeTime.setOnPreferenceChangeListener(this);
        int headsUpSnooze = Settings.System.getInt(getContentResolver(),
                Settings.System.HEADS_UP_NOTIFICATION_SNOOZE, defaultSnooze);
        mHeadsUpSnoozeTime.setValue(String.valueOf(headsUpSnooze));
        updateHeadsUpSnoozeTimeSummary(headsUpSnooze);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCustomApplicationPrefs();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHeadsUpTimeOut) {
            int headsUpTimeOut = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_TIMEOUT, headsUpTimeOut);
            updateHeadsUpTimeOutSummary(headsUpTimeOut);
            return true;
        } else if (preference == mHeadsUpSnoozeTime) {
            int headsUpSnooze = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HEADS_UP_NOTIFICATION_SNOOZE,
                    headsUpSnooze);
            updateHeadsUpSnoozeTimeSummary(headsUpSnooze);
            return true;
        }
        return false;
    }

    private void updateHeadsUpTimeOutSummary(int value) {
        String summary = getResources().getString(R.string.heads_up_time_out_summary,
                value / 1000);
        mHeadsUpTimeOut.setSummary(summary);
    }

    private void updateHeadsUpSnoozeTimeSummary(int value) {
        if (value == 0) {
            mHeadsUpSnoozeTime.setSummary(getResources().getString(R.string.heads_up_snooze_disabled_summary));
        } else if (value == 60000) {
            mHeadsUpSnoozeTime.setSummary(getResources().getString(R.string.heads_up_snooze_summary_one_minute));
        } else {
            String summary = getResources().getString(R.string.heads_up_snooze_summary, value / 60 / 1000);
            mHeadsUpSnoozeTime.setSummary(summary);
        }
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == DIALOG_BLACKLIST_APPS ) {
            return MetricsProto.MetricsEvent.BENZO;
        }
        if (dialogId == DIALOG_WHITELIST_APPS ) {
            return MetricsProto.MetricsEvent.BENZO;
        }
        return 0;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog;
        final ListView list = new ListView(getActivity());
        list.setAdapter(mPackageAdapter);

        builder.setTitle(R.string.profile_choose_app);
        builder.setView(list);
        dialog = builder.create();

        final Toast toast = Toast.makeText(getActivity(), R.string.heads_up_contains_key,
                Toast.LENGTH_SHORT);

        switch (id) {
            case DIALOG_BLACKLIST_APPS:
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            View view, int position, long id) {
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        if (!isPackageAlreadyAdded(info.packageName)) {
                            addCustomApplicationPref(info.packageName, mBlacklistPackages);
                            dialog.cancel();
                        } else {
                            toast.show();
                        }
                    }
                });
                break;
            case DIALOG_WHITELIST_APPS:
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            View view, int position, long id) {
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        if (!isPackageAlreadyAdded(info.packageName)) {
                            addCustomApplicationPref(info.packageName, mWhitelistPackages);
                            dialog.cancel();
                        } else {
                            toast.show();
                        }
                    }
                });
        }
        return dialog;
    }

    /**
     * Application class
     */
    private static class Package {
        public String name;
        /**
         * Stores all the application values in one call
         * @param name
         */
        public Package(String name) {
            this.name = name;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name);
            return builder.toString();
        }

        public static Package fromString(String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }

            try {
                Package item = new Package(value);
                return item;
            } catch (NumberFormatException e) {
                return null;
            }
        }

    };

    private void refreshCustomApplicationPrefs() {
        if (!parsePackageList()) {
            return;
        }

        // Add the Application Preferences
        if (mBlacklistPrefList != null && mWhitelistPrefList != null) {
            mBlacklistPrefList.removeAll();
            mWhitelistPrefList.removeAll();

            for (Package pkg : mBlacklistPackages.values()) {
                try {
                    Preference pref = createPreferenceFromInfo(pkg);
                    mBlacklistPrefList.addPreference(pref);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }
            }
            for (Package pkg : mWhitelistPackages.values()) {
                try {
                    Preference pref = createPreferenceFromInfo(pkg);
                    mWhitelistPrefList.addPreference(pref);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }
            }
        }

        // Keep these at the top
        mAddBlacklistPref.setOrder(0);
        mAddWhitelistPref.setOrder(0);
        // Add 'add' options
        mBlacklistPrefList.addPreference(mAddBlacklistPref);
        mWhitelistPrefList.addPreference(mAddWhitelistPref);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mAddBlacklistPref) {
            showDialog(DIALOG_BLACKLIST_APPS);
        } else if (preference == mAddWhitelistPref) {
            showDialog(DIALOG_WHITELIST_APPS);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (preference == mBlacklistPrefList.findPreference(preference.getKey())) {
                                 removeApplicationPref(preference.getKey(), mBlacklistPackages);
                            } else if (preference == mWhitelistPrefList.findPreference(preference.getKey())) {
                                 removeApplicationPref(preference.getKey(), mWhitelistPackages);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

        builder.show();
        }
        return true;
    }

    private void addCustomApplicationPref(String packageName, Map<String,Package> map) {
        Package pkg = new Package(packageName);
        map.put(packageName, pkg);
        savePackageList(false, map);
        refreshCustomApplicationPrefs();
    }

    private Preference createPreferenceFromInfo(Package pkg)
            throws PackageManager.NameNotFoundException {
        PackageInfo info = mPackageManager.getPackageInfo(pkg.name,
                PackageManager.GET_META_DATA);
        Preference pref =
                new Preference(getActivity());

        pref.setKey(pkg.name);
        pref.setTitle(info.applicationInfo.loadLabel(mPackageManager));
        pref.setIcon(info.applicationInfo.loadIcon(mPackageManager));
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
        return pref;
    }

    private void removeApplicationPref(String packageName, Map<String,Package> map) {
        if (map.remove(packageName) != null) {
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }

    private boolean parsePackageList() {
        boolean parsed = false;

        final String blacklistString = Settings.System.getString(getContentResolver(),
                Settings.System.HEADS_UP_BLACKLIST_VALUES);
        final String whitelistString = Settings.System.getString(getContentResolver(),
                Settings.System.HEADS_UP_WHITELIST_VALUES);

        if (!TextUtils.equals(mBlacklistPackageList, blacklistString)) {
            mBlacklistPackageList = blacklistString;
            mBlacklistPackages.clear();
            parseAndAddToMap(blacklistString, mBlacklistPackages);
            parsed = true;
        }

        if (!TextUtils.equals(mWhitelistPackageList, whitelistString)) {
            mWhitelistPackageList = whitelistString;
            mWhitelistPackages.clear();
            parseAndAddToMap(whitelistString, mWhitelistPackages);
            parsed = true;
        }

        return parsed;
    }

    private void parseAndAddToMap(String baseString, Map<String,Package> map) {
        if (baseString == null) {
            return;
        }

        final String[] array = TextUtils.split(baseString, "\\|");
        for (String item : array) {
            if (TextUtils.isEmpty(item)) {
                continue;
            }
            Package pkg = Package.fromString(item);
            map.put(pkg.name, pkg);
        }
    }

    private void savePackageList(boolean preferencesUpdated, Map<String,Package> map) {
        String setting;
        if (map == mBlacklistPackages) {
            setting = Settings.System.HEADS_UP_BLACKLIST_VALUES;
        } else {
            setting = Settings.System.HEADS_UP_WHITELIST_VALUES;
        }

        List<String> settings = new ArrayList<String>();
        for (Package app : map.values()) {
            settings.add(app.toString());
        }
        final String value = TextUtils.join("|", settings);
        if (preferencesUpdated) {
            if (map == mBlacklistPackages) {
                mBlacklistPackageList = value;
            } else {
                mWhitelistPackageList = value;
            }
        }
        Settings.System.putString(getContentResolver(),
                setting, value);
    }

    private boolean isPackageAlreadyAdded(String packageName) {
        boolean result = mBlacklistPackages.containsKey(packageName)
                || mWhitelistPackages.containsKey(packageName);
        return result;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BENZO;
    }
}
