package com.oopsipushedtomain;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for displaying and editing an attendee's profile.
 * This class allows for interaction with Firebase Firestore to retrieve and update user data.
 */
public class ProfileActivity extends AppCompatActivity implements EditFieldDialogFragment.EditFieldDialogListener {

    // Declare the user
    private User user;

    // Declare a QRCode for scanning
    private QRCode qrCode;


    // Declare UI elements for labels and values
    private TextView nameValue, nicknameValue, birthdayValue, homepageValue, addressValue, phoneNumberValue, emailValue;
    private Button eventsButton, scanQRCodeButton, adminButton;
    private Switch toggleGeolocationSwitch;
    private String userId = "USER-0000000000"; // Get from bundle

    // Activity result launcher for getting the result of the QRCodeScan
    private ActivityResultLauncher<Intent> qrCodeActivityResultLauncher;


    /**
     * Initializes the activity and sets up the UI elements
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Load the information about the given user
        user = new User(userId, new User.DataLoadedListener() {
            @Override
            public void onDataLoaded() {
                // Initialize the UI elements and load attendee data
                initializeViews();
            }
        });

        // Initialize UI elements and load attendee data
        initializeViews();

        // Setup listeners for interactive elements
        setupListeners();

        // Qr code activity launcher
        // ChatGPT, How do I pass a variable back to the calling activity?, Can you give me the code for registerForActivityResult()
        // Register the activity result
        qrCodeActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == Activity.RESULT_OK) {
                    // Get the data
                    Intent data = o.getData();

                    // If the data is not null, load the QR code
                    if (data != null) {
                        String qrCodeString = data.getStringExtra("result");

                        // Check the user into the event
                        user.checkIn(qrCodeString);

                        // Show the scanned data
                        Toast.makeText(getApplicationContext(), "Checked into event: " + qrCodeString, Toast.LENGTH_LONG).show();
                        Log.d("QR Code", "Checked into event: " + qrCodeString);

                    } else {
                        Toast.makeText(getApplicationContext(), "Data Error", Toast.LENGTH_LONG).show();
                        Log.d("QR Code", "QR Code Not Scanned");
                    }
                }
            }
        });
    }

    /**
     * Updates the user elements on the UI
     */
    private void updateUIElements() {

        // Get the data from user
        String name = user.getName();
        String nickname = user.getNickname();
        String homepage = user.getHomepage();
        String address = user.getAddress();
        String phone = user.getPhone();
        String email = user.getEmail();
        Date birthday = user.getBirthday();

        // Update the fields
        if (name != null) {
            nameValue.setText(name);
        }

        if (nickname != null) {
            nicknameValue.setText(nickname);
        }

        if (birthday != null) {
            // Format the date
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            birthdayValue.setText(formatter.format(birthday));
        }

        if (homepage != null) {
            homepageValue.setText(homepage);
        }

        if (address != null) {
            addressValue.setText(address);
        }

        if (phone != null) {
            phoneNumberValue.setText(phone);
        }

        if (email != null) {
            emailValue.setText(email);
        }
    }

    /**
     * Handles the positive click action from the edit field dialog.
     * Updates the corresponding profile field with the new value entered by the user.
     *
     * @param dialog
     * @param fieldName
     * @param fieldValue
     */

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String fieldName, String fieldValue) {

        // Update the corresponding field in your UI based on fieldName
        Map<String, Object> update = new HashMap<>();
        switch (fieldName) {
            case "Name":
                nameValue.setText(fieldValue);
                user.setName(fieldValue);
                break;
            case "Nickname":
                nicknameValue.setText(fieldValue);
                user.setNickname(fieldValue);
                break;
            case "Birthday":
                birthdayValue.setText(fieldValue);

                // Format the given date, ChatGPT: How do i format a string into date
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date birthday = formatter.parse(fieldValue);
                    user.setBirthday(birthday);
                } catch (ParseException e) {
                    Log.d("ProfileActivity", "Date formatting failed");
                }
                break;
            case "Homepage":
                homepageValue.setText(fieldValue); // Corrected to update homepageTextView
                user.setHomepage(fieldValue);
                break;
            case "Address":
                addressValue.setText(fieldValue); // Corrected to update addressTextView
                user.setAddress(fieldValue);
                break;
            case "Phone Number":
                phoneNumberValue.setText(fieldValue); // Corrected to update phoneNumberTextView
                user.setPhone(fieldValue);
                break;
            case "Email":
                emailValue.setText(fieldValue); // Corrected to update emailTextView
                user.setEmail(fieldValue);
                break;
        }
    }

    /**
     * Handles the positive click action from the edit field dialog.
     *
     * @param dialog
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    /**
     * Shows the edit field dialog for a field on the page using its current value
     *
     * @param fieldName
     * @param fieldValue
     */
    public void showEditFieldDialog(String fieldName, String fieldValue) {
        DialogFragment dialog = new EditFieldDialogFragment();
        Bundle args = new Bundle();
        args.putString("fieldName", fieldName);
        args.putString("fieldValue", fieldValue);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "EditFieldDialogFragment");
    }

    /**
     * Initialize views to connect to layout file
     */
    private void initializeViews() {

        // Initialize values (TextViews for field values)
        nameValue = findViewById(R.id.nameTextView);
        nicknameValue = findViewById(R.id.nicknameTextView);
        birthdayValue = findViewById(R.id.birthdayValueTextView);
        homepageValue = findViewById(R.id.homepageValueTextView);
        addressValue = findViewById(R.id.addressValueTextView);
        phoneNumberValue = findViewById(R.id.phoneNumberValueTextView);
        emailValue = findViewById(R.id.emailValueTextView);

        // Initialize buttons
        eventsButton = findViewById(R.id.eventsButton);
        scanQRCodeButton = findViewById(R.id.scanQRCodeButton);
        adminButton = findViewById(R.id.adminButton);

        // Initialize switch
        toggleGeolocationSwitch = findViewById(R.id.toggleGeolocationSwitch);

        // Load user data into views
        updateUIElements();
    }

    /**
     * Set listeners for clickable fields on the page
     */
    private void setupListeners() {
        // Set click listeners for each editable field
        nameValue.setOnClickListener(v -> showEditFieldDialog("Name", nameValue.getText().toString()));
        nicknameValue.setOnClickListener(v -> showEditFieldDialog("Nickname", nicknameValue.getText().toString()));
        birthdayValue.setOnClickListener(v -> showEditFieldDialog("Birthday", birthdayValue.getText().toString()));
        homepageValue.setOnClickListener(v -> showEditFieldDialog("Homepage", homepageValue.getText().toString()));
        addressValue.setOnClickListener(v -> showEditFieldDialog("Address", addressValue.getText().toString()));
        phoneNumberValue.setOnClickListener(v -> showEditFieldDialog("Phone Number", phoneNumberValue.getText().toString()));
        emailValue.setOnClickListener(v -> showEditFieldDialog("Email", emailValue.getText().toString()));
        eventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, EventListActivity.class);
                startActivity(intent);
            }
        });
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AdminActivity.class);
                startActivity(intent);
            }
        });

        scanQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to the scanning activity and scan
                Intent intent = new Intent(getApplicationContext(), QRScanner.class);

                // Start the activity
                qrCodeActivityResultLauncher.launch(intent);

                // This is asynchronous. DO NOT PUT CODE HERE

            }

        });
    }
}
