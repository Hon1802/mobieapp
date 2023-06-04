package hcmute.edu.vn.finalprojectdemo.fragment;

import android.content.Intent;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import hcmute.edu.vn.finalprojectdemo.model.GlobalUserID;
import hcmute.edu.vn.finalprojectdemo.R;
import hcmute.edu.vn.finalprojectdemo.model.Item;
import hcmute.edu.vn.finalprojectdemo.model.User;
import hcmute.edu.vn.finalprojectdemo.controller.UserDAO;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Uri selectedImageUri;
    // TODO 2.1: defines the model to be used
    String modelPath = "lite-model_yamnet_classification_tflite_1.tflite";

    // TODO 2.2: defining the minimum threshold
    float probabilityThreshold = 0.3f;
    private TextView tv_contentListen;
    private Button bt_searchAudio, bt_listenAgain;
    DatabaseReference databaseReference;
    private ArrayList<Item> arrayList;
    private ArrayAdapter<Item> arrayAdapter;
    private Item item;
    private int REQUEST_RECORD_AUDIO = 1337;

    public AudioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AudioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AudioFragment newInstance(String param1, String param2) {
        AudioFragment fragment = new AudioFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_audio, container, false);

        // Anh Xa
        tv_contentListen = view.findViewById(R.id.tv_contentListen);
        bt_searchAudio = view.findViewById(R.id.bt_searchAudio);
        bt_listenAgain = view.findViewById(R.id.bt_listenAgain);



        // Handle button
        bt_searchAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item = new Item();
                item.setResourceImage("https://firebasestorage.googleapis.com/v0/b/finalproject-mopr-demo.appspot.com/o/images%2Fic_audio.png?alt=media&token=8e878674-4f08-4399-b723-ea22df8daa51");
                item.setTxtImage(tv_contentListen.getText().toString().trim());


                // Check if the item is already present in the arrayList

                UserDAO userDAO = new UserDAO();
                userDAO.getUserById(GlobalUserID.GlobalUserID, new UserDAO.GetUserByIdCallback() {
                    @Override
                    public void onUserRetrieved(User user) {
                        if(user != null){
                            arrayList = new ArrayList<>();
                            arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, arrayList);


                            if(user.getHistory() == null) {
                                if (!arrayList.contains(item)) {
                                    arrayList.add(item);
                                    arrayAdapter.notifyDataSetChanged();
                                    user.setHistory(arrayList);  // Initialize the history if it is null
                                }
                            }
                            else{
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
                            String query = tv_contentListen.getText().toString().trim();
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

        bt_listenAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    listenAudio();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            listenAudio();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return view;
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

    private void listenAudio() throws IOException {
        // Handle function
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);


        // TODO 2.3: Loading the model from the assets folder
        AudioClassifier classifier = AudioClassifier.createFromFile(getActivity(), modelPath);

        // TODO 3.1: Creating an audio recorder
        TensorAudio tensor = classifier.createInputTensorAudio();

        // TODO 3.2: Showing the audio recorder specification
        TensorAudio.TensorAudioFormat format = classifier.getRequiredTensorAudioFormat();
        String recorderSpecs = "Number Of Channels: " + format.getChannels() + "\n" +
                "Sample Rate: " + format.getSampleRate();
//        bt_listenAgain.setText(recorderSpecs);

        // TODO 3.3: Creating an audio record
        AudioRecord record = classifier.createAudioRecord();
        record.startRecording();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // TODO 4.1: Classifying audio data
                int numberOfSamples = tensor.load(record);
                List<Classifications> output = classifier.classify((TensorAudio) tensor);

//                // TODO 4.2: Filtering out classifications with low probability
                List<Category> filteredModelOutput = new ArrayList<>();
                List<Category> categories = output.get(0).getCategories();
                for (Category category : categories) {
                    if (category.getScore() > probabilityThreshold) {
                        filteredModelOutput.add(category);
                    }
                }

                // TODO 4.3: Creating a multiline string with the filtered results
                List<Category> sortedOutput = new ArrayList<>(filteredModelOutput);
                Collections.sort(sortedOutput, (c1, c2) -> Float.compare(c2.getScore(), c1.getScore()));

                StringBuilder outputStrBuilder = new StringBuilder();
                for (Category category : sortedOutput) {
                    outputStrBuilder.append(category.getLabel())
                            .append("\n");
                }
                String outputStr = outputStrBuilder.toString();

                // TODO 4.4: Updating the UI
                if (!outputStr.isEmpty()) {
                    requireActivity().runOnUiThread(() -> tv_contentListen.setText(outputStr));
                }
            }
        }, 1, 500);
    }

    @Override
    public void onResume() {
        try {
            listenAudio();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.onResume();
    }

}