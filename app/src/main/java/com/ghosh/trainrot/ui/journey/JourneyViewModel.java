package com.ghosh.trainrot.ui.journey;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ghosh.trainrot.features.journey.JourneyPlanner;
import java.util.List;
import javax.inject.Inject;

public class JourneyViewModel extends ViewModel {
    private final JourneyPlanner journeyPlanner;
    private final MutableLiveData<List<JourneyPlanner.JourneyRoute>> journeyRoutes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public JourneyViewModel(JourneyPlanner journeyPlanner) {
        this.journeyPlanner = journeyPlanner;
    }

    public void planJourney(String from, String to, String date,
                          JourneyPlanner.OptimizationMode mode,
                          JourneyPlanner.JourneyPreferences preferences) {
        loading.setValue(true);
        error.setValue(null);

        // Execute journey planning in background
        new Thread(() -> {
            try {
                List<JourneyPlanner.JourneyRoute> routes = journeyPlanner.planJourney(from, to, mode, preferences);
                journeyRoutes.postValue(routes);
            } catch (Exception e) {
                error.postValue(e.getMessage());
            } finally {
                loading.postValue(false);
            }
        }).start();
    }

    public LiveData<List<JourneyPlanner.JourneyRoute>> getJourneyRoutes() {
        return journeyRoutes;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }
} 