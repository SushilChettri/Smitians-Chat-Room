package com.sushil.smitianschatroom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar =(Toolbar)findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //i am going to add title to that  and update button on this toolbar
        getSupportActionBar().setTitle("Find Friends");
        //In which activity when the user want to go back when the users click on the back button on the toolbar .Goto manifest file

    }

    @Override
    protected void onStart() {
        super.onStart();

        //create a firebase recycler option
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UsersRef,Contacts.class) //here i basically pass the reference to user firebase database and module class
                .build();


        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter =  //it need two parameters one is module class(Contacts) and other is FindFriendViewHolder,Call it as a adapter
        new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) { //Pass the option here
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                /*Now we are retrieving the name, image,status from FirebaseDataBase using our contacts  module class and by using this model object will set name,
               status,profile image to out fields which we define and initialize on our FindFriendViewHolder class */
               holder.userName.setText(model.getName());
               holder.userStatus.setText(model.getStatus());
               //In Order to add image we need to use picasso library and placeholder display the default profile if the user has not set any profile picture
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);


                //i will add firebase item click listener ,whenever users click on any profile then it should get the unique user ID of the users
                //and by unique user id we can retrieve all the information of users on the profile activity

                holder.itemView.setOnClickListener(new View.OnClickListener() { //item view is basically object inside the findFriendView holder..When the user click on any view(name,status ) we get the unique userID of users
                    @Override
                    public void onClick(View v) {
                      String visit_user_id = getRef(position).getKey(); //When i click on other users i can see all the information whether the user is online or not and we pass the position it basically when i click on any friend list, i will get the position of that object..So that we can get the unique user ID after getting the position
                        Intent profileIntent = new Intent(FindFriendsActivity.this,ProfileActivity.class);//We can simply create intent to send the user to profile activity and we can pass the visit user id to that activity and by using the unique visit user id we can retrieve all the information
                        //We want to send the ID
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });

            }

            //onCreateViewHolder  is basically for user_display_layout
            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);//By using the viewGroup(parent) object we get the context and we connect user_display_layout to our this find friend view holder class
                FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);//pass the view object here
                return viewHolder;
            }
        };

    //We can set out RecyclerView i.e FindFriendRecyclerList
        FindFriendsRecyclerList.setAdapter(adapter);
       //we have to start listening the firebase Recycler adapter which is by name adapter
        adapter.startListening();
    }

    //We can Access the user_display_layout fields on our view holder class
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            //Now we have to initialize here into the constructor by using the itemView Object
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus =itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);

        }
    }

}
