package com.example.chesstree.ui.main;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.chesstree.MainActivity;
import com.example.chesstree.Node;
import com.example.chesstree.R;

import java.util.ArrayList;

public class NoteEditorFragment extends DialogFragment {

    private static final String ARG_PARAM1 = "Node Position";
    private static final String ARG_PARAM2 = "Node Move";

    private String mParam1;
    private char mParam2;
    private Node node = null;
    private boolean error = false;

    public NoteEditorFragment() {
        // Required empty public constructor
    }

    public static NoteEditorFragment newInstance(String param1, char param2) {
        NoteEditorFragment fragment = new NoteEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putChar(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getChar(ARG_PARAM2);
            for(int i=0; i< MainActivity.nodesList.size(); i++) {

                if(MainActivity.nodesList.get(i).equals(mParam1, mParam2))
                {node = MainActivity.nodesList.get(i); break;}
            }
            if(node == null) {
                Log.e("NodeNotFound", "Node Not Found");
                error = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_note_editor, container, false);
        EditText textView = root.findViewById(R.id.editText);
        EditText et1 = root.findViewById(R.id.editText2);
        if(error) {
            textView.setText(R.string.nodeerror);
            return root;
        }
        if(!node.getNote().equals("")) textView.setText(node.getNote());
        textView.addTextChangedListener(new NoteWatcher());

        StringBuilder tagdisplay = new StringBuilder();

        for(int i=0; i<node.getTags().size(); i++) {

            if(node.getTags().get(i).equals("")) continue;
            tagdisplay.append(node.getTags().get(i));
            tagdisplay.append(", ");
        }

        String tagtext = new String(tagdisplay);

        if(!tagtext.equals("")) et1.setText(tagtext);
        et1.addTextChangedListener(new TagWatcher());
        return root;
    }

    private class NoteWatcher implements TextWatcher {

        private NoteWatcher() {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            node.setNote(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class TagWatcher implements TextWatcher {

        private TagWatcher() {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            ArrayList<String> tags = new ArrayList<>();
            String l = s.toString();
            String[] str = l.split("\\s|,");

            for(int i=0; i<str.length; i++) {

                tags.add(str[i]);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
