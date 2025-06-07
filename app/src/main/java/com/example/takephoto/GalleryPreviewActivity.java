package com.example.takephoto;

import android.app.Dialog;
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

public class GalleryPreviewActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btnNext, btnReupload;
    private ImageButton btnBack;
    private Dialog loadingDialogCustom;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_preview);

        imageView = findViewById(R.id.imageView);
        btnNext = findViewById(R.id.btnNext);
        btnReupload = findViewById(R.id.btnReupload);
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

        btnReupload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 300);
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                imageView.setImageURI(imageUri);
            }
        }
    }

    private void sendToRoboflow(Bitmap bitmap) {
        String base64Image = bitmapToBase64(bitmap);

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                "data:image/jpeg;base64," + base64Image
        );

        String ROBOFLOW_URL = "https://serverless.roboflow.com/skintypeppb/1?api_key=WC4BNY1aso9MDT8eP7uo";

        runOnUiThread(this::showLoadingDialog);

        Request request = new Request.Builder()
                .url(ROBOFLOW_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    dismissLoadingDialog();
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
                            if (tipeKulit != null && tipeKulit.length() > 0) {
                                tipeKulit = tipeKulit.substring(0, 1).toUpperCase() + tipeKulit.substring(1).toLowerCase();
                            }
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
                        dismissLoadingDialog();
                        showResultDialog(finalMessage, finalTipeKulit);
                    });
                }
            }
        });
    }

    private void showResultDialog(String message, String tipeKulit) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_result);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        Button btnDetail = dialog.findViewById(R.id.btnDetail);
        Button btnClose = dialog.findViewById(R.id.btnClose);
        ImageView imgResult = dialog.findViewById(R.id.imgResult);


        imgResult.setImageResource(R.drawable.ic_check_circle);

        tvTitle.setText("Hasil Prediksi");
        tvMessage.setText(message);

        btnDetail.setOnClickListener(v -> {
            if (tipeKulit != null) {
                Intent intent = null;
                switch (tipeKulit.toLowerCase()) {
                    case "oily":
                        intent = new Intent(this, OilySkinInfoActivity.class);
                        break;
                    case "dry":
                        intent = new Intent(this, DrySkinInfoActivity.class);
                        break;
                    case "normal":
                        intent = new Intent(this, NormalSkinInfoActivity.class);
                        break;
                }
                if (intent != null) {
                    startActivity(intent);
                }
            }
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showLoadingDialog() {
        if (loadingDialogCustom == null) {
            loadingDialogCustom = new Dialog(this);
            loadingDialogCustom.setContentView(R.layout.dialog_loading);
            loadingDialogCustom.setCancelable(false);
            if (loadingDialogCustom.getWindow() != null) {
                loadingDialogCustom.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
        loadingDialogCustom.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialogCustom != null && loadingDialogCustom.isShowing()) {
            loadingDialogCustom.dismiss();
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
}