package com.example.chesstree.ui.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chesstree.R;

public class PlansFragment extends Fragment {

    public PlansFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static PlansFragment newInstance() {
        PlansFragment fragment = new PlansFragment();
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
        return inflater.inflate(R.layout.fragment_plans, container, false);
    }

}
