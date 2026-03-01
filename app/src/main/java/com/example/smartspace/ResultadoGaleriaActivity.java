package com.example.smartspace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.IOException;

public class ResultadoGaleriaActivity extends AppCompatActivity {

    private ImageView imageGaleria;
    private TextView tvResultadoGaleria;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_galeria);

        imageGaleria = findViewById(R.id.imageGaleria);
        tvResultadoGaleria = findViewById(R.id.tvResultadoGaleria);

        // Launcher moderno para galería
        galleryLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK
                                    && result.getData() != null) {

                                Uri imageUri =
                                        result.getData().getData();

                                if (imageUri != null) {
                                    procesarImagen(imageUri);
                                }
                            }
                        });

        // Imagen inicial enviada desde MainActivity
        String uriString =
                getIntent().getStringExtra("imageUri");

        if (uriString != null) {
            procesarImagen(Uri.parse(uriString));
        }

        Button btnOtraImagen =
                findViewById(R.id.btnOtraImagen);

        Button btnVolver =
                findViewById(R.id.btnVolverInicio);

        // 🔁 Seleccionar otra imagen
        btnOtraImagen.setOnClickListener(v -> {
            Intent intent =
                    new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            galleryLauncher.launch(intent);
        });

        // ⬅ Volver
        btnVolver.setOnClickListener(v -> finish());
    }

    private void procesarImagen(Uri imageUri) {

        try {

            Bitmap bitmap =
                    MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(),
                            imageUri);

            detectarObjetos(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
            tvResultadoGaleria.setText("Error al cargar imagen");
        }
    }

    private void detectarObjetos(Bitmap bitmap) {

        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(
                                ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .build();

        ObjectDetector detector =
                ObjectDetection.getClient(options);

        InputImage image =
                InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(objects -> {

                    Bitmap mutable =
                            bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    Canvas canvas = new Canvas(mutable);

                    Paint paint = new Paint();
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(0);

                    int count = 0;

                    for (DetectedObject obj : objects) {
                        Rect bounds = obj.getBoundingBox();
                        canvas.drawRect(bounds, paint);
                        count++;
                    }

                    imageGaleria.setImageBitmap(mutable);
                    tvResultadoGaleria.setText(
                            "Objetos detectados: " + count);
                })
                .addOnFailureListener(e ->
                        tvResultadoGaleria.setText(
                                "Error al detectar objetos"));
    }
}