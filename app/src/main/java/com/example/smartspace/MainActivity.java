package com.example.smartspace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_RESULTADO = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCamara = findViewById(R.id.btnCamara);
        Button btnGaleria = findViewById(R.id.btnGaleria);
        Button btnLive = findViewById(R.id.btnLive);

        //Tomar Foto
        btnCamara.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            } else {
                abrirCamara();
            }
        });

        //Seleccionar Imagen
        btnGaleria.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        });

        //Live
        btnLive.setOnClickListener(v ->
                startActivity(new Intent(this, LiveActivity.class)));
    }

    private void abrirCamara() {
        Intent takePictureIntent =
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(takePictureIntent,
                REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {

        super.onActivityResult(requestCode,
                resultCode, data);

        if (resultCode == RESULT_OK) {

            //Foto tomada
            if (requestCode == REQUEST_IMAGE_CAPTURE
                    && data != null) {

                Bitmap imageBitmap =
                        (Bitmap) data.getExtras().get("data");

                if (imageBitmap != null) {

                    Intent intent =
                            new Intent(this,
                                    ResultadoActivity.class);

                    intent.putExtra("bitmap", imageBitmap);

                    startActivityForResult(intent,
                            REQUEST_RESULTADO);
                }
            }

            //Imagen de galería
            if (requestCode == REQUEST_IMAGE_GALLERY
                    && data != null) {

                Uri imageUri = data.getData();

                if (imageUri != null) {

                    Intent intent =
                            new Intent(this,
                                    ResultadoGaleriaActivity.class);

                    intent.putExtra("imageUri",
                            imageUri.toString());

                    startActivity(intent);
                }
            }

            // Volver desde ResultadoActivity
            if (requestCode == REQUEST_RESULTADO) {
                abrirCamara();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {

            abrirCamara();
        }
    }
}