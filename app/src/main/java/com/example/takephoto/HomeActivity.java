package com.example.takephoto;

import android.content.Intent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;



public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupBottomNavigation();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        findViewById(R.id.btnOilySkin)
                .setOnClickListener(v -> startActivity(new Intent(this, OilySkinInfoActivity.class)));
        findViewById(R.id.btnDrySkin)
                .setOnClickListener(v -> startActivity(new Intent(this, DrySkinInfoActivity.class)));
        findViewById(R.id.btnNormalSkin)
                .setOnClickListener(v -> startActivity(new Intent(this, NormalSkinInfoActivity.class)));
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_scan) {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
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
}
