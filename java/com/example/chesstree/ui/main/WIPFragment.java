package com.example.chesstree.ui.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chesstree.R;

public class WIPFragment extends Fragment {

    public WIPFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static WIPFragment newInstance() {
        WIPFragment fragment = new WIPFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wip, container, false);
    }
}
