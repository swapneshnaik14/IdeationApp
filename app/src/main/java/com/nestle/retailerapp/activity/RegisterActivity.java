package com.nestle.retailerapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nestle.retailerapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.VISIBLE;

public class RegisterActivity extends AppCompatActivity {

    TextView loginAnchor;
    TextInputLayout emailInput, passwordInput, nameInput;
    Spinner cityInput;
    Button registerButton;
    ProgressBar registerProgress;

    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginAnchor = findViewById(R.id.loginAnchor);
        emailInput = findViewById(R.id.emailInputLayoutRegister);
        passwordInput = findViewById(R.id.passwordInputLayoutRegister);
        nameInput = findViewById(R.id.nameInputLayoutRegister);
        cityInput = findViewById(R.id.cityDropdownRegister);
        registerButton = findViewById(R.id.buttonRegister);
        registerProgress = findViewById(R.id.registerButtonProgress);

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        final ArrayList<String> items = new ArrayList<>();
        items.add("Select City");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        cityInput.setAdapter(adapter);

        dbRef.child("cities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    items.add(snapshot.getValue().toString());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error", databaseError+ "<--");
            }
        });


        loginAnchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(emailInput.getEditText().getText().toString())) {
                    Snackbar.make(v,"Email should not be empty", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(passwordInput.getEditText().getText().toString())) {
                    Snackbar.make(v,"Password should not be empty", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(nameInput.getEditText().getText().toString())) {
                    Snackbar.make(v,"Name should not be empty", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else if (cityInput.getSelectedItem().toString().equals("Select City")) {
                    Snackbar.make(v,"Please select a city", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    registerWithEmailAndPassword(emailInput.getEditText().getText().toString(), passwordInput.getEditText().getText().toString(), nameInput.getEditText().getText().toString(), cityInput.getSelectedItem().toString(), v);
                }
            }
        });

    }

    public void registerWithEmailAndPassword(final String email, String password, final String name, final String city, final View v) {
        registerProgress.setVisibility(VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("name", name);
                            map.put("email", email);
                            map.put("city", city);
                            dbRef.child("retailers").child(user.getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    registerProgress.setVisibility(View.GONE);
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                }
                            });
                        } else {
                            Snackbar.make(v,"Signup Error", BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}