package com.syntech.goodtip;

import android.graphics.Color;

public class ColorPicker {

    public static int getColor(int seekbarProgress){
        float[] hsv = {
                seekbarProgress,
                1.f,
                1.f};
        return Color.HSVToColor(hsv);
    }
}
