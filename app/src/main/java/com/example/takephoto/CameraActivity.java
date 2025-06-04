package com.example.takephoto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Size;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;


public class CameraActivity extends AppCompatActivity {

    private TextureView textureView;
    private ImageView imageView;
    private Button btnCapture, btnRetake, btnNext, btnViewPhotos, btnSwitchCamera;

    private CameraDevice cameraDevice;
    private Size imageDimension;
    private ImageReader imageReader;

    private String cameraId;
    private boolean isUsingBackCamera = true;

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        textureView = findViewById(R.id.textureView);
        imageView = findViewById(R.id.imageView);
        btnCapture = findViewById(R.id.btnCapture);
        btnRetake = findViewById(R.id.btnRetake);
        btnNext = findViewById(R.id.btnNext);
        btnViewPhotos = findViewById(R.id.btnViewPhotos);

        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);


        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        textureView.setSurfaceTextureListener(surfaceListener);
        btnCapture.setOnClickListener(v -> takePhoto());
        btnRetake.setOnClickListener(v -> resetPreview());
        btnNext.setOnClickListener(v -> {
            imageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            new PhotoDatabaseHelper(this).insertPhoto(bitmap);
            Toast.makeText(this, "Photo saved!", Toast.LENGTH_SHORT).show();
            finish(); // return to MainActivity
        });

        btnViewPhotos.setOnClickListener(v ->
                startActivity(new Intent(this, PhotoGalleryActivity.class)));


        btnSwitchCamera.setOnClickListener(v -> {
            isUsingBackCamera = !isUsingBackCamera;
            if (cameraDevice != null) cameraDevice.close();
            openCamera();
        });

    }

    private final TextureView.SurfaceTextureListener surfaceListener = new TextureView.SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int w, int h) { openCamera(); }
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture s, int w, int h) {}
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture s) { return false; }
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture s) {}
    };

    private String getCameraId(boolean useBackCamera) throws CameraAccessException {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        for (String id : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (useBackCamera && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            } else if (!useBackCamera && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return null; // fallback
    }


    private void openCamera() {
        try {
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            cameraId = getCameraId(isUsingBackCamera);

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
            runOnUiThread(() -> {
                textureView.setVisibility(View.GONE);
                imageView.setImageBitmap(rotated);
                imageView.setVisibility(View.VISIBLE);
                btnCapture.setVisibility(View.GONE);
                btnViewPhotos.setVisibility(View.GONE);
                btnSwitchCamera.setVisibility(View.GONE);
                btnRetake.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Bitmap rotateBitmap(Bitmap src) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    private void resetPreview() {
        imageView.setVisibility(View.GONE);
        textureView.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.VISIBLE);
        btnViewPhotos.setVisibility(View.VISIBLE);
        btnSwitchCamera.setVisibility(View.VISIBLE);
        btnRetake.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        startPreview();
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
