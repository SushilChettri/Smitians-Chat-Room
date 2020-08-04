package com.sushil.smitianschatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.mbms.MbmsErrors;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button LoginButton,PhoneLoginButton;
    private EditText UserEmail,UserPassword;
    private TextView NeedNewAccountLink,ForgetPasswordLink;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();


        InitializeFields();

        //Whats happen?if user click on NeedNewAccountLink.I mean it goes to next page.Here is the logic
        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //Call the function
                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        //We have to set onClickListener in PhoneLoginButton
        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Once the user click on phone login button
                Intent phoneLoginIntent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });


    }

    private void AllowUserToLogin() {
        String email=UserEmail.getText().toString(); // We will get the Email and Converting them into String
        String password=UserPassword.getText().toString(); // We will get the Password and Converting them into String

        //If user does not put any email then the message will be displayed on the screen
        if(TextUtils.isEmpty(email))
        {

            Toast.makeText(this,"Please enter your Smit Email..",Toast.LENGTH_SHORT).show();
        }

        //If user does not put password  then the message will be displayed on the screen
        if(TextUtils.isEmpty(password))
        {

            Toast.makeText(this,"Please enter your password..",Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true); //When loading bar appear on screen and if user click on the screen then this loading bar will disappeared from screen until the new account have been created
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    SendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this,"Logged in Successful..",Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }
                                else
                                {
                                    String message=task.getException().toString();
                                    Toast.makeText(LoginActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();

                                }
                        }
                    });
        }
    }

    //Now i initialize all fields in one function..so it's good way to practice to put all fields in one function.
    //Programs look good adn readable
    private void InitializeFields() {
        LoginButton=(Button) findViewById(R.id.login_button);
        PhoneLoginButton=(Button) findViewById(R.id.phone_login_button);
        UserEmail=(EditText) findViewById(R.id.login_email);
        UserPassword=(EditText) findViewById(R.id.login_password);
        NeedNewAccountLink=(TextView)findViewById(R.id.need_new_account_link);
        ForgetPasswordLink=(TextView)findViewById(R.id.forget_password_link);
        loadingBar=new ProgressDialog(this);

    }

   /* @Override
    protected void onStart() {
        super.onStart();
        if(currentUser != null){   //User is already logggd in
            SendUserToMainActivity();
        }
    } */

    private void SendUserToMainActivity() {
        /*Reason is that when ever user logged it into the main page or app then
        users won't go back..If the users want to go back they must have to be logout that's why i removed the first method */

        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        //Add some redirection so that users can't go when he/she pressed back button
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    //When click on Need New Account then page will intent to register page..(One page to another page)
    private void SendUserToRegisterActivity()
    {
        Intent registerIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }
}
