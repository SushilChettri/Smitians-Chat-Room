package com.sushil.smitianschatroom;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;

    private final List<Messages> messagesList = new ArrayList<>(); //we are going to retrieve or fetch the messages
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private ProgressDialog loadingBar;

    private String saveCurrentTime, saveCurrentDate;
    private String checker="",myUrl="";//Need string type variable so to check the status of the images or files
    private StorageTask uploadTask;
    private Uri fileUri;//Uri:user who pick from gallery


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();


        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString(); //Pass the same key name i.e visit_user_id as Chat Fragment
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();


        //We will initialize all over controls TextView, CircleImageView and all the things which will initialize later on
        IntializeControllers();


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call the SendMessage method whenever user click on SendMessage Button
                SendMessage();
            }
        });


        DisplayLastSeen();

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add dialogs box which contains three options for selection of images,pdf files,word files from his mobile phone storage
                CharSequence options[] = new CharSequence[]
                        {
                                //We will have three options
                                "Images",
                                "PDF Files",
                                "Ms Word Files"

                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() { //This three options are the items
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(i==0)//For images,users will going to select images
                        {
                            checker = "image";
                            //Create intent to send users to mobile phone gallery
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");//Since its images and we have just send the users to mobile phone gallery but not to any other place
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);//requestCode we can pass anyone

                            //Display the image to sender and receiver both

                        }
                        if(i==1)
                        {
                            checker = "pdf";
                        }
                        if(i==2)
                        {
                            checker = "docx";
                        }
                    }
                });

                builder.show();
            }
        });


    }


    private void IntializeControllers() {
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        //Now we need to access the custom chat bar
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        //first i gave reference to custom chat Bar after  we can initialized the userImage,userName,userLastSeen other if we initialize before reference custom layoutInflater then app will not work
        //which Belongs to custom chat Bar
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);

        MessageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        loadingBar = new ProgressDialog(this);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());



    }

    //TO GET THE IMAGE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode == RESULT_OK && data!=null && data.getData() != null)
        {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait,we are sending that file..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();//which user select from phone gallery and storing inside the Uri type
            if(!checker.equals("image")) //If the user is not selected the image or user select the pdf files or doc files
            {

            }
            else if(checker.equals("image"))//if user select the image
            {
                //now we will just save that image inside the firebase storage or in the firebase database then will display to the users
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                //NEEDING FOR STORING THE IMAGE IN DATABASE
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                //As we know we display the message to both the sender and receiver.So we have to create a reference for receiver also.
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                //There are million of messages between different users.So we have to use a unique random key for each message that no message will replace by previous one.

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push(); //This will basically create a key

                final String messagePushID = userMessageKeyRef.getKey();//messagePushID is basically  for every message i.e random key or unique key
                final StorageReference filePath=storageReference.child(messagePushID + "."+"jpg");//By Using the StorageReference its basically pointing to the "Image Files" child.So we can pass MessagePushID and type of image file

                //Put the file inside the storage
                uploadTask = filePath.putFile(fileUri);//pass file Uri which contain the image
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        //If the task is not successful then throw an exception
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                    //If this event is completed successfully
                }).addOnCompleteListener(new OnCompleteListener<Uri>(){
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();//myUri is  a string data type we need to convert it into downloadUrl to String type


                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message",myUrl);//pass myUrl variable which contains the link of that image
                            messageTextBody.put("name",fileUri.getLastPathSegment()); //we given the name from the  file Url we can get the last segment of that
                            messageTextBody.put("type", checker); //Type of LastSegment is checker,if its image it contains the value image
                            messageTextBody.put("from", messageSenderID);//From whom the users is going to receive this and  i.e messageSenderID so that display their profile picture on their chat activity to the receiver
                            messageTextBody.put("to", messageReceiverID);//To which users we are going to send it
                            messageTextBody.put("messageID", messagePushID);//Save the messageID which is basically the random key
                            messageTextBody.put("time", saveCurrentTime);//In which time user sent the message
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            //FOR SENDER
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            //FOR RECEIVER
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    //After that i want to clear the EditText
                                    MessageInputText.setText("");
                                }
                            });
                        }

                    }
                });
            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(ChatActivity.this,"Nothing Selected, Error",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //whenever the chat activity starts we want to display last seen  of the users
    private void DisplayLastSeen() {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            } else if (state.equals("offline")) {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        } else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {  //whenever the new child is added then it should display to it.I mean it should update on the screen and show the messages
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);//Messages is a class that i already created

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //We will write the code the save the message in firebase database
    private void SendMessage() {
        //Whenever the user click the button then we have to get the message from the EditText
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            //As we know we display the message to both the sender and receiver.So we have to create a reference for receiver also.
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            //There are million of messages between different users.So we have to use a unique random key for each message that no message will replace by previous one.

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push(); //This will basically create a key

            String messagePushID = userMessageKeyRef.getKey();//messagePushID is basically  for every message i.e random key or unique key

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);  //It need a key i.e message and value for that will be the messageText which we are getting from the users
            messageTextBody.put("type", "text"); //Either its a text message or user is going to send some file to other users..so from now we are working with just text messages.So the type will be text
            messageTextBody.put("from", messageSenderID);//From whom the users is going to receive this and  i.e messageSenderID so that display their profile picture on their chat activity to the receiver
            messageTextBody.put("to", messageReceiverID);//To which users we are going to send it
            messageTextBody.put("messageID", messagePushID);//Save the messageID which is basically the random key
            messageTextBody.put("time", saveCurrentTime);//In which time user sent the message
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            //FOR SENDER
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
           //FOR RECEIVER
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    //After that i want to clear the EditText
                    MessageInputText.setText("");
                }
            });
        }
    }

}

