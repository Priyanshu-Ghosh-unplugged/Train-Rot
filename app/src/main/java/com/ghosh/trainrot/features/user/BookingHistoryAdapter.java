package com.ghosh.trainrot.features.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.ghosh.trainrot.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {
    private List<UserManager.BookingRecord> bookings;
    private final SimpleDateFormat dateFormat;

    public BookingHistoryAdapter(List<UserManager.BookingRecord> bookings) {
        this.bookings = bookings;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserManager.BookingRecord booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateBookings(List<UserManager.BookingRecord> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView trainNumberText;
        private final TextView routeText;
        private final TextView dateText;
        private final TextView pnrText;
        private final Chip statusChip;
        private final TextView passengerCountText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            trainNumberText = itemView.findViewById(R.id.trainNumberText);
            routeText = itemView.findViewById(R.id.routeText);
            dateText = itemView.findViewById(R.id.dateText);
            pnrText = itemView.findViewById(R.id.pnrText);
            statusChip = itemView.findViewById(R.id.statusChip);
            passengerCountText = itemView.findViewById(R.id.passengerCountText);
        }

        void bind(UserManager.BookingRecord booking) {
            trainNumberText.setText(booking.trainNumber);
            routeText.setText(String.format("%s → %s", 
                booking.fromStation, booking.toStation));
            dateText.setText(dateFormat.format(new Date(booking.timestamp)));
            pnrText.setText(booking.pnr);
            statusChip.setText(booking.status);
            passengerCountText.setText(String.format("%d passengers", 
                booking.passengers.size()));

            // Set status chip color based on booking status
            int statusColor;
            switch (booking.status) {
                case "CONFIRMED":
                    statusColor = R.color.status_confirmed;
                    break;
                case "WAITLIST":
                    statusColor = R.color.status_waitlist;
                    break;
                case "CANCELLED":
                    statusColor = R.color.status_cancelled;
                    break;
                default:
                    statusColor = R.color.status_unknown;
            }
            statusChip.setChipBackgroundColorResource(statusColor);
        }
    }
} 