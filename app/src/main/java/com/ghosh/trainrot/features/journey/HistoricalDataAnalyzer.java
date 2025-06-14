package com.ghosh.trainrot.features.journey;

import android.content.Context;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.*;
import java.util.concurrent.*;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HistoricalDataAnalyzer {
    private static final int ANALYSIS_WINDOW_DAYS = 30;
    private static final double ON_TIME_THRESHOLD = 0.8; // 80% on-time threshold
    
    private final DatabaseReference historyRef;
    private final Map<String, TrainPerformance> trainPerformanceCache;
    private final ScheduledExecutorService scheduler;

    @Inject
    public HistoricalDataAnalyzer(Context context) {
        this.historyRef = FirebaseDatabase.getInstance().getReference("train_history");
        this.trainPerformanceCache = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        loadHistoricalData();
        schedulePeriodicAnalysis();
    }

    public double getTrainReliabilityScore(String trainNumber) {
        TrainPerformance performance = trainPerformanceCache.get(trainNumber);
        if (performance == null) {
            return 0.5; // Default score for unknown trains
        }
        
        return calculateReliabilityScore(performance);
    }

    private double calculateReliabilityScore(TrainPerformance performance) {
        double score = 0.0;
        
        // On-time performance
        double onTimeRate = performance.onTimeCount / (double) performance.totalTrips;
        score += 0.4 * (onTimeRate >= ON_TIME_THRESHOLD ? 1.0 : onTimeRate / ON_TIME_THRESHOLD);
        
        // Average delay
        double normalizedDelay = Math.max(0, 1 - (performance.averageDelay / 120.0)); // 2 hours max
        score += 0.3 * normalizedDelay;
        
        // Cancellation rate
        double cancellationRate = performance.cancellationCount / (double) performance.totalTrips;
        score += 0.2 * (1 - cancellationRate);
        
        // Customer satisfaction
        score += 0.1 * performance.customerSatisfaction;
        
        return Math.min(1.0, score);
    }

    private void loadHistoricalData() {
        historyRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                for (com.google.firebase.database.DataSnapshot trainSnapshot : snapshot.getChildren()) {
                    String trainNumber = trainSnapshot.getKey();
                    TrainPerformance performance = new TrainPerformance();
                    
                    for (com.google.firebase.database.DataSnapshot tripSnapshot : trainSnapshot.getChildren()) {
                        TripRecord record = tripSnapshot.getValue(TripRecord.class);
                        if (record != null) {
                            updatePerformance(performance, record);
                        }
                    }
                    
                    trainPerformanceCache.put(trainNumber, performance);
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updatePerformance(TrainPerformance performance, TripRecord record) {
        performance.totalTrips++;
        
        if (record.isCancelled) {
            performance.cancellationCount++;
        } else {
            if (record.delayMinutes <= 15) {
                performance.onTimeCount++;
            }
            performance.totalDelayMinutes += record.delayMinutes;
            performance.averageDelay = performance.totalDelayMinutes / 
                (performance.totalTrips - performance.cancellationCount);
        }
        
        performance.customerSatisfaction = (performance.customerSatisfaction * 
            (performance.totalTrips - 1) + record.satisfactionRating) / performance.totalTrips;
    }

    private void schedulePeriodicAnalysis() {
        scheduler.scheduleAtFixedRate(() -> {
            long cutoffTime = System.currentTimeMillis() - 
                TimeUnit.DAYS.toMillis(ANALYSIS_WINDOW_DAYS);
            
            for (Map.Entry<String, TrainPerformance> entry : trainPerformanceCache.entrySet()) {
                TrainPerformance performance = entry.getValue();
                performance.lastAnalysisTime = System.currentTimeMillis();
                
                // Update database with analyzed data
                historyRef.child(entry.getKey()).child("analysis")
                    .setValue(performance);
            }
        }, 1, 24, TimeUnit.HOURS);
    }

    public void recordTrip(String trainNumber, TripRecord record) {
        // Update local cache
        TrainPerformance performance = trainPerformanceCache.computeIfAbsent(
            trainNumber, k -> new TrainPerformance()
        );
        updatePerformance(performance, record);
        
        // Update database
        historyRef.child(trainNumber).push().setValue(record);
    }

    public static class TrainPerformance {
        public int totalTrips;
        public int onTimeCount;
        public int cancellationCount;
        public long totalDelayMinutes;
        public double averageDelay;
        public double customerSatisfaction;
        public long lastAnalysisTime;

        public TrainPerformance() {
            this.totalTrips = 0;
            this.onTimeCount = 0;
            this.cancellationCount = 0;
            this.totalDelayMinutes = 0;
            this.averageDelay = 0;
            this.customerSatisfaction = 0.5;
            this.lastAnalysisTime = System.currentTimeMillis();
        }
    }

    public static class TripRecord {
        public String trainNumber;
        public String date;
        public int delayMinutes;
        public boolean isCancelled;
        public double satisfactionRating;
        public Map<String, Integer> stationDelays;
        public String cancellationReason;
        public long timestamp;

        public TripRecord() {
            // Required for Firebase
        }

        public TripRecord(String trainNumber, String date, int delayMinutes,
                         boolean isCancelled, double satisfactionRating) {
            this.trainNumber = trainNumber;
            this.date = date;
            this.delayMinutes = delayMinutes;
            this.isCancelled = isCancelled;
            this.satisfactionRating = satisfactionRating;
            this.stationDelays = new HashMap<>();
            this.timestamp = System.currentTimeMillis();
        }

        public void addStationDelay(String station, int delay) {
            stationDelays.put(station, delay);
        }

        public void setCancellationReason(String reason) {
            this.cancellationReason = reason;
        }
    }
} 