/*
 * Copyright (C) 2021 Benzo Rom
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

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.SearchIndexableResource;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.logging.nano.MetricsProto;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

@SearchIndexable
public class VisualizerSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String VISUALIZER_CUSTOM_COLOR = "visualizer_custom_color";

    private ColorPickerPreference mVisualizerColor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.visualizer_settings);

        // Custom color
        mVisualizerColor = (ColorPickerPreference) findPreference(VISUALIZER_CUSTOM_COLOR);
        int customColor = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.VISUALIZER_CUSTOM_COLOR, 0xff1976d2,
                UserHandle.USER_CURRENT);
        String customColorHex = String.format("#%08x", (0xff1976d2 & customColor));
        mVisualizerColor.setSummary(customColorHex);
        mVisualizerColor.setNewPreviewColor(customColor);
        mVisualizerColor.setAlphaSliderEnabled(true);
        mVisualizerColor.setOnPreferenceChangeListener(this);
    }

     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mVisualizerColor) {
            String colorString = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int colorInt = ColorPickerPreference.convertToColorInt(colorString);
            Settings.Secure.putIntForUser(getActivity().getContentResolver(),
                    Settings.Secure.VISUALIZER_CUSTOM_COLOR, colorInt,
                    UserHandle.USER_CURRENT);
            preference.setSummary(colorString);
        }
        return true;
     }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BENZO;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.visualizer_settings);
}
