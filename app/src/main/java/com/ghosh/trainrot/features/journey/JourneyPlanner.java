package com.ghosh.trainrot.features.journey;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.*;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.*;

@Singleton
public class JourneyPlanner {
    private static final int PARAMETER_COUNT = 17;
    private static final int ROUTE_THRESHOLD = 28000;
    private static final int MAX_CONNECTIONS = 3;
    private static final int MIN_TRANSFER_TIME = 15; // minutes
    private static final int MAX_TRANSFER_TIME = 120; // minutes

    private final ExecutorService executor;
    private final DelayPredictor delayPredictor;
    private final StationFacilityAnalyzer facilityAnalyzer;
    private final HistoricalDataAnalyzer historicalAnalyzer;

    @Inject
    public JourneyPlanner(DelayPredictor delayPredictor, 
                         StationFacilityAnalyzer facilityAnalyzer,
                         HistoricalDataAnalyzer historicalAnalyzer) {
        this.executor = Executors.newFixedThreadPool(4);
        this.delayPredictor = delayPredictor;
        this.facilityAnalyzer = facilityAnalyzer;
        this.historicalAnalyzer = historicalAnalyzer;
    }

    public enum OptimizationMode {
        MINIMUM_DURATION,
        LOWEST_COST,
        COMFORT
    }

    public List<JourneyRoute> planJourney(String source, String destination, 
                                        OptimizationMode mode, 
                                        JourneyPreferences preferences) {
        // 1. Fetch available routes
        List<JourneyRoute> allRoutes = fetchAvailableRoutes(source, destination);
        
        // 2. Apply optimization based on mode
        List<JourneyRoute> optimizedRoutes = optimizeRoutes(allRoutes, mode, preferences);
        
        // 3. Consider historical delay patterns
        applyDelayPatterns(optimizedRoutes);
        
        // 4. Apply smart filters
        return applySmartFilters(optimizedRoutes, preferences);
    }

    private List<JourneyRoute> fetchAvailableRoutes(String source, String destination) {
        List<JourneyRoute> routes = new ArrayList<>();
        Set<String> visitedStations = new HashSet<>();
        
        // Use BFS to find all possible routes
        Queue<JourneyRoute> queue = new LinkedList<>();
        queue.add(new JourneyRoute(source));
        
        while (!queue.isEmpty() && routes.size() < ROUTE_THRESHOLD) {
            JourneyRoute currentRoute = queue.poll();
            String lastStation = currentRoute.getLastStation();
            
            if (lastStation.equals(destination)) {
                routes.add(currentRoute);
                continue;
            }
            
            if (currentRoute.getLegs().size() >= MAX_CONNECTIONS) {
                continue;
            }
            
            List<JourneyLeg> possibleLegs = findPossibleLegs(lastStation);
            for (JourneyLeg leg : possibleLegs) {
                String nextStation = leg.getDestinationStation();
                if (!visitedStations.contains(nextStation)) {
                    visitedStations.add(nextStation);
                    JourneyRoute newRoute = currentRoute.addLeg(leg);
                    queue.add(newRoute);
                }
            }
        }
        
        return routes;
    }

    private List<JourneyRoute> optimizeRoutes(List<JourneyRoute> routes, 
                                            OptimizationMode mode,
                                            JourneyPreferences preferences) {
        switch (mode) {
            case MINIMUM_DURATION:
                return optimizeForDuration(routes);
            case LOWEST_COST:
                return optimizeForCost(routes, preferences);
            case COMFORT:
                return optimizeForComfort(routes, preferences);
            default:
                return routes;
        }
    }

    private List<JourneyRoute> optimizeForDuration(List<JourneyRoute> routes) {
        return routes.stream()
            .sorted(Comparator.comparingInt(JourneyRoute::getTotalDuration))
            .limit(10)
            .collect(Collectors.toList());
    }

    private List<JourneyRoute> optimizeForCost(List<JourneyRoute> routes, 
                                             JourneyPreferences preferences) {
        // Apply split-ticketing logic
        List<JourneyRoute> optimizedRoutes = new ArrayList<>();
        
        for (JourneyRoute route : routes) {
            List<JourneyRoute> splitRoutes = findSplitTicketOptions(route);
            optimizedRoutes.addAll(splitRoutes);
        }
        
        return optimizedRoutes.stream()
            .sorted(Comparator.comparingDouble(JourneyRoute::getTotalCost))
            .limit(10)
            .collect(Collectors.toList());
    }

    private List<JourneyRoute> optimizeForComfort(List<JourneyRoute> routes,
                                                JourneyPreferences preferences) {
        return routes.stream()
            .map(route -> {
                double comfortScore = calculateComfortScore(route, preferences);
                route.setComfortScore(comfortScore);
                return route;
            })
            .sorted(Comparator.comparingDouble(JourneyRoute::getComfortScore).reversed())
            .limit(10)
            .collect(Collectors.toList());
    }

    private void applyDelayPatterns(List<JourneyRoute> routes) {
        for (JourneyRoute route : routes) {
            for (JourneyLeg leg : route.getLegs()) {
                int predictedDelay = delayPredictor.predictDelay(
                    leg.getTrainNumber(),
                    leg.getSourceStation()
                );
                leg.setPredictedDelay(predictedDelay);
            }
        }
    }

    private List<JourneyRoute> applySmartFilters(List<JourneyRoute> routes,
                                               JourneyPreferences preferences) {
        return routes.stream()
            .filter(route -> isValidTransferTime(route, preferences))
            .filter(route -> matchesTrainClassPreferences(route, preferences))
            .filter(route -> hasAdequateLuggageBuffer(route, preferences))
            .collect(Collectors.toList());
    }

    private boolean isValidTransferTime(JourneyRoute route, JourneyPreferences preferences) {
        List<JourneyLeg> legs = route.getLegs();
        for (int i = 0; i < legs.size() - 1; i++) {
            JourneyLeg currentLeg = legs.get(i);
            JourneyLeg nextLeg = legs.get(i + 1);
            
            int transferTime = calculateTransferTime(currentLeg, nextLeg);
            if (transferTime < preferences.getMinTransferTime() || 
                transferTime > preferences.getMaxTransferTime()) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesTrainClassPreferences(JourneyRoute route, 
                                               JourneyPreferences preferences) {
        return route.getLegs().stream()
            .allMatch(leg -> preferences.getPreferredTrainClasses()
                .contains(leg.getTrainClass()));
    }

    private boolean hasAdequateLuggageBuffer(JourneyRoute route, 
                                           JourneyPreferences preferences) {
        if (!preferences.isIncludeLuggageBuffer()) {
            return true;
        }
        
        return route.getLegs().stream()
            .allMatch(leg -> calculateTransferTime(leg, null) >= 15);
    }

    private double calculateComfortScore(JourneyRoute route, 
                                       JourneyPreferences preferences) {
        double score = 0.0;
        
        for (JourneyLeg leg : route.getLegs()) {
            // Train class comfort
            score += getTrainClassComfortScore(leg.getTrainClass());
            
            // Station facility comfort
            score += facilityAnalyzer.getStationComfortScore(leg.getSourceStation());
            score += facilityAnalyzer.getStationComfortScore(leg.getDestinationStation());
            
            // Historical reliability
            score += historicalAnalyzer.getTrainReliabilityScore(leg.getTrainNumber());
        }
        
        return score / route.getLegs().size();
    }

    private double getTrainClassComfortScore(String trainClass) {
        switch (trainClass) {
            case "1A": return 1.0;
            case "2A": return 0.8;
            case "3A": return 0.6;
            case "SL": return 0.4;
            default: return 0.5;
        }
    }

    private int calculateTransferTime(JourneyLeg currentLeg, JourneyLeg nextLeg) {
        if (nextLeg == null) {
            return 0;
        }
        
        int arrivalTime = currentLeg.getArrivalTimeMinutes();
        int departureTime = nextLeg.getDepartureTimeMinutes();
        
        return departureTime - arrivalTime;
    }

    private List<JourneyLeg> findPossibleLegs(String station) {
        // TODO: Implement actual train schedule lookup
        return new ArrayList<>();
    }

    private List<JourneyRoute> findSplitTicketOptions(JourneyRoute route) {
        // TODO: Implement split-ticketing logic
        return Collections.singletonList(route);
    }

    public static class JourneyPreferences {
        private int minTransferTime = MIN_TRANSFER_TIME;
        private int maxTransferTime = MAX_TRANSFER_TIME;
        private List<String> preferredTrainClasses = new ArrayList<>();
        private boolean includeLuggageBuffer = true;

        // Getters and setters
        public int getMinTransferTime() { return minTransferTime; }
        public void setMinTransferTime(int minTransferTime) { 
            this.minTransferTime = minTransferTime; 
        }
        
        public int getMaxTransferTime() { return maxTransferTime; }
        public void setMaxTransferTime(int maxTransferTime) { 
            this.maxTransferTime = maxTransferTime; 
        }
        
        public List<String> getPreferredTrainClasses() { return preferredTrainClasses; }
        public void setPreferredTrainClasses(List<String> preferredTrainClasses) { 
            this.preferredTrainClasses = preferredTrainClasses; 
        }
        
        public boolean isIncludeLuggageBuffer() { return includeLuggageBuffer; }
        public void setIncludeLuggageBuffer(boolean includeLuggageBuffer) { 
            this.includeLuggageBuffer = includeLuggageBuffer; 
        }
    }

    public static class JourneyRoute {
        private List<JourneyLeg> legs = new ArrayList<>();
        private double totalCost;
        private int totalDuration;
        private double comfortScore;

        public JourneyRoute(String sourceStation) {
            // Initialize with source station
        }

        public JourneyRoute addLeg(JourneyLeg leg) {
            JourneyRoute newRoute = new JourneyRoute(legs.get(0).getSourceStation());
            newRoute.legs.addAll(this.legs);
            newRoute.legs.add(leg);
            return newRoute;
        }

        public String getLastStation() {
            return legs.isEmpty() ? null : legs.get(legs.size() - 1).getDestinationStation();
        }

        // Getters and setters
        public List<JourneyLeg> getLegs() { return legs; }
        public void setLegs(List<JourneyLeg> legs) { this.legs = legs; }
        
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
        
        public int getTotalDuration() { return totalDuration; }
        public void setTotalDuration(int totalDuration) { 
            this.totalDuration = totalDuration; 
        }
        
        public double getComfortScore() { return comfortScore; }
        public void setComfortScore(double comfortScore) { 
            this.comfortScore = comfortScore; 
        }
    }

    public static class JourneyLeg {
        private String trainNumber;
        private String trainName;
        private String sourceStation;
        private String destinationStation;
        private int departureTimeMinutes;
        private int arrivalTimeMinutes;
        private String platform;
        private double fare;
        private String trainClass;
        private int predictedDelay;

        // Getters and setters
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { 
            this.trainNumber = trainNumber; 
        }
        
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { 
            this.trainName = trainName; 
        }
        
        public String getSourceStation() { return sourceStation; }
        public void setSourceStation(String sourceStation) { 
            this.sourceStation = sourceStation; 
        }
        
        public String getDestinationStation() { return destinationStation; }
        public void setDestinationStation(String destinationStation) { 
            this.destinationStation = destinationStation; 
        }
        
        public int getDepartureTimeMinutes() { return departureTimeMinutes; }
        public void setDepartureTimeMinutes(int departureTimeMinutes) { 
            this.departureTimeMinutes = departureTimeMinutes; 
        }
        
        public int getArrivalTimeMinutes() { return arrivalTimeMinutes; }
        public void setArrivalTimeMinutes(int arrivalTimeMinutes) { 
            this.arrivalTimeMinutes = arrivalTimeMinutes; 
        }
        
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { 
            this.platform = platform; 
        }
        
        public double getFare() { return fare; }
        public void setFare(double fare) { 
            this.fare = fare; 
        }
        
        public String getTrainClass() { return trainClass; }
        public void setTrainClass(String trainClass) { 
            this.trainClass = trainClass; 
        }
        
        public int getPredictedDelay() { return predictedDelay; }
        public void setPredictedDelay(int predictedDelay) { 
            this.predictedDelay = predictedDelay; 
        }
    }
} 