package com.example.takephoto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class OilySkinInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oily);

        findViewById(R.id.btnCariFacewash).setOnClickListener(v -> {
            openShopee("Elsheskin Oily Cleanser Wash");
        });
        findViewById(R.id.btnCariMoisturizer).setOnClickListener(v -> {
            openShopee("Azarine Pure Radiance Barrier Moisturizer");
        });
        findViewById(R.id.btnCariToner).setOnClickListener(v -> {
            openShopee("Elsheskin Oily Refresh Toner");
        });
        findViewById(R.id.btnCariSunscreen).setOnClickListener(v -> {
            openShopee("Wardah UV Shield Active Protection Serum");
        });

        // Back to Home button
        findViewById(R.id.btnBackHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void openShopee(String keyword) {
        String url = "https://shopee.co.id/search?keyword=" + keyword.replace(" ", "%20");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}