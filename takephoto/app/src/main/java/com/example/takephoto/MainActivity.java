package com.example.takephoto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import android.graphics.Bitmap;
import java.io.IOException;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 300;

    private ImageView imageView;
    private Button btnStart, btnNext;
    private ImageButton btnBack;
    private TextView titleText, previewTitleText;

    private Button btnStoredPhotos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        showMainUI();
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        previewTitleText = findViewById(R.id.previewTitleText);
        imageView = findViewById(R.id.imageView);
        btnStart = findViewById(R.id.btnStart);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnStoredPhotos = findViewById(R.id.btnStoredPhotos);

    }

    private void setupListeners() {
        btnStart.setOnClickListener(this::showOptionMenu);
        btnBack.setOnClickListener(v -> showMainUI());
        btnNext.setOnClickListener(v -> Toast.makeText(this, "Next clicked", Toast.LENGTH_SHORT).show());
        btnStoredPhotos.setOnClickListener(v -> startActivity(new Intent(this, PhotoGalleryActivity.class)));

    }

    private void showOptionMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Take Photo");
        popup.getMenu().add("Select from Gallery");

        popup.setOnMenuItemClickListener(item -> {
            String choice = item.getTitle().toString();
            if (choice.equals("Take Photo")) {
                startActivity(new Intent(this, CameraActivity.class));
            } else if (choice.equals("Select from Gallery")) {
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_GALLERY);
            }
            return true;
        });
        popup.show();
    }

    private void showMainUI() {
        titleText.setVisibility(View.VISIBLE);
        btnStart.setVisibility(View.VISIBLE);

        previewTitleText.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
    }

    private void showPreviewUI() {
        titleText.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);

        previewTitleText.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageView.setImageBitmap(bitmap);
                new PhotoDatabaseHelper(this).insertPhoto(bitmap); // Optional: store the photo immediately
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
            showPreviewUI();
        }

    }
}