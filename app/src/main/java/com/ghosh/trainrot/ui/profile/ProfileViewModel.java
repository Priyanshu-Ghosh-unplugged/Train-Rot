package com.ghosh.trainrot.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import javax.inject.Inject;

public class ProfileViewModel extends ViewModel {
    private final FirebaseAuth auth;
    private final DatabaseReference database;
    private final MutableLiveData<UserProfile> profile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public ProfileViewModel() {
        this.auth = FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance().getReference("users");
        loadProfile();
    }

    private void loadProfile() {
        String userId = auth.getCurrentUser().getUid();
        loading.setValue(true);

        database.child(userId).get().addOnSuccessListener(snapshot -> {
            UserProfile userProfile = snapshot.getValue(UserProfile.class);
            if (userProfile != null) {
                profile.postValue(userProfile);
            }
            loading.postValue(false);
        }).addOnFailureListener(e -> {
            error.postValue(e.getMessage());
            loading.postValue(false);
        });
    }

    public void updateProfile(String name, String email, String phone) {
        String userId = auth.getCurrentUser().getUid();
        loading.setValue(true);

        UserProfile updatedProfile = new UserProfile(name, email, phone);
        database.child(userId).setValue(updatedProfile)
            .addOnSuccessListener(aVoid -> {
                profile.postValue(updatedProfile);
                loading.postValue(false);
            })
            .addOnFailureListener(e -> {
                error.postValue(e.getMessage());
                loading.postValue(false);
            });
    }

    public LiveData<UserProfile> getProfile() {
        return profile;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public static class UserProfile {
        private String name;
        private String email;
        private String phone;
        private String userId;

        public UserProfile() {
            // Required for Firebase
        }

        public UserProfile(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
} 