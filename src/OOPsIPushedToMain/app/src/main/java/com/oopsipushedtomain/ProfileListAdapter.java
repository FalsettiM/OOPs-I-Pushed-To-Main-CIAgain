package com.oopsipushedtomain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * ProfileListAdapter is an adapter class for populating a RecyclerView with Profile objects.
 * It binds the data to the RecyclerView and manages the ViewHolder.
 */
public class ProfileListAdapter extends RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder> {
    private List<Profile> profileList; // List to hold profile data
    private Context context;

    /**
     * Constructor for ProfileListAdapter.
     * @param context The context of the activity or fragment.
     * @param profileList The list of Profile objects to be displayed.
     */
    public ProfileListAdapter(Context context, List<Profile> profileList) {
        this.context = context;
        this.profileList = profileList;
    }

    /**
     * ViewHolder class for layout binding.
     */
    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView; // TextView to display the name

        public ProfileViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.profileNameTextView); // Initialize name TextView
            // Initialize other views from the item layout here
        }
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the ViewHolder
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        holder.nameTextView.setText(profile.getName()); // Set name to TextView
        // Set other details to views as needed
    }

    @Override
    public int getItemCount() {
        return profileList.size(); // Return the size of the profileList
    }
}
