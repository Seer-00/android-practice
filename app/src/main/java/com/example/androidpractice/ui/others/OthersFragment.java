package com.example.androidpractice.ui.others;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.androidpractice.ISBN.toScanActivity;
import com.example.androidpractice.MainActivity;
import com.example.androidpractice.R;


public class OthersFragment extends Fragment {

    private OthersViewModel othersViewModel;

    private Button btn_scan;

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

        return root;
    }
}