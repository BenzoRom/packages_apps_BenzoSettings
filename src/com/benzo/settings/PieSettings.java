/*
 * Copyright (C) 2017 ParanoidAndroid Project
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

package com.benzo.settings;

import android.os.Bundle;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.List;

public class PieSettings extends SettingsPreferenceFragment implements
         OnPreferenceChangeListener, Indexable {

    private static final String KEY_PIE_BATTERY = "pie_battery_mode";
    private static final String KEY_PIE_THEME = "pie_theme_mode";
    private static final String KEY_PIE_STATUS = "pie_status_indicator";
    private static final String PA_PIE_GRAVITY = "pa_pie_gravity";

    private ListPreference mTheme;
    private ListPreference mBattery;
    private ListPreference mStatus;
    private ListPreference mPieGravity;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.pie_settings);
        ContentResolver resolver = getActivity().getContentResolver();

        mTheme = (ListPreference) findPreference(KEY_PIE_THEME);
        if (mTheme != null) {
            int value = Settings.Secure.getInt(resolver,
                    Settings.Secure.PIE_THEME_MODE, 0);
            mTheme.setValue(String.valueOf(value));
            mTheme.setOnPreferenceChangeListener(this);
        }

        mBattery = (ListPreference) findPreference(KEY_PIE_BATTERY);
        if (mBattery != null) {
            int value = Settings.Secure.getInt(resolver,
                    Settings.Secure.PIE_BATTERY_MODE, 2);
            mBattery.setValue(String.valueOf(value));
            mBattery.setOnPreferenceChangeListener(this);
        }

        mStatus = (ListPreference) findPreference(KEY_PIE_STATUS);
        if (mStatus != null) {
            int value = Settings.Secure.getInt(resolver,
                    Settings.Secure.PIE_STATUS_INDICATOR, 0);
            mStatus.setValue(String.valueOf(value));
            mStatus.setOnPreferenceChangeListener(this);
        }

        mPieGravity = (ListPreference) findPreference(PA_PIE_GRAVITY);
        if (mPieGravity != null) {
            int pieGravity = Settings.Secure.getInt(resolver,
                    Settings.Secure.PIE_GRAVITY, 2);
            mPieGravity.setValue(String.valueOf(pieGravity));
            mPieGravity.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        int value = Integer.parseInt((String) newValue);
        if (preference == mBattery) {
            Settings.Secure.putInt(resolver, Settings.Secure.PIE_BATTERY_MODE, value);
        }
        if (preference == mTheme) {
            Settings.Secure.putInt(resolver, Settings.Secure.PIE_THEME_MODE, value);
        }
        if (preference == mStatus) {
            Settings.Secure.putInt(resolver, Settings.Secure.PIE_STATUS_INDICATOR, value);
        } if (preference == mPieGravity) {
            Settings.Secure.putInt(resolver, Settings.Secure.PIE_GRAVITY, value);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BENZO;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                     SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.pie_settings;
                    result.add(sir);
                    return result;
                }
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
    };
}
