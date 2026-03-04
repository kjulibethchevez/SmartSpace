package com.example.smartspace;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Size;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import java.util.concurrent.ExecutionException;

public class LiveActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ObjectDetector objectDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        previewView = findViewById(R.id.previewView);

        iniciarDetector();

        iniciarCamara();
    }

    private void iniciarDetector(){

        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("detect.tflite")
                        .build();

        CustomObjectDetectorOptions options =
                new CustomObjectDetectorOptions.Builder(localModel)
                        .setDetectorMode(
                                CustomObjectDetectorOptions.STREAM_MODE)
                        .enableClassification()
                        .setClassificationConfidenceThreshold(0.5f)
                        .setMaxPerObjectLabelCount(1)
                        .build();

        objectDetector =
                ObjectDetection.getClient(options);
    }

    private void iniciarCamara(){

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA;

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setTargetResolution(new Size(1280,720))
                                .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        this::analizarImagen);

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {

                Toast.makeText(
                        this,
                        "Error al iniciar cámara",
                        Toast.LENGTH_SHORT).show();
            }

        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analizarImagen(ImageProxy imageProxy){

        if(imageProxy.getImage() == null){
            imageProxy.close();
            return;
        }

        InputImage image =
                InputImage.fromMediaImage(
                        imageProxy.getImage(),
                        imageProxy.getImageInfo()
                                .getRotationDegrees());

        objectDetector.process(image)
                .addOnSuccessListener(objects -> {

                    for(DetectedObject object : objects){

                        Rect bounds =
                                object.getBoundingBox();

                        if(!object.getLabels().isEmpty()){

                            String label =
                                    object.getLabels()
                                            .get(0)
                                            .getText();

                            float confidence =
                                    object.getLabels()
                                            .get(0)
                                            .getConfidence();

                        }
                    }

                    imageProxy.close();
                })

                .addOnFailureListener(e -> imageProxy.close());
    }
}