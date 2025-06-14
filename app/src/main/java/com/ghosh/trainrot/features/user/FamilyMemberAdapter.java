package com.ghosh.trainrot.features.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.ghosh.trainrot.R;
import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.ViewHolder> {
    private List<UserManager.FamilyMember> familyMembers;
    private final OnFamilyMemberClickListener editListener;
    private final OnFamilyMemberClickListener removeListener;

    public interface OnFamilyMemberClickListener {
        void onClick(UserManager.FamilyMember member);
    }

    public FamilyMemberAdapter(List<UserManager.FamilyMember> familyMembers,
                             OnFamilyMemberClickListener editListener,
                             OnFamilyMemberClickListener removeListener) {
        this.familyMembers = familyMembers;
        this.editListener = editListener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_family_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserManager.FamilyMember member = familyMembers.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return familyMembers.size();
    }

    public void updateFamilyMembers(List<UserManager.FamilyMember> newMembers) {
        this.familyMembers = newMembers;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView relationText;
        private final TextView ageText;
        private final TextView idProofText;
        private final MaterialButton editButton;
        private final MaterialButton removeButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            relationText = itemView.findViewById(R.id.relationText);
            ageText = itemView.findViewById(R.id.ageText);
            idProofText = itemView.findViewById(R.id.idProofText);
            editButton = itemView.findViewById(R.id.editButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }

        void bind(UserManager.FamilyMember member) {
            nameText.setText(member.name);
            relationText.setText(member.relation);
            ageText.setText(String.valueOf(member.age));
            idProofText.setText(String.format("%s: %s", 
                member.idProofType, member.idProofNumber));

            editButton.setOnClickListener(v -> editListener.onClick(member));
            removeButton.setOnClickListener(v -> removeListener.onClick(member));
        }
    }
} 