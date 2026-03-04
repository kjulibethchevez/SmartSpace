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

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;

public class ResultadoGaleriaActivity extends AppCompatActivity {

    ImageView imageGaleria;
    TextView tvResultadoGaleria;

    ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_galeria);

        imageGaleria = findViewById(R.id.imageGaleria);
        tvResultadoGaleria = findViewById(R.id.tvResultadoGaleria);

        galleryLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                                Uri imageUri = result.getData().getData();

                                if (imageUri != null) {
                                    procesarImagen(imageUri);
                                }
                            }
                        });

        String uriString = getIntent().getStringExtra("imageUri");

        if (uriString != null) {
            procesarImagen(Uri.parse(uriString));
        }

        Button btnOtraImagen = findViewById(R.id.btnOtraImagen);
        Button btnVolver = findViewById(R.id.btnVolverInicio);

        btnOtraImagen.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            galleryLauncher.launch(intent);
        });

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
            tvResultadoGaleria.setText("Error al cargar imagen");
        }
    }

    private void detectarObjetos(Bitmap bitmap) {

        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("detect.tflite")
                        .build();

        CustomObjectDetectorOptions options =
                new CustomObjectDetectorOptions.Builder(localModel)
                        .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .setClassificationConfidenceThreshold(0.5f)
                        .setMaxPerObjectLabelCount(1)
                        .build();

        ObjectDetector detector = ObjectDetection.getClient(options);

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(objects -> {

                    Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(mutable);

                    Paint boxPaint = new Paint();
                    boxPaint.setColor(Color.GREEN);
                    boxPaint.setStyle(Paint.Style.STROKE);
                    boxPaint.setStrokeWidth(6);

                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextSize(50);
                    textPaint.setFakeBoldText(true);

                    String resultado = "";

                    for (DetectedObject obj : objects) {

                        Rect bounds = obj.getBoundingBox();
                        canvas.drawRect(bounds, boxPaint);

                        if (!obj.getLabels().isEmpty()) {

                            String label = obj.getLabels().get(0).getText();
                            float confidence = obj.getLabels().get(0).getConfidence();

                            String texto = label + " (" + Math.round(confidence * 100) + "%)";

                            resultado += texto + "\n";

                            canvas.drawText(texto, bounds.left, bounds.top - 10, textPaint);
                        }
                    }

                    imageGaleria.setImageBitmap(mutable);

                    if (resultado.isEmpty()) {
                        tvResultadoGaleria.setText("No se identificaron objetos");
                    } else {
                        tvResultadoGaleria.setText(resultado);
                    }

                })
                .addOnFailureListener(e ->
                        tvResultadoGaleria.setText("Error al detectar objetos"));
    }
}