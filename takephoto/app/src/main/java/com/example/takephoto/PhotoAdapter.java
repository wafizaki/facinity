package com.example.takephoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.GridView;

import java.util.ArrayList;

public class PhotoAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Bitmap> images;

    public PhotoAdapter(Context context, ArrayList<Bitmap> images) {
        this.context = context;
        this.images = images;
    }

    public int getCount() { return images.size(); }
    public Object getItem(int pos) { return images.get(pos); }
    public long getItemId(int pos) { return pos; }

    public View getView(int pos, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(images.get(pos));
        imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }
}
