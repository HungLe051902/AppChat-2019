package com.example.appchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.appchat.Adapter.UserAdapter;
import com.example.appchat.Model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.security.AccessController.getContext;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UsersRef;

    private EditText txt_search_friend;
    private ArrayList<Contacts> mUsers;
    private UserAdapter userAdapter;

    //private GenericTypeIndicator<ArrayList<Contacts>> mUsers = new GenericTypeIndicator<ArrayList<Contacts>>(){};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        FindFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mUsers = new ArrayList<>();

        txt_search_friend = findViewById(R.id.txt_search_friend);
        txt_search_friend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    private void searchUsers(final String s) {
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        final Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("name")
                .startAt(s)
                .endAt(s + "\uf8ff");

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(query, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(position).getKey();

                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };

        FindFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();

    }


    @Override
    protected void onStart() {
        super.onStart();

        if (txt_search_friend.getText().toString().equals("")) {
            final FirebaseRecyclerOptions<Contacts> options =
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(UsersRef, Contacts.class)
                            .build();

            FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                            holder.userName.setText(model.getName());
                            holder.userStatus.setText(model.getStatus());
                            Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String visit_user_id = getRef(position).getKey();

                                    Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                    profileIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        }

                        @NonNull
                        @Override
                        public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                            FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                            return viewHolder;
                        }
                    };

            FindFriendsRecyclerList.setAdapter(adapter);

            adapter.startListening();
        }
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
