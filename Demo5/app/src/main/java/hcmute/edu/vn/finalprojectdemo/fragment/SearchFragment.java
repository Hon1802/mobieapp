package hcmute.edu.vn.finalprojectdemo.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.finalprojectdemo.model.GlobalUserID;
import hcmute.edu.vn.finalprojectdemo.model.Item;
import hcmute.edu.vn.finalprojectdemo.adapter.ItemAdapter;
import hcmute.edu.vn.finalprojectdemo.R;
import hcmute.edu.vn.finalprojectdemo.model.User;
import hcmute.edu.vn.finalprojectdemo.controller.UserDAO;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private RecyclerView rcvItem;
    private ItemAdapter itAdap;
    private int itemCount;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button btSearch;
    private EditText etSearch;
    private Uri selectedImageUri;
    public static ArrayList<Item> itemList;
    private User user;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Anh Xa
        rcvItem = view.findViewById(R.id.rv_search);
        btSearch = view.findViewById(R.id.bt_search);
        etSearch = view.findViewById(R.id.et_search);
        selectedImageUri = ImageFragment.selectedImageUri;
        itemList = new ArrayList<>();

        itemCount = ItemAdapter.itemCount;

        // Handle button
        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    String url = "https://www.google.com/search?q=" + query + "&tbm=isch";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UserDAO userDAO = new UserDAO();
        userDAO.getUserById(GlobalUserID.GlobalUserID, new UserDAO.GetUserByIdCallback() {
            @Override
            public void onUserRetrieved(User userFirebase) {
                if (userFirebase != null) {
                    List<Item> list = userFirebase.getHistory();
                    itemCount = list.size();
                    itAdap = new ItemAdapter(getActivity());
                    if(itemCount > 0){
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);
                        // Set the custom SpanSizeLookup
                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                            @Override
                            public int getSpanSize(int position) {
                                return 1;
                            }
                        });
                        rcvItem.setLayoutManager(gridLayoutManager);
                        rcvItem.setAdapter(itAdap);
                        getUserHistory();
                    }
                }
            }

            @Override
            public void onError(DatabaseError error) {
                Toast.makeText(getActivity(), "No user found!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getUserHistory() {
        UserDAO userDAO = new UserDAO();
        userDAO.getUserById(GlobalUserID.GlobalUserID, new UserDAO.GetUserByIdCallback() {
            @Override
            public void onUserRetrieved(User userFirebase) {
                if (userFirebase != null) {
                    List<Item> list = userFirebase.getHistory();
                    if (list != null) {
                        itAdap.setData(list);
                        rcvItem.setAdapter(itAdap);
                    }
                }
            }

            @Override
            public void onError(DatabaseError error) {
                Toast.makeText(getActivity(), "No user found!", Toast.LENGTH_SHORT).show();
            }
        });
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

}