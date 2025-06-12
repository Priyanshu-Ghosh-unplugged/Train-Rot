package com.ghosh.trainrot.ui.tatkal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ghosh.trainrot.features.tatkal.TatkalBookingService;
import javax.inject.Inject;

public class TatkalViewModel extends ViewModel {
    private final TatkalBookingService tatkalService;
    private final MutableLiveData<Integer> queuePosition = new MutableLiveData<>();
    private final MutableLiveData<String> bookingStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public TatkalViewModel(TatkalBookingService tatkalService) {
        this.tatkalService = tatkalService;
    }

    public void startTatkalBooking(TatkalBookingService.TatkalBookingRequest request) {
        loading.setValue(true);
        error.setValue(null);

        tatkalService.startTatkalBooking(request, new TatkalBookingService.BookingCallback() {
            @Override
            public void onQueuePositionUpdate(int position) {
                queuePosition.postValue(position);
            }

            @Override
            public void onBookingSuccess(String pnr) {
                bookingStatus.postValue("Booking Successful! PNR: " + pnr);
                loading.postValue(false);
            }

            @Override
            public void onBookingFailure(String error) {
                TatkalViewModel.this.error.postValue(error);
                loading.postValue(false);
            }
        });
    }

    public LiveData<Integer> getQueuePosition() {
        return queuePosition;
    }

    public LiveData<String> getBookingStatus() {
        return bookingStatus;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }
} 