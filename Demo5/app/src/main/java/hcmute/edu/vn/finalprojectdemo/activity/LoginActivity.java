package hcmute.edu.vn.finalprojectdemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import hcmute.edu.vn.finalprojectdemo.model.GlobalUserID;
import hcmute.edu.vn.finalprojectdemo.R;
import hcmute.edu.vn.finalprojectdemo.model.User;

public class LoginActivity extends AppCompatActivity {

    Button bt_login, bt_register, bt_forget_password;
    EditText et_username;
    EditText et_password;
    User user;
    DatabaseReference database;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Anh Xa
        bt_login = findViewById(R.id.bt_login);
        bt_register = findViewById(R.id.bt_register);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        bt_forget_password = findViewById(R.id.bt_forget_password);

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        bt_forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void login(String username, String password){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        database = firebaseDatabase.getReferenceFromUrl("https://finalproject-mopr-demo-default-rtdb.firebaseio.com/").child("Users/");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    user = dataSnapshot.getValue(User.class);
                    if(user!=null)
                    {
                        String usernamedata = user.getUsername();
                        String passdata = user.getPassword();
                        if(usernamedata!= null && passdata!= null){
                            if(usernamedata.trim().equals(username) && passdata.trim().equals(password)){
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                GlobalUserID.setGlobalUserID(user.getId());
                            }
                        }

                    }

//                    else{
//                        Toast.makeText(LoginActivity.this, "Login Failed! Wrong username or password", Toast.LENGTH_SHORT).show();
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Cancelled!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(et_username.getText().toString().trim(), et_password.getText().toString().trim());
            }
        });
    }
}