package com.ghosh.trainrot.features.journey;

import android.content.Context;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StationFacilityAnalyzer {
    private static final double AC_LOUNGE_WEIGHT = 0.3;
    private static final double PLATFORM_QUALITY_WEIGHT = 0.2;
    private static final double FOOD_OPTIONS_WEIGHT = 0.15;
    private static final double WASHROOM_QUALITY_WEIGHT = 0.15;
    private static final double ACCESSIBILITY_WEIGHT = 0.1;
    private static final double SECURITY_WEIGHT = 0.1;
    
    private final DatabaseReference stationRef;
    private final Map<String, StationFacilities> stationCache;

    @Inject
    public StationFacilityAnalyzer(Context context) {
        this.stationRef = FirebaseDatabase.getInstance().getReference("stations");
        this.stationCache = new HashMap<>();
        loadStationData();
    }

    public double getStationComfortScore(String stationCode) {
        StationFacilities facilities = stationCache.get(stationCode);
        if (facilities == null) {
            return 0.5; // Default score for unknown stations
        }
        
        return calculateComfortScore(facilities);
    }

    private double calculateComfortScore(StationFacilities facilities) {
        double score = 0.0;
        
        // AC Lounge availability and quality
        score += AC_LOUNGE_WEIGHT * (facilities.hasACLounge ? 1.0 : 0.0);
        score += AC_LOUNGE_WEIGHT * facilities.acLoungeQuality;
        
        // Platform quality
        score += PLATFORM_QUALITY_WEIGHT * facilities.platformQuality;
        
        // Food options
        score += FOOD_OPTIONS_WEIGHT * facilities.foodOptionsQuality;
        
        // Washroom quality
        score += WASHROOM_QUALITY_WEIGHT * facilities.washroomQuality;
        
        // Accessibility features
        score += ACCESSIBILITY_WEIGHT * facilities.accessibilityScore;
        
        // Security measures
        score += SECURITY_WEIGHT * facilities.securityScore;
        
        return Math.min(1.0, score);
    }

    private void loadStationData() {
        stationRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                for (com.google.firebase.database.DataSnapshot stationSnapshot : snapshot.getChildren()) {
                    StationFacilities facilities = stationSnapshot.getValue(StationFacilities.class);
                    if (facilities != null) {
                        stationCache.put(stationSnapshot.getKey(), facilities);
                    }
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                // Handle error
            }
        });
    }

    public void updateStationFacilities(String stationCode, StationFacilities facilities) {
        // Update local cache
        stationCache.put(stationCode, facilities);
        
        // Update database
        stationRef.child(stationCode).setValue(facilities);
    }

    public static class StationFacilities {
        public boolean hasACLounge;
        public double acLoungeQuality;
        public double platformQuality;
        public double foodOptionsQuality;
        public double washroomQuality;
        public double accessibilityScore;
        public double securityScore;
        public List<String> availableFacilities;
        public Map<String, Integer> facilityRatings;
        public long lastUpdated;

        public StationFacilities() {
            // Required for Firebase
        }

        public StationFacilities(boolean hasACLounge, double acLoungeQuality,
                               double platformQuality, double foodOptionsQuality,
                               double washroomQuality, double accessibilityScore,
                               double securityScore) {
            this.hasACLounge = hasACLounge;
            this.acLoungeQuality = acLoungeQuality;
            this.platformQuality = platformQuality;
            this.foodOptionsQuality = foodOptionsQuality;
            this.washroomQuality = washroomQuality;
            this.accessibilityScore = accessibilityScore;
            this.securityScore = securityScore;
            this.availableFacilities = new ArrayList<>();
            this.facilityRatings = new HashMap<>();
            this.lastUpdated = System.currentTimeMillis();
        }

        public void addFacility(String facility, int rating) {
            availableFacilities.add(facility);
            facilityRatings.put(facility, rating);
            lastUpdated = System.currentTimeMillis();
        }

        public void removeFacility(String facility) {
            availableFacilities.remove(facility);
            facilityRatings.remove(facility);
            lastUpdated = System.currentTimeMillis();
        }

        public void updateFacilityRating(String facility, int rating) {
            if (facilityRatings.containsKey(facility)) {
                facilityRatings.put(facility, rating);
                lastUpdated = System.currentTimeMillis();
            }
        }
    }
} 