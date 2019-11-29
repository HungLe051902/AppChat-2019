package com.example.appchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appchat.Adapter.GroupChatAdapter;
import com.example.appchat.Adapter.MessageAdapter;
import com.example.appchat.Model.GroupMessages;
import com.example.appchat.Model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton SendMessageButton, SendFilesButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView groupName;

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;


    private final List<GroupMessages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupChatAdapter groupChatAdapter;
    private RecyclerView userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUserName, saveCurrentTime, saveCurrentDate;

    private String checker = "", myUri = "";
    private StorageTask uploadTask;
    private Uri fileUri;

    private ProgressDialog loadingBar;
    String messagekEY;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        //Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTimeFormat.format(calForTime.getTime());

        messagekEY = GroupNameRef.push().getKey();

        loadingBar = new ProgressDialog(this);

        //messageSenderID = mAuth.getCurrentUser().getUid();


//        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
//        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
 //       messageReceiverImage = getIntent().getExtras().get("visit_image").toString();



        InitializeFields();

        GetUserInfo();

        groupName.setText(currentGroupName);


        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                //mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Image",
                        "PDF Files",
                        "Ms Word Files"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select the file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(i == 0){
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if(i == 1){
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        }
                        if(i == 2){
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select Ms Word File"), 438);
                        }
                    }
                });
                builder.show();
            }
        });

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                GroupMessages groupMessages = dataSnapshot.getValue(GroupMessages.class);

                messagesList.add(groupMessages);

                groupChatAdapter.notifyDataSetChanged();

                userMessagesList.scrollToPosition(userMessagesList.getAdapter().getItemCount()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {

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

    private void InitializeFields()
    {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = findViewById(R.id.send_message_button);
        SendFilesButton = findViewById(R.id.send_files_btn);
        userMessageInput = findViewById(R.id.input_group_message);
        //displayTextMessages = findViewById(R.id.group_chat_text_display);
        //mScrollView = findViewById(R.id.my_scroll_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.customer_group_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        groupName = findViewById(R.id.custom_group_name);

        groupChatAdapter = new GroupChatAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_group_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(groupChatAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null){

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, we are sendind that file...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if(!checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference("Document Files");

                final String messagePushID = GroupMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            HashMap<String, Object> groupMessageKey = new HashMap<>();
                            GroupNameRef.updateChildren(groupMessageKey);

                            GroupMessageKeyRef = GroupNameRef.child(messagekEY);

                            HashMap<String, Object> messageInfoMap = new HashMap<>();
                            messageInfoMap.put("name", fileUri.getLastPathSegment());
                            messageInfoMap.put("message", downloadUri);
                            messageInfoMap.put("date", saveCurrentDate);
                            messageInfoMap.put("time", saveCurrentTime);
                            messageInfoMap.put("from", currentUserID);
                            messageInfoMap.put("messageID", messagePushID);
                            messageInfoMap.put("type", checker);
                            GroupMessageKeyRef.updateChildren(messageInfoMap);
                            loadingBar.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }else if(checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference("Image Files");

                final StorageReference filePath = storageReference.child(fileUri.getLastPathSegment());

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            HashMap<String, Object> groupMessageKey = new HashMap<>();
                            GroupNameRef.updateChildren(groupMessageKey);

                            GroupMessageKeyRef = GroupNameRef.child(messagekEY);

                            String messagePushID = GroupMessageKeyRef.getKey();

                            HashMap<String, Object> messageInfoMap = new HashMap<>();
                            messageInfoMap.put("name", fileUri.getLastPathSegment());
                            messageInfoMap.put("message", downloadUri);
                            messageInfoMap.put("date", saveCurrentDate);
                            messageInfoMap.put("time", saveCurrentTime);
                            messageInfoMap.put("from", currentUserID);
                            messageInfoMap.put("messageID", messagePushID);
                            messageInfoMap.put("type", checker);

                            GroupMessageKeyRef.updateChildren(messageInfoMap);
                            loadingBar.dismiss();
                        }
                    }
                });
            }else{
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected, Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }


    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
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
    }




    private void SaveMessageInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();

        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        }
        else
        {

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messagekEY);

            String messagePushID = GroupMessageKeyRef.getKey();

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            //messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", saveCurrentDate);
            messageInfoMap.put("time", saveCurrentTime);
            messageInfoMap.put("from", currentUserID);
            messageInfoMap.put("messageID", messagePushID);
            messageInfoMap.put("type", "text");

            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }



    /*private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while(iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }*/
}
