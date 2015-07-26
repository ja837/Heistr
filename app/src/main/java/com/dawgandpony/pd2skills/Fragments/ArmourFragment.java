package com.dawgandpony.pd2skills.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dawgandpony.pd2skills.Activities.EditBuildActivity;
import com.dawgandpony.pd2skills.R;



import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArmourFragment extends Fragment {

    ListView lvArmour;



    EditBuildActivity activity;


    public ArmourFragment() {
        // Required empty public constructor
    }

    public static ArmourFragment newInstance() {

        Bundle args = new Bundle();

        ArmourFragment fragment = new ArmourFragment();

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (EditBuildActivity) getActivity();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_armour, container, false);
        lvArmour = (ListView) rootView.findViewById(R.id.lvArmour);

        ArrayList<String> armours = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.armour)));

        ArrayAdapter<String> mAdapter2 = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_single_choice, armours);
        lvArmour.setAdapter(mAdapter2);
        lvArmour.setItemChecked(activity.getCurrentBuild().getArmour(), true);
        lvArmour.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selected = lvArmour.getCheckedItemPosition();
                activity.updateArmour(selected);

            }
        });


        return  rootView;
    }


}
