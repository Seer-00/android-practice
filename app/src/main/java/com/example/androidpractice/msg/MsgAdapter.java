package com.example.androidpractice.msg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidpractice.R;
import com.example.androidpractice.contacts.Contact;
import com.example.androidpractice.contacts.ContactAdapter;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Msg> msgList;
    private Context context;
    private static String leftUri;
    private static String rightUri;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        SimpleDraweeView leftImage;
        SimpleDraweeView rightImage;
        TextView leftMsg;
        TextView rightMsg;

        public ViewHolder(View view) {
            super(view);
            leftLayout = view.findViewById(R.id.left_layout);
            rightLayout = view.findViewById(R.id.right_layout);
            leftImage = view.findViewById(R.id.left_image);
            rightImage = view.findViewById(R.id.right_image);
            leftMsg = view.findViewById(R.id.left_msg);
            rightMsg = view.findViewById(R.id.right_msg);
        }
    }

    public MsgAdapter(List<Msg> msgList, Context context, String leftName, String rightName) {
        this.msgList = msgList;
        this.context = context;
        leftUri = "res://" + context.getPackageName() + "/"
                + ContactAdapter.getResID().get(getPicIdx(leftName));
        rightUri = "res://" + context.getPackageName() + "/"
                + ContactAdapter.getResID().get(getPicIdx(rightName));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_msg, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Msg msg = msgList.get(position);

        // receive message
        if (msg.getType() == Msg.TYPE_RECV) {
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);

            holder.leftImage.setImageURI(leftUri);
            holder.leftMsg.setText(msg.getContent());
        }
        // send message
        else if (msg.getType() == Msg.TYPE_SEND) {
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);

            holder.rightImage.setImageURI(rightUri);
            holder.rightMsg.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    private int getPicIdx(String name) {
        return Math.abs(name.hashCode() % Contact.getPicNumber());
    }
}
