package com.jamieadkins.heistr.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.jamieadkins.heistr.BuildObjects.Build;
import com.jamieadkins.heistr.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jamie on 04/09/2015.
 */
public class NewBuildDialog extends DialogFragment {

    ArrayList<Build> buildList;

    public static NewBuildDialog newInstance(ArrayList<Build> builds) {
        NewBuildDialog dialog = new NewBuildDialog();
        dialog.buildList = new ArrayList<>();
        dialog.buildList.addAll(builds);
        return dialog;
    }

    public interface NewBuildDialogListener {
        public void onDialogNewBuild(DialogFragment dialog, String name, int infamies, String pd2SkillsURL, int templateBuildPos);
    }

    NewBuildDialogListener mListener;


    public NewBuildDialog() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_new_build, null);

        builder.setView(v);


        Spinner spTemplate = (Spinner) v.findViewById(R.id.spTemplate);
        List<String> list = new ArrayList<>();
        list.add("Select Template");

        for (Build b : buildList) {
            list.add(b.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spTemplate.setAdapter(adapter);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        builder.setTitle("New Build")
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //Name
                        EditText etName = (EditText) getDialog().findViewById(R.id.etNewBuildName);
                        final String name = etName.getText().toString();

                        //Infamies
                        CheckBox cb = (CheckBox) getDialog().findViewById(R.id.cbInfamies);
                        int infamies = 1;
                        if (cb.isChecked()) {
                            infamies = 16;
                        }

                        //URL
                        EditText etURL = (EditText) getDialog().findViewById(R.id.etPD2SkillsURL);
                        final String url = etURL.getText().toString();

                        //Template
                        Spinner spTemplate = (Spinner) getDialog().findViewById(R.id.spTemplate);
                        int selected = spTemplate.getSelectedItemPosition() - 1; // -1 for templete text


                        mListener.onDialogNewBuild(NewBuildDialog.this, name, infamies, url, selected);

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });


        // Create the AlertDialog object and return it
        return builder.create();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NewBuildDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }


}
