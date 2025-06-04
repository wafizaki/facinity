package com.example.takephoto;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ProgressDialog;

public class GalleryPreviewActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btnNext, btnBack;
    private ProgressDialog loadingDialog;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_preview);

        imageView = findViewById(R.id.imageView);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            imageView.setImageURI(imageUri);
        }

        btnNext.setOnClickListener(v -> {
            if (imageUri != null) {
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    sendToRoboflow(bitmap);
                } catch (Exception e) {
                    Toast.makeText(this, "Gagal membaca gambar", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void sendToRoboflow(Bitmap bitmap) {
        String base64Image = bitmapToBase64(bitmap);

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                "data:image/jpeg;base64," + base64Image
        );

        String ROBOFLOW_URL = "https://detect.roboflow.com/skin-types-ykqvh/3?api_key=zKH7SfpnLcq4Dx6pqVrN";

        runOnUiThread(() -> {
            loadingDialog = new ProgressDialog(GalleryPreviewActivity.this);
            loadingDialog.setMessage("Memproses gambar...");
            loadingDialog.setCancelable(false);
            loadingDialog.show();
        });

        Request request = new Request.Builder()
                .url(ROBOFLOW_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                    Toast.makeText(GalleryPreviewActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (response.body() != null) {
                    String result = response.body().string();
                    String message = "Tidak ada prediksi.";
                    String tipeKulit = null;
                    double conf = 0;
                    try {
                        JSONObject json = new JSONObject(result);
                        JSONArray predictions = json.getJSONArray("predictions");
                        if (predictions.length() > 0) {
                            JSONObject pred = predictions.getJSONObject(0);
                            tipeKulit = pred.getString("class");
                            conf = pred.getDouble("confidence");
                            int persen = (int) Math.round(conf * 100);
                            message = "Tipe Kulit: " + tipeKulit + "\nTingkat: " + persen + "%";
                        }
                    } catch (Exception e) {
                        message = "Gagal membaca hasil prediksi.";
                    }
                    String finalMessage = message;
                    String finalTipeKulit = tipeKulit;
                    runOnUiThread(() -> {
                        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
                        new androidx.appcompat.app.AlertDialog.Builder(GalleryPreviewActivity.this)
                            .setTitle("Hasil Prediksi")
                            .setMessage(finalMessage)
                            .setPositiveButton("Selengkapnya", (dialog, which) -> {
                                if (finalTipeKulit != null) {
                                    Intent intent = null;
                                    switch (finalTipeKulit.toLowerCase()) {
                                        case "oily":
                                            intent = new Intent(GalleryPreviewActivity.this, OilySkinInfoActivity.class);
                                            break;
                                        case "dry":
                                            intent = new Intent(GalleryPreviewActivity.this, DrySkinInfoActivity.class);
                                            break;
                                        case "normal":
                                            intent = new Intent(GalleryPreviewActivity.this, NormalSkinInfoActivity.class);
                                            break;
                                    }
                                    if (intent != null) {
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Tutup", null)
                            .show();
                    });
                }
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
}