package com.ghosh.trainrot.features.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ghosh.trainrot.R;
import com.ghosh.trainrot.databinding.FragmentHomeBinding;
import com.ghosh.trainrot.features.booking.BookingHistoryAdapter;
import com.ghosh.trainrot.features.journey.JourneyHistoryAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private JourneyHistoryAdapter journeyAdapter;
    private BookingHistoryAdapter bookingAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerViews();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        // Setup Recent Journeys RecyclerView
        journeyAdapter = new JourneyHistoryAdapter();
        binding.recentJourneysRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recentJourneysRecyclerView.setAdapter(journeyAdapter);

        // Setup Upcoming Bookings RecyclerView
        bookingAdapter = new BookingHistoryAdapter();
        binding.upcomingBookingsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.upcomingBookingsRecyclerView.setAdapter(bookingAdapter);
    }

    private void setupClickListeners() {
        // Quick Actions
        binding.planJourneyButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_journey));

        binding.bookTatkalButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_tatkal));

        binding.trackTrainButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_tracking));

        // Journey History Item Click
        journeyAdapter.setOnItemClickListener(journey -> {
            Bundle args = new Bundle();
            args.putString("journeyId", journey.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.navigation_journey, args);
        });

        // Booking History Item Click
        bookingAdapter.setOnItemClickListener(booking -> {
            Bundle args = new Bundle();
            args.putString("bookingId", booking.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.navigation_bookings, args);
        });
    }

    private void observeViewModel() {
        viewModel.getRecentJourneys().observe(getViewLifecycleOwner(), journeys -> {
            journeyAdapter.submitList(journeys);
            binding.recentJourneysEmptyView.setVisibility(
                    journeys.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getUpcomingBookings().observe(getViewLifecycleOwner(), bookings -> {
            bookingAdapter.submitList(bookings);
            binding.upcomingBookingsEmptyView.setVisibility(
                    bookings.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 