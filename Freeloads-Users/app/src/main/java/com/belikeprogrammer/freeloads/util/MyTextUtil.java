package com.belikeprogrammer.freeloads.util;

import android.text.TextUtils;

public class MyTextUtil {

    public boolean isEmpty(String... s) {

        for (String str : s) {

            if (TextUtils.isEmpty(str)) return true;
        }
        return false;
    }
}
