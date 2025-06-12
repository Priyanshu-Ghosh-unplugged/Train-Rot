package com.ghosh.trainrot.features.tatkal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CaptchaSolver {
    private static final String TAG = "CaptchaSolver";
    private static final String MODEL_FILE = "captcha_model.tflite";
    private static final int INPUT_SIZE = 64;
    private static final int OUTPUT_SIZE = 36; // 26 letters + 10 digits
    
    private final Context context;
    private final Interpreter interpreter;
    private final ImageProcessor imageProcessor;
    
    @Inject
    public CaptchaSolver(Context context) {
        this.context = context;
        
        // Load TFLite model
        try {
            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE);
            interpreter = new Interpreter(modelBuffer);
            
            // Initialize image processor
            imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .build();
                
        } catch (Exception e) {
            Log.e(TAG, "Failed to load CAPTCHA model", e);
            throw new RuntimeException("Failed to load CAPTCHA model", e);
        }
    }
    
    public String solveCaptcha(byte[] captchaImage) {
        try {
            // Convert byte array to Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(captchaImage, 0, captchaImage.length);
            
            // Preprocess image
            TensorImage tensorImage = new TensorImage();
            tensorImage.load(bitmap);
            tensorImage = imageProcessor.process(tensorImage);
            
            // Prepare input buffer
            ByteBuffer inputBuffer = tensorImage.getBuffer();
            
            // Prepare output buffer
            ByteBuffer outputBuffer = ByteBuffer.allocateDirect(OUTPUT_SIZE * 4);
            outputBuffer.order(java.nio.ByteOrder.nativeOrder());
            
            // Run inference
            interpreter.run(inputBuffer, outputBuffer);
            
            // Process results
            outputBuffer.rewind();
            float[] probabilities = new float[OUTPUT_SIZE];
            outputBuffer.get(probabilities);
            
            // Convert probabilities to characters
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < probabilities.length; i++) {
                if (probabilities[i] > 0.5) {
                    result.append(indexToChar(i));
                }
            }
            
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "CAPTCHA solving failed", e);
            return null;
        }
    }
    
    private char indexToChar(int index) {
        if (index < 26) {
            return (char) ('A' + index);
        } else {
            return (char) ('0' + (index - 26));
        }
    }
    
    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }
} 