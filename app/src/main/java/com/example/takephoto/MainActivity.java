package com.example.takephoto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 300;

    private Button btnStart;
    private TextView titleText;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleText = findViewById(R.id.titleText);
        btnStart = findViewById(R.id.btnStart);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        btnStart.setOnClickListener(this::showOptionMenu);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Set item scan aktif saat MainActivity dibuka
        bottomNavigationView.setSelectedItemId(R.id.nav_scan);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0); 
                finish();
                return true;
            } else if (id == R.id.nav_scan) {
                return true;
            } else if (id == R.id.nav_products) {
                Toast.makeText(this, "Products feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
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