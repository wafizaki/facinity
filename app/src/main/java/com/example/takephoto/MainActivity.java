package com.example.takephoto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import okhttp3.*;
import android.app.ProgressDialog;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 300;
    private static final String ROBOFLOW_URL = "https://detect.roboflow.com/skin-types-ykqvh/3?api_key=zKH7SfpnLcq4Dx6pqVrN";

    private ImageView imageView;
    private Button btnStart, btnNext, btnStoredPhotos;
    private ImageButton btnBack, btnHome;
    private TextView titleText, previewTitleText;
    private Uri selectedImageUri;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        showMainUI();
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        previewTitleText = findViewById(R.id.previewTitleText);
        imageView = findViewById(R.id.imageView);
        btnStart = findViewById(R.id.btnStart);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnHome = findViewById(R.id.btnHome);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(this::showOptionMenu);

        // Tombol back kembali ke MainActivity (refresh/ulang)
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                sendToRoboflow(selectedImageUri);
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol home ke HomeActivity
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
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
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_GALLERY);
            }
            return true;
        });
        popup.show();
    }

    private void showMainUI() {
        titleText.setVisibility(View.VISIBLE);
        btnStart.setVisibility(View.VISIBLE);

        previewTitleText.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnHome.setVisibility(View.VISIBLE);
    }

    private void showPreviewUI() {
        titleText.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);

        previewTitleText.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        btnHome.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
            showPreviewUI();
        }
    }

    private void sendToRoboflow(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            String base64Image = bitmapToBase64(bitmap);

            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/x-www-form-urlencoded"),
                    "data:image/jpeg;base64," + base64Image);

            Request request = new Request.Builder()
                    .url(ROBOFLOW_URL)
                    .post(body)
                    .build();

            runOnUiThread(() -> {
                loadingDialog = new ProgressDialog(MainActivity.this);
                loadingDialog.setMessage("Memproses gambar...");
                loadingDialog.setCancelable(false);
                loadingDialog.show();
            });

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        if (loadingDialog != null && loadingDialog.isShowing())
                            loadingDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
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
                            if (loadingDialog != null && loadingDialog.isShowing())
                                loadingDialog.dismiss();

                            // Tampilkan dialog hasil prediksi dengan tombol "Selengkapnya"
                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("Hasil Prediksi")
                                .setMessage(finalMessage)
                                .setPositiveButton("Selengkapnya", (dialog, which) -> {
                                    if (finalTipeKulit != null) {
                                        Intent intent = null;
                                        switch (finalTipeKulit.toLowerCase()) {
                                            case "oily":
                                                intent = new Intent(MainActivity.this, OilySkinInfoActivity.class);
                                                break;
                                            case "dry":
                                                intent = new Intent(MainActivity.this, DrySkinInfoActivity.class);
                                                break;
                                            case "normal":
                                                intent = new Intent(MainActivity.this, NormalSkinInfoActivity.class);
                                                break;
                                        }
                                        if (intent != null) {
                                            startActivity(intent);
                                        }
                                    }
                                })
                                .setNegativeButton("Tutup", null);
                            builder.show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

}