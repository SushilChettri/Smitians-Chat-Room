package com.sushil.smitianschatroom;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.nio.file.attribute.UserPrincipalLookupService;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef,UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID="";//String type variable where we can simply store the currentUserID

    private String retImage = "default_image";

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView =  inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);//we have to pass currentUserID we are logged into app and he is going to  send message to his friends
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        //Now by using the firebase RecyclerAdapter we can retrieve the all over Chats.The Chat list from our Contacts nodes

        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //We add Firebase Recycler options, and we will give query to database
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class)
                 .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter =//we have to pass model and static class
                    new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {//pass the options which contain the query
                        @Override
                        protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                                //Now we wil get each ID line by line and first we get the first ID and then from the user node we have to retrieve the profile image ,userName and userStatus and same we will do for second,third and so on.
                                //In that way we get Chat list  by using the  user Node
                                final String usersIDs = getRef(position).getKey();//It will get the first position and so on(line by line) and get the key of each position
                                final String[] retImage = {"default_image"};//We need to convert this into array

                                //Now Create a reference to user Node and retrieve the profile image and name for each users
                                UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.exists()) {
                                            //First we check for image i.e profile image and its optional
                                            if (dataSnapshot.hasChild("image")) {
                                                //if the profile image of users is available then we can retrieve that image
                                                 retImage[0] = dataSnapshot.child("image").getValue().toString();
                                                //for Display the image
                                                Picasso.get().load(retImage[0]).into(holder.profileImage);

                                            }

                                            final String retName = dataSnapshot.child("name").getValue().toString();
                                            final String retStatus = dataSnapshot.child("status").getValue().toString();

                                            holder.userName.setText(retName); //we are  to display status here but We are display the last seen of user


                                            //Check for online or offline state
                                            if(dataSnapshot.child("userState").hasChild("state")) //dataSnapShot is basically a reference to it and going to retrieve a specific state either online or offile
                                            {
                                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                                if(state.equals("online"))
                                                {
                                                    holder.userStatus.setText("online");
                                                }
                                                else if(state.equals("offline"))
                                                {
                                                    holder.userStatus.setText("Last seen: "+date+ " "+time);
                                                }
                                            }
                                            //Old user who are not update the app where user state is not available in database
                                            else
                                            {
                                                holder.userStatus.setText("Offline");//Display last seen instead of status

                                            }




                                            //Whenever user click on any item in ChatList from  recycler view then it should get the ID and we can go to Chat Activity for this users
                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                                    //When users click on any usersList then we get the that username and the profile picture if we want but we will get the name of ID of the users
                                                    //and then will send it to ChatActivity from the ChatFragment
                                                    //In Order to that
                                                    chatIntent.putExtra("visit_user_id",usersIDs);
                                                    chatIntent.putExtra("visit_user_name",retName);
                                                    chatIntent.putExtra("visit_image",retName);
                                                    startActivity(chatIntent);

                                                }
                                            });
                                        }
                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                        }

                        @NonNull
                        @Override
                        public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                            //we need to access the user display layout which i defined and initialized inside the ChatViewHolder
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup,false);
                            return new ChatsViewHolder(view);
                        }
                    };
        chatsList.setAdapter(adapter);//We have ot set the adapter to recycler view
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder//it should not be void because we have to return view to it by using the onCreate and onBind holder
    {
        //Now we have to include layout which contains basically user_display_layout
        CircleImageView profileImage;
        TextView userStatus,userName;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName  = itemView.findViewById(R.id.user_profile_name);
        }
    }
}
