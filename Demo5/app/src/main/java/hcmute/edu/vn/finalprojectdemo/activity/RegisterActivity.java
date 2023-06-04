package hcmute.edu.vn.finalprojectdemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import hcmute.edu.vn.finalprojectdemo.R;
import hcmute.edu.vn.finalprojectdemo.model.User;

public class RegisterActivity extends AppCompatActivity {
    Button bt_register;
    EditText et_username_register, et_password_register, et_repassword_register, et_email_register;
    DatabaseReference databaseReference;
    User user;

    String usernameCheck;
    String emailCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Anh Xa
        bt_register = findViewById(R.id.bt_register);
        et_username_register = findViewById(R.id.et_username_register);
        et_password_register = findViewById(R.id.et_password_register);
        et_repassword_register = findViewById(R.id.et_repassword_register);
        et_email_register = findViewById(R.id.et_email_register);

        // Handle click event
        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

    }

    private void register(){
        String username = et_username_register.getText().toString().trim();
        String password = et_password_register.getText().toString().trim();
        String repassword = et_repassword_register.getText().toString().trim();
        String email = et_email_register.getText().toString().trim();

        int min = 1;
        int max = 100;
        Random random = new Random();
        int id = random.nextInt(max - min + 1) + min;
        user = new User(id,username, password ,email, null);

        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://finalproject-mopr-demo-default-rtdb.firebaseio.com/").child("Users/");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user1 = dataSnapshot.getValue(User.class);
                    if(user1!=null){
                        String usernameCheck = user1.getUsername();
                        String emailCheck = user1.getEmail();
                        if(usernameCheck!=null && emailCheck!=null)
                        {
                            usernameCheck = usernameCheck.trim();
                            emailCheck = emailCheck.trim();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterActivity.this, "Cancelled!", Toast.LENGTH_SHORT).show();
            }
        });

        if(username.equals(usernameCheck)){
            Toast.makeText(this, "Username existed!", Toast.LENGTH_SHORT).show();
        } else if (email.equals(emailCheck)) {
            Toast.makeText(this, "Email existed!", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(repassword)){
            Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT).show();
        }
        else {
            databaseReference.child(String.valueOf(id)).setValue(user);
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    }
}