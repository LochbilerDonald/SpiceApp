package com.example.spiceapp.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.spiceapp.Adapters.ContactsAdapter;
import com.example.spiceapp.FirebaseManager;
import com.example.spiceapp.FirebaseObjects.Chat;
import com.example.spiceapp.FirebaseObjects.User;
import com.example.spiceapp.MakeChat;
import com.example.spiceapp.MessageActivity;
import com.example.spiceapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Layout which shows a list of all chats
 * with people whom you have an ongoing
 * conversation with
 * @author Ryan Simpson
 */

/*TODO:
    1. Make Group chats appear on the chats tab
    2. Allow users to create their own group chat
    3. Have DMs and Group Chats coexist on this page
    4. Use checkboxes in conjunction with recycler view to allow users to add other users to group chat
*/
public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private ContactsAdapter contactsAdapter;
    private List<User> mUsers;

    FirebaseUser mUser;
    DatabaseReference reference;

    private List<String> usersList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_fragment, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fabNewChat);

        recyclerView = view.findViewById(R.id.chatsRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(v.getContext(), MakeChat.class);
                startActivityForResult(nextScreen, 0);
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getSender().equals(mUser.getEmail().replace('.','_'))){
                        if(!usersList.contains(chat.getSender()) && !usersList.contains(chat.getReciever())) {
                            usersList.add(chat.getReciever());
                        }
                    }
                    if(chat.getReciever().equals(mUser.getEmail().replace('.','_'))){
                        if(!usersList.contains(chat.getSender()) && !usersList.contains(chat.getReciever())) {
                            usersList.add(chat.getSender());
                        }
                    }
                }
                readChats();



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }


    private void readChats() {
        mUsers = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    for(String email : usersList){
                        if(user.getEmail().replace('.','_').equals(email)){
                            if(mUsers.size() != 0) {
                                for (User user1 : mUsers) {
                                    if (!user.getEmail().replace('.', '_').equals(user1.getEmail().replace('.', '_'))) {
                                        mUsers.add(user);
                                        break;
                                    }
                                }
                            } else {
                                mUsers.add(user);
                            }
                        }
                    }

                }

                contactsAdapter = new ContactsAdapter(getContext(), mUsers);
                recyclerView.setAdapter(contactsAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}