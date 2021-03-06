package com.example.androidpractice.ui.contact;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidpractice.contacts.Contact;
import com.example.androidpractice.contacts.ContactAdapter;
import com.example.androidpractice.MainActivity;
import com.example.androidpractice.R;
import com.example.androidpractice.XConnectionHelp;

import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ContactFragment extends Fragment {

    public static final String TAG = ContactFragment.class.getName();

    private static XConnectionHelp conn;
    private static ChatManager chatManager;
    private static Roster roster;
    private static List<Contact> contactList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // init contact list
        initList();

        RecyclerView recyclerView = root.findViewById(R.id.home_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(manager);
        ContactAdapter adapter = new ContactAdapter(contactList, this.getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this.getActivity(), DividerItemDecoration.VERTICAL));

        return root;
    }

    private void initList() {
        getConnObjFromActivity();
        if (conn == null | chatManager == null | roster == null) {
            Log.i(TAG, "conn|chatManger|roster is null");
            return;
        }

        if (contactList == null) {
            contactList = new LinkedList<>();
        } else {
            contactList.clear();
        }
        // ???????????????????????????
        try {
            roster.reload(); // ???????????????????????????????????????????????????UI
        } catch (NotLoggedInException | NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
        // ??????????????????
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            Log.i(TAG, "JID: " + entry.getJid() + " Group: " + entry.getGroups()
                    + " Type: " + entry.getType() + " Name: " + entry.getName());

            contactList.add(new Contact(entry.getJid().toString()));
        }
        // ???????????????????????????
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private void getConnObjFromActivity() {
        if (conn == null) {
            conn = ((MainActivity) getActivity()).getConn();
        }
        if (chatManager == null) {
            chatManager = ((MainActivity) getActivity()).getChatManager();
        }
        if (roster == null) {
            roster = ((MainActivity)getActivity()).getRoster();
        }
    }
}