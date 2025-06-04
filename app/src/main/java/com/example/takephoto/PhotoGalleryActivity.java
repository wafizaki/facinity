package com.example.takephoto;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import java.util.ArrayList;

public class PhotoGalleryActivity extends AppCompatActivity {

    private GridView gridView;
    private PhotoAdapter adapter;
    private PhotoDatabaseHelper dbHelper;
    private ArrayList<Bitmap> photoList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        Button btnBack = findViewById(R.id.btnBackToMain);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoGalleryActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Optional: clears stack
            startActivity(intent);
            finish(); // Optional: prevents stacking the gallery page again
        });


        gridView = findViewById(R.id.gridView);
        dbHelper = new PhotoDatabaseHelper(this);

        photoList = dbHelper.getAllPhotos();
        adapter = new PhotoAdapter(this, photoList);
        gridView.setAdapter(adapter);

    }
}
