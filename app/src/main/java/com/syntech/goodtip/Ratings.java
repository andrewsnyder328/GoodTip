package com.syntech.goodtip;

public class Ratings {

    private int ratingProgress;
    private String ratingTitle;

    public Ratings(int ratingProgress, String ratingTitle) {
        this.ratingTitle = ratingTitle;
        this.ratingProgress = ratingProgress;
        this.setRatingProgress(0);
    }

    public String getRatingTitle() {
        return this.ratingTitle;
    }

    public int getRatingProgress(){
        return this.ratingProgress;
    }

    public void setRatingProgress(int progress){
        this.ratingProgress = progress;
    }

}
