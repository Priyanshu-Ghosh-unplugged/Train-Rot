package com.ghosh.trainrot.ui.tatkal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.ghosh.trainrot.databinding.FragmentTatkalBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TatkalFragment extends Fragment {
    private FragmentTatkalBinding binding;
    private TatkalViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTatkalBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TatkalViewModel.class);
        
        setupTatkalForm();
        observeViewModel();
        
        return binding.getRoot();
    }

    private void setupTatkalForm() {
        binding.startBookingButton.setOnClickListener(v -> {
            TatkalBookingService.TatkalBookingRequest request = createBookingRequest();
            viewModel.startTatkalBooking(request);
        });
    }

    private TatkalBookingService.TatkalBookingRequest createBookingRequest() {
        TatkalBookingService.TatkalBookingRequest request = new TatkalBookingService.TatkalBookingRequest();
        request.setTrainNumber(binding.trainNumber.getText().toString());
        request.setFromStation(binding.fromStation.getText().toString());
        request.setToStation(binding.toStation.getText().toString());
        request.setJourneyDate(binding.journeyDate.getText().toString());
        request.setQuota(binding.quotaSpinner.getSelectedItem().toString());
        request.setPaymentMethod(getSelectedPaymentMethod());
        // TODO: Add passenger profiles
        return request;
    }

    private TatkalBookingService.PaymentMethod getSelectedPaymentMethod() {
        int selectedId = binding.paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.payment_upi) {
            return TatkalBookingService.PaymentMethod.UPI;
        } else if (selectedId == R.id.payment_card) {
            return TatkalBookingService.PaymentMethod.CREDIT_CARD;
        } else {
            return TatkalBookingService.PaymentMethod.NET_BANKING;
        }
    }

    private void observeViewModel() {
        viewModel.getQueuePosition().observe(getViewLifecycleOwner(), position -> {
            binding.queuePosition.setText("Queue Position: " + position);
        });

        viewModel.getBookingStatus().observe(getViewLifecycleOwner(), status -> {
            binding.bookingStatus.setText(status);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.startBookingButton.setEnabled(!isLoading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.errorText.setText(error);
                binding.errorText.setVisibility(View.VISIBLE);
            } else {
                binding.errorText.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 