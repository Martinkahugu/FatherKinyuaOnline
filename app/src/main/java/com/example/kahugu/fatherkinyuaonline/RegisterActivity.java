package com.example.kahugu.fatherkinyuaonline;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity
{

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        UserConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        CreateAccountButton = (Button) findViewById(R.id.register_create_account);
        loadingBar = new ProgressDialog(this);


        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"please write your email....", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"please write your password....", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(confirmPassword))
        {
            Toast.makeText(this,"please confirm your password....", Toast.LENGTH_SHORT).show();
        }

        else if (!password.equals( confirmPassword ))
        {
            Toast.makeText(this,"your password does not match the confirm password....", Toast.LENGTH_SHORT).show();
        }

        else
            {
                loadingBar.setTitle("Creating New Account");
                loadingBar.setMessage("Please wait as we create your Account");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                mAuth.createUserWithEmailAndPassword(email, password)
                 .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task)
                     {
                        if (task.isSuccessful())
                        {
                            sendUserToSetupActivity();
                            
                            Toast.makeText(RegisterActivity.this, "You are authenticated successfully...",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }

                        else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error ocurred: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                     }
                 });
            }
    }

    private void sendUserToSetupActivity()
    {
        Intent setUpIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setUpIntent);
        finish();
    }


}