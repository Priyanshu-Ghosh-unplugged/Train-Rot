package com.ghosh.trainrot.features.booking;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ghosh.trainrot.R;
import com.ghosh.trainrot.data.model.Booking;
import com.ghosh.trainrot.databinding.ItemBookingHistoryBinding;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class BookingHistoryAdapter extends ListAdapter<Booking, BookingHistoryAdapter.BookingViewHolder> {

    private OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public BookingHistoryAdapter() {
        super(new DiffUtil.ItemCallback<Booking>() {
            @Override
            public boolean areItemsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingHistoryBinding binding = ItemBookingHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookingHistoryBinding binding;

        BookingViewHolder(ItemBookingHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Booking booking) {
            binding.fromStationText.setText(booking.getFromStation());
            binding.toStationText.setText(booking.getToStation());
            binding.routeText.setText("Via: " + booking.getRoute());
            binding.dateText.setText(dateFormat.format(booking.getDate()));
            binding.trainNumberText.setText(booking.getTrainNumber());
            binding.trainNameText.setText(booking.getTrainName());
            binding.pnrText.setText("PNR: " + booking.getPnr());
            binding.passengerCountText.setText(booking.getPassengerCount() + " Passengers");
            binding.statusChip.setText(booking.getStatus().toString());

            // Set status chip color
            int statusColorResId;
            switch (booking.getStatus()) {
                case CONFIRMED:
                    statusColorResId = R.color.status_confirmed;
                    break;
                case WAITLIST:
                    statusColorResId = R.color.status_waitlist;
                    break;
                case CANCELLED:
                    statusColorResId = R.color.status_cancelled;
                    break;
                default:
                    statusColorResId = R.color.status_unknown;
            }
            binding.statusChip.setChipBackgroundColorResource(statusColorResId);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(booking);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Booking booking);
    }
} 