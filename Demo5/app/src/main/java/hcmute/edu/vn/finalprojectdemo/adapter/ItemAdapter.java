package hcmute.edu.vn.finalprojectdemo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.database.DatabaseError;

import java.util.List;

import hcmute.edu.vn.finalprojectdemo.controller.UserDAO;
import hcmute.edu.vn.finalprojectdemo.model.GlobalUserID;
import hcmute.edu.vn.finalprojectdemo.model.Item;
import hcmute.edu.vn.finalprojectdemo.R;
import hcmute.edu.vn.finalprojectdemo.model.User;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context mContext;

    public ItemAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private List<Item> itemAdapterList;
    public static int itemCount;

    public void setData(List<Item> list){
        this.itemAdapterList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Item item = itemAdapterList.get(position);
        if(item == null){
            return;
        }

        UserDAO userDAO = new UserDAO();
        userDAO.getUserById(GlobalUserID.GlobalUserID, new UserDAO.GetUserByIdCallback() {
            @Override
            public void onUserRetrieved(User userFirebase) {
                if (userFirebase != null && userFirebase.getHistory() != null) {
                    List<Item> historyList = userFirebase.getHistory();
                    itemCount = historyList.size();
                    if (position < historyList.size()) {
                        Item historyItem = historyList.get(position);
                        String imageUrl = historyItem.getResourceImage(); // Assuming imageUrl is the URL of the image
                        Glide.with(mContext)
                                .load(imageUrl)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(holder.img_item);
                        holder.txt_item.setText(historyItem.getTxtImage());
                    }
                }
            }

            @Override
            public void onError(DatabaseError error) {
                Toast.makeText(mContext, "No user found!", Toast.LENGTH_SHORT).show();
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle item click here
                String query = item.getTxtImage().trim();
                // Perform actions with the clicked text
                if (!query.isEmpty()) {
                    String url = "https://www.google.com/search?q=" + query + "&tbm=isch";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(itemAdapterList !=null){
            return itemAdapterList.size();
        }
        return 0;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        private ImageView img_item;
        private TextView txt_item;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            img_item = itemView.findViewById(R.id.img_item);
            txt_item = itemView.findViewById(R.id.txt_item);
        }
    }
}
