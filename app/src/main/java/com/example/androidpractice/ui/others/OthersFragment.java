package com.example.androidpractice.ui.others;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidpractice.User;
import com.example.androidpractice.contacts.Contact;
import com.example.androidpractice.contacts.ContactAdapter;
import com.example.androidpractice.MainActivity;
import com.example.androidpractice.R;
import com.example.androidpractice.XConnectionHelp;
import com.facebook.drawee.view.SimpleDraweeView;

import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;


public class OthersFragment extends Fragment {

    private static final String TAG = OthersFragment.class.getName();

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
        final MainActivity mainActivity = (MainActivity) getActivity();
        User user = mainActivity.getUser();

        final EditText edt_isbn = view.findViewById(R.id.edit_isbn);
        Button btn_search = view.findViewById(R.id.btn_search_book);
        ImageButton btn_scan = view.findViewById(R.id.btn_to_scan);
        SimpleDraweeView userImage = view.findViewById(R.id.img_user);
        TextView userName = view.findViewById(R.id.tv_user);

        /* set image and name of user */
        String imageUri = "res://" + mainActivity.getPackageName() + "/"
                + ContactAdapter.getResID().get(getPicIdx(user.getName()));
        userImage.setImageURI(imageUri);
        userName.setText(user.getName());

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.startScanner();
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String isbn = edt_isbn.getText().toString().trim();
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


/*
*   在 fragment 中启动扫描，但在 MainActivity 中相应回调
* */