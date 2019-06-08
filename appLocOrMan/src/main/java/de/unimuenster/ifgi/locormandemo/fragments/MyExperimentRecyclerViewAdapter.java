package de.unimuenster.ifgi.locormandemo.fragments;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import de.unimuenster.ifgi.locormandemo.R;
import de.unimuenster.ifgi.locormandemo.fragments.ExperimentFragment.OnListFragmentInteractionListener;
import de.unimuenster.ifgi.locormandemo.manipulations.Experiment;
import de.unimuenster.ifgi.locormandemo.manipulations.Manipulation;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Manipulation} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyExperimentRecyclerViewAdapter extends RecyclerView.Adapter<MyExperimentRecyclerViewAdapter.ViewHolder> {

    private final List<Manipulation> mValues;
    private final Experiment mExperiment;
    private final OnListFragmentInteractionListener mListener;


    public MyExperimentRecyclerViewAdapter(Experiment experiment, OnListFragmentInteractionListener listener) {
        mValues = experiment.getManipulationList();
        mExperiment = experiment;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_experiment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) { /*TODO albert platzhalter */
        holder.mManipulation = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getId());
        holder.mNameView.setText(mValues.get(position).getName());
        holder.mSwitchCompatView.setChecked(mValues.get(position).getState());


        holder.mSwitchCompatView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mListener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that a manipulation has been turned on or off.
                    holder.mManipulation.setState(b);
                    mExperiment.setStateOfManipulation(holder.mManipulation.getId(),b);
                    mListener.onListFragmentInteraction(mExperiment);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mNameView;
        public final SwitchCompat mSwitchCompatView;
        public Manipulation mManipulation;




        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.manipulationID);
            mNameView = (TextView) view.findViewById(R.id.manipulationName);
            mSwitchCompatView = (SwitchCompat) view.findViewById(R.id.stateSwitch);




        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdView.getText() + "'" ;
        }
    }



}
