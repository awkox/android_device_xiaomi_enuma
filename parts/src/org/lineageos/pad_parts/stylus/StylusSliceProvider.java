/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.pad_parts.stylus;

import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;

import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import androidx.slice.builders.ListBuilder;

import org.lineageos.pad_parts.R;

public class StylusSliceProvider extends SliceProvider {

    private static final String TAG = "StylusSliceProvider";
    private static final boolean DEBUG = true;

    @Override
    public boolean onCreateSliceProvider() {
        return true;
    }

    @Override
    public Slice onBindSlice(Uri sliceUri) {
        if (DEBUG) Log.d(TAG, "onBindSlice: " + sliceUri);
        if (getContext() == null) {
            return null;
        }
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();

        ListBuilder listBuilder = new ListBuilder(getContext(), sliceUri, ListBuilder.INFINITY);

        ListBuilder.RowBuilder hardwareVersionBuilder = createHardwareVersionBuilder();
        if (hardwareVersionBuilder != null) {
            listBuilder.addRow(hardwareVersionBuilder);
        }

        StrictMode.setThreadPolicy(oldPolicy);
        return listBuilder.build();
    }

    private ListBuilder.RowBuilder createHardwareVersionBuilder() {
        if (getContext() == null) {
            return null;
        }

        int hardwareVersion = StylusUtils.getStylusVersion();
        if (hardwareVersion == -1) {
            return null;
        }
        String versionName = getContext().getString(hardwareVersion == 1
                ? R.string.stylus_hardware_version_1 : R.string.stylus_hardware_version_2);

        return new ListBuilder.RowBuilder()
                .setTitle(getContext().getString(R.string.stylus_hardware_version_title))
                .setSubtitle(versionName);
    }
}
