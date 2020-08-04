package com.sushil.smitianschatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private String receiverUserID, Current_State,senderUserID;//senderUserID is the one who is online

    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;

    private DatabaseReference UserRef,ChatRequestRef,ContactsRef;
    private FirebaseAuth mAuth;//field To get the currentUserID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        //Create link or reference to user node
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests"); //New parent node
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();//we have to receive the user ID from FindFriendActivity activity & storing the ID here and inside the get we should give key value
        senderUserID = mAuth.getCurrentUser().getUid(); //By using the mAuth we are going to retrieve the currentUserID


        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton=(Button) findViewById(R.id.decline_message_request_button);
        Current_State = "new"; //That is true user or new to each others
    //We can Retrieve Information and display it on the  profile activity
        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
        //By using the UserRef we are going to retrieve the Data of the users
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //If the users want to set profile picture or not,so we have add validation for that
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    //In Order to display image we have to using the PICASSO library
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);//field name of the profile image is userProfileImage and if the user does not set any profile picture we have to used placeholder for that

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();

                }
                else{
                    //Profile picture is optional if the user has not set any profile picture then we will not retrieve the image and we only retrieve the username,status.
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();


                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequests();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void ManageChatRequests() {

        //Once the we sent request then it show cancel chat request but again when we go back and open the same user profile then it display same original Sent message..So we have to fix that
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //We have to retrieve the request first
                        if(dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if(request_type.equals("sent"))//If the request is sent
                            {
                                Current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received")) {
                                    Current_State="request_received";
                                    SendMessageRequestButton.setText("Accept Chat Request");//This button is for receiver.I mean, Receiver will see the button and will show to the users as Accept Chat Request
                                    //Now user can have a option, I mean receiver of that request..Person who will receive the chat request..He has a first option as 'Accept Chat Request'
                                    //AND OTHER ONE IS DECLINE MESSAGE REQUEST
                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);//This button is only visible to the receiver of chat request
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        CancelChatRequest();

                                    }
                                });

                            }
                            }
                        else{
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserID))
                                            {
                                                Current_State = "friends";
                                                SendMessageRequestButton.setText("Remove this Contacts");

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //We will not display the send message button who is using his own account
        //I mean user can't send message to his own profile,which mean sender and receiver are not equal
        if(!senderUserID.equals(receiverUserID)){
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Once the user sent the message then  make the button disable
                        SendMessageRequestButton.setEnabled(false);

                        //If the current state is new to each other then we can start chat with each others
                    if(Current_State.equals("new"))
                    {
                        //CREATE A METHOD
                        SendChatRequest();
                    }

                    //Now If the users want to cancel  the request then  We will allow the sender to cancel the request
                    if(Current_State.equals("request_sent"))
                    {
                        //If the request is already sent then user has right now to cancel the Chat Request
                        //CALL THE  METHOD  for //Once the receiver cancel the chat request which user received from another user, I mean from the sender
                        CancelChatRequest();
                    }

                    //If the receiver received the Chat Request from any users
                    if(Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                   if(Current_State.equals("friends")) //if the both the  users are added to contact list
                   {
                       RemoveSpecificContact();
                   }

                }
            });
        }
        else{
            //who want to chat with his own account which is not possible at all
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }


    }

    private void RemoveSpecificContact() {
        //This is for the receiver and we have to remove for the sender aswell
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Record is updated for both users in firebase database.I mean i have to remove request for receiver also
                        if(task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }

                    }
                });



    }

    private void AcceptChatRequest()
    {
        //Now we display the Contact List How much of person user have we wil show contact list for that we will create new node and inside there will save all contacts of a specific users
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved") //Contacts is a another parent node or list and value is 'Saved' once the user click on Accept Chat Request Button then that contact will be added or saved in contact list and which will display on the contact fragment later on
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        //for sender we are saving the contact list because we have to display the contact on both account,I mean in both sender and receiver
                        ContactsRef.child(receiverUserID).child(senderUserID)
                                .child("Contacts").setValue("Saved") //Contacts is a another parent node or list and value is 'Saved' once the user click on Accept Chat Request Button then that contact will be added or saved in contact list and which will display on the contact fragment later on
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                      SendMessageRequestButton.setEnabled(true);
                                      Current_State = "friends";  //Both the person are friends to each other..I mean both can talk or contact to each other
                                      SendMessageRequestButton.setText("Remove this Contact");  // Now both the user have the choice. I mean both users has choice to remove the contacts(if they are not happy with each other)

                                        DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                        DeclineMessageRequestButton.setEnabled(false);
                                    }
                                });
                         }
                    }

                });
    }

    private void CancelChatRequest() {
        //This is for the sender and we have to remove for the receiver aswell
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Record is updated for both users in firebase database.I mean i have to remove request for receiver also
                        if(task.isSuccessful()){
                                   ChatRequestRef.child(receiverUserID).child(senderUserID)
                                   .removeValue()
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {

                                           if(task.isSuccessful())
                                           {
                                               SendMessageRequestButton.setEnabled(true);
                                               Current_State = "new"; //Request will be cancel So both are new to each other
                                                SendMessageRequestButton.setText("Send Message");

                                               //Once the receiver cancel the chat request which user received from another user, I mean from the sender
                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                           }
                                               }
                                   });
                        }

                    }
                });

    }

    //First we store the sender userID and then the receiver because we have to tell the user who is going to send the Chat Request
    private void SendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent") //For the sender That's request is sent
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                         if(task.isSuccessful())
                        {

                            ChatRequestRef.child(receiverUserID).child(senderUserID) //For receiver that request is received
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                              SendMessageRequestButton.setEnabled(false);
                                                Current_State = "request_sent";
                                                SendMessageRequestButton.setText("Cancel Chat Request");//user has a choice to cancel the chat request
                                            }
                                        }
                                    });
                        }

                    }
                });
    }
}
