package com.example.androidpractice.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.androidpractice.MainActivity;
import com.example.androidpractice.R;
import com.example.androidpractice.XConnectionHelp;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class HomeFragment extends Fragment {

    public static final String TAG = HomeFragment.class.getName();

    /*private Button btnSend;
    private EditText edtContent;
    private static XConnectionHelp conn;
    private static ChatManager chatManager;*/

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView");

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // final TextView textView = root.findViewById(R.id.text_home);

        // get conn and chatManager from MainActivity


        /*homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                // textView.setText(s);
                Log.i(TAG, "onChanged");
            }
        });*/


        /*if (chatManager == null) {
            // Assume we've created an XMPPConnection name "connection".
            chatManager = ChatManager.getInstanceFor(conn.getConnection());
            chatManager.addIncomingListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                    Log.i(TAG, "New message from " + from + ": " + message.getBody());
                }

            });
        }*/

        return root;
    }
}