package com.example.biometriclogin;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

import android.content.Intent;
import android.media.Session2Command;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class EditUserInfo extends Fragment {

    private NavController navController;
    private ImageButton userImageButton;
    private Uri userImageUri = null;

    private ProgressBar progressBar;

    private TextInputLayout nameLayout, phoneLayout;
    private TextInputEditText nameEditText, phoneEditText;

    private static final int IMAGE_REQUEST_CODE = 2;
    private static final String LOGTAG = "EditUserInfo";


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;


    public EditUserInfo() {
        // Required empty public constructor
    }

    public static EditUserInfo newInstance(String param1, String param2) {
        EditUserInfo fragment = new EditUserInfo();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userImageUri = null;

        navController = Navigation.findNavController(view);
        userImageButton = view.findViewById(R.id.edit_user_image);

        nameEditText = view.findViewById(R.id.edit_user_name_text);
        phoneEditText = view.findViewById(R.id.edit_user_phone_text);

        nameLayout = view.findViewById(R.id.edit_user_name_layout);
        phoneLayout = view.findViewById(R.id.edit_user_phone_layout);
        progressBar = view.findViewById(R.id.edit_progress_bar);

        progressBar.setVisibility(View.GONE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {


            documentReference = db.document("users/" + user.getEmail());
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        phoneEditText.setVisibility(View.VISIBLE);
                        phoneEditText.setText(documentSnapshot.getString("Phone"));
                    } else {
                        phoneEditText.setVisibility(GONE);
                    }
                }
            });

            if (user.getPhotoUrl() != null) {
                Glide.with(userImageButton).load(user.getPhotoUrl()).into(userImageButton);
            }
            if (user.getDisplayName() != null) {
                nameEditText.setText(user.getDisplayName());
            } else nameEditText.setText("");
        }

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                phoneLayout.setHelperTextEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        view.findViewById(R.id.edit_user_main_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                documentReference = db.document("users/" + user.getEmail());
                String phone_number_string = phoneEditText.getText().toString();

                Map<String, Object> user_phone = new HashMap<>();
                user_phone.put("Phone", phone_number_string);


                if (phone_number_string.isEmpty() | Utils.isValidPhone(phone_number_string)) {
                    progressBar.setVisibility(View.VISIBLE);

                    if (user != null) {

                        documentReference.set(user_phone).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        if (userImageUri != null) {
                                            //image is selected
                                            if (user != null) {
                                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(nameEditText.getText().toString()).setPhotoUri(userImageUri).build();
                                                user.updateProfile(userProfileChangeRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        navController.navigateUp();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(getContext(), "Some error occurred.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } else if (userImageUri == null) {
                                            //image is not selected
                                            if (user != null) {
                                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(nameEditText.getText().toString()).build();
                                                user.updateProfile(userProfileChangeRequest)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                navController.navigateUp();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressBar.setVisibility(View.GONE);
                                                                Toast.makeText(getContext(), "Some error occurred.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        } else {
                                            navController.navigateUp();
                                        }
                                    }

                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getContext(), "Some error occurred.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    progressBar.setVisibility(GONE);
                    phoneLayout.setHelperTextEnabled(true);
                    phoneLayout.setHelperText("Invalid Phone Number");
                }


                Utils.hideKeyboard(getActivity());

            }
        });

        userImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri initialuri = new Uri.Builder().appendPath("/").build();
                openFile(initialuri);
            }
        });

    }


    private void openFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case IMAGE_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    userImageUri = data.getData();
                    userImageButton.setImageURI(userImageUri);
                    Log.d(LOGTAG, data.getDataString());
                }
            }
        }

    }


}