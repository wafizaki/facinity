package com.example.takephoto;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;

public class DrySkinInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dry_skin_info);

        findViewById(R.id.btnCariFacewash).setOnClickListener(v -> {
            openShopee("Wardah Nature Facewash");
        });
        findViewById(R.id.btnCariMoisturizer).setOnClickListener(v -> {
            openShopee("Ceramic Skin Saviour Somethinc");
        });
        findViewById(R.id.btnCariToner).setOnClickListener(v -> {
            openShopee("Wardah Nature Daily Seaweed Primary Skin Hydrating Booster");
        });
        findViewById(R.id.btnCariSunscreen).setOnClickListener(v -> {
            openShopee("Wardah UV Shield Physical Sunscreen");
        });
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