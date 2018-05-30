package com.syntech.goodtip;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnStartDragListener {

    static RecyclerView recyclerView;
    static EditText subTotal;
    static TextView suggestedTip;
    static List<Ratings> ratingsList;
    public static EditText newRatingTitle;
    public static FloatingActionButton addRating;
    static Button tipRangeBtn;
    boolean backSpaceFlag;
    public static int minTip;
    public static int maxTip;
    public SharedPreferences preferences;
    public static TextView tipPercent;
    public static TextView orderTotal;
    public static boolean round;

    public static int color = Color.parseColor("#ffe240");

    private ItemTouchHelper mItemTouchHelper;
    private InputFilter filter = (charSequence, start, end, dest, dStart, dEnd) -> {

        if (end == 0 || dStart < dEnd) {
            // backspace was pressed! handle accordingly
            backSpaceFlag = true;
        }

        return charSequence;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("MyObject", "");
        Type type = new TypeToken<List<Ratings>>(){}.getType();

        if (gson.fromJson(json, type) != null){
            ratingsList = gson.fromJson(json, type);
        } else {
            ratingsList = new ArrayList<>();
            ratingsList.add(
                    new Ratings(ratingsList.size(), "Staff Friendliness"));
            ratingsList.add(
                    new Ratings(ratingsList.size(), "Order Accuracy"));
            ratingsList.add(
                    new Ratings(ratingsList.size(), "Frequent Table Checks"));
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        minTip = preferences.getInt("minTip",minTip);
        maxTip = preferences.getInt("maxTip",maxTip);

        if (minTip == 0 && maxTip == 0){
            minTip = 10;
            maxTip = 20;
        }

        round = preferences.getBoolean("round", round);

        tipRangeBtn = findViewById(R.id.tipRangeBtn);
        addRating = findViewById(R.id.addRating);
        subTotal = findViewById(R.id.subTotal);
        subTotal.setFilters(new InputFilter[] { filter });
        subTotal.addTextChangedListener(mWatcher);
        suggestedTip = findViewById(R.id.suggestedTip);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        tipPercent = findViewById(R.id.tipPercent);
        orderTotal = findViewById(R.id.orderTotal);

        tipRangeBtn.setText(String.valueOf(minTip) + "% - " + String.valueOf(maxTip) + "%");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(mLayoutManager);

        final ItemAdapter mAdapter = new ItemAdapter(this, ratingsList, this);
        ItemTouchHelper.Callback callback =
                new EditItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);

        tipPercent.setText("(" + TipCalc.getTipPercent(ratingsList) + "%)");

        setOrderTotal();

        subTotal.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

        });

        subTotal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                subTotal.post(new Runnable() {
                    @Override
                    public void run() {
                        subTotal.setSelection(subTotal.getText().length());
                    }
                });
                return false;
            }
        });

        addRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subTotal.clearFocus();

                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.add_rating, null);
                ImageButton diagNeg = mView.findViewById(R.id.dialog_negative);
                ImageButton diagPos = mView.findViewById(R.id.dialog_positive);
                newRatingTitle = mView.findViewById(R.id.ratingTitle);
                newRatingTitle.setOnEditorActionListener(new DoneOnEditorActionListener());

                newRatingTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (newRatingTitle.hasFocus()){
                            newRatingTitle.setHint("e.g., " + RatingExamples.getExample());
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(newRatingTitle, InputMethodManager.SHOW_IMPLICIT);
                        } else {
                            newRatingTitle.setHint("");
                        }
                    }
                });

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                diagNeg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.hide();
                    }
                });

                diagPos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!newRatingTitle.getText().toString().equals("")){
                            ratingsList.add(
                                    new Ratings(ratingsList.size(), newRatingTitle.getText().toString()));

                            dialog.hide();
                            mAdapter.notifyItemInserted(ratingsList.size());
                            TipCalc.updateTip(ratingsList);
                            suggestedTip.setText("$" + TipCalc.getTip());
                            setTipColor();
                            tipPercent.setText("(" + TipCalc.getTipPercent(ratingsList) + "%)");
                            setOrderTotal();

                            List<Ratings> temp = new ArrayList<>();
                            for (int i = 0; i < ratingsList.size(); i++){
                                temp.add(
                                        new Ratings(temp.size(), ratingsList.get(i).getRatingTitle()));
                                temp.get(i).setRatingProgress(0);
                            }
                            SharedPreferences appSharedPrefs = PreferenceManager
                                    .getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                            Gson gson = new Gson();
                            String json = gson.toJson(temp);
                            prefsEditor.putString("MyObject", json);
                            prefsEditor.apply();
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter a title.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                newRatingTitle.setOnEditorActionListener(new DoneOnEditorActionListener());

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0)
                    addRating.hide();
                else if (dy < 0)
                    addRating.show();
            }
        });

        subTotal.setOnEditorActionListener(new DoneOnEditorActionListener());

        tipRangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.set_tip_range, null);
                NumberPicker picker1 = mView.findViewById(R.id.picker1);
                NumberPicker picker2 = mView.findViewById(R.id.picker2);
                TextView tip_range = mView.findViewById(R.id.tip_range);
                ImageButton tip_range_cancel = mView.findViewById(R.id.tip_range_cancel);
                ImageButton tip_range_confirm = mView.findViewById(R.id.tip_range_confirm);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                picker1.setMinValue(0);
                picker2.setMinValue(minTip+1);

                //Specify the maximum value/number of NumberPicker
                picker1.setMaxValue(maxTip-1);
                picker2.setMaxValue(100);

                //Gets whether the selector wheel wraps when reaching the min/max value.
                picker1.setWrapSelectorWheel(false);
                picker2.setWrapSelectorWheel(false);

                picker1.setValue(minTip);
                picker2.setValue(maxTip);

                tip_range.setText(getTipText(picker1.getValue(),picker2.getValue()));

                picker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        if (newVal > oldVal){
                            picker2.setMinValue(picker2.getMinValue()+1);
                        } else {
                            picker2.setMinValue(picker2.getMinValue()-1);
                        }
                        tip_range.setText(getTipText(picker1.getValue(),picker2.getValue()));
                    }
                });

                picker2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        if (newVal > oldVal){
                            picker1.setMaxValue(picker1.getMaxValue()+1);
                        } else {
                            picker1.setMaxValue(picker1.getMaxValue()-1);
                        }
                        tip_range.setText(getTipText(picker1.getValue(),picker2.getValue()));
                    }
                });

                tip_range_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.hide();
                    }
                });

                tip_range_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences.Editor editor = preferences.edit();
                        minTip = picker1.getValue();
                        maxTip = picker2.getValue();
                        editor.putInt("minTip", minTip);
                        editor.putInt("maxTip", maxTip);
                        editor.commit();
                        tipRangeBtn.setText(getTipText(minTip,maxTip));
                        dialog.hide();
                        TipCalc.updateTip(ratingsList);
                        suggestedTip.setText("$" + TipCalc.getTip());
                        setTipColor();
                        tipPercent.setText("(" + TipCalc.getTipPercent(ratingsList) + "%)");
                        setOrderTotal();

                    }
                });

            }
        });

    }

    private String getTipText(int a, int b){
        return String.valueOf(a) + "% - " + String.valueOf(b) + "%";
    }

    private final TextWatcher mWatcher = new TextWatcher() {
        boolean ignore = false;

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            if (ignore) {
                return;
            }

            ignore = true; // prevent infinite loop
            if (backSpaceFlag){
                format(true);
            } else {
                format(false);
            }
            ignore = false; // release, so the TextWatcher start to listen again.
            backSpaceFlag = false;
            subTotal.setSelection(subTotal.getText().length());

            if (ratingsList.size()!=0){
                TipCalc.updateTip(ratingsList);
                suggestedTip.setText("$" + TipCalc.getTip());
                setTipColor();
                tipPercent.setText("(" + TipCalc.getTipPercent(ratingsList) + "%)");
            }

            setOrderTotal();
        }
    };

    public void format(boolean flag){
        DecimalFormat df = new DecimalFormat("0.00");

        if (subTotal.getText().length() == 0){
            subTotal.setText("0.00");
        }
        String s = subTotal.getText().toString();
        String sTemp = "";
        Double d;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < s.length(); i++){
            builder.append(s.charAt(i));
            sTemp = builder.toString();
        }
        if (flag){
            d = Double.valueOf(sTemp)/10;
        } else {
            d = Double.valueOf(sTemp)*10;
        }
        if (subTotal.getText().length()>6){
            sTemp = builder.deleteCharAt(s.length()-1).toString();
            d = Double.valueOf(sTemp);
        }
        subTotal.setText(df.format(d));

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public static void setTipColor(){
        if (suggestedTip.getText().toString().equals("$0.00")){
            suggestedTip.setTypeface(null, Typeface.NORMAL);
            suggestedTip.setTextColor(Color.WHITE);
        } else {
            suggestedTip.setTypeface(null, Typeface.BOLD);
            suggestedTip.setTextColor(color);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.getItem(0).setChecked(round);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("round", true);

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.round:
                if (item.isChecked()){
                    item.setChecked(false);
                    editor.putBoolean("round", false);
                    round = false;
                    suggestedTip.setText("$" + TipCalc.getTip());

                } else {
                    item.setChecked(true);
                    editor.putBoolean("round", true);
                    round = true;
                    suggestedTip.setText("$" + TipCalc.getTip());
                }
                setOrderTotal();
                setTipColor();
                editor.apply();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void setOrderTotal(){
        TipCalc.updateTip(ratingsList);
        double ot;
        double s = Double.valueOf(subTotal.getText().toString());
        double t = Double.valueOf(suggestedTip.getText().toString().substring(1, suggestedTip.length()));

        DecimalFormat df;
        if (round){
            ot = s + t;
            df = new DecimalFormat("0");
        } else {
            ot = s + t;
            df = new DecimalFormat("0.00");
        }

        orderTotal.setText("$" + df.format(ot));
    }

    public static Boolean isRounded(){
        return round;
    }

}
