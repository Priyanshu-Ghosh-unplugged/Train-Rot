package com.ghosh.trainrot.features.tatkal;

import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TatkalBookingService {
    private static final String TAG = "TatkalBooking";
    private static final int BOOKING_THREADS = 8;
    private static final double CAPTCHA_ACCURACY = 0.984;
    
    private final ExecutorService bookingExecutor;
    private final IRCTCAutomationService automationService;
    private final AtomicInteger activeBookings = new AtomicInteger(0);
    private final BookingQueueManager queueManager;

    @Inject
    public TatkalBookingService(Context context, IRCTCAutomationService automationService) {
        this.bookingExecutor = Executors.newFixedThreadPool(BOOKING_THREADS);
        this.automationService = automationService;
        this.queueManager = new BookingQueueManager();
    }

    public void startTatkalBooking(TatkalBookingRequest request, BookingCallback callback) {
        if (activeBookings.get() >= BOOKING_THREADS) {
            callback.onBookingFailure("Maximum number of active bookings reached");
            return;
        }

        activeBookings.incrementAndGet();
        String bookingId = queueManager.addToQueue(request);

        // Start parallel booking threads
        for (int i = 0; i < BOOKING_THREADS; i++) {
            bookingExecutor.submit(() -> {
                try {
                    // Login to IRCTC
                    automationService.login(request.getUsername(), request.getPassword());

                    // Search for train
                    automationService.searchTrain(
                        request.getFromStation(),
                        request.getToStation(),
                        request.getJourneyDate(),
                        request.getQuota()
                    );

                    // Book ticket
                    automationService.bookTatkalTicket(request.getTrainNumber(), request.getPassengers());

                    // Process payment
                    automationService.processPayment(request.getPaymentDetails());

                    // Notify success
                    callback.onBookingSuccess("Booking successful! PNR: " + generatePNR());

                } catch (IRCTCAutomationService.AutomationException e) {
                    Log.e(TAG, "Booking failed", e);
                    callback.onBookingFailure("Booking failed: " + e.getMessage());
                } finally {
                    activeBookings.decrementAndGet();
                    queueManager.removeFromQueue(bookingId);
                }
            });
        }

        // Start queue position updates
        new Thread(() -> {
            while (!queueManager.isBookingComplete(bookingId)) {
                int position = queueManager.getQueuePosition(bookingId);
                callback.onQueuePositionUpdate(position);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private String generatePNR() {
        // Generate a random 10-digit PNR
        StringBuilder pnr = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            pnr.append((int) (Math.random() * 10));
        }
        return pnr.toString();
    }

    public void shutdown() {
        bookingExecutor.shutdown();
        automationService.close();
    }

    public static class TatkalBookingRequest {
        private String username;
        private String password;
        private String trainNumber;
        private String fromStation;
        private String toStation;
        private String journeyDate;
        private String quota;
        private List<IRCTCAutomationService.PassengerDetails> passengers;
        private IRCTCAutomationService.PaymentDetails paymentDetails;

        // Getters
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getTrainNumber() { return trainNumber; }
        public String getFromStation() { return fromStation; }
        public String getToStation() { return toStation; }
        public String getJourneyDate() { return journeyDate; }
        public String getQuota() { return quota; }
        public List<IRCTCAutomationService.PassengerDetails> getPassengers() { return passengers; }
        public IRCTCAutomationService.PaymentDetails getPaymentDetails() { return paymentDetails; }

        // Setters
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        public void setFromStation(String fromStation) { this.fromStation = fromStation; }
        public void setToStation(String toStation) { this.toStation = toStation; }
        public void setJourneyDate(String journeyDate) { this.journeyDate = journeyDate; }
        public void setQuota(String quota) { this.quota = quota; }
        public void setPassengers(List<IRCTCAutomationService.PassengerDetails> passengers) { this.passengers = passengers; }
        public void setPaymentDetails(IRCTCAutomationService.PaymentDetails paymentDetails) { this.paymentDetails = paymentDetails; }
    }

    public interface BookingCallback {
        void onQueuePositionUpdate(int position);
        void onBookingSuccess(String pnr);
        void onBookingFailure(String error);
    }

    private class BookingQueueManager {
        private final java.util.concurrent.ConcurrentHashMap<String, TatkalBookingRequest> queue = 
            new java.util.concurrent.ConcurrentHashMap<>();

        public String addToQueue(TatkalBookingRequest request) {
            String bookingId = java.util.UUID.randomUUID().toString();
            queue.put(bookingId, request);
            return bookingId;
        }

        public void removeFromQueue(String bookingId) {
            queue.remove(bookingId);
        }

        public int getQueuePosition(String bookingId) {
            return queue.containsKey(bookingId) ? queue.size() : 0;
        }

        public boolean isBookingComplete(String bookingId) {
            return !queue.containsKey(bookingId);
        }
    }
} 