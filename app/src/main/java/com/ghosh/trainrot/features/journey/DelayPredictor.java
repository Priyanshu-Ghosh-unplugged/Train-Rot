package com.ghosh.trainrot.features.journey;

import android.content.Context;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DelayPredictor {
    private static final String MODEL_FILE = "delay_prediction_model.tflite";
    private static final int SEQUENCE_LENGTH = 24; // Hours of historical data
    private static final int FEATURE_COUNT = 8; // Number of features per hour
    
    private final Interpreter tflite;
    private final DatabaseReference delayRef;
    private final Map<String, Queue<DelayRecord>> trainDelayHistory;

    @Inject
    public DelayPredictor(Context context) {
        this.tflite = new Interpreter(loadModelFile(context));
        this.delayRef = FirebaseDatabase.getInstance().getReference("delays");
        this.trainDelayHistory = new HashMap<>();
        loadHistoricalData();
    }

    public int predictDelay(String trainNumber, String station) {
        // Get historical delay data
        Queue<DelayRecord> history = trainDelayHistory.getOrDefault(trainNumber, new LinkedList<>());
        
        // Prepare input data
        float[][] inputData = prepareInputData(history, station);
        
        // Run prediction
        float[][] outputData = new float[1][1];
        tflite.run(inputData, outputData);
        
        // Convert prediction to minutes
        return Math.round(outputData[0][0]);
    }

    private float[][] prepareInputData(Queue<DelayRecord> history, String station) {
        float[][] inputData = new float[1][SEQUENCE_LENGTH * FEATURE_COUNT];
        int index = 0;
        
        // Fill with historical data
        for (DelayRecord record : history) {
            if (index >= SEQUENCE_LENGTH * FEATURE_COUNT) break;
            
            inputData[0][index++] = record.delayMinutes;
            inputData[0][index++] = record.isWeekend ? 1.0f : 0.0f;
            inputData[0][index++] = record.isHoliday ? 1.0f : 0.0f;
            inputData[0][index++] = record.weatherSeverity;
            inputData[0][index++] = record.stationCongestion;
            inputData[0][index++] = record.trackMaintenance ? 1.0f : 0.0f;
            inputData[0][index++] = record.crewChange ? 1.0f : 0.0f;
            inputData[0][index++] = record.technicalIssue ? 1.0f : 0.0f;
        }
        
        // Pad remaining slots with zeros
        while (index < SEQUENCE_LENGTH * FEATURE_COUNT) {
            inputData[0][index++] = 0.0f;
        }
        
        return inputData;
    }

    private void loadHistoricalData() {
        delayRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                for (com.google.firebase.database.DataSnapshot trainSnapshot : snapshot.getChildren()) {
                    String trainNumber = trainSnapshot.getKey();
                    Queue<DelayRecord> history = new LinkedList<>();
                    
                    for (com.google.firebase.database.DataSnapshot delaySnapshot : trainSnapshot.getChildren()) {
                        DelayRecord record = delaySnapshot.getValue(DelayRecord.class);
                        if (record != null) {
                            history.add(record);
                            if (history.size() > SEQUENCE_LENGTH) {
                                history.poll();
                            }
                        }
                    }
                    
                    trainDelayHistory.put(trainNumber, history);
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                // Handle error
            }
        });
    }

    private MappedByteBuffer loadModelFile(Context context) {
        try {
            FileInputStream inputStream = new FileInputStream(context.getAssets().openFd(MODEL_FILE).getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = context.getAssets().openFd(MODEL_FILE).getStartOffset();
            long declaredLength = context.getAssets().openFd(MODEL_FILE).getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (Exception e) {
            throw new RuntimeException("Error loading model file", e);
        }
    }

    public void recordDelay(String trainNumber, String station, int delayMinutes, 
                          boolean isWeekend, boolean isHoliday, float weatherSeverity,
                          float stationCongestion, boolean trackMaintenance,
                          boolean crewChange, boolean technicalIssue) {
        DelayRecord record = new DelayRecord(
            trainNumber, station, delayMinutes, System.currentTimeMillis(),
            isWeekend, isHoliday, weatherSeverity, stationCongestion,
            trackMaintenance, crewChange, technicalIssue
        );
        
        // Update local cache
        Queue<DelayRecord> history = trainDelayHistory.computeIfAbsent(
            trainNumber, k -> new LinkedList<>()
        );
        history.add(record);
        if (history.size() > SEQUENCE_LENGTH) {
            history.poll();
        }
        
        // Update database
        delayRef.child(trainNumber).push().setValue(record);
    }

    public static class DelayRecord {
        public String trainNumber;
        public String station;
        public int delayMinutes;
        public long timestamp;
        public boolean isWeekend;
        public boolean isHoliday;
        public float weatherSeverity;
        public float stationCongestion;
        public boolean trackMaintenance;
        public boolean crewChange;
        public boolean technicalIssue;

        public DelayRecord() {
            // Required for Firebase
        }

        public DelayRecord(String trainNumber, String station, int delayMinutes, long timestamp,
                         boolean isWeekend, boolean isHoliday, float weatherSeverity,
                         float stationCongestion, boolean trackMaintenance,
                         boolean crewChange, boolean technicalIssue) {
            this.trainNumber = trainNumber;
            this.station = station;
            this.delayMinutes = delayMinutes;
            this.timestamp = timestamp;
            this.isWeekend = isWeekend;
            this.isHoliday = isHoliday;
            this.weatherSeverity = weatherSeverity;
            this.stationCongestion = stationCongestion;
            this.trackMaintenance = trackMaintenance;
            this.crewChange = crewChange;
            this.technicalIssue = technicalIssue;
        }
    }
} 