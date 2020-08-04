package com.sushil.smitianschatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private  PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        //Initialize the fields

        InputPhoneNumber=(EditText)findViewById(R.id.phone_number_input);
        InputVerificationCode=(EditText)findViewById(R.id.verification_code_input);
        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);
        VerifyButton=(Button)findViewById(R.id.verify_button);
        loadingBar = new ProgressDialog(this);

        //Set a clickListener for SendVerificationCodeButton
        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ONCE THE USER CLICK ON THE SEND VERIFICATION CODE BUTTON
                //Make sendVerificationCodeButton and InputPhone Number Invisible


                //InOrder to get the phone number,we have to do
                String phoneNumber = InputPhoneNumber.getText().toString();

                //Once we do that we have to verify whether the phone number is empty or not
                //If its empty
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Phone enter your phone number first..", Toast.LENGTH_SHORT).show();
                }
                //but if user has enter the phone number then we have send verification code to user
                else{
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, While we are authenticating your phone..");
                    loadingBar.setCanceledOnTouchOutside(false);//If the user click on the screen then this loading  bar will not disappeared from the screen until the app is successfully allowed you to login
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number which we get from the user to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout.SO when that time user will receive the verification code on his mobile phone or on the sim card on which he wants to verify
                            PhoneLoginActivity.this, // Activity (for callback binding)..its the context
                            callbacks);        // OnVerificationStateChangedCallbacks and last one is the callback that i created a field at the top
                }

            }
        });

        //SET ONCLICK LISTENER TO VERIFY  CODE BUTTON
        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //This is the process where user has to enter the verification code which server sent to his mobile phone
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                //First of all,we have to get the text or verification code
                String verificationCode = InputVerificationCode.getText().toString();
                //If users does not write verification code
                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code first", Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, While we are verifying verification code..");
                    loadingBar.setCanceledOnTouchOutside(false);//If the user click on the screen then this loading  bar will not disappeared from the screen until the app is successfully allowed you to login
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);//verification code: that is user will enter after receiving the verification code
                    //We have to pass the credential to it so then that will check that method will if this is correct or not
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //This method is called whenever the verification is completed or successfully
                //If the user enter phone number and then we sent verification code and if he verified that code
                //Then we will allow the user to login into the App

                signInWithPhoneAuthCredential(phoneAuthCredential);


            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {


                //Or if the user enter invalid phone number or user provide wrong verification code
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number,Please enter correct phone number..", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();


                //In that case user has to provide phone number again..
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                //Make VerifyButton and InputVerification Code Visible
                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);


            }


            @Override
            public void onCodeSent( String verificationId,  //This method is called when a code is sent to the user phone
                                    PhoneAuthProvider.ForceResendingToken token) {


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                //IF THE CODE IS SENT TO THE USER
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent..please check and verify", Toast.LENGTH_SHORT).show();

                //I cut this logic from onClickListener method and paste it here
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                //Make VerifyButton and InputVerification Code Visible
                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);


            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //If the task is successful, it mean that the user is provided the correct code
                            //Now he is ready to go to the main App and first create a progress dialog fo that at declaration section
                            //Now i dismiss the loading bar if sign in is successful
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations,you're logged in successfully..  ", Toast.LENGTH_SHORT).show();
                            //now i want to send user to main app i.e is MainActivity
                            //Create a method SendUserToMainActivity
                            SendUserToMainActivity();

                        } else {

                            //IN CASE OF ERROR
                            String message  = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();



                        }
                    }

                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        //Finish the previous things
        finish();

    }
}
