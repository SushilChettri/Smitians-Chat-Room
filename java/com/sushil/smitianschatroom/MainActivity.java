package com.sushil.smitianschatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;


    private FirebaseAuth mAuth;

    private DatabaseReference RootRef;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create Firebase then only current user..All user data are stored in firebase
        mAuth=FirebaseAuth.getInstance();//By using mAuth we can get the ID


        //From here,OnCreate method
        RootRef= FirebaseDatabase.getInstance().getReference();


        //Search for id and Select the exact if from layout section
        mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Smitians:Chat Room");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Main Logic or to  access all fragmentation from TabsAccessorAdaptor
        myViewPager=(ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    // onStart method is called whenever we run the app or whenever we mainActivity Open
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser =mAuth.getCurrentUser();
        //user is not logged in
        if(currentUser==null){
            SendUserToLoginActivity();
        }
        //What if user is already logged in
        else
        {
            updateUserStatus("online");
            //Create a method
            VerifyUserExistance();
        }
    }

    //I mean to minimize the app
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser =mAuth.getCurrentUser();
        if(currentUser != null)//if the users is logged in already
        {
            updateUserStatus("offline");
        }
    }

    //Any reason if app crash then we have to update the status of a user in that case also


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser =mAuth.getCurrentUser();
        if(currentUser != null)//if the users is logged in already
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserID=mAuth.getCurrentUser().getUid();
        //We are checking for user if either user is registered or not(authenticated or not)
        //Parent node child(Users) and sub child(currentUserID)
        //If you did not used parent child then will not go from setting or update profile to mainActivity
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /* user which  has registered just we can check  the name if the
                users is old user then we can send a message update picture
                 */
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_LONG).show();
                }
                //He need to set his profile picture or username
                else{

                    SendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //FOR CREATING AN MENU OPTION
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item); //By using the item we can access the position of option

        if(item.getItemId() == R.id.main_logout_option)
        {
            //When ever user basically logout button from the app it update to the offline state
            updateUserStatus("offline");
            //We already created firebase auth
            mAuth.signOut();
            SendUserToLoginActivity();

        }
        if(item.getItemId() == R.id.main_settings_option)
        {
            SendUserToSettingActivity();

        }
        if(item.getItemId() == R.id.main_create_group_option)
        {
            RequestNewGroup();

        }
        if(item.getItemId() == R.id.main_find_friends_option)
        {
            //CALL THAT METHOD
            SendUserToFindFriendsActivity();

        }
        return  true;
    }

    private void RequestNewGroup() {
        //we will ask the user to enter the group name.Once the user enter will stored
        //inside the firebase database then will retrieve it in our group fragment
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");//This will be the title for dialog box

        //We need edit text field to get the group name from user
        final EditText groupNameField=new EditText(MainActivity.this);
        //We need to give hint to the users
        groupNameField.setHint("e.g SMIT BCA Students");
        builder.setView(groupNameField);

        //Now we have to add two button create and cancel button
        //FOR CREATE
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //If the user click on create button the what will happen that text will converted in string
                String groupName=groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this,"Please Write Group Name",Toast.LENGTH_LONG).show();
                }
                //Inside else, we create method
                else{

                    CreateNewGroup(groupName);
                }

            }
        });


        //Now Second button is  a cancel button.Once we we click cancel then it must be cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();

            }
        });
        builder.show();



    }

    //how will write code to store group name into firebase database
    //write argument String groupName and pass it as argument inside else block
    //We sending groupname as a parameter to the function CreateNewGroup
    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue("")//groupName is a key value and value is null
                .addOnCompleteListener(new OnCompleteListener<Void>() { //if the task is successfull then we have to tell the user that group is created successfully.
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this,groupName+" group is created Succcessfully ",Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        //This is a complete validation
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingActivity() {
        Intent settingIntent=new Intent(MainActivity.this,SettingActivity.class);
        startActivity(settingIntent);
    }

    private void SendUserToFindFriendsActivity()
    {
        Intent findFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

     private void updateUserStatus(String state)
     {
         String saveCurrentTime,saveCurrentDate;

         Calendar calendar = Calendar.getInstance();
         SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
         saveCurrentDate = currentDate.format(calendar.getTime());//it will get the above format and store inside the saveCurrentDate string type variable

         SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
         saveCurrentTime = currentTime.format(calendar.getTime());

         //save the data into database
         HashMap<String,Object> onlineStateMap = new HashMap<>();
         onlineStateMap.put("time",saveCurrentTime);
         onlineStateMap.put("date",saveCurrentDate);
         onlineStateMap.put("state",state);

         //Get current user so that we can save all the OnlineState information inside the user node for each specific users under the unique user ID
        currentUserID = mAuth.getCurrentUser().getUid();//By using mAuth we can get the ID

         //By using the rootRef we are going to store the data inside the users node
        RootRef.child("Users").child(currentUserID)//Users:parent node for all users and currentUserID:to search for a specific users,I mean to store the data
               .child("userState")//userState:mean online or offline state
                .updateChildren(onlineStateMap);
     }
}


