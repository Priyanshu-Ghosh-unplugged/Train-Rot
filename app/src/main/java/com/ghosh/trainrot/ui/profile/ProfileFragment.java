package com.ghosh.trainrot.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.ghosh.trainrot.databinding.FragmentProfileBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        setupProfileUI();
        observeViewModel();
        
        return binding.getRoot();
    }

    private void setupProfileUI() {
        binding.saveButton.setOnClickListener(v -> {
            String name = binding.nameInput.getText().toString();
            String email = binding.emailInput.getText().toString();
            String phone = binding.phoneInput.getText().toString();
            
            viewModel.updateProfile(name, email, phone);
        });

        binding.preferencesButton.setOnClickListener(v -> {
            // TODO: Navigate to preferences screen
        });

        binding.familyGroupButton.setOnClickListener(v -> {
            // TODO: Navigate to family group screen
        });

        binding.historyButton.setOnClickListener(v -> {
            // TODO: Navigate to history screen
        });
    }

    private void observeViewModel() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            binding.nameInput.setText(profile.getName());
            binding.emailInput.setText(profile.getEmail());
            binding.phoneInput.setText(profile.getPhone());
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.saveButton.setEnabled(!isLoading);
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