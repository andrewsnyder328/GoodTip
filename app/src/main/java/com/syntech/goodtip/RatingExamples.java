package com.syntech.goodtip;

import java.util.Random;

public class RatingExamples {
    public static String[] examples = {"Overall service", "Staff was nice", "Menu availability", "Drinks stayed refilled", };
    public static String getExample() {
        String s = examples[new Random().nextInt(examples.length)];
        return s;
    }
}
