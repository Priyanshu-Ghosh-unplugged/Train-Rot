package com.ghosh.trainrot.features.journey;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ghosh.trainrot.data.model.Journey;
import com.ghosh.trainrot.databinding.ItemJourneyHistoryBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class JourneyHistoryAdapter extends ListAdapter<Journey, JourneyHistoryAdapter.JourneyViewHolder> {

    private OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public JourneyHistoryAdapter() {
        super(new DiffUtil.ItemCallback<Journey>() {
            @Override
            public boolean areItemsTheSame(@NonNull Journey oldItem, @NonNull Journey newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Journey oldItem, @NonNull Journey newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public JourneyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemJourneyHistoryBinding binding = ItemJourneyHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new JourneyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class JourneyViewHolder extends RecyclerView.ViewHolder {
        private final ItemJourneyHistoryBinding binding;

        JourneyViewHolder(ItemJourneyHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Journey journey) {
            binding.fromStationText.setText(journey.getFromStation());
            binding.toStationText.setText(journey.getToStation());
            binding.dateText.setText(dateFormat.format(journey.getDate()));
            binding.trainNumberText.setText(journey.getTrainNumber());
            binding.trainNameText.setText(journey.getTrainName());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(journey);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Journey journey);
    }
} 