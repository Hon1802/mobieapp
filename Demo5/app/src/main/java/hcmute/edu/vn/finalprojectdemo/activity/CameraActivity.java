package hcmute.edu.vn.finalprojectdemo.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import hcmute.edu.vn.finalprojectdemo.R;


public class CameraActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

    }
    public void onBackPressed(){
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            finishAfterTransition();
        } else {
            super.onBackPressed();
        }
    }
}