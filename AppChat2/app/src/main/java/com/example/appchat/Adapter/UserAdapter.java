package com.example.appchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.appchat.FindFriendsActivity;
import com.example.appchat.Model.Contacts;
import com.example.appchat.Model.Messages;
import com.example.appchat.ProfileActivity;
import com.example.appchat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends FirebaseRecyclerAdapter<Contacts, UserAdapter.UserViewHolder> {

    private Context mContext;
    private ArrayList<Contacts> userList;
    //private FirebaseAuth mAuth;
    //private DatabaseReference usersRef;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public UserAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options) {
        super(options);
    }

    public UserAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options, Context mContext, ArrayList<Contacts> userList) {
        super(options);
        this.mContext = mContext;
        this.userList = userList;
        //this.mAuth = mAuth;
        //this.usersRef = usersRef;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, final int position, @NonNull Contacts model) {
        //usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        final Contacts contacts = userList.get(position);

        holder.userName.setText(contacts.getName());
        holder.userStatus.setText(contacts.getStatus());
        Picasso.get().load(contacts.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {

                String visit_user_id = getRef(position).getKey();

                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra("visit_user_id", visit_user_id);
                mContext.startActivity(profileIntent);
            }
        });
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
        return new UserViewHolder(view);
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }

    /*private Context mContext;
    private ArrayList<Contacts> userList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public UserAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options) {
        super(options);
    }

    public UserAdapter(@NonNull FirebaseRecyclerOptions<Contacts> options, Context mContext, ArrayList<Contacts> userList) {
        super(options);
        this.mContext = mContext;
        this.userList = userList;
        //this.mAuth = mAuth;
        //this.usersRef = usersRef;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder userViewHolder, final int position, Contacts model) {



        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //final Contacts contacts = userList.get(position);

        userViewHolder.userName.setText(model.getName());
        userViewHolder.userStatus.setText(model.getStatus());
        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(userViewHolder.profileImage);

        userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                String visit_user_id = getRef(position).getKey();

                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra("visit_user_id", visit_user_id);
                mContext.startActivity(profileIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }*/
}
