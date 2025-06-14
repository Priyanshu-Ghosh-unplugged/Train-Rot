package com.ghosh.trainrot.features.user;

import android.content.Context;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserManager {
    private static final int MAX_FAMILY_MEMBERS = 6;
    private static final int MAX_RECENT_BOOKINGS = 5;
    
    private final FirebaseAuth auth;
    private final DatabaseReference userRef;
    private final Map<String, UserProfile> profileCache;
    private UserProfile currentUser;

    @Inject
    public UserManager(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.userRef = FirebaseDatabase.getInstance().getReference("users");
        this.profileCache = new HashMap<>();
        setupAuthStateListener();
    }

    private void setupAuthStateListener() {
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                loadUserProfile(firebaseUser.getUid());
            } else {
                currentUser = null;
            }
        });
    }

    public void signInWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(task.getException().getMessage());
                }
            });
    }

    public void signInWithPhone(String phoneNumber, String verificationCode, 
                              AuthCallback callback) {
        AuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, phoneNumber);
        auth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(task.getException().getMessage());
                }
            });
    }

    public void signInWithEmail(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(task.getException().getMessage());
                }
            });
    }

    public void signOut() {
        auth.signOut();
    }

    private void loadUserProfile(String userId) {
        userRef.child(userId).addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    UserProfile profile = snapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        currentUser = profile;
                        profileCache.put(userId, profile);
                    } else {
                        createNewProfile(userId);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle error
                }
            });
    }

    private void createNewProfile(String userId) {
        UserProfile profile = new UserProfile();
        profile.userId = userId;
        currentUser = profile;
        profileCache.put(userId, profile);
        userRef.child(userId).setValue(profile);
    }

    public void updateProfile(UserProfile profile) {
        if (currentUser != null) {
            userRef.child(currentUser.userId).setValue(profile);
            currentUser = profile;
            profileCache.put(profile.userId, profile);
        }
    }

    public void addFamilyMember(FamilyMember member) {
        if (currentUser != null && 
            currentUser.familyMembers.size() < MAX_FAMILY_MEMBERS) {
            currentUser.familyMembers.add(member);
            userRef.child(currentUser.userId)
                .child("familyMembers")
                .setValue(currentUser.familyMembers);
        }
    }

    public void removeFamilyMember(String memberId) {
        if (currentUser != null) {
            currentUser.familyMembers.removeIf(m -> m.id.equals(memberId));
            userRef.child(currentUser.userId)
                .child("familyMembers")
                .setValue(currentUser.familyMembers);
        }
    }

    public void addRecentBooking(BookingRecord booking) {
        if (currentUser != null) {
            currentUser.recentBookings.add(0, booking);
            if (currentUser.recentBookings.size() > MAX_RECENT_BOOKINGS) {
                currentUser.recentBookings.remove(
                    currentUser.recentBookings.size() - 1
                );
            }
            userRef.child(currentUser.userId)
                .child("recentBookings")
                .setValue(currentUser.recentBookings);
        }
    }

    public void updateTravelPreferences(TravelPreferences preferences) {
        if (currentUser != null) {
            currentUser.travelPreferences = preferences;
            userRef.child(currentUser.userId)
                .child("travelPreferences")
                .setValue(preferences);
        }
    }

    public UserProfile getCurrentUser() {
        return currentUser;
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public static class UserProfile {
        public String userId;
        public String name;
        public String email;
        public String phone;
        public String photoUrl;
        public List<FamilyMember> familyMembers;
        public List<BookingRecord> recentBookings;
        public TravelPreferences travelPreferences;
        public Map<String, Object> preferences;
        public long lastUpdated;

        public UserProfile() {
            this.familyMembers = new ArrayList<>();
            this.recentBookings = new ArrayList<>();
            this.travelPreferences = new TravelPreferences();
            this.preferences = new HashMap<>();
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    public static class FamilyMember {
        public String id;
        public String name;
        public String relation;
        public int age;
        public String gender;
        public String idProofType;
        public String idProofNumber;

        public FamilyMember() {
            // Required for Firebase
        }

        public FamilyMember(String name, String relation, int age, 
                          String gender, String idProofType, String idProofNumber) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.relation = relation;
            this.age = age;
            this.gender = gender;
            this.idProofType = idProofType;
            this.idProofNumber = idProofNumber;
        }
    }

    public static class BookingRecord {
        public String bookingId;
        public String trainNumber;
        public String fromStation;
        public String toStation;
        public String date;
        public String pnr;
        public String status;
        public List<PassengerDetails> passengers;
        public long timestamp;

        public BookingRecord() {
            // Required for Firebase
        }

        public BookingRecord(String trainNumber, String fromStation, 
                           String toStation, String date, String pnr,
                           List<PassengerDetails> passengers) {
            this.bookingId = UUID.randomUUID().toString();
            this.trainNumber = trainNumber;
            this.fromStation = fromStation;
            this.toStation = toStation;
            this.date = date;
            this.pnr = pnr;
            this.status = "CONFIRMED";
            this.passengers = passengers;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class PassengerDetails {
        public String name;
        public int age;
        public String gender;
        public String berthPreference;
        public String seatNumber;
        public String coachNumber;

        public PassengerDetails() {
            // Required for Firebase
        }

        public PassengerDetails(String name, int age, String gender, 
                              String berthPreference) {
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.berthPreference = berthPreference;
        }
    }

    public static class TravelPreferences {
        public boolean vegetarian;
        public boolean lowerBerthPriority;
        public List<String> preferredTrainClasses;
        public int minTransferTime;
        public int maxTransferTime;
        public boolean includeLuggageBuffer;
        public Map<String, Object> customPreferences;

        public TravelPreferences() {
            this.vegetarian = false;
            this.lowerBerthPriority = false;
            this.preferredTrainClasses = new ArrayList<>();
            this.minTransferTime = 15;
            this.maxTransferTime = 120;
            this.includeLuggageBuffer = true;
            this.customPreferences = new HashMap<>();
        }
    }
} 