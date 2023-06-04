package hcmute.edu.vn.finalprojectdemo.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.task.vision.detector.Detection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.finalprojectdemo.activity.CameraActivity;
import hcmute.edu.vn.finalprojectdemo.databinding.FragmentCameraBinding;
import hcmute.edu.vn.finalprojectdemo.model.GlobalUserID;
import hcmute.edu.vn.finalprojectdemo.R;
import hcmute.edu.vn.finalprojectdemo.model.Item;
import hcmute.edu.vn.finalprojectdemo.model.User;
import hcmute.edu.vn.finalprojectdemo.controller.UserDAO;
import hcmute.edu.vn.finalprojectdemo.ObjectDetectorHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends Fragment implements ObjectDetectorHelper.DetectorListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ObjectDetectorHelper objectDetectorHelper;
    private ObjectDetectorHelper.DetectorListener listener;
    private Button btn_open, btn_choose, bt_searchImageView;
    private ImageView imgView;
    public static String savedText;
    public static Uri selectedImageUri;
    private Bitmap bitmap;
    private DatabaseReference databaseReference;
    private FragmentCameraBinding fragmentCameraBinding = null;

    private FragmentCameraBinding getFragmentCameraBinding() {
        return fragmentCameraBinding;
    }

    private void setFragmentCameraBinding(FragmentCameraBinding fragmentCameraBinding) {
        fragmentCameraBinding = fragmentCameraBinding;
    }

    private Bitmap bitmapBuffer;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(String param1, String param2) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        // Anh Xa
        imgView = view.findViewById(R.id.imageView);
        bt_searchImageView = view.findViewById(R.id.bt_searchImageView);
        objectDetectorHelper = new ObjectDetectorHelper(0.5f,2,3,0,0,getActivity(),this);

        // Handle button
        btn_open = view.findViewById(R.id.btn_open);
        btn_choose = view.findViewById(R.id.btn_choose);

        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                startActivity(intent);
            }
        });

        btn_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });


        bt_searchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Item> arrayList = new ArrayList<>();
                ArrayAdapter<Item> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, arrayList);

                Item item = new Item();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://finalproject-mopr-demo.appspot.com");
                StorageReference mp3Ref = storageRef.child("images/" + savedText+".png");

                InputStream streamImg = new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                };
                try {
                    streamImg = getContext().getContentResolver().openInputStream(selectedImageUri);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                UploadTask uploadTask = mp3Ref.putStream(streamImg);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL
                        mp3Ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                // Set the download URL as the image resource
                                item.setResourceImage(downloadUrl);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure to get the download URL
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });

                item.setTxtImage(savedText.trim());

                // Check if the item is already present in the arrayList
                if (!arrayList.contains(item)) {
                    arrayList.add(item);
                    arrayAdapter.notifyDataSetChanged();
                }

                UserDAO userDAO = new UserDAO();
                userDAO.getUserById(GlobalUserID.GlobalUserID, new UserDAO.GetUserByIdCallback() {
                    @Override
                    public void onUserRetrieved(User user) {
                        if (user != null) {
                            if (user.getHistory() == null) {
                                user.setHistory(arrayList);  // Initialize the history if it is null
                            }
                            else {
                                boolean itemExists = false;
                                for (Item historyItem : user.getHistory()) {
                                    if (historyItem.getTxtImage().equals(item.getTxtImage())) {
                                        itemExists = true;
                                        break;
                                    }
                                }
                                if (!itemExists) {
                                    user.getHistory().add(item);
                                }
                            }
                            databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://finalproject-mopr-demo-default-rtdb.firebaseio.com/").child("Users/").child(String.valueOf(GlobalUserID.GlobalUserID));
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("history", user.getHistory());
                            databaseReference.updateChildren(updates);
                            String query = savedText.trim();
                            if (!query.isEmpty()) {
                                String url = "https://www.google.com/search?q=" + query + "&tbm=isch";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        Toast.makeText(getActivity(), "No user found!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    void imageChooser() {
        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 456);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 456 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            selectedImageUri = uri;
            if(imgView != null){
                try {
                    bitmap = uriToBitmap(selectedImageUri);
                    if (bitmap != null) {
                        imgView.setImageBitmap(bitmap);
                        detectObjects(bitmap);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private Bitmap uriToBitmap(Uri imageUri) throws IOException {
        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        imgView = getView().findViewById(R.id.imageView);

        if (selectedImageUri != null) {
            imgView.setImageURI(selectedImageUri);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (selectedImageUri != null) {
            Bundle args = getArguments();
            if (args == null) {
                args = new Bundle();
            }
            args.putString("image_uri", selectedImageUri.toString());
            setArguments(args);
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedImageUri != null) {
            outState.putString("image_uri", selectedImageUri.toString());
        }
    }

    private void detectObjects(Bitmap bitmap) {
        // Pass the Bitmap and rotation to the object detector helper for processing and detection
        int imageRotation = 0; // Provide the appropriate rotation value
        objectDetectorHelper.detect(bitmap, imageRotation);
    }

    @Override
    public void onError(@NonNull String error) {

    }

    @Override
    public void onResults(@Nullable List<Detection> results, long inferenceTime, int imageHeight, int imageWidth) {
        // Process the detection results here
        // You can access the detections from the 'results' list

        // Example: Log the number of detections
        Log.d(TAG, "Number of detections: " + results.size());

        // Example: Iterate over the detections and perform custom logic
        for (Detection detection : results) {
            // Access detection properties such as bounding box, labels, etc.
            RectF boundingBox = detection.getBoundingBox();
            String label = detection.getCategories().get(0).getLabel();
            float score = detection.getCategories().get(0).getScore();

            // Set the name of object for search
            savedText = label;
        }

        // Draw bounding boxes on the Bitmap
        Bitmap resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.RED);

        for (Detection detection : results) {
            RectF boundingBox = detection.getBoundingBox();
            canvas.drawRect(boundingBox, paint);
        }

        // Update the ImageView with the resultBitmap
        imgView.setImageBitmap(resultBitmap);
    }
}