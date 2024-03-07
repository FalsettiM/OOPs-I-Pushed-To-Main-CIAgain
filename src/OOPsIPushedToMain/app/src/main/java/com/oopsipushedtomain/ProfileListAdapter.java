package com.oopsipushedtomain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProfileListAdapter extends RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder> {
    private List<Profile> profileList; // List to hold profile data
    private Context context;

    // Constructor
    public ProfileListAdapter(Context context, List<Profile> profileList) {
        this.context = context;
        this.profileList = profileList;
    }

    // ViewHolder class for layout binding
    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView; // Assuming you have at least a name to display

        public ProfileViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.profileNameTextView);
            // Initialize other views from the item layout here
        }
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        holder.nameTextView.setText(profile.getName());
        // Set other details to views as needed
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }
}

