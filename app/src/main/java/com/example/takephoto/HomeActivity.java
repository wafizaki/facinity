package com.example.takephoto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CardView cardArtikel1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupBottomNavigation();
        setupArticleCard();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        cardArtikel1 = findViewById(R.id.cardArtikel1);

        // Setup skin type buttons - check if they exist first
        View btnOilySkin = findViewById(R.id.btnOilySkin);
        if (btnOilySkin != null) {
            btnOilySkin.setOnClickListener(v ->
                    startActivity(new Intent(this, OilySkinInfoActivity.class)));
        }

        View btnDrySkin = findViewById(R.id.btnDrySkin);
        if (btnDrySkin != null) {
            btnDrySkin.setOnClickListener(v ->
                    startActivity(new Intent(this, DrySkinInfoActivity.class)));
        }

        View btnNormalSkin = findViewById(R.id.btnNormalSkin);
        if (btnNormalSkin != null) {
            btnNormalSkin.setOnClickListener(v ->
                    startActivity(new Intent(this, NormalSkinInfoActivity.class)));
        }
    }

    private void setupArticleCard() {
        if (cardArtikel1 != null) {
            cardArtikel1.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://olay.co.uk/skin-care-tips/night-routine/nighttime-skincare-routine"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show();
                }
            });
        }
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

            return false;
        });
    }
}
