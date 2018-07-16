package com.example.kahugu.fatherkinyuaonline;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, Fullname, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;

    String currentUserID;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        Fullname = (EditText) findViewById(R.id.setup_fullname);
        CountryName = (EditText) findViewById(R.id.setup_countryname);
        SaveInformationButton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) 
            {
              SaveAccountSetupInformation();  
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
             Intent galleryIntent = new Intent();
             galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
             galleryIntent.setType("image/*");
             startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

            UserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.exists())
                    {
                        String image = dataSnapshot.child("ProfileImage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile image");
                loadingBar.setMessage("Please wait as we update your profile image");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.dismiss();

                Uri resultUri = result.getUri();

                StorageReference filepath = UserProfileImageRef.child(currentUserID + ".jpg");
                
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) 
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, "Profile image saved", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UserRef.child("Profile Image").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                          if (task.isSuccessful())
                                          {
                                              Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);

                                              Toast.makeText(SetupActivity.this, "Profile saved to firebase", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                          }
                                          else
                                              {
                                                  String message = task.getException().getMessage();
                                                  Toast.makeText(SetupActivity.this, "Error Occurred" + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                              }
                                        }
                                    });
                        }
                        
                    }
                });
            }
            else 
                {
                    Toast.makeText(this, "Error occurred: Image cant be cropped, try again", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

        }
    }

    private void SaveAccountSetupInformation()
    {
        String username = UserName.getText().toString();
        String fullname = Fullname.getText().toString();
        String country = CountryName.getText().toString();

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your Username", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please write your Full Name", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your Country Name", Toast.LENGTH_SHORT).show();
        }
        else
            {
                loadingBar.setTitle("Saving your Information");
                loadingBar.setMessage("Please wait as we create your Account");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                HashMap userMap = new HashMap();
                userMap.put("username", username);
                userMap.put("fullname", fullname);
                userMap.put("country", country);
                userMap.put("status", "Hey there");
                userMap.put("gender", "none");
                userMap.put("dob", "none");
                userMap.put("relationshipstatus", "none");

                UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) 
                    {
                       if (task.isSuccessful())
                       {
                           SendUserToMainActivity();
                           Toast.makeText(SetupActivity.this, "Your account is created successfully.", Toast.LENGTH_LONG).show();
                           loadingBar.dismiss();
                       }
                       else
                           {
                               String message = task.getException().getMessage();
                               Toast.makeText(SetupActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                               loadingBar.dismiss();
                           }
                    }
                });
            }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
