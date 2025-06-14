package com.ghosh.trainrot.features.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ghosh.trainrot.data.model.Journey;
import com.ghosh.trainrot.data.model.Booking;
import java.util.List;
import javax.inject.Inject;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Journey>> recentJourneys = new MutableLiveData<>();
    private final MutableLiveData<List<Booking>> upcomingBookings = new MutableLiveData<>();

    @Inject
    public HomeViewModel() {
        // Initialize with empty lists
        recentJourneys.setValue(List.of());
        upcomingBookings.setValue(List.of());
    }

    public LiveData<List<Journey>> getRecentJourneys() {
        return recentJourneys;
    }

    public LiveData<List<Booking>> getUpcomingBookings() {
        return upcomingBookings;
    }
} 