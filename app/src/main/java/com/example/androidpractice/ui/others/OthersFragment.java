package com.example.androidpractice.ui.others;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.androidpractice.ISBN.toScanActivity;
import com.example.androidpractice.MainActivity;
import com.example.androidpractice.R;
import com.example.androidpractice.XConnectionHelp;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;


public class OthersFragment extends Fragment {

    private static final String TAG = OthersFragment.class.getName();

    private OthersViewModel othersViewModel;

    private Button btn_scan;

    private Button btnSend;
    private EditText edtContent;
    private static XConnectionHelp conn;
    private static ChatManager chatManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        othersViewModel =
                ViewModelProviders.of(this).get(OthersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_others, container, false);
        /*
        final TextView textView = root.findViewById(R.id.text_notifications);
        othersViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */
        btn_scan = root.findViewById(R.id.btn_to_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), toScanActivity.class);
                startActivity(intent);
            }
        });

        btnSend = root.findViewById(R.id.btn_send);
        edtContent = root.findViewById(R.id.edit_content);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Send clicked");

                getConnAndChatManager();

                String content = edtContent.getText().toString();

                try {
                    EntityBareJid jid = JidCreate.entityBareFrom("test1@ubuntu");
                    Chat chat = chatManager.chatWith(jid);
                    chat.send("Howdy!");

                } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.getMessage());
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    private void getConnAndChatManager() {
        if (conn == null) {
            conn = ((MainActivity) getActivity()).getConn();
        }
        if (chatManager == null) {
            chatManager = ((MainActivity) getActivity()).getChatManager();
        }
    }
}