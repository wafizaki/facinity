package com.example.takephoto;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
    private TextView tvLoading; // Tambahan: simpan referensi TextView loading
    private Uri imageUri;

    private static final String FACE_DETECT_URL = "https://serverless.roboflow.com/deteksi-wajah-vhzmz/1?api_key=WC4BNY1aso9MDT8eP7uo";
    private static final String ROBOFLOW_URL = "https://serverless.roboflow.com/skintypeppb/1?api_key=WC4BNY1aso9MDT8eP7uo";

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
                    showLoadingDialog("Detecting face...");
                    checkFaceFirst(bitmap);
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

    // Step 1: Cek face dulu
    private void checkFaceFirst(Bitmap bitmap) {
        updateLoadingText("Detecting face...");
        String base64Image = bitmapToBase64(bitmap);

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                "data:image/jpeg;base64," + base64Image
        );

        Request request = new Request.Builder()
                .url(FACE_DETECT_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    dismissLoadingDialog();
                    Toast.makeText(GalleryPreviewActivity.this, "Request gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (response.body() != null) {
                    String result = response.body().string();
                    Log.d("FaceDetection", "Roboflow response: " + result);
                    boolean faceDetected = false;
                    double conf = 0;
                    try {
                        JSONObject json = new JSONObject(result);
                        JSONArray predictions = json.getJSONArray("predictions");
                        if (predictions.length() > 0) {
                            JSONObject pred = predictions.getJSONObject(0);
                            String predClass = pred.getString("class");
                            if ("wajah".equalsIgnoreCase(predClass)) {
                                faceDetected = true;
                                conf = pred.optDouble("confidence", 0);
                            }
                        }
                    } catch (Exception e) {
                        faceDetected = false;
                    }
                    boolean finalFaceDetected = faceDetected;
                    double finalConf = conf;
                    runOnUiThread(() -> {
                        if (finalFaceDetected) {
                            updateLoadingText("Analyzing skin type...");
                            sendToRoboflow(bitmap);
                        } else {
                            dismissLoadingDialog();
                            showNoFaceDialog();
                        }
                    });
                }
            }
        });
    }

    // Step 2: Modal hasil prediksi skin type
    private void showSkinTypeResultDialog(String message, String tipeKulit) {
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

        tvTitle.setText("Skin Type Prediction");
        tvMessage.setText(message);

        btnDetail.setVisibility(View.VISIBLE);
        btnDetail.setText("Details");
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

    // Modal jika tidak ada wajah terdeteksi
   private void showNoFaceDialog() {
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

        // Ganti icon menjadi X merah
        imgResult.setImageResource(R.drawable.ic_error_red); // Pastikan drawable ini ada

        tvTitle.setText("No Face Detected");
        tvMessage.setText("No face detected in the selected image. Please try another photo.");
        btnDetail.setVisibility(View.GONE);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Step 3: Prediksi skin type
    private void sendToRoboflow(Bitmap bitmap) {
        updateLoadingText("Analyzing skin type...");
        String base64Image = bitmapToBase64(bitmap);

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                "data:image/jpeg;base64," + base64Image
        );

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
                    String message = "No prediction.";
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
                            message = "Skin Type: " + tipeKulit + "\nConfidence: " + persen + "%";
                        }
                    } catch (Exception e) {
                        message = "Failed to read prediction result.";
                    }
                    String finalMessage = message;
                    String finalTipeKulit = tipeKulit;
                    runOnUiThread(() -> {
                        dismissLoadingDialog();
                        showSkinTypeResultDialog(finalMessage, finalTipeKulit);
                    });
                }
            }
        });
    }

    // Show loading dialog with custom message (hanya 1 modal, update text saja)
    private void showLoadingDialog(String message) {
        if (loadingDialogCustom == null) {
            loadingDialogCustom = new Dialog(this);
            loadingDialogCustom.setContentView(R.layout.dialog_loading);
            loadingDialogCustom.setCancelable(false);
            if (loadingDialogCustom.getWindow() != null) {
                loadingDialogCustom.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            tvLoading = loadingDialogCustom.findViewById(R.id.tvLoading);
        }
        if (tvLoading == null) {
            tvLoading = loadingDialogCustom.findViewById(R.id.tvLoading);
        }
        if (tvLoading != null) {
            tvLoading.setText(message);
        }
        if (!loadingDialogCustom.isShowing()) {
            loadingDialogCustom.show();
        }
    }

    // Update tulisan loading tanpa membuat modal baru
    private void updateLoadingText(String message) {
        runOnUiThread(() -> {
            if (loadingDialogCustom != null && loadingDialogCustom.isShowing()) {
                if (tvLoading == null) {
                    tvLoading = loadingDialogCustom.findViewById(R.id.tvLoading);
                }
                if (tvLoading != null) {
                    tvLoading.setText(message);
                }
            } else {
                showLoadingDialog(message);
            }
        });
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