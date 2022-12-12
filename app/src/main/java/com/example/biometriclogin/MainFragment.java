package com.example.biometriclogin;

import static android.view.View.GONE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainFragment extends Fragment {
    private NavController navController;
    private FirebaseUser user;
    private TextView userName, phone, email;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;

    private Switch biometricEnabledSwitch;

    private ImageView userImage;

    SharedPreferences sharedPreferences;

    private static final String LOGTAG = "MainFragment";
    private static final String BIOMETRIC_SHARED_PREFERENCES = "BIOMETRIC_SHARED_PREFERENCES";
    private static final String BIOMETRIC_SHARED_PREFERENCES_ENABLED_FLAG = "BIOMETRIC_SHARED_PREFERENCES_ENABLED_FLAG";

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(getActivity(), androidx.navigation.fragment.R.id.nav_host_fragment_container);
        biometricEnabledSwitch = view.findViewById(R.id.main_biometric_switch);
        userImage = view.findViewById(R.id.main_person_image);
        userName = view.findViewById(R.id.main_name_text);
        phone = view.findViewById(R.id.main_phone_text);
        email = view.findViewById(R.id.main_email_text);

        sharedPreferences = getActivity().getSharedPreferences(BIOMETRIC_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        biometricEnabledSwitch.setChecked(sharedPreferences.getBoolean(BIOMETRIC_SHARED_PREFERENCES_ENABLED_FLAG, false)); //Set initial state for switch
        phone.setVisibility(GONE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            documentReference = db.document("users/" + user.getEmail());
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        phone.setVisibility(View.VISIBLE);
                        String phoneNumberString = documentSnapshot.getString("Phone");
                        if (!phoneNumberString.isEmpty()) {
                            phone.setText(phoneNumberString);
                        } else {
                            phone.setVisibility(GONE);
                        }
                    } else {
                        phone.setVisibility(GONE);
                    }
                }
            });


            if (user.getPhotoUrl() != null) {
                userImage.setVisibility(View.VISIBLE);
                Glide.with(userImage).load(user.getPhotoUrl()).into(userImage);
            } else userImage.setVisibility(GONE);


            if (user.getDisplayName() != null) {
                if (!user.getDisplayName().isEmpty()) {
                    userName.setText(user.getDisplayName());
                    userName.setVisibility(View.VISIBLE);
                }else userName.setVisibility(GONE);
            } else userName.setVisibility(GONE);
            if (user.getEmail() != null) {
                if (!user.getEmail().isEmpty()) {
                    email.setText(user.getEmail());
                    email.setVisibility(View.VISIBLE);
                } else email.setVisibility(GONE);
            } else email.setVisibility(GONE);

        }


        view.findViewById(R.id.main_logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(BIOMETRIC_SHARED_PREFERENCES_ENABLED_FLAG, false);
                editor.apply();
                navController.navigate(MainFragmentDirections.actionMainFragmentToLoginNestedGraph());
            }
        });

        view.findViewById(R.id.main_exit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finishAffinity();
            }
        });
        view.findViewById(R.id.main_edit_user_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(MainFragmentDirections.actionMainFragmentToEditUserInfo());
            }
        });
        biometricEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Log out regardless of button state
                //does not modify button state
                FirebaseAuth.getInstance().signOut();
                sharedPreferences.edit().putBoolean(BIOMETRIC_SHARED_PREFERENCES_ENABLED_FLAG, false).apply();
                navController.navigate(MainFragmentDirections.actionMainFragmentToLoginNestedGraph());
                Toast.makeText(getContext(), "Please login to verify your identity.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}