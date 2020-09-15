package com.iolink.test.samplepost.Adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iolink.test.samplepost.R;

import java.util.List;


public class GalleryAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final List<String> Images;
    private final List<String> titles;

    public GalleryAdapter(Activity context, List<String> Images, List<String> titles) {
        super(context, R.layout.list_gallery, titles);
        this.context=context;
        this.Images=Images;
        this.titles=titles;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_gallery, null,true);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        TextView textView = (TextView) rowView.findViewById(R.id.txt);

        Bitmap myBitmap = BitmapFactory.decodeFile(Images.get(position));
        imageView.setImageBitmap(myBitmap);

        textView.setText(titles.get(position));
        return rowView;

    };
}
