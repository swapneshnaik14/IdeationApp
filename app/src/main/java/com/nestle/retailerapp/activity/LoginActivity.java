package com.nestle.retailerapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nestle.retailerapp.R;


public class LoginActivity extends AppCompatActivity {

    TextView registerNowAnchor;
    TextInputLayout emailInput, passwordInput;
    ProgressBar loginProgress;
    Button loginButton;
    FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerNowAnchor = findViewById(R.id.registerAnchor);
        emailInput = findViewById(R.id.emailInputLayoutLogin);
        passwordInput = findViewById(R.id.passwordInputLayoutLogin);

        loginButton = findViewById(R.id.buttonLogin);
        loginProgress = findViewById(R.id.loginButtonProgress);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(emailInput.getEditText().getText().toString())) {
                    Snackbar.make(v, "Email should not be empty", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(passwordInput.getEditText().getText().toString())) {
                    Snackbar.make(v, "Password should not be empty", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    loginWithEmailAndPassword(emailInput.getEditText().getText().toString(), passwordInput.getEditText().getText().toString(), v);
                }
            }
        });

        registerNowAnchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }

    public void loginWithEmailAndPassword(String email, String password, final View v) {
        loginProgress.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Snackbar.make(v, "Authentication failed.",
                                    BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}