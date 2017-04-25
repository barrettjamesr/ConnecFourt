package com.smartphoneappdev.wcd.connecfourt;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by JRB on 4/11/2016.
 * Adapter to add text and images to the spinners
 */

public class ImageArrayAdapter extends ArrayAdapter {
    private Integer[] images;
    private String[] text;
    private Context context;

    public ImageArrayAdapter(Context context, Integer[] images, String[] text) {
        super(context, android.R.layout.simple_spinner_item, text);
        if (images != null) {
            this.images = images;
        }
        this.text = text;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    private View getImageForPosition(int position) {
        TextView vwItem = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int margins =  (int) context.getResources().getDimension(R.dimen.table_margin_hori);
        params.setMargins(margins,0,margins,0);
        vwItem.setLayoutParams(params);
        vwItem.setPadding (Constants.grid_padding_cols,Constants.grid_padding_cols,Constants.grid_padding_cols,Constants.grid_padding_cols);
        vwItem.setTextColor(ContextCompat.getColor(context, R.color.text_grey));
        if (images != null) {
            vwItem.setCompoundDrawablesWithIntrinsicBounds(images[position], 0, 0, 0);
        }
        vwItem.setText(text[position]);
        vwItem.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return vwItem;
    }

}
