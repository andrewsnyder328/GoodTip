package com.syntech.goodtip;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.syntech.goodtip.ColorPicker.getColor;
import static com.syntech.goodtip.MainActivity.ratingsList;
import static com.syntech.goodtip.ColorPicker.getColor;
import static com.syntech.goodtip.MainActivity.*;
import static com.syntech.goodtip.MainActivity.ratingsList;
import static com.syntech.goodtip.MainActivity.recyclerView;
import static com.syntech.goodtip.MainActivity.suggestedTip;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    public static List<Ratings> mPersonList;
    OnItemClickListener mItemClickListener;
    private static final int TYPE_ITEM = 0;
    private final LayoutInflater mInflater;
    private final OnStartDragListener mDragStartListener;
    public static Context mContext;

    int position;

    public ItemAdapter(Context context, List<Ratings> list, OnStartDragListener dragListner) {
        this.mPersonList = list;
        this.mInflater = LayoutInflater.from(context);
        mDragStartListener = dragListner;
        mContext = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            View v = mInflater.inflate(R.layout.ratings_layout, viewGroup, false);
            return new VHItem(v );
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int i) {

        position = i;
        if (viewHolder instanceof VHItem) {

            final VHItem holder= (VHItem)viewHolder;
            ((VHItem) viewHolder).ratingTitle.setText(mPersonList.get(i).getRatingTitle());

            ((VHItem) viewHolder).handle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });

            ((VHItem) viewHolder).ratingBar.setProgress(mPersonList.get(i).getRatingProgress());

            //((VHItem) viewHolder).ratingRank.setText("#" + String.valueOf(mPersonList.get(i).getRatingRank()+1));
        }

        //((VHItem) viewHolder).ratingRank.setText("#" + String.valueOf(i+1));

    }

    @Override
    public int getItemCount() {
        return mPersonList.size();
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;

    }

    public class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener ,ItemTouchHelperViewHolder{
        public TextView ratingRank;
        private TextView ratingTitle;
        private SeekBar ratingBar;
        private ImageButton ratingDelete;
        private ImageView handle;

        public VHItem(final View itemView) {
            super(itemView);
            //ratingRank = (TextView) itemView.findViewById(R.id.ratingRank);
            ratingTitle = (TextView) itemView.findViewById(R.id.ratingTitle);
            ratingBar = (SeekBar) itemView.findViewById(R.id.ratingBar);
            ratingDelete = (ImageButton) itemView.findViewById(R.id.ratingDelete);
            handle = (ImageView) itemView.findViewById(R.id.handle);
            itemView.setOnClickListener(this);
            ratingBar.setProgressTintList(ColorStateList.valueOf(getColor(ratingBar.getProgress())));
            ratingBar.setThumbTintList(ColorStateList.valueOf(getColor(ratingBar.getProgress())));

            final RecyclerView.ViewHolder viewHolder;
            TextView textView;

            ratingBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    ratingBar.setProgressTintList(ColorStateList.valueOf(getColor(ratingBar.getProgress())));
                    ratingBar.setThumbTintList(ColorStateList.valueOf(getColor(ratingBar.getProgress())));
                    mPersonList.get(getPosition()).setRatingProgress(ratingBar.getProgress());
                    TipCalc.updateTip(mPersonList);
                    MainActivity.suggestedTip.setText("$" + TipCalc.getTip());
                    MainActivity.setTipColor();
                    MainActivity.tipPercent.setText("(" + TipCalc.getTipPercent(ratingsList) + "%)");
                    MainActivity.setOrderTotal();

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            ratingDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, mPersonList.get(getPosition()).getRatingTitle() + " deleted.",
                            Toast.LENGTH_SHORT).show();
                    mPersonList.remove(getPosition());
                    notifyItemRemoved(getPosition());
                    //refreshRanks(getPosition());

                    List<Ratings> temp = new ArrayList<>();
                    for (int i = 0; i < mPersonList.size(); i++){
                        temp.add(
                                new Ratings(temp.size(), mPersonList.get(i).getRatingTitle()));
                        temp.get(i).setRatingProgress(0);
                    }
                    SharedPreferences appSharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(temp);
                    prefsEditor.putString("MyObject", json);
                    prefsEditor.apply();
                    MainActivity.addRating.show();
                    TipCalc.updateTip(mPersonList);
                    MainActivity.setTipColor();
                    MainActivity.suggestedTip.setText("$" + TipCalc.getTip());
                    MainActivity.tipPercent.setText("(" + TipCalc.getTipPercent(ratingsList) + "%)");
                    MainActivity.setOrderTotal();
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    @Override
    public void onItemDismiss(int position) {
        mPersonList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        RecyclerView.ViewHolder viewHolder;
        TextView textView;
        if (fromPosition < mPersonList.size() && toPosition < mPersonList.size()) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mPersonList, i, i + 1);
                    /*
                    viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                    textView = viewHolder.itemView.findViewById(R.id.ratingRank);
                    textView.setText("#" + String.valueOf(toPosition + 1));
                    viewHolder = recyclerView.findViewHolderForAdapterPosition(i+1);
                    textView = viewHolder.itemView.findViewById(R.id.ratingRank);
                    textView.setText("#" + String.valueOf(fromPosition + 1));
                    */

                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mPersonList, i, i - 1);
                    /*
                    viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                    //View v = recyclerView.getChildAt(i);
                    textView = viewHolder.itemView.findViewById(R.id.ratingRank);
                    textView.setText("#" + String.valueOf(toPosition + 1));
                    viewHolder = recyclerView.findViewHolderForAdapterPosition(i-1);
                    //View v = recyclerView.getChildAt(i);
                    textView = viewHolder.itemView.findViewById(R.id.ratingRank);
                    textView.setText("#" + String.valueOf(fromPosition + 1));
                    */                }
            }

            List<Ratings> temp = new ArrayList<>();
            for (int i = 0; i < mPersonList.size(); i++){
                temp.add(
                        new Ratings(temp.size(), mPersonList.get(i).getRatingTitle()));
                temp.get(i).setRatingProgress(0);
            }
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(temp);
            prefsEditor.putString("MyObject", json);
            prefsEditor.apply();

        }

        notifyItemMoved(fromPosition, toPosition);

        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mPersonList);
        prefsEditor.putString("MyObject", json);
        prefsEditor.apply();

        return true;

    }

}
