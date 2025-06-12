package com.ghosh.trainrot.ui.journey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.ghosh.trainrot.databinding.FragmentJourneyBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class JourneyFragment extends Fragment {
    private FragmentJourneyBinding binding;
    private JourneyViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentJourneyBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(JourneyViewModel.class);
        
        setupJourneyForm();
        observeViewModel();
        
        return binding.getRoot();
    }

    private void setupJourneyForm() {
        binding.optimizeButton.setOnClickListener(v -> {
            String from = binding.fromStation.getText().toString();
            String to = binding.toStation.getText().toString();
            String date = binding.journeyDate.getText().toString();
            
            JourneyPlanner.OptimizationMode mode = getSelectedMode();
            JourneyPlanner.JourneyPreferences preferences = createPreferences();
            
            viewModel.planJourney(from, to, date, mode, preferences);
        });
    }

    private JourneyPlanner.OptimizationMode getSelectedMode() {
        int selectedId = binding.optimizationModeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.mode_time) {
            return JourneyPlanner.OptimizationMode.MINIMUM_DURATION;
        } else if (selectedId == R.id.mode_cost) {
            return JourneyPlanner.OptimizationMode.LOWEST_COST;
        } else {
            return JourneyPlanner.OptimizationMode.COMFORT;
        }
    }

    private JourneyPlanner.JourneyPreferences createPreferences() {
        JourneyPlanner.JourneyPreferences preferences = new JourneyPlanner.JourneyPreferences();
        preferences.setMinTransferTime(Integer.parseInt(binding.minTransferTime.getText().toString()));
        preferences.setMaxTransferTime(Integer.parseInt(binding.maxTransferTime.getText().toString()));
        preferences.setIncludeLuggageBuffer(binding.includeLuggageBuffer.isChecked());
        return preferences;
    }

    private void observeViewModel() {
        viewModel.getJourneyRoutes().observe(getViewLifecycleOwner(), routes -> {
            // Update UI with journey routes
            binding.routesRecyclerView.setAdapter(new JourneyRoutesAdapter(routes));
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                // Show error message
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