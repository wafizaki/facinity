package com.example.takephoto;

import android.content.Intent;
import android.net.Uri;
import androidx.cardview.widget.CardView;
import android.view.View;
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
    //private CardView cardArtikel1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupBottomNavigation();
        CardView cardArtikel1 = findViewById(R.id.cardArtikel1);
        cardArtikel1.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://olay.co.uk/skin-care-tips/night-routine/nighttime-skincare-routine"));
            startActivity(intent);
        });
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        //cardArtikel1 = findViewById(R.id.cardArtikel1);

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
                overridePendingTransition(0, 0); 
                return true;
            } 
            // else if (id == R.id.nav_products) {
            //     Toast.makeText(this, "Products feature coming soon", Toast.LENGTH_SHORT).show();
            //     return true;
            // } else if (id == R.id.nav_profile) {
            //     Toast.makeText(this, "Profile feature coming soon", Toast.LENGTH_SHORT).show();
            //     return true;
            // }
            return false;
        });

    }
}
