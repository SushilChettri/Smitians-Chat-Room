package com.sushil.smitianschatroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

        private Button UpdateAccountSetting;
        private EditText userName,userStatus;
        private CircleImageView userProfileImage;

        private String currentUserID;
        private FirebaseAuth mAuth;
        private DatabaseReference RootRef;

        private static final int GalleryPick = 1;
        private StorageReference UserProfileImagesRef; //create another storage reference here,so that we can create a folder inside the firebase storage and in that folder we will store only the profile image of the users.
        private ProgressDialog loadingBar; //add Progress dialog for beauty of app
        private Toolbar settingToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images"); //profile Images is  a folder name


        //Create a method call
        InitializeFields();

        //After complete successful registration then user will allow to access to profile picture and status but not username
        //if y0u did not registered we can see see username which i wrote code below if statement.(VISIBLE)
        userName.setVisibility(View.INVISIBLE);

        //When User click on update button
        UpdateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Method Call
                UpdateSetting();
            }
        });

        RetrieveUserInfo();


        //When the users click on the image view which is there is setting layout we can send the user to mobile phone gallery
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Here we can write the code for sending the users to mobile phone gallery
                //That's should select only the image file from the mobile phone
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*"); //Now we have to define a type i.e  which type of file will select from users mobiles phone
                startActivityForResult(galleryIntent,GalleryPick);    //Basically send user to mobile phone gallery and we need to define integer data type at the top and pass it here

            }
        });
    }



    private void InitializeFields() {
        UpdateAccountSetting=(Button) findViewById(R.id.update_setting_button);
        userName=(EditText) findViewById(R.id.set_user_name);
        userStatus=(EditText) findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        settingToolBar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(settingToolBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }


    //Now whenever the users select any images then we will get that image and by using the crop image library we will allowed the user to crop that image
    //I mean,when the users click on that image, i should get that image and send the users to image crop activity..so that user can crop that image too
   @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Now i'm going to use the crop image library and allowed the user sto crop the image
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {//Data is null that means user must select the image first
            Uri ImageUri = data.getData();

            //Basically this will open the crop image activity where the users can crop the image
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)//We can set the aspect ratio as well like 1:1(crop  size will be 1:1)
                    .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            /*Now we will allow the users to crop the option.Once the users click on that ,that image will store inside the firebase storage or firebase database
            then we display the image back  on the setting profile image view.I mean, users can use the image app*/
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait,Your profile image is updating..");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                final Uri resultUri = result.getUri();//This resultUri basically contain the cropped image

                //Now its time to store the cropped image inside the firebase storage.For that needing storage reference
                final StorageReference filePath = UserProfileImagesRef.child(currentUserID + " .jpg");//We can store image by using current user id of users and we can store as a type jpg


                //UPDATE LOGIC
                //Its time to store this we can pass cropped image which is store inside the resultUri
                //addOnSuccessListenerListener,So that we can see either its is uploaded or not
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //Now i will get the link of the profile image and store the image reference inside the firebase database and display the image back to the users that is in the setting activity
                                final String downloadUrl = uri.toString(); //In this way we get the link of the profile image from the firebase storage and convert them into string

                                RootRef.child("Users").child(currentUserID).child("image")    //we want to give a key name
                                        .setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                //If the image is store inside the firebase database
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingActivity.this, "Image save in database ,Successfully..", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
            else{

                    Toast.makeText(SettingActivity.this, "Try Again ", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
            }

        }
    }





                   private void UpdateSetting() {
                       String setUserName = userName.getText().toString();
                       String setStatus = userStatus.getText().toString();

                       //If the username is Empty
                       if (TextUtils.isEmpty(setUserName)) {
                           Toast.makeText(this, "Please write your Username first..", Toast.LENGTH_LONG).show();
                       }
                       if (TextUtils.isEmpty(setStatus)) {
                           Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
                       } else {

                           HashMap<String, Object> profileMap = new HashMap<>();
                           //This is a hashmap which consist of key(uid) and value (currentUserid)
                           profileMap.put("uid", currentUserID);
            /* The key should be written as name because we used name  in mainActivity if we used different here
            then app will crash .Make sure to use same name */
                           profileMap.put("name", setUserName);
                           profileMap.put("status", setStatus);

                           //By Using Rootref we can save the current child, WHILE Users is a parent child
                           RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()) {
                                               SendUserToMainActivity();
                                               Toast.makeText(SettingActivity.this, "Profile updated Successfully..", Toast.LENGTH_SHORT).show();
                                           }
                                           //getException tell us which type of Error occur
                                           else {
                                               String message = task.getException().toString();
                                               Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                           }
                                       }
                                   });
                       }
                   }

                   private void RetrieveUserInfo() {

                       //Parent and child..Now we are retrieving old username or old status of the user
                       RootRef.child("Users").child(currentUserID)
                               .addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       //dataSnapshot dot exists: If the user has created an profile or not and if currentuserID is exists in database under the node user
                                       if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))) {
                                           String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                                           String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                                           String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();


                                           userName.setText(retrieveUserName);
                                           userStatus.setText(retrieveStatus);
                                           //By picasso library we will display the profile,Profile image is not done by firebase
                                           Picasso.get().load(retrieveProfileImage).into(userProfileImage);//we need to provide a link to our database which store inside the  retrieveProfileImage and into is on which stream you want to display ,that is on the CircleImageView
                                       }
                                       //Now if user set his username and status but not profile picture but here profile picture is optional
                                       else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                                           String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                                           String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                                           userName.setText(retrieveUserName);
                                           userStatus.setText(retrieveStatus);


                                       }
                                       //If none of this is exists or both the two condtions are false
                                       else {
                                           userName.setVisibility(View.VISIBLE);
                                           Toast.makeText(SettingActivity.this, "Please set and update your profile information..", Toast.LENGTH_LONG).show();
                                       }

                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });

                   }


                   //Once the profile is updated successfully the we send to the main activity
                   private void SendUserToMainActivity() {
                       Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
                       //This is a complete validation
                       mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(mainIntent);
                       finish();
                   }
               }