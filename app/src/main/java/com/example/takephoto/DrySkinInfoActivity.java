package com.example.takephoto;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
// ...existing code...

public class DrySkinInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oily);

        findViewById(R.id.btnCariProduk).setOnClickListener(v -> {
            String url = "https://shopee.co.id/search?keyword=skincare%20dry%20skin";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }
}