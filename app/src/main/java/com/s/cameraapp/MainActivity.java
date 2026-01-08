package com.s.cameraapp;   // ✅ Package declaration starts here

// ✅ Import statements start here
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// ✅ Import statements end here

// ✅ Class declaration starts here
public class MainActivity extends AppCompatActivity implements PhotoAdapter.OnPhotoClickListener {

    // ✅ Variable declarations start here
    private PreviewView previewView;
    private Button btnOpenCamera, btnTakePhoto;
    private RecyclerView rvPhotos;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private PhotoAdapter photoAdapter;
    private ArrayList<String> photoList = new ArrayList<>();
    private ProcessCameraProvider cameraProvider;

    private static final int REQUEST_PHOTO_VIEW = 200;
    // ✅ Variable declarations end here

    // ✅ onCreate() method starts here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ UI initialization
        previewView = findViewById(R.id.previewView);
        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        rvPhotos = findViewById(R.id.rvPhotos);

        // ✅ RecyclerView setup
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoAdapter = new PhotoAdapter(photoList, this);
        rvPhotos.setAdapter(photoAdapter);

        // ✅ Camera executor setup
        cameraExecutor = Executors.newSingleThreadExecutor();
        loadSavedPhotos();

        // ✅ Button click listeners
        btnOpenCamera.setOnClickListener(v -> {
            startCamera(); // open selfie camera
            btnOpenCamera.setVisibility(Button.GONE);
            btnTakePhoto.setVisibility(Button.VISIBLE);
        });

        btnTakePhoto.setOnClickListener(v -> takePhoto());
    }
    // ✅ onCreate() method ends here

    // ✅ startCamera() method starts here
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Camera start failed", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    // ✅ startCamera() method ends here

    // ✅ takePhoto() method starts here
    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        File folder = new File(getExternalFilesDir(null), "photos");
        if (!folder.exists()) folder.mkdirs();

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg";
        File photoFile = new File(folder, fileName);

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> photoAdapter.addPhoto(photoFile.getAbsolutePath()));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Photo capture failed", Toast.LENGTH_SHORT).show());
                    }
                });
    }
    // ✅ takePhoto() method ends here

    // ✅ onPhotoClick() method starts here
    @Override
    public void onPhotoClick(String path) {
        Intent intent = new Intent(MainActivity.this, PhotoViewActivity.class);
        intent.putExtra("photoPath", path);
        startActivityForResult(intent, REQUEST_PHOTO_VIEW);
    }
    // ✅ onPhotoClick() method ends here

    // ✅ onActivityResult() method starts here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PHOTO_VIEW && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("deleted")) {
                photoAdapter.removePhoto(data.getStringExtra("deleted"));
            } else if (data.hasExtra("cropped")) {
                photoAdapter.addPhoto(data.getStringExtra("cropped"));
            } else if (data.hasExtra("favorite")) {
                photoAdapter.toggleFavorite(data.getStringExtra("favorite"));
            } else if (data.hasExtra("renamed")) {
                String renamedPath = data.getStringExtra("renamed");
                String oldPath = data.getStringExtra("oldPath");

                photoAdapter.removePhoto(oldPath);     // remove old photo
                photoAdapter.addPhoto(renamedPath);    // add new photo
            }
        }
    }
    // ✅ onActivityResult() method ends here

    // ✅ loadSavedPhotos() method starts here
    private void loadSavedPhotos() {
        File folder = new File(getExternalFilesDir(null), "photos");
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    photoList.add(f.getAbsolutePath());
                }
            }
        }
    }
    // ✅ loadSavedPhotos() method ends here

    // ✅ onDestroy() method starts here
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
    // ✅ onDestroy() method ends here
}
// ✅ Class ends here