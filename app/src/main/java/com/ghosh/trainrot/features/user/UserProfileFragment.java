package com.ghosh.trainrot.features.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ghosh.trainrot.R;
import com.ghosh.trainrot.databinding.FragmentUserProfileBinding;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class UserProfileFragment extends Fragment {
    private FragmentUserProfileBinding binding;
    private UserViewModel viewModel;
    private FamilyMemberAdapter familyMemberAdapter;
    private BookingHistoryAdapter bookingHistoryAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupProfileSection();
        setupFamilyMembersSection();
        setupBookingHistorySection();
        setupTravelPreferencesSection();
        observeViewModel();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_profile) {
                showEditProfileDialog();
                return true;
            }
            return false;
        });
    }

    private void setupProfileSection() {
        MaterialCardView profileCard = binding.profileCard;
        profileCard.setOnClickListener(v -> showEditProfileDialog());
        
        FloatingActionButton fab = binding.fabAddFamilyMember;
        fab.setOnClickListener(v -> showAddFamilyMemberDialog());
    }

    private void setupFamilyMembersSection() {
        RecyclerView familyMembersRecyclerView = binding.familyMembersRecyclerView;
        familyMembersRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext())
        );
        familyMemberAdapter = new FamilyMemberAdapter(
            new ArrayList<>(),
            this::showEditFamilyMemberDialog,
            this::showRemoveFamilyMemberDialog
        );
        familyMembersRecyclerView.setAdapter(familyMemberAdapter);
    }

    private void setupBookingHistorySection() {
        RecyclerView bookingHistoryRecyclerView = binding.bookingHistoryRecyclerView;
        bookingHistoryRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext())
        );
        bookingHistoryAdapter = new BookingHistoryAdapter(new ArrayList<>());
        bookingHistoryRecyclerView.setAdapter(bookingHistoryAdapter);
    }

    private void setupTravelPreferencesSection() {
        ChipGroup trainClassChipGroup = binding.trainClassChipGroup;
        trainClassChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            List<String> selectedClasses = new ArrayList<>();
            for (int i = 0; i < group.getChildCount(); i++) {
                Chip chip = (Chip) group.getChildAt(i);
                if (chip.isChecked()) {
                    selectedClasses.add(chip.getText().toString());
                }
            }
            viewModel.updatePreferredTrainClasses(selectedClasses);
        });

        MaterialButton savePreferencesButton = binding.savePreferencesButton;
        savePreferencesButton.setOnClickListener(v -> {
            UserManager.TravelPreferences preferences = new UserManager.TravelPreferences();
            preferences.vegetarian = binding.vegetarianSwitch.isChecked();
            preferences.lowerBerthPriority = binding.lowerBerthSwitch.isChecked();
            preferences.includeLuggageBuffer = binding.luggageBufferSwitch.isChecked();
            preferences.minTransferTime = Integer.parseInt(
                binding.minTransferTimeInput.getText().toString()
            );
            preferences.maxTransferTime = Integer.parseInt(
                binding.maxTransferTimeInput.getText().toString()
            );
            viewModel.updateTravelPreferences(preferences);
        });
    }

    private void observeViewModel() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                updateProfileUI(profile);
                familyMemberAdapter.updateFamilyMembers(profile.familyMembers);
                bookingHistoryAdapter.updateBookings(profile.recentBookings);
                updateTravelPreferencesUI(profile.travelPreferences);
            }
        });
    }

    private void updateProfileUI(UserManager.UserProfile profile) {
        binding.profileName.setText(profile.name);
        binding.profileEmail.setText(profile.email);
        binding.profilePhone.setText(profile.phone);
        // Load profile photo using Glide or similar library
    }

    private void updateTravelPreferencesUI(UserManager.TravelPreferences preferences) {
        binding.vegetarianSwitch.setChecked(preferences.vegetarian);
        binding.lowerBerthSwitch.setChecked(preferences.lowerBerthPriority);
        binding.luggageBufferSwitch.setChecked(preferences.includeLuggageBuffer);
        binding.minTransferTimeInput.setText(String.valueOf(preferences.minTransferTime));
        binding.maxTransferTimeInput.setText(String.valueOf(preferences.maxTransferTime));
        
        ChipGroup trainClassChipGroup = binding.trainClassChipGroup;
        for (String trainClass : preferences.preferredTrainClasses) {
            for (int i = 0; i < trainClassChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) trainClassChipGroup.getChildAt(i);
                if (chip.getText().toString().equals(trainClass)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText emailInput = dialogView.findViewById(R.id.emailInput);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.phoneInput);

        UserManager.UserProfile currentProfile = viewModel.getUserProfile().getValue();
        if (currentProfile != null) {
            nameInput.setText(currentProfile.name);
            emailInput.setText(currentProfile.email);
            phoneInput.setText(currentProfile.phone);
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_profile)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                UserManager.UserProfile updatedProfile = new UserManager.UserProfile();
                updatedProfile.name = nameInput.getText().toString();
                updatedProfile.email = emailInput.getText().toString();
                updatedProfile.phone = phoneInput.getText().toString();
                viewModel.updateProfile(updatedProfile);
            })
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show();
    }

    private void showAddFamilyMemberDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_family_member, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText relationInput = dialogView.findViewById(R.id.relationInput);
        TextInputEditText ageInput = dialogView.findViewById(R.id.ageInput);
        TextInputEditText idProofInput = dialogView.findViewById(R.id.idProofInput);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_family_member)
            .setView(dialogView)
            .setPositiveButton(R.string.add_member, (dialog, which) -> {
                UserManager.FamilyMember member = new UserManager.FamilyMember(
                    nameInput.getText().toString(),
                    relationInput.getText().toString(),
                    Integer.parseInt(ageInput.getText().toString()),
                    "Male", // Default gender
                    "Aadhaar", // Default ID proof type
                    idProofInput.getText().toString()
                );
                viewModel.addFamilyMember(member);
            })
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show();
    }

    private void showEditFamilyMemberDialog(UserManager.FamilyMember member) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_family_member, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText relationInput = dialogView.findViewById(R.id.relationInput);
        TextInputEditText ageInput = dialogView.findViewById(R.id.ageInput);
        TextInputEditText idProofInput = dialogView.findViewById(R.id.idProofInput);

        nameInput.setText(member.name);
        relationInput.setText(member.relation);
        ageInput.setText(String.valueOf(member.age));
        idProofInput.setText(member.idProofNumber);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_family_member)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                member.name = nameInput.getText().toString();
                member.relation = relationInput.getText().toString();
                member.age = Integer.parseInt(ageInput.getText().toString());
                member.idProofNumber = idProofInput.getText().toString();
                viewModel.updateFamilyMember(member);
            })
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show();
    }

    private void showRemoveFamilyMemberDialog(UserManager.FamilyMember member) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.remove_family_member)
            .setMessage(getString(R.string.remove_family_member_confirmation, member.name))
            .setPositiveButton(R.string.remove, (dialog, which) -> 
                viewModel.removeFamilyMember(member.id))
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 