package com.s.cameraapp;

// 🔹 Import required Android and Java classes
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.yalantis.ucrop.UCrop;

import java.io.File;

// 🔹 Start of PhotoViewActivity class
public class PhotoViewActivity extends AppCompatActivity {

    // 🔹 Declare UI elements and photo path
    private ImageView imageView;
    private String photoPath;
    private Button btnCrop, btnDelete, btnRename, btnFavorite, btnShare;

    // 🔹 Start of onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        // 🔹 Initialize UI elements
        imageView = findViewById(R.id.imageView);
        btnCrop = findViewById(R.id.btnCrop);
        btnDelete = findViewById(R.id.btnDelete);
        btnRename = findViewById(R.id.btnRename);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);

        // 🔹 Load photo into ImageView
        photoPath = getIntent().getStringExtra("photoPath");
        if (photoPath != null) {
            imageView.setImageURI(Uri.fromFile(new File(photoPath)));
        }

        // 🔹 Start of DELETE button logic
        btnDelete.setOnClickListener(v -> {
            File file = new File(photoPath);
            if (file.exists() && file.delete()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("deleted", photoPath);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
        // 🔹 End of DELETE button logic

        // 🔹 Start of CROP button logic
        btnCrop.setOnClickListener(v -> {
            Uri sourceUri = Uri.fromFile(new File(photoPath));
            File destFile = new File(getCacheDir(), "cropped.jpg");
            Uri destUri = Uri.fromFile(destFile);
            UCrop.of(sourceUri, destUri).start(PhotoViewActivity.this);
        });
        // 🔹 End of CROP button logic

        // 🔹 Start of RENAME button logic
        btnRename.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(PhotoViewActivity.this);
            builder.setTitle("Enter new name");

            final EditText input = new EditText(PhotoViewActivity.this);
            input.setHint("New file name");
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    File oldFile = new File(photoPath);
                    File newFile = new File(oldFile.getParent(), newName + ".jpg");

                    if (oldFile.renameTo(newFile)) {
                        String oldPath = photoPath;
                        photoPath = newFile.getAbsolutePath();

                        imageView.setImageURI(null);
                        imageView.setImageURI(Uri.fromFile(newFile));
                        imageView.invalidate();

                        Toast.makeText(this, "Renamed to: " + newFile.getName(), Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("renamed", photoPath);
                        resultIntent.putExtra("oldPath", oldPath);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
        // 🔹 End of RENAME button logic

        // 🔹 Start of FAVORITE button logic
        btnFavorite.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("favorite", photoPath);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        // 🔹 End of FAVORITE button logic

        // 🔹 Start of SHARE button logic (using FileProvider)
        btnShare.setOnClickListener(v -> {
            File file = new File(photoPath);
            Uri uri = FileProvider.getUriForFile(
                    PhotoViewActivity.this,
                    "com.s.cameraapp.fileprovider",
                    file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this photo!");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Image via"));
        });
        // 🔹 End of SHARE button logic
    }
    // 🔹 End of onCreate method

    // 🔹 Start of onActivityResult method (for UCrop result)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("cropped", resultUri.getPath());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Crop failed", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR && data != null) {
            Toast.makeText(this, "Crop error", Toast.LENGTH_SHORT).show();
        }
    }
    // 🔹 End of onActivityResult method
}   // 🔹 End of PhotoViewActivity class