package com.example.reto2apps;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btn = findViewById(R.id.button);
        EditText txt = findViewById(R.id.editTextTextPersonName);

//        ProgressDialog progress = new ProgressDialog(this);
//        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progress.setTitle("Loading");
//        progress.setMessage("Wait while loading...");
//        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
//        progress.show();
// To dismiss the dialog
        //progress.dismiss();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("INFO",txt.getText().toString());
                if(txt.getText().toString().equals("")){
                    Snackbar.make(view, "Please fill your username", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                Intent intent = new Intent(getBaseContext(), HomeScreen.class);
                intent.putExtra("user_name", txt.getText().toString());
                startActivity(intent);

            }
        });
    }
}