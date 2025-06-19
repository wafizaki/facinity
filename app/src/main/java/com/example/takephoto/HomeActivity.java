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
    private CardView cardArtikel1, cardArtikel2;

    // Product cards
    private CardView cardProduct1, cardProduct2, cardProduct3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupBottomNavigation();
        setupArticleCards();
        setupProductCards();
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        cardArtikel1 = findViewById(R.id.cardArtikel1);
        cardArtikel2 = findViewById(R.id.cardArtikel2);

        // Initialize product cards - adjust IDs based on your XML layout
        cardProduct1 = findViewById(R.id.cardProduct1); // Somethinc Moisturizer
        cardProduct2 = findViewById(R.id.cardProduct2); // YOU Sunscreen
        cardProduct3 = findViewById(R.id.cardProduct3); // Wardah Cushion

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

    private void setupArticleCards() {
        // Article Card 1 - Night Routine
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

        // Article Card 2 - Acne Treatment
        if (cardArtikel2 != null) {
            cardArtikel2.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.healthline.com/health/skin/how-to-get-rid-of-acne"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupProductCards() {
        // Product 1: Somethinc Moisturizer
        if (cardProduct1 != null) {
            cardProduct1.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://id.shp.ee/dEBEEfe"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open product link", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Product 2: YOU Sunscreen SPF50
        if (cardProduct2 != null) {
            cardProduct2.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://id.shp.ee/tAj1fuM"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open product link", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Product 3: Wardah Exclusive Flawless Cover Cushion
        if (cardProduct3 != null) {
            cardProduct3.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://id.shp.ee/HybQ6D3"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open product link", Toast.LENGTH_SHORT).show();
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