package hcmute.edu.vn.finalprojectdemo.controller;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hcmute.edu.vn.finalprojectdemo.model.User;

public class UserDAO {
    DatabaseReference database;

    public void getUserById(int id, GetUserByIdCallback callback){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        database = firebaseDatabase.getReferenceFromUrl("https://finalproject-mopr-demo-default-rtdb.firebaseio.com/").child("Users/");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = null;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User userFirebase = dataSnapshot.getValue(User.class);
                    if(id == userFirebase.getId()){
                        user = userFirebase;
                        break;
                    }
                }
                if (user != null) {
                    callback.onUserRetrieved(user); // Pass the retrieved user object to the callback
                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error); // Pass the database error to the callback
            }
        });
    }
    public interface GetUserByIdCallback {
        void onUserRetrieved(User user);
        void onError(DatabaseError error);
    }
}
