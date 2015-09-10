/*
 * Copyright (C) 2017 Benzo Rom
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.benzo.settings.preference.CustomSeekBarPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

public class GlobalActionsSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {

    private static final String PREF_ON_THE_GO_ALPHA = "on_the_go_alpha";

    private CustomSeekBarPreference mOnTheGoAlphaPref;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.global_actions);
        PreferenceScreen prefSet = getPreferenceScreen();

        mOnTheGoAlphaPref = (CustomSeekBarPreference) findPreference(PREF_ON_THE_GO_ALPHA);
        float otgAlpha = Settings.System.getFloat(getContentResolver(),
		Settings.System.ON_THE_GO_ALPHA, 0.5f);
        final int alpha = ((int) (otgAlpha * 100));
        mOnTheGoAlphaPref.setValue(alpha);
        mOnTheGoAlphaPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mOnTheGoAlphaPref) {
            float val = (Integer) newValue;
            Settings.System.putFloat(getContentResolver(),
		Settings.System.ON_THE_GO_ALPHA, val / 100);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BENZO;
    }
}
