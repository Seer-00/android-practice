package com.example.androidpractice.contacts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidpractice.ChatActivity;
import com.example.androidpractice.MainActivity;
import com.example.androidpractice.R;
import com.example.androidpractice.User;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private static final String TAG = ContactAdapter.class.getName();
    private static final String PIC_NAME = "picture_";

    private static Map<Integer, Integer> resID;

    private List<Contact> mContactList;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View contactView;
        SimpleDraweeView contactImage;
        TextView contactName;

        public ViewHolder(@NonNull View view) {
            super(view);
            contactView = view;
            contactImage = view.findViewById(R.id.contact_image);
            contactName = view.findViewById(R.id.contact_name);
        }
    }

    public ContactAdapter(List<Contact> mContactList, Context context) {
        this.mContactList = mContactList;
        this.context = context;
        initResMap();
    }

    /**
     *  initialize resource map:
     *  KEY     = Contact.getPictureIdx (equals to Contact.name.hashCode())
     *  VALUE   = resourceID of picture (file name: picture_x) in R.drawable
     */
    private void initResMap() {
        resID = new HashMap<>();
        ApplicationInfo appInfo = context.getApplicationInfo();
        for (int i = 0; i < Contact.getPicNumber(); i++) {
            resID.put(i, context.getResources().getIdentifier(
                    PIC_NAME + i, "drawable", appInfo.packageName
            ));
        }
    }

    public static Map<Integer, Integer> getResID() {
        return resID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_contact, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.contactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Contact contact = mContactList.get(position);
//                Toast.makeText(v.getContext(), contact.getName(), Toast.LENGTH_SHORT).show();
                startChatActivity(((MainActivity)context).getUser(), contact.getJID());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = mContactList.get(position);

        /*RoundingParams roundingParams = new RoundingParams();
        roundingParams.setCornersRadius(20);
        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(context.getResources());
        GenericDraweeHierarchy hierarchy = builder.build();
        hierarchy.setRoundingParams(roundingParams);
        holder.contactImage.setHierarchy(hierarchy);*/

        // Path: res/drawable/picture_x.jpg
        String imageUri = "res://" + context.getPackageName() + "/" + resID.get(contact.getPictureIdx());
        Log.i(TAG, imageUri);
        holder.contactImage.setImageURI(imageUri);
        holder.contactName.setText(contact.getName());
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }


    private void startChatActivity(User self, String peerJID) {
        Intent intent = new Intent(this.context, ChatActivity.class);
        intent.putExtra("Self", self);
        intent.putExtra("PeerJID", peerJID);
        this.context.startActivity(intent);
    }
}
