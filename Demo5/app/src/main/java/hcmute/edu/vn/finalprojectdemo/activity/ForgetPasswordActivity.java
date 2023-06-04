package hcmute.edu.vn.finalprojectdemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hcmute.edu.vn.finalprojectdemo.R;
import hcmute.edu.vn.finalprojectdemo.model.GlobalUserID;
import hcmute.edu.vn.finalprojectdemo.model.User;

public class ForgetPasswordActivity extends AppCompatActivity {
    private TextView tv_password_reset;
    private Button bt_forget;
    private EditText et_email_forget;
    User user;
    DatabaseReference database;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        // Anh Xa
        et_email_forget = findViewById(R.id.et_email_forget);
        bt_forget = findViewById(R.id.bt_forget);
        tv_password_reset = findViewById(R.id.tv_password_reset);

        // Handle function
        bt_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email_forget.getText().toString().trim();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                database = firebaseDatabase.getReferenceFromUrl("https://finalproject-mopr-demo-default-rtdb.firebaseio.com/").child("Users/");
                database.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            user = dataSnapshot.getValue(User.class);
                            if(user.getEmail()!= null)
                            {
                                if(user.getEmail().trim().equals(email)){
                                    tv_password_reset.setText("Your password is: "+user.getPassword().trim());
                                    tv_password_reset.setVisibility(View.VISIBLE);
                                }
                                else{
                                    tv_password_reset.setText("Email does not match!");
                                    tv_password_reset.setVisibility(View.VISIBLE);
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ForgetPasswordActivity.this, "Cancelled!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }
}