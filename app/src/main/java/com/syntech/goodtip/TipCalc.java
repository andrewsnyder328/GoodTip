package com.syntech.goodtip;

import java.text.DecimalFormat;
import java.util.List;

public class TipCalc {
    private static String sTipPercent;
    private static Double dSuggestedTip;
    private static String sSuggestedTip;

    public static void updateTip(List<Ratings> ratings){
        int tipMin = MainActivity.minTip;
        int tipMax = MainActivity.maxTip;
        double range = tipMax - tipMin;
        dSuggestedTip = 0.00;
        double tipPercent;
        Double orderTotal = Double.valueOf(MainActivity.subTotal.getText().toString());
        double itemWeight = range/ratings.size();

        for (int i = 0; i <ratings.size(); i++){
            dSuggestedTip = dSuggestedTip + ((itemWeight*ratings.get(i).getRatingProgress())/100);
        }
        tipPercent = (dSuggestedTip + tipMin)/100;
        dSuggestedTip = (orderTotal*tipPercent);

        if (ratings.size()==0){
            dSuggestedTip = 0.00;
        }

        DecimalFormat df = new DecimalFormat("0.00");
        if (ratings.size() == 0){
            sSuggestedTip = df.format(0.00);
        } else {
            sSuggestedTip = df.format(dSuggestedTip);
        }

    }

    public static String getTip(){
        DecimalFormat df = new DecimalFormat("0.00");
        if (MainActivity.isRounded()){
            double rnd;
            double dif;
            rnd = Math.ceil(Double.valueOf(MainActivity.subTotal.getText().toString()));
            dif = rnd - Double.valueOf(MainActivity.subTotal.getText().toString());
            return df.format(Math.round(dSuggestedTip)+dif);
        } else {
            return df.format(dSuggestedTip);
        }

    }

    public static String getTipPercent(List<Ratings> ratings){
        int tipMin = MainActivity.minTip;
        int tipMax = MainActivity.maxTip;
        double range = tipMax - tipMin;
        double suggestedTip = 0;
        double tipPercent;
        double itemWeight = range/ratings.size();

        for (int i = 0; i <ratings.size(); i++){
            suggestedTip = suggestedTip + ((itemWeight*ratings.get(i).getRatingProgress())/100);
        }
        tipPercent = (suggestedTip + tipMin);
        DecimalFormat df = new DecimalFormat("0.0");
        sTipPercent = df.format(tipPercent);
        return sTipPercent;
    }
}
