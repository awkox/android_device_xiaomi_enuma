/*
 * Copyright (C)  The LineageOS Project
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

package org.lineageos.pad_parts.usb;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import org.lineageos.pad_parts.R;
import org.lineageos.pad_parts.utils.SettingsUtils;

public class UsbHostSettingsFragment extends PreferenceFragment {
    private static final String TAG = "UsbHostFragment";
    private static final boolean DEBUG = true;

    private SwitchPreference mEnablePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.usb_host_settings);

        mEnablePref = findPreference(SettingsUtils.USB_HOST_ENABLE);
        if (mEnablePref == null) {
            return;
        }

        // The switch reflects the controller state rather than a stored
        // preference, so it lines up with what the summary reports.
        mEnablePref.setChecked(
                SettingsUtils.isSettingEnabled(getContext(), SettingsUtils.USB_HOST_ENABLE));
        mEnablePref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (Boolean) newValue;
            if (DEBUG) Log.d(TAG, "usb_host_enable -> " + enabled);
            SettingsUtils.setSettingEnabled(getContext(), SettingsUtils.USB_HOST_ENABLE, enabled);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateModeSummary();
    }

    private void updateModeSummary() {
        if (mEnablePref == null) {
            return;
        }
        String mode = UsbHostUtils.getMode();
        if (DEBUG) Log.d(TAG, "usb mode: " + mode);

        int resId;
        switch (mode) {
            case "host":
                resId = R.string.usb_host_mode_host;
                break;
            case "peripheral":
                resId = R.string.usb_host_mode_peripheral;
                break;
            case "none":
            default:
                resId = R.string.usb_host_mode_none;
                break;
        }
        mEnablePref.setSummary(getString(resId));
    }
}
