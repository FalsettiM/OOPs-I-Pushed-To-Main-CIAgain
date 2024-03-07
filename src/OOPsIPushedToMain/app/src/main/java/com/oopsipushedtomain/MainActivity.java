package com.oopsipushedtomain;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    User user;

    Button button;
    Button userButton;
    View view;

    public void AfterDone() {
        Log.d("Firestore", user.getEmail());
        user.setProfileImage(Bitmap.createBitmap(view.getWidth(),view.getHeight(), Bitmap.Config.ARGB_8888 ));

        user.getProfileImage(new User.OnBitmapReceivedListener() {
            @Override
            public void onBitmapReceived(Bitmap bitmap) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.hello_button);
        userButton = findViewById(R.id.user_test);
        view = findViewById(R.id.hello_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });



        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a existing user
                user = new User("USER-9DRH1BAQZQMGZJEZFMGL", new User.DataLoadedListener() {
                    @Override
                    public void onDataLoaded() {
                        AfterDone();
                    }
                });


//                user.setAddress("Yo");
//

//                user.setAddress("Address");
//                user.setName("Name");
//                user.setBirthday(new Date());
//                user.setPhone("000000");
//                user.setNickname("Nick");
//                user.setHomepage("Ual");
//                user.setProfileImage(Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888));
//                user.setProfileImage(null);



;

                // New User
//                User user = new User();
            }
        });

    }
}