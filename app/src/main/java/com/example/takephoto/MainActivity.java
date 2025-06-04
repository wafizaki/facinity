package com.example.takephoto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 300;

    private Button btnStart;
    private ImageButton btnHome, btnBack;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleText = findViewById(R.id.titleText);
        btnStart = findViewById(R.id.btnStart);
        btnHome = findViewById(R.id.btnHome);
        btnBack = findViewById(R.id.btnBack);

        btnStart.setOnClickListener(this::showOptionMenu);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
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
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
            return true;
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Intent intent = new Intent(MainActivity.this, GalleryPreviewActivity.class);
                intent.putExtra("imageUri", selectedImageUri.toString());
                startActivity(intent);
            }
        }
    }
}