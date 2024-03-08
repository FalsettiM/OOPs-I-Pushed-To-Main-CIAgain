package com.oopsipushedtomain;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * A fragment for showing a given Bitmap image
 */
public class ShowImageFragment extends Fragment {

    private Bitmap image;

    /**
     * Empty constructor
     */
    public ShowImageFragment() {
        // Required empty public constructor
    }


    /**
     * Creates a new instance of this fragment
     *
     * @param image The image that you want to show
     * @return The instance of the fragment
     */
    public static ShowImageFragment newInstance(Bitmap image) {
        ShowImageFragment fragment = new ShowImageFragment();

        // Add to bundle
        Bundle args = new Bundle();
        args.putParcelable("image", image);

        // Set arguments and return
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Runs when the fragment is created
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Loads the image to the fragment and sets the on click listeners
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return A reference to the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_image, container, false);

        // Get the image view
        ImageView imageView = view.findViewById(R.id.image_view);

        // Put the bitmap in the image view
        if (getArguments() != null) {
            image = getArguments().getParcelable("image");
            imageView.setImageBitmap(image);
        }

        // Set the click listener for the close button
        Button closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Use the back button", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}