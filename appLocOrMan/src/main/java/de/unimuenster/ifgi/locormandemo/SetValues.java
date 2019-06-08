package de.unimuenster.ifgi.locormandemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.unimuenster.ifgi.locormandemo.manipulations.ExperimentProvider;

public class SetValues extends AppCompatActivity {

    EditText getDisAcc;
    EditText getOrDelay;
    EditText getLoDelay;
    EditText getOrAcc;
    EditText getLoAcc;


    TextView setDisAcc;
    TextView setOrDelay;
    TextView setLoDelay;
    TextView setOrAcc;
    TextView setLoAcc;



    Button changeBtn;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_vaules);



        this.getDisAcc = (EditText) findViewById(R.id.getDisAcc);
        this.setDisAcc = (TextView) findViewById(R.id.setDisAcc);
        setDisAcc.setText(ExperimentProvider.MyClass.dispAccManipValue + "m");




        this.getOrDelay = (EditText) findViewById(R.id.getOrDelay);
        this.setOrDelay = (TextView) findViewById(R.id.setOrDelay);
        setOrDelay.setText(ExperimentProvider.MyClass.orManipRecency01Value + "s");




        this.getLoDelay = (EditText) findViewById(R.id.getLoDelay);
        this.setLoDelay = (TextView) findViewById(R.id.setLoDelay);
        setLoDelay.setText(ExperimentProvider.MyClass.recencyManip02Value + "s");





        this.getOrAcc = (EditText) findViewById(R.id.getOrAcc);
        this.setOrAcc = (TextView) findViewById(R.id.setOrAcc);
        setOrAcc.setText(ExperimentProvider.MyClass.orManipSystAcc01Value + "°");




        this.getLoAcc = (EditText) findViewById(R.id.getLoAcc);
        this.setLoAcc = (TextView) findViewById(R.id.setLoAcc);
        setLoAcc.setText(ExperimentProvider.MyClass.systAccManipVaule + "m");










        this.changeBtn = (Button) findViewById(R.id.changeBtn);


        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setDisAcc.setText(getDisAcc.getText().toString()+ "m");
                ExperimentProvider.MyClass.dispAccManipValue = getDisAcc.getText().toString();


                setOrDelay.setText(getOrDelay.getText().toString()+ "s");
                ExperimentProvider.MyClass.orManipRecency01Value = getOrDelay.getText().toString();


                setLoDelay.setText(getLoDelay.getText().toString()+ "s");
                ExperimentProvider.MyClass.recencyManip02Value = getLoDelay.getText().toString();


                setOrAcc.setText(getOrAcc.getText().toString()+ "°");
                ExperimentProvider.MyClass.orManipSystAcc01Value = getOrAcc.getText().toString();


                setLoAcc.setText(getLoAcc.getText().toString()+ "m");
                ExperimentProvider.MyClass.systAccManipVaule = getLoAcc.getText().toString();


            }
        });






     }



}



