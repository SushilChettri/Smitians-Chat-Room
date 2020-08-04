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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink;


    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //These field i did not initialize to xml file or anywhere(Firebase declared separaetly).Thats why i did not put that var in function.

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        //Best Way
        InitializeFields();

    //Function to go from one page to another,When user click only on Already Have Account Link.
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        //When click on Create Account Button then that OnClickListener function will work on that button.
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User Defined method for CreateAccount
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email=UserEmail.getText().toString(); // We will get the Email and Converting them into String
        String password=UserPassword.getText().toString(); // We will get the Password and Converting them into String

        //If user does not put any email then the message will be displayed on the screen
        if(TextUtils.isEmpty(email))
        {

            Toast.makeText(this,"Please enter your Smit Email..",Toast.LENGTH_SHORT).show();
        }

        //If user does not put any email then the message will be displayed on the screen
        if(TextUtils.isEmpty(password))
        {

            Toast.makeText(this,"Please enter your password..",Toast.LENGTH_SHORT).show();
        }
        //If all the condition are satisfied..I mean,if user puts both email and password.With the help of firebaseAUth we will do
        else{

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait,while we are creating Smit new account for you.");
            loadingBar.setCanceledOnTouchOutside(true); //When loading bar appear on screen and if user click on the screen then this loading bar will disappeared from screen until the new account have been created
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //if The Account is created successfully then we displayed a message
                            if(task.isSuccessful()){
                               // SendUserToLoginActivity();//If the account is created successfully the will send user to login activity.
                                //Once user registered after that instead of send user to login activity we can directly send to main page

                                //For knowing that logic, open firebase amd create account then we wil got to know
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");//the value is null

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"Account Created SuccessFully.. ",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String message=task.getException().toString();//If error occur then it identified what kind have been error occur
                                //After identified the error then it displayed  a  message(What kind of error is this
                                Toast.makeText(RegisterActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();//if any error occur then after solve or click on screen its dismiss the loading bar
                            }
                        }
                    });

        }

    }



    private void InitializeFields() {
        CreateAccountButton=(Button) findViewById(R.id.register_button);
        UserEmail=(EditText) findViewById(R.id.register_email);
        UserPassword=(EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink=(TextView)findViewById(R.id.already_have_account_link);

        loadingBar=new ProgressDialog(this);
    }

    //User defined function that goes from one page to another.
    private void SendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        //Add some redirection so that users can't go when he/she pressed back button
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
