package com.ghosh.trainrot.ui.tracking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.ghosh.trainrot.databinding.FragmentTrackingBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TrackingFragment extends Fragment {
    private FragmentTrackingBinding binding;
    private TrackingViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTrackingBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TrackingViewModel.class);
        
        setupTrackingUI();
        observeViewModel();
        
        return binding.getRoot();
    }

    private void setupTrackingUI() {
        binding.startTrackingButton.setOnClickListener(v -> {
            String trainNumber = binding.trainNumber.getText().toString();
            viewModel.startTracking(trainNumber);
        });

        binding.reportIssueButton.setOnClickListener(v -> {
            showReportIssueDialog();
        });

        binding.startARNavigationButton.setOnClickListener(v -> {
            String destination = binding.destinationStation.getText().toString();
            viewModel.startARNavigation(destination);
        });
    }

    private void showReportIssueDialog() {
        // TODO: Implement issue reporting dialog
    }

    private void observeViewModel() {
        viewModel.getPlatform().observe(getViewLifecycleOwner(), platform -> {
            binding.platformText.setText("Platform: " + platform);
        });

        viewModel.getDelay().observe(getViewLifecycleOwner(), delay -> {
            binding.delayText.setText("Delay: " + delay + " minutes");
        });

        viewModel.getCoachPosition().observe(getViewLifecycleOwner(), position -> {
            binding.coachPositionText.setText("Coach Position: " + position);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.startTrackingButton.setEnabled(!isLoading);
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