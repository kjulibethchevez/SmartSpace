package com.example.smartspace;

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

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.IOException;

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

        recibirImagen();
    }

    private void recibirImagen() {

        try {

            Bitmap bitmap =
                    getIntent().getParcelableExtra("bitmap");

            if (bitmap != null) {
                detectarObjetos(bitmap);
                return;
            }

            String uriString =
                    getIntent().getStringExtra("imageUri");

            if (uriString != null) {

                Uri imageUri = Uri.parse(uriString);

                Bitmap bitmapFromUri =
                        MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                imageUri);

                detectarObjetos(bitmapFromUri);
            }

        } catch (IOException e) {
            e.printStackTrace();
            tvConteo.setText("Error al cargar imagen");
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

                    imageResultado.setImageBitmap(mutable);
                    tvConteo.setText(
                            "Objetos detectados: " + count);
                })
                .addOnFailureListener(e ->
                        tvConteo.setText("Error al detectar objetos"));
    }
}