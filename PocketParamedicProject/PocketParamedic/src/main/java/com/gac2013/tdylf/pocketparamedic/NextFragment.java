package com.gac2013.tdylf.pocketparamedic;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class NextFragment extends Fragment implements ContinuousSpeechRecognizer.RecognizedTextListener {

    private ContinuousSpeechRecognizer csr;
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        FragmentManager fm = getFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++)
            Log.e(getClass().getName(), i + " -> " + fm.getBackStackEntryAt(i).getName());

        //if (fm.getBackStackEntryCount() > 1 &&
          //      fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 2).getName().equals("instructions"))
            //fm.popBackStack();

        State currentState = StateMachine.getCurrentState();
        ViewGroup vg = (ViewGroup)inflater.inflate(R.layout.instructions, container, false);
        ((TextView)vg.findViewById(R.id.tvInstr)).setText("" + currentState.getId());
        ((ImageView)vg.findViewById(R.id.ivInstr)).setImageResource(currentState.getImageResId());
        return vg;
    }

    @Override
    public void onResume() {
        super.onResume();
        context = getActivity().getApplicationContext();
        csr = new ContinuousSpeechRecognizer(context);
        csr.setListener(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                csr.startRecognition();
            }
        }, 2000);

    }

    @Override
    public void onStop() {
        super.onStop();
        csr.stopRecognition();
    }

    @Override
    public void onResults(ArrayList<String> results) {
        if (results.contains("yes")) {
            Toast.makeText(context, "yes", Toast.LENGTH_SHORT).show();
            performYesTransition();

        } else if (results.contains("no")) {
            Toast.makeText(context, "no", Toast.LENGTH_SHORT).show();
            performNoTransition();
        }
    }

    private void performYesTransition() {
        int state = StateMachine.getCurrentState().getYesAnswered();
        StateMachine.setCurrentState(state);
        ((MainActivity)getActivity()).setupInstructionFragment();
    }

    private void performNoTransition() {
        int state = StateMachine.getCurrentState().getNoAnswered();
        StateMachine.setCurrentState(state);
        ((MainActivity)getActivity()).setupInstructionFragment();
    }
}
