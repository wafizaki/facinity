package com.example.takephoto;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class CameraActivity extends AppCompatActivity {

    private TextureView textureView;
    private ImageView imageView;
    private ImageButton btnCapture;
    private Button btnRetake, btnNext;
    private CameraDevice cameraDevice;
    private Size imageDimension;
    private ImageReader imageReader;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Bitmap capturedBitmap;
    private String cameraId;
    private boolean isUsingBackCamera = true;
    private Dialog loadingDialogCustom;
    private TextView tvLoading;

    private static final String FACE_DETECT_URL = "https://serverless.roboflow.com/deteksi-wajah-vhzmz/1?api_key=WC4BNY1aso9MDT8eP7uo";
    private static final String ROBOFLOW_URL = "https://serverless.roboflow.com/skintypeppb/1?api_key=WC4BNY1aso9MDT8eP7uo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        textureView = findViewById(R.id.textureView);
        imageView = findViewById(R.id.imageView);
        btnCapture = findViewById(R.id.btnCapture);
        btnRetake = findViewById(R.id.btnRetake);
        btnNext = findViewById(R.id.btnNext);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        textureView.setSurfaceTextureListener(surfaceListener);
        btnCapture.setOnClickListener(v -> takePhoto());
        btnRetake.setOnClickListener(v -> resetPreview());
        btnNext.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                showLoadingDialog("Detecting face...");
                checkFaceFirst(capturedBitmap);
            } else {
                Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show();
            }
        });
        ImageButton btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnSwitchCamera.setOnClickListener(v -> {
            isUsingBackCamera = !isUsingBackCamera;
            closeCamera();
            openCamera();
        });
    }

    // Step 1: Deteksi wajah dulu, jika ada lanjut ke skin type
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
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    dismissLoadingDialog();
                    Toast.makeText(CameraActivity.this, "Request gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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

        imgResult.setImageResource(R.drawable.ic_error_red);

        tvTitle.setText("No Face Detected");
        tvMessage.setText("No face detected in the captured image. Please try again.");
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
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    dismissLoadingDialog();
                    Toast.makeText(CameraActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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

    private final TextureView.SurfaceTextureListener surfaceListener = new TextureView.SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int w, int h) { openCamera(); }
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture s, int w, int h) {}
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture s) { return false; }
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture s) {}
    };

    private void openCamera() {
        try {
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            String[] cameraIdList = manager.getCameraIdList();
            for (String id : cameraIdList) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (isUsingBackCamera && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    break;
                } else if (!isUsingBackCamera && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    break;
                }
            }
            if (cameraId == null) cameraId = cameraIdList[0];

            imageDimension = manager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, cameraStateCallback, null);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }
        public void onDisconnected(@NonNull CameraDevice camera) { camera.close(); }
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void startPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try { session.setRepeatingRequest(builder.build(), null, null); }
                    catch (CameraAccessException e) { e.printStackTrace(); }
                }
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    showToast("Configuration failed");
                }
            }, null);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void takePhoto() {
        if (cameraDevice == null) return;
        try {
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            Size[] sizes = manager.getCameraCharacteristics(cameraDevice.getId())
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);
            Size captureSize = (sizes != null && sizes.length > 0) ? sizes[0] : new Size(640, 480);

            imageReader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(), ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(reader -> {
                try (Image image = reader.acquireLatestImage()) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    saveAndDisplay(bytes);
                }
            }, null);

            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(imageReader.getSurface());
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                            public void onCaptureCompleted(@NonNull CameraCaptureSession s, @NonNull CaptureRequest r, @NonNull TotalCaptureResult res) {
                                session.close();
                            }
                        }, null);
                    } catch (CameraAccessException e) { e.printStackTrace(); }
                }

                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    showToast("Capture failed");
                }
            }, null);
        } catch (CameraAccessException e) { e.printStackTrace(); }
    }

    private void saveAndDisplay(byte[] bytes) {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), UUID.randomUUID() + ".jpg");
        try (OutputStream output = new FileOutputStream(file)) {
            output.write(bytes);
            Bitmap rotated = rotateBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            if (!isUsingBackCamera && rotated != null && textureView.getWidth() > 0 && textureView.getHeight() > 0) {
                rotated = Bitmap.createScaledBitmap(rotated, textureView.getWidth(), textureView.getHeight(), true);
            }
            capturedBitmap = rotated;
            final Bitmap finalRotated = rotated;
            runOnUiThread(() -> {
                textureView.setVisibility(View.GONE);
                imageView.setImageBitmap(finalRotated);
                imageView.setVisibility(View.VISIBLE);
                findViewById(R.id.btnCaptureCard).setVisibility(View.GONE);
                btnRetake.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetPreview() {
        imageView.setVisibility(View.GONE);
        textureView.setVisibility(View.VISIBLE);
        findViewById(R.id.btnCaptureCard).setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        startPreview();
    }

    private Bitmap rotateBitmap(Bitmap src) {
        Matrix matrix = new Matrix();
        if (isUsingBackCamera) {
            matrix.postRotate(90);
        } else {
            matrix.postRotate(-90);
            matrix.postScale(-1, 1);
        }
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(CameraActivity.this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(req, permissions, results);
        if (req == REQUEST_CAMERA_PERMISSION && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)
            openCamera();
        else showToast("Camera permission denied");
    }

    @Override
    protected void onPause() {
        if (cameraDevice != null) cameraDevice.close();
        super.onPause();
    }
}