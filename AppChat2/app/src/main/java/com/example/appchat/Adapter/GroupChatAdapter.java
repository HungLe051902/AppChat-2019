package com.example.appchat.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.ChatActivity;
import com.example.appchat.GroupChatActivity;
import com.example.appchat.ImageViewerActivity;
import com.example.appchat.MainActivity;
import com.example.appchat.Model.GroupMessages;
import com.example.appchat.Model.Messages;
import com.example.appchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupChatViewHolder> {

    private List<GroupMessages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, UsersRef;

    private String currentUserName;

    public GroupChatAdapter (List<GroupMessages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public GroupChatAdapter.GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new GroupChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupChatAdapter.GroupChatViewHolder groupChatViewHolder, final int position) {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        GroupMessages groupMessages = userMessagesList.get(position);

        String fromUserID = groupMessages.getFrom();
        String fromMessageType = groupMessages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(groupChatViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        groupChatViewHolder.receiverMessageText.setVisibility(View.GONE);
        groupChatViewHolder.receiverProfileImage.setVisibility(View.GONE);
        groupChatViewHolder.senderMessageText.setVisibility(View.GONE);
        groupChatViewHolder.messageSenderPicture.setVisibility(View.GONE);
        groupChatViewHolder.messageReceiverPicture.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                groupChatViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                groupChatViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                groupChatViewHolder.senderMessageText.setTextColor(Color.BLACK);
                groupChatViewHolder.senderMessageText.setText(groupMessages.getMessage() + "\n\n" + groupMessages.getTime() + " - " + groupMessages.getDate());
            }
            else
            {
                groupChatViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                groupChatViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                groupChatViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                groupChatViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                groupChatViewHolder.receiverMessageText.setText(groupMessages.getMessage() + "\n\n" + groupMessages.getTime() + " - " + groupMessages.getDate());
            }
        } else if(fromMessageType.equals("image")) {

            if (fromUserID.equals(messageSenderId)) {
                groupChatViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(groupMessages.getMessage()).into(groupChatViewHolder.messageSenderPicture);
            } else {
                groupChatViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                groupChatViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                //messageViewHolder.isRecyclable();
                Picasso.get().load(groupMessages.getMessage()).into(groupChatViewHolder.messageReceiverPicture);
            }
        } else if(fromMessageType.equals("pdf") || fromMessageType.equals("docx")){
            if (fromUserID.equals(messageSenderId)) {
                groupChatViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                groupChatViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                groupChatViewHolder.senderMessageText.setTextColor(Color.BLACK);
                groupChatViewHolder.senderMessageText.setText(groupMessages.getName());
                //Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/app-chat-7b761.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=eedae95d-168d-4dce-bbe4-8aab64044bf9").into(messageViewHolder.messageSenderPicture);

                //messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.file);

                groupChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        groupChatViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }else {
                groupChatViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                groupChatViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                groupChatViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                groupChatViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                groupChatViewHolder.receiverMessageText.setText(groupMessages.getName());

                //Picasso.get()
                //.load("https://firebasestorage.googleapis.com/v0/b/app-chat-7b761.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=eedae95d-168d-4dce-bbe4-8aab64044bf9")
                //.into(messageViewHolder.messageReceiverPicture);

                //messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.file);


            }
        }

        if(fromUserID.equals(messageSenderId)){
            groupChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancle",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(groupChatViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0)
                                {
                                    deleteSentMessage(position, groupChatViewHolder);

                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 3)
                                {
                                    deleteMessageForEveryOne(position, groupChatViewHolder);

                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("text")){
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Cancle",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(groupChatViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0)
                                {
                                    deleteSentMessage(position, groupChatViewHolder);

                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 2)
                                {
                                    deleteMessageForEveryOne(position, groupChatViewHolder);

                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("image")){
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "View this image",
                                "Cancle",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(groupChatViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0)
                                {
                                    deleteSentMessage(position, groupChatViewHolder);

                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1)
                                {
                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 3)
                                {
                                    deleteMessageForEveryOne(position, groupChatViewHolder);

                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else{
            groupChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancle"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(groupChatViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0)
                                {
                                    deleteReceiveMessage(position, groupChatViewHolder);

                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("text")){
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Cancle"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(groupChatViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0)
                                {
                                    deleteReceiveMessage(position, groupChatViewHolder);

                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("image")){
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "View this image",
                                "Cancle",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(groupChatViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0)
                                {
                                    deleteReceiveMessage(position, groupChatViewHolder);
                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), MainActivity.class);
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1)
                                {
                                    Intent intent = new Intent(groupChatViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    groupChatViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;

        public GroupChatViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }

    private void deleteSentMessage(final int position, final GroupChatAdapter.GroupChatViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteReceiveMessage(final int position, final GroupChatAdapter.GroupChatViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    /*rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });*/
                }
                else{
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryOne(final int position, final GroupChatAdapter.GroupChatViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
