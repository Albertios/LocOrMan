package de.unimuenster.ifgi.locormandemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    Button app1, app2, app3;
    RelativeLayout layout;
    TextView textView;
    ProgressBar progressBar;
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Button demoButton = (Button) findViewById(R.id.demoButton);
        Button adminButton = (Button) findViewById(R.id.adminButton);
        Button settingsButton = (Button) findViewById(R.id.settingsButton);
        Button setValues = (Button) findViewById(R.id.setValues);
        app1 = (Button) findViewById(R.id.app1);
        app2 = (Button) findViewById(R.id.app2);
        app3 = (Button) findViewById(R.id.app3);

        layout = (RelativeLayout) findViewById(R.id.layout);
        textView = (TextView) findViewById(R.id.textView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);


        demoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start map intent
                startMapActivity();
            }
        });

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start admin panel intent
                startAdminActivity();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start settings menu intent
                startSettingsActivity();
            }
        });

        setValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start map intent
                startValuesActivity();
            }
        });

        app1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                textView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        textView.setText("changed to app 1");
                        layout.setBackgroundColor(Color.RED);
                        progressBar.setVisibility(View.INVISIBLE);

                    }
                }, 1000);

                progressBar.setVisibility(View.VISIBLE);



            }
        });

        app2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                textView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                layout.setBackgroundColor(Color.BLUE);
                textView.setText("changed to app 2");
                        progressBar.setVisibility(View.INVISIBLE);

                    }
                }, 1000);


                progressBar.setVisibility(View.VISIBLE);
            }
        });

        app3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                textView.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                layout.setBackgroundColor(Color.GREEN);
                textView.setText("changed to app 3");
                        progressBar.setVisibility(View.INVISIBLE);

                }
            }, 1000);


                progressBar.setVisibility(View.VISIBLE);


            }
        });





    }








    private void startMapActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void startAdminActivity() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    private void startValuesActivity() {
        Intent intent = new Intent(getBaseContext(), SetValues.class);
        startActivity(intent);
    }

    private void startApp1() {

    }




}
