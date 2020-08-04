package com.sushil.smitianschatroom;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;

    private DatabaseReference ContactsRef, UserRef; //UserRef in order to retrieve the user profile,name  and status and initialized it in OnCreateView
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext())); //I initialized the recyclerView

        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid(); //Lets get current user ID by using mAuth
        ContactsRef  = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);//Contact list should be each specific users and   //FOR THE ONLINE USER OR CURRENT USER, WE HAVE TO DISPLAY ONLY THE THOSE USERS WHO WILL BE LOGGED IN INTO THIS ACCOUNT THAT IS HOW MANY PEOPLE ARE ADDED IN HIS CONTACT LIST
        UserRef  =FirebaseDatabase.getInstance().getReference().child("Users");

        return  ContactsView;



    }

    //Now by using the firebase recyclerAdapter we can retrieve all of Contacts from firebase database
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>() //We need to pass contacts module
                .setQuery(ContactsRef,Contacts.class)//we basically pass a reference to a database..I mean to add in a contact list and pass module class
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter   //Its need two parameter first one in model class and other is static class which is view holder class
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) { //pass options, which is basically our query
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                //Here, we have to retrieve the profile image, user name and status
                //And By recyclerView we can display the this things on our contacts fragment
                //By using the different firebase ID(for name,status,image) we have to get the three keys and by using the ID we can access the Users Node and retrieve the profile image,username of all users
                String userIDs = getRef(position).getKey();

                UserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {


                            //Check for online or offline state
                            if(dataSnapshot.child("userState").hasChild("state")) //dataSnapShot is basically a reference to it and going to retrieve a specific state either online or offile
                            {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                if(state.equals("online"))
                                {
                                    //we are displaying the online icon
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            //Old user who are not update the app where user state is not available in database
                            else
                            {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);//Display last seen instead of status

                            }

                            //Image is optional,we have to check whether the user has set image or not and if its set then we retrieve the image
                            if(dataSnapshot.hasChild("image")){
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);  //holder=object is viewholder Class
                                holder.userStatus.setText(profileStatus);

                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            else  //if the user does not set any profile image or using the default profile image
                            {
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);  //holder=object is viewholder Class
                                holder.userStatus.setText(profileStatus);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                //First we will access this user display layout and then we will return our this view
                //IN ORDER TO DO THAT

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
            //create object of ContactView Holder class
            ContactsViewHolder viewHolder = new ContactsViewHolder(view);
            return viewHolder;
            }
        };

        //Now we have to set firebase recycler adapter on our recycler view whose name is myContact list
        myContactsList.setAdapter(adapter);
        adapter.startListening();

    }

        //We have to Create View Holder class and  Access user display layout in viewHolder class
        public static class ContactsViewHolder extends RecyclerView.ViewHolder
        {

            TextView userName,userStatus;
            CircleImageView profileImage;
            ImageView onlineIcon;

            public ContactsViewHolder(@NonNull View itemView) {
                super(itemView);

                userName = itemView.findViewById(R.id.user_profile_name);
                userStatus = itemView.findViewById(R.id.user_status);
                profileImage = itemView.findViewById(R.id.users_profile_image);
                onlineIcon = (ImageView) itemView.findViewById(R.id.user_online_status);
            }
        }

}
