package com.sushil.smitianschatroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
//import android.widget.Toolbar;

public class GroupChatActivity extends AppCompatActivity {

  private Toolbar mToolbar;
  private ImageButton SendMessageButton;
  private EditText userMessageInput;
  private ScrollView mScrollView;
  private TextView displayTextMessage;

  private  String currentGroupName,currentUserID,currentUserName,currentDate,currentTime; //We have to get the current username..SO for the current username we need the unique user ID and we also need current date and time
    //For that , we need a Firebase Auth
    private FirebaseAuth mAuth;
    //we need to create a reference of database.so we have to  link to firebase
    private DatabaseReference UserRef,GroupNameRef,GroupMessageKeyRef;//Create another database reference i.e GroupNameRef
    //GroupMessageKeyRef is reference to that key


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //We have to written this logic at the top of the program because it can first retrieve the GroupName from the previous Group Fragment and then we can used currentGroupName which is written just below this logic
        currentGroupName=getIntent().getExtras().get("groupName").toString();//we have to used same name that i had used in GroupFragment i.e is "groupName"..we are getting from previous group fragment
        //now we have to pass currentGroupName as a Toast Message
        Toast.makeText(GroupChatActivity.this,currentGroupName,Toast.LENGTH_LONG).show();

        mAuth=FirebaseAuth.getInstance();
        //With the help of currentUserID we can retrieve the username
        currentUserID=mAuth.getCurrentUser().getUid();//now we will get the Id
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);//Reference to any group onWhich we can click and sub child should be a current Group Name
        //By using this line of code we are getting the group name from our previous GroupFragment and we are storing inside the current Group Name



        InitializeFields();

        //we can create another method
        GetUserInfo();

         //we can send the message.first of all,for sending the message that send button or image button(arrow) should be validate
            //that button should be clickable
            SendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Create a method for save all users sent message
                    SaveMessageInfoToDatabase();

                    //we need that once the user send the message then we want to Empty deck EditText After sending the message
                    userMessageInput.setText("");//set text to null

                    //It scroll down below at the button and it automatically show the new message
                    //and used same logic  in DisplayMessages as well
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

                }
            });
    }

    //onStart method execute Whenever and activity start
    @Override
    protected void onStart() {
        super.onStart();
        //Basically a reference to our "Groups" node which is parent node and currentGroupName in which users is going to send a message to see what people doing inside the Group
       GroupNameRef.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //If the group is exists
               if(dataSnapshot.exists()){
                   //If we want to add a new child or send a new message
                   DisplayMessages(dataSnapshot); // By using the DataSnapshot we can retrieve the database
               }
           }

           @Override
           public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               //If the group is exists
               if(dataSnapshot.exists()){
                   //If we want to add a new child or send a new message
                   DisplayMessages(dataSnapshot); // By using the DataSnapshot we can retrieve the database
               }
           }

           @Override
           public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

           }

           @Override
           public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }



    private void InitializeFields() {
        mToolbar=(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);//we have to displayed currentGroupName on top of the title bar


        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput=(EditText)findViewById(R.id.input_group_message);
        displayTextMessage=(TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
    }

    private void GetUserInfo() {
        //As we know we have to retrieve the current user ID(is the ID of the user) and who is online and want to send message to someone
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //First check user Id is exists or not
                if(dataSnapshot.exists()){
                    //if its  exists then we retrieve the current user name
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void SaveMessageInfoToDatabase() {
        //Inside the method we can get text to the input fields
        String message=userMessageInput.getText().toString();
        String messageKEY=GroupNameRef.push().getKey();//It Basically reference to that key,Create and a get that key
        if(TextUtils.isEmpty(message)){ //it means users does not input somethings
            Toast.makeText(this, "Please write Message First..", Toast.LENGTH_SHORT).show();
        }
        else //But if user write  the message
            {
                //lets first get the  current time..i Mean, when user sent message
                Calendar calForDate = Calendar.getInstance();
                //FORMAT FOR GETTING A DATE
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                //we can get the Date..
                currentDate=currentDateFormat.format((calForDate.getTime()));//In this way we get the date and store it inside the currentDate variable,which is a string datatype because we have convert it into the string in order to save inside the FirebaseDatabase

                //NOW INORDER TO GET THE TIME,WHAT WE HAVE TO DO IS ..
                Calendar calForTime= Calendar.getInstance();
                //FORMAT FOR GETTING A TIME
                SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm:ss a");//Inorder to get the time in 12 hours format with AM and PM..and  simply write a,it will get AM and PM..
                //we can get the time
                currentTime=currentTimeFormat.format((calForTime.getTime()));

                //we need to save  all this in firebase database along side with other message information
                //For that we needing HashMap
                HashMap<String,Object> groupMessageKey = new HashMap<>();
                GroupNameRef.updateChildren(groupMessageKey);

                GroupMessageKeyRef = GroupNameRef.child(messageKEY);//we are getting the reference to that messageKEY and storing that inside the GroupMessageKeyRef

                //By using the above reference we store the message zeta .We Can Create another HashMap message InfoMap
                HashMap<String,Object> messageInfoMap=new HashMap<>();
                // FOR GET NAME,DATE,TIME AND MESSAGE INPUT
                    messageInfoMap.put("name",currentUserName); //first is the  name variable store inside the current user name variable
                    messageInfoMap.put("message",message);
                    messageInfoMap.put("date",currentDate);
                    messageInfoMap.put("time",currentTime);

                    //Lets Update the children with messageInfoMap
                GroupMessageKeyRef.updateChildren(messageInfoMap);
            }
    }


    private void DisplayMessages(DataSnapshot dataSnapshot) {
            //We can retrieve and display all the messages for each specific groups
            //SO will be using Iterator
        Iterator iterator = dataSnapshot.getChildren().iterator(); //It should get the children by using Iterator method and it will get each message in each specific group[Line by Line]
        //In Order to get each message line by line and key values
        while(iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String)((DataSnapshot)iterator.next()).getValue();

            //NOW WE HAVE TO DISPLAY THE MESSAGES
            displayTextMessage.append(chatName +" :\n" +chatMessage +"\n" +chatTime + "    "+ chatDate + "\n\n\n");

            //We will add automatic scroll upto the button..So that users don't need to  scroll  manually again again for a new message
            //So, we want to retrieve the message.We should scroll it automatically for each message
            //it should scroll new message first
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);//It scroll down below at the button and it automatically show the new message

        }
    }

}

