package com.ghosh.trainrot.features.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class UserViewModel extends ViewModel {
    private final UserManager userManager;
    private final MutableLiveData<UserManager.UserProfile> userProfile;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> error;

    @Inject
    public UserViewModel(UserManager userManager) {
        this.userManager = userManager;
        this.userProfile = new MutableLiveData<>();
        this.isLoading = new MutableLiveData<>(false);
        this.error = new MutableLiveData<>();
        loadUserProfile();
    }

    private void loadUserProfile() {
        isLoading.setValue(true);
        UserManager.UserProfile profile = userManager.getCurrentUser();
        if (profile != null) {
            userProfile.setValue(profile);
        } else {
            error.setValue("User not logged in");
        }
        isLoading.setValue(false);
    }

    public void updateProfile(UserManager.UserProfile profile) {
        isLoading.setValue(true);
        try {
            userManager.updateProfile(profile);
            userProfile.setValue(profile);
        } catch (Exception e) {
            error.setValue(e.getMessage());
        }
        isLoading.setValue(false);
    }

    public void addFamilyMember(UserManager.FamilyMember member) {
        isLoading.setValue(true);
        try {
            userManager.addFamilyMember(member);
            UserManager.UserProfile currentProfile = userProfile.getValue();
            if (currentProfile != null) {
                currentProfile.familyMembers.add(member);
                userProfile.setValue(currentProfile);
            }
        } catch (Exception e) {
            error.setValue(e.getMessage());
        }
        isLoading.setValue(false);
    }

    public void updateFamilyMember(UserManager.FamilyMember member) {
        isLoading.setValue(true);
        try {
            UserManager.UserProfile currentProfile = userProfile.getValue();
            if (currentProfile != null) {
                for (int i = 0; i < currentProfile.familyMembers.size(); i++) {
                    if (currentProfile.familyMembers.get(i).id.equals(member.id)) {
                        currentProfile.familyMembers.set(i, member);
                        break;
                    }
                }
                userManager.updateProfile(currentProfile);
                userProfile.setValue(currentProfile);
            }
        } catch (Exception e) {
            error.setValue(e.getMessage());
        }
        isLoading.setValue(false);
    }

    public void removeFamilyMember(String memberId) {
        isLoading.setValue(true);
        try {
            userManager.removeFamilyMember(memberId);
            UserManager.UserProfile currentProfile = userProfile.getValue();
            if (currentProfile != null) {
                currentProfile.familyMembers.removeIf(m -> m.id.equals(memberId));
                userProfile.setValue(currentProfile);
            }
        } catch (Exception e) {
            error.setValue(e.getMessage());
        }
        isLoading.setValue(false);
    }

    public void updateTravelPreferences(UserManager.TravelPreferences preferences) {
        isLoading.setValue(true);
        try {
            userManager.updateTravelPreferences(preferences);
            UserManager.UserProfile currentProfile = userProfile.getValue();
            if (currentProfile != null) {
                currentProfile.travelPreferences = preferences;
                userProfile.setValue(currentProfile);
            }
        } catch (Exception e) {
            error.setValue(e.getMessage());
        }
        isLoading.setValue(false);
    }

    public void updatePreferredTrainClasses(List<String> trainClasses) {
        UserManager.UserProfile currentProfile = userProfile.getValue();
        if (currentProfile != null) {
            currentProfile.travelPreferences.preferredTrainClasses = trainClasses;
            updateTravelPreferences(currentProfile.travelPreferences);
        }
    }

    public void addRecentBooking(UserManager.BookingRecord booking) {
        isLoading.setValue(true);
        try {
            userManager.addRecentBooking(booking);
            UserManager.UserProfile currentProfile = userProfile.getValue();
            if (currentProfile != null) {
                currentProfile.recentBookings.add(0, booking);
                if (currentProfile.recentBookings.size() > 5) {
                    currentProfile.recentBookings.remove(
                        currentProfile.recentBookings.size() - 1
                    );
                }
                userProfile.setValue(currentProfile);
            }
        } catch (Exception e) {
            error.setValue(e.getMessage());
        }
        isLoading.setValue(false);
    }

    public LiveData<UserManager.UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final UserManager userManager;

        public Factory(UserManager userManager) {
            this.userManager = userManager;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(UserViewModel.class)) {
                return (T) new UserViewModel(userManager);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
} 