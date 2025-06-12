package com.ghosh.trainrot.ui.tracking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ghosh.trainrot.features.realtime.RealTimeService;
import javax.inject.Inject;

public class TrackingViewModel extends ViewModel {
    private final RealTimeService realTimeService;
    private final MutableLiveData<String> platform = new MutableLiveData<>();
    private final MutableLiveData<Integer> delay = new MutableLiveData<>();
    private final MutableLiveData<String> coachPosition = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public TrackingViewModel(RealTimeService realTimeService) {
        this.realTimeService = realTimeService;
    }

    public void startTracking(String trainNumber) {
        loading.setValue(true);
        error.setValue(null);

        realTimeService.startTracking(trainNumber, new RealTimeService.TrackingCallback() {
            @Override
            public void onPlatformUpdate(String platform) {
                TrackingViewModel.this.platform.postValue(platform);
            }

            @Override
            public void onDelayUpdate(int delayMinutes) {
                delay.postValue(delayMinutes);
            }

            @Override
            public void onCoachPositionUpdate(String coachNumber, String position) {
                coachPosition.postValue(coachNumber + ": " + position);
            }

            @Override
            public void onNavigationUpdate(RealTimeService.NavigationInstruction instruction) {
                // Handle navigation updates
            }
        });
    }

    public void startARNavigation(String destination) {
        realTimeService.startARNavigation(destination);
    }

    public void reportIssue(RealTimeService.IssueReport report) {
        realTimeService.reportIssue(report);
    }

    public LiveData<String> getPlatform() {
        return platform;
    }

    public LiveData<Integer> getDelay() {
        return delay;
    }

    public LiveData<String> getCoachPosition() {
        return coachPosition;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }
} 