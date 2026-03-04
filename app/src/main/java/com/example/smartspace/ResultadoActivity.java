package com.example.smartspace;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

public class ResultadoActivity extends AppCompatActivity {

    private ImageView imageResultado;
    private TextView tvConteo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        imageResultado = findViewById(R.id.imageResultado);
        tvConteo = findViewById(R.id.tvConteo);

        Button btnOtraFoto = findViewById(R.id.btnOtraFoto);
        Button btnVolver = findViewById(R.id.btnVolver);

        btnOtraFoto.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        btnVolver.setOnClickListener(v -> finish());

        // Recibir imagen desde MainActivity
        Bitmap bitmap = getIntent().getParcelableExtra("bitmap");

        if (bitmap != null) {
            detectarObjetos(bitmap);
        } else {
            tvConteo.setText("No se recibió imagen");
        }
    }

    private void detectarObjetos(Bitmap bitmap) {

        // Cargar modelo TensorFlow Lite
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

        ObjectDetector detector =
                ObjectDetection.getClient(options);

        InputImage image =
                InputImage.fromBitmap(bitmap, 0);

        detector.process(image)
                .addOnSuccessListener(objects -> {

                    Bitmap mutable =
                            bitmap.copy(Bitmap.Config.ARGB_8888, true);

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

                            String label =
                                    obj.getLabels().get(0).getText();

                            float confidence =
                                    obj.getLabels().get(0).getConfidence();

                            String texto =
                                    label + " (" +
                                            Math.round(confidence * 100) +
                                            "%)";

                            resultado += texto + "\n";

                            canvas.drawText(
                                    texto,
                                    bounds.left,
                                    bounds.top - 10,
                                    textPaint);
                        }
                    }

                    imageResultado.setImageBitmap(mutable);

                    if (resultado.isEmpty()) {
                        tvConteo.setText("No se detectaron objetos");
                    } else {
                        tvConteo.setText(resultado);
                    }

                })
                .addOnFailureListener(e ->
                        tvConteo.setText("Error al detectar objetos"));
    }
}