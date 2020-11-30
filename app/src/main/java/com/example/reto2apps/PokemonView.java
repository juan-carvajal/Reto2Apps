package com.example.reto2apps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.reto2apps.models.Pokemon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PokemonView extends AppCompatActivity {

    private TextView atcTxt;
    private TextView dfsTxt;
    private TextView hpTxt;
    private TextView speedTxt;

    private String username;
    private long pokeID;
    private Context context;
    private FirebaseFirestore db;

    private Button releaseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_view);

        atcTxt = findViewById(R.id.att_txt);
        dfsTxt = findViewById(R.id.dfs_txt);
        hpTxt =findViewById(R.id.hp_txt);
        speedTxt = findViewById(R.id.spd_txt);

        releaseBtn = findViewById(R.id.release_btn);

        this.username = getIntent().getStringExtra("user_name");
        this.pokeID = getIntent().getLongExtra("pokemon_id",1);

        FirebaseApp.initializeApp(this);
        context = this;

        db = FirebaseFirestore.getInstance();

        ProgressDialog progress = new ProgressDialog(context);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setTitle("Loading");
        progress.setMessage("Fetching pokemon...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        db.collection("users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Pokemon pokemon = document.toObject(Pokemon.class);
                        loggedUser.pokemons.add(pokemon);
                        db.collection("users").document(loggedUser.username)
                                .set(loggedUser)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        pokemons.clear();
                                        pokemons.addAll(loggedUser.pokemons);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("Error");
                                        builder.setMessage("The pokemon got away!");
                                        builder.setPositiveButton("Ok", null);

                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                });

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Error");
                        builder.setMessage("This pokemon does not exist!");
                        builder.setPositiveButton("Ok", null);

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error");
                    builder.setMessage("Unexpected error when reaching database");
                    builder.setPositiveButton("Ok", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                progress.dismiss();
            }
        });


        releaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog progress = new ProgressDialog(context);
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setTitle("Loading");
                progress.setMessage("Releasing pokemon...");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();


            }
        });

        Intent intent = new Intent(getBaseContext(), MainActivity.class);
    }
}