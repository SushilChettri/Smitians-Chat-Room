package com.sushil.smitianschatroom;

import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View groupFragmentView;

    //Now we need to create ArrayList and Adapter to store or retrieve and display the groups our ListView
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();

    //Now we need to create Reference database in group node
    private DatabaseReference GroupRef;


    public GroupsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and initialize in groupFragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        //By using this reference we can retrieve our group
        GroupRef= FirebaseDatabase.getInstance().getReference().child("Groups");

            //METHOD CALL
            IntializeFields();
            
            //Method for call Retrieve and display groupt
            RetrieveAndDisplayGroups();


        //When user click on any group name list then that will send user from group fragment to group Chat Activity
        //along with that group name

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>  adapterView, View view, int position, long id) {
                //If i click on any group name then it will get the name and stored it inside the  currentGroupName as a string type variable
                String currentGroupName = adapterView.getItemAtPosition(position).toString();
                //Once we get that we will send user to Group Chat Activity along side with GroupName
                Intent  groupChatIntent=new Intent(getContext(),GroupChatActivity.class);//In case of this fragment we will get the context then we will send the user to group chat activity
                groupChatIntent.putExtra("groupName",currentGroupName);//Now it will send the Group Chat Activity from the group fragment along side with this value(currentGroupName) and 'groupname' is a key value
                startActivity(groupChatIntent);

            }
        });



        return groupFragmentView;
    }



    private void IntializeFields() {
        list_view=(ListView) groupFragmentView.findViewById(R.id.list_view);
        //Its Array List Basically, we will be storing group from our database
         arrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_groups);//Here we need to provide a getContext() which is our group fragment.In Fragment we will get the context method by simply calling that how we can access that
        //we used simple list item 1 for display the list group then we have to pass list of groups(because i did not put last argument that i faced troubled)
        //NOW WE HAVE TO SET THE ARRAY ADAPTER
        list_view.setAdapter(arrayAdapter);
    }

    private void RetrieveAndDisplayGroups()
    {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();//Basically, containing all group names
                //we have to implement a logic that  it will retrieve the each groups line by line
                Iterator iterator=dataSnapshot.getChildren().iterator(); //Now we can read any child of these parent name Groups
                //FOR READING LINE BY LINE
                while(iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());//This Basically,Prevent the duplication of the used and getKey will get all group name

                }
                list_of_groups.clear();
                list_of_groups.addAll(set);
                //To see the change on the screen  update the listView adapter
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
