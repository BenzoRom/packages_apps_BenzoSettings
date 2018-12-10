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

import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManagerGlobal;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.benzo.settings.preference.SystemSettingSeekBarPreference;
import com.benzo.settings.preference.SystemSettingSwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.List;

public class NavbarSettings extends SettingsPreferenceFragment implements
         OnPreferenceChangeListener, Indexable {

    private static final String KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String LONG_PRESS_KILL_DELAY = "long_press_kill_delay";
    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_CHARGING_COLOR = "battery_bar_charging_color";
    private static final String PREF_BATT_BAR_LOW_COLOR_WARNING = "battery_bar_battery_low_color_warning";
    private static final String PREF_BATT_BAR_USE_GRADIENT_COLOR = "battery_bar_use_gradient_color";
    private static final String PREF_BATT_BAR_LOW_COLOR = "battery_bar_low_color";
    private static final String PREF_BATT_BAR_HIGH_COLOR = "battery_bar_high_color";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";
    private static final String NAVIGATION_BAR_SHOW = "navigation_bar_show";
    private static final String USE_BOTTOM_GESTURE_NAVIGATION = "use_bottom_gesture_navigation";
    private static final String KEY_SWIPE_LENGTH = "gesture_swipe_length";
    private static final String KEY_SWIPE_TIMEOUT = "gesture_swipe_timeout";

    private SwitchPreference mKillAppLongPressBack;
    private SystemSettingSeekBarPreference mLongpressKillDelay;
    private ListPreference mBatteryBar;
    private ListPreference mBatteryBarStyle;
    private ListPreference mBatteryBarThickness;
    private SwitchPreference mBatteryBarChargingAnimation;
    private SwitchPreference mBatteryBarUseGradient;
    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mBatteryBarChargingColor;
    private ColorPickerPreference mBatteryBarBatteryLowColor;
    private ColorPickerPreference mBatteryBarBatteryLowColorWarn;
    private ColorPickerPreference mBatteryBarBatteryHighColor;
    private SystemSettingSwitchPreference mNavigationBarShow;
    private SystemSettingSwitchPreference mUseBottomGestureNavigation;
    private SystemSettingSeekBarPreference mSwipeTriggerLength;
    private SystemSettingSeekBarPreference mSwipeTriggerTimeout;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.navbar_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        // navigation bar show
        mNavigationBarShow = (SystemSettingSwitchPreference) findPreference(NAVIGATION_BAR_SHOW);
        mNavigationBarShow.setOnPreferenceChangeListener(this);
        int navigationBarShow = Settings.System.getInt(getContentResolver(),
                NAVIGATION_BAR_SHOW, 0);
        mNavigationBarShow.setChecked(navigationBarShow != 0);

        // use bottom gestures
        mUseBottomGestureNavigation = (SystemSettingSwitchPreference) findPreference(USE_BOTTOM_GESTURE_NAVIGATION);
        mUseBottomGestureNavigation.setOnPreferenceChangeListener(this);
        int useBottomGestureNavigation = Settings.System.getInt(getContentResolver(),
                USE_BOTTOM_GESTURE_NAVIGATION, 0);
        mUseBottomGestureNavigation.setChecked(useBottomGestureNavigation != 0);

        mSwipeTriggerLength = (SystemSettingSeekBarPreference) findPreference(KEY_SWIPE_LENGTH);
        int triggerLength = Settings.System.getInt(resolver, Settings.System.BOTTOM_GESTURE_SWIPE_LIMIT,
                getSwipeLengthInPixel(getResources().getInteger(com.android.internal.R.integer.nav_gesture_swipe_min_length)));
        mSwipeTriggerLength.setValue(triggerLength);
        mSwipeTriggerLength.setOnPreferenceChangeListener(this);

        mSwipeTriggerTimeout = (SystemSettingSeekBarPreference) findPreference(KEY_SWIPE_TIMEOUT);
        int triggerTimeout = Settings.System.getInt(resolver, Settings.System.BOTTOM_GESTURE_TRIGGER_TIMEOUT,
                getResources().getInteger(com.android.internal.R.integer.nav_gesture_swipe_timout));
        mSwipeTriggerTimeout.setValue(triggerTimeout);
        mSwipeTriggerTimeout.setOnPreferenceChangeListener(this);

        // kill-app long press back
        mKillAppLongPressBack = (SwitchPreference) findPreference(KILL_APP_LONGPRESS_BACK);
        mKillAppLongPressBack.setOnPreferenceChangeListener(this);
        int killAppLongPressBack = Settings.Secure.getInt(getContentResolver(),
                KILL_APP_LONGPRESS_BACK, 0);
        mKillAppLongPressBack.setChecked(killAppLongPressBack != 0);

        // kill-app long press back delay
        mLongpressKillDelay = (SystemSettingSeekBarPreference) findPreference(LONG_PRESS_KILL_DELAY);
        int killconf = Settings.System.getInt(getContentResolver(),
                Settings.System.LONG_PRESS_KILL_DELAY, 1000);
        mLongpressKillDelay.setValue(killconf);
        mLongpressKillDelay.setOnPreferenceChangeListener(this);

        // battery bar
        mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
        int batteryBarLocation = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOCATION, 0);
        mBatteryBar.setValue(String.valueOf(batteryBarLocation));
        mBatteryBar.setSummary(mBatteryBar.getEntry());
        mBatteryBar.setOnPreferenceChangeListener(this);

        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        int batteryBarStyle = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_STYLE, 0);
        mBatteryBarStyle.setValue(String.valueOf(batteryBarStyle));
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());
        mBatteryBarStyle.setOnPreferenceChangeListener(this);

        mBatteryBarThickness = (ListPreference) findPreference(PREF_BATT_BAR_WIDTH);
        int batteryBarThickness = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_THICKNESS, 1);
        mBatteryBarThickness.setValue(String.valueOf(batteryBarThickness));
        mBatteryBarThickness.setSummary(mBatteryBarThickness.getEntry());
        mBatteryBarThickness.setOnPreferenceChangeListener(this);

        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
        int barColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_COLOR, 0xff00a3ff);
        String barColorHex = String.format("#%08x", (0xff00a3ff & barColor));
        mBatteryBarColor.setSummary(barColorHex);
        mBatteryBarColor.setNewPreviewColor(barColor);
        mBatteryBarColor.setAlphaSliderEnabled(true);
        mBatteryBarColor.setOnPreferenceChangeListener(this);

        mBatteryBarChargingColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_CHARGING_COLOR);
        int chargeColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_CHARGING_COLOR, 0xff00f00);
        String chargeColorHex = String.format("#%08x", (0xff00ff00 & chargeColor));
        mBatteryBarChargingColor.setSummary(chargeColorHex);
        mBatteryBarChargingColor.setNewPreviewColor(chargeColor);
        mBatteryBarChargingColor.setAlphaSliderEnabled(true);
        mBatteryBarChargingColor.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryLowColorWarn = (ColorPickerPreference) findPreference(PREF_BATT_BAR_LOW_COLOR_WARNING);
        int warnColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, 0xffff6600);
        String warnColorHex = String.format("#%08x", (0xffff6600 & warnColor));
        mBatteryBarBatteryLowColorWarn.setSummary(warnColorHex);
        mBatteryBarBatteryLowColorWarn.setNewPreviewColor(warnColor);
        mBatteryBarBatteryLowColorWarn.setAlphaSliderEnabled(true);
        mBatteryBarBatteryLowColorWarn.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryLowColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_LOW_COLOR);
        int lowColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_LOW_COLOR, 0xffff0040);
        String lowColorHex = String.format("#%08x", (0xffff0040 & lowColor));
        mBatteryBarBatteryLowColor.setSummary(lowColorHex);
        mBatteryBarBatteryLowColor.setNewPreviewColor(lowColor);
        mBatteryBarBatteryLowColor.setAlphaSliderEnabled(true);
        mBatteryBarBatteryLowColor.setOnPreferenceChangeListener(this);

        mBatteryBarBatteryHighColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_HIGH_COLOR);
        int highColor = Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_HIGH_COLOR, 0xff99CC00);
        String highColorHex = String.format("#%08x", (0xff99CC00 & highColor));
        mBatteryBarBatteryHighColor.setSummary(highColorHex);
        mBatteryBarBatteryHighColor.setNewPreviewColor(highColor);
        mBatteryBarBatteryHighColor.setAlphaSliderEnabled(true);
        mBatteryBarBatteryHighColor.setOnPreferenceChangeListener(this);

        mBatteryBarUseGradient = (SwitchPreference) findPreference(PREF_BATT_BAR_USE_GRADIENT_COLOR);
        mBatteryBarUseGradient.setChecked(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_USE_GRADIENT_COLOR, 0) == 1);
        mBatteryBarUseGradient.setOnPreferenceChangeListener(this);

        mBatteryBarChargingAnimation = (SwitchPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
                Settings.System.BATTERY_BAR_ANIMATE, 0) == 1);
        mBatteryBarChargingAnimation.setOnPreferenceChangeListener(this);

        updateBatteryBarOptions();
        updateNavigationBarOptions();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNavigationBarShow) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		NAVIGATION_BAR_SHOW, value ? 1 : 0);
            updateNavigationBarOptions();
            return true;
        } else if (preference == mUseBottomGestureNavigation) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		USE_BOTTOM_GESTURE_NAVIGATION, value ? 1 : 0);
            updateNavigationBarOptions();
            return true;
        } else if (preference == mSwipeTriggerLength) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BOTTOM_GESTURE_SWIPE_LIMIT, value);
            return true;
        } else if (preference == mSwipeTriggerTimeout) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BOTTOM_GESTURE_TRIGGER_TIMEOUT, value);
            return true;
        } else if (preference == mKillAppLongPressBack) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
		KILL_APP_LONGPRESS_BACK, value ? 1 : 0);
            return true;
        } else if (preference == mLongpressKillDelay) {
            int killconf = (Integer) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LONG_PRESS_KILL_DELAY, killconf);
            return true;
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBarChargingColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_CHARGING_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_LOW_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColorWarn) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBarBatteryHighColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_HIGH_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mBatteryBar) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBar.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_LOCATION, val);
            mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
            updateBatteryBarOptions();
            return true;
        } else if (preference == mBatteryBarStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_STYLE, val);
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarThickness) {
            int val = Integer.parseInt((String) newValue);
            int index = mBatteryBarThickness.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_THICKNESS, val);
            mBatteryBarStyle.setSummary(mBatteryBarThickness.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarChargingAnimation) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_ANIMATE, value ? 1 : 0);
            return true;
        } else if (preference == mBatteryBarUseGradient) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_BAR_USE_GRADIENT_COLOR, value ? 1 : 0);
            return true;
        }
        return false;
     }

    private void updateNavigationBarOptions() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
            Settings.System.NAVIGATION_BAR_SHOW, 0) == 0) {
            mUseBottomGestureNavigation.setEnabled(true);
            mSwipeTriggerLength.setEnabled(true);
            mSwipeTriggerTimeout.setEnabled(true);
        } else {
            mUseBottomGestureNavigation.setEnabled(false);
            mSwipeTriggerLength.setEnabled(true);
            mSwipeTriggerTimeout.setEnabled(true);
        }
    }

    private void updateBatteryBarOptions() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
            Settings.System.BATTERY_BAR_LOCATION, 0) == 0) {
            mBatteryBarStyle.setEnabled(false);
            mBatteryBarThickness.setEnabled(false);
            mBatteryBarChargingAnimation.setEnabled(false);
            mBatteryBarColor.setEnabled(false);
            mBatteryBarChargingColor.setEnabled(false);
            mBatteryBarUseGradient.setEnabled(false);
            mBatteryBarBatteryLowColor.setEnabled(false);
            mBatteryBarBatteryHighColor.setEnabled(false);
            mBatteryBarBatteryLowColorWarn.setEnabled(false);
        } else {
            mBatteryBarStyle.setEnabled(true);
            mBatteryBarThickness.setEnabled(true);
            mBatteryBarChargingAnimation.setEnabled(true);
            mBatteryBarColor.setEnabled(true);
            mBatteryBarChargingColor.setEnabled(true);
            mBatteryBarUseGradient.setEnabled(true);
            mBatteryBarBatteryLowColor.setEnabled(true);
            mBatteryBarBatteryHighColor.setEnabled(true);
            mBatteryBarBatteryLowColorWarn.setEnabled(true);
        }
    }

    private int getSwipeLengthInPixel(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BENZO;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                     SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.navbar_settings;
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
