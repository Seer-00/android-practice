package com.example.androidpractice.ui.others;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidpractice.QRCodeActivity;
import com.example.androidpractice.User;
import com.example.androidpractice.contacts.Contact;
import com.example.androidpractice.contacts.ContactAdapter;
import com.example.androidpractice.MainActivity;
import com.example.androidpractice.R;
import com.example.androidpractice.XConnectionHelp;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.zxing.integration.android.IntentIntegrator;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;


public class OthersFragment extends Fragment {

    private static final String TAG = OthersFragment.class.getName();

    private static MainActivity mainActivity;
    private static User user;
    private static XConnectionHelp conn;
    private static ChatManager chatManager;
    private static Roster roster;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        getConnObjFromActivity();

        View root = inflater.inflate(R.layout.fragment_others, container, false);

        initViews(root);

        return root;
    }

    private void getConnObjFromActivity() {
        if (conn == null) {
            conn = ((MainActivity) getActivity()).getConn();
        }
        if (chatManager == null) {
            chatManager = ((MainActivity) getActivity()).getChatManager();
        }
        if (roster == null) {
            roster = ((MainActivity) getActivity()).getRoster();
        }
    }

    private void initViews(View view) {
        mainActivity = (MainActivity) getActivity();
        user = mainActivity.getUser();

        final EditText edt_contact = view.findViewById(R.id.edit_add_contact);
        final EditText edt_isbn = view.findViewById(R.id.edit_isbn);
        Button btn_rm = view.findViewById(R.id.btn_rm_contact);
        Button btn_add = view.findViewById(R.id.btn_add_contact);
        ImageButton btn_scan_contact = view.findViewById(R.id.btn_to_scan_contact);
        Button btn_search = view.findViewById(R.id.btn_search_book);
        ImageButton btn_scan_isbn = view.findViewById(R.id.btn_to_scan_isbn);
        SimpleDraweeView userImage = view.findViewById(R.id.img_user);
        TextView userName = view.findViewById(R.id.tv_user);
        ImageButton btn_qrcode = view.findViewById(R.id.img_qrcode);

        /* set image, name, QRCode of user */
        String imageUri = "res://" + mainActivity.getPackageName() + "/"
                + ContactAdapter.getResID().get(getPicIdx(user.getName()));
        userImage.setImageURI(imageUri);
        userName.setText(user.getName());
        btn_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity, QRCodeActivity.class);
                intent.putExtra("Name", user.getName());
                mainActivity.startActivity(intent);
            }
        });


        /* buttons for adding and removing contact */
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String peerName = edt_contact.getText().toString().trim();
                if (peerName.isEmpty()) {
                    Toast.makeText(mainActivity, "输入的联系人为空", Toast.LENGTH_SHORT).show();
                }
                BareJid peerJID;
                try {
                    if (!conn.isUserExistInServer(peerName)) {
                        Toast.makeText(mainActivity, "用户不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    peerJID = JidCreate.bareFrom(peerName + "@" + user.getDom());
                    roster.createEntry(peerJID, peerName, null);
                    Toast.makeText(mainActivity, "好友请求已发送", Toast.LENGTH_SHORT).show();
                } catch (XmppStringprepException
                        | InterruptedException
                        | SmackException.NotLoggedInException
                        | XMPPException.XMPPErrorException
                        | SmackException.NotConnectedException
                        | SmackException.NoResponseException e) {
                    Toast.makeText(mainActivity, "添加好友出现错误", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        btn_rm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String peerName = edt_contact.getText().toString().trim();
                if (peerName.isEmpty()) {
                    Toast.makeText(mainActivity, "输入的联系人为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    RosterEntry entry = roster.getEntry(JidCreate.bareFrom(peerName + "@" + user.getDom()));
                    if (entry == null) {
                        Toast.makeText(mainActivity, "该联系人不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    roster.removeEntry(entry);
                } catch (XmppStringprepException
                        | InterruptedException
                        | SmackException.NotLoggedInException
                        | XMPPException.XMPPErrorException
                        | SmackException.NotConnectedException
                        | SmackException.NoResponseException e) {
                    Toast.makeText(mainActivity, "删除好友出现错误", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        btn_scan_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.startScanner(MainActivity.SCAN_TYPE_QRCODE);
            }
        });


        /* buttons for ISBN activities */
        btn_scan_isbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.startScanner(MainActivity.SCAN_TYPE_ISBN);
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String isbn = edt_isbn.getText().toString().trim();
                if (isbn.isEmpty()) {
                    Toast.makeText(mainActivity, "输入的ISBN为空", Toast.LENGTH_SHORT).show();
                }
                mainActivity.startDownloadBookInfo(isbn);
                edt_isbn.setText("");
            }
        });
    }

    private int getPicIdx(String name) {
        return Math.abs(name.hashCode() % Contact.getPicNumber());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /*IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if ((result == null) || (result.getContents() == null)) {
            Log.i(TAG, "Cancel scanning");
            Toast.makeText(mainActivity, "扫描取消", Toast.LENGTH_SHORT).show();
            return;
        }

        *//* ISBN for debugging *//*
        // String contents = "0000000000000";      // book not found
        // String contents = "9787121402180";      // no cover
        // String contents = "9787115209306";      // valid

        String contents = result.getContents();

        Log.i(TAG, "Scanning result: " + contents);
        Toast.makeText(mainActivity, "扫描结果: " + contents, Toast.LENGTH_LONG).show();

        // 下载耗时，显示进度条
        mainActivity.progressDialog = new ProgressDialog(this.getActivity());
        mainActivity.progressDialog.setMessage("通信中...");
        mainActivity.progressDialog.show();

        DownloadThread thread = new DownloadThread(BookAPI.URL_ISBN_BASE + contents);
        thread.start();*/
    }
}
