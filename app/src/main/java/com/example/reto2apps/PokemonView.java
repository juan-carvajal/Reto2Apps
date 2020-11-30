package com.example.reto2apps;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.reto2apps.models.Pokemon;
import com.example.reto2apps.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PokemonView extends AppCompatActivity {

    private static final String TAG = "POKEMON_VIEW";
    private TextView atcTxt;
    private TextView dfsTxt;
    private TextView hpTxt;
    private TextView speedTxt;
    private TextView nameTxt;

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
        hpTxt = findViewById(R.id.hp_txt);
        speedTxt = findViewById(R.id.spd_txt);
        nameTxt = findViewById(R.id.name_txt);
        releaseBtn = findViewById(R.id.release_btn);

        this.username = getIntent().getStringExtra("user_name");
        Log.d(TAG, "USERNAME: " + username);
        this.pokeID = getIntent().getLongExtra("pokemon_id", 1);

        //FirebaseApp.initializeApp(this);
        context = this;

        db = FirebaseFirestore.getInstance();

        ProgressDialog progress = new ProgressDialog(context);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setTitle("Loading");
        progress.setMessage("Fetching pokemon...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        Log.d(TAG, pokeID + "");
        db.collection("pokemons").document(pokeID + "").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        progress.dismiss();
                        Pokemon pokemon = document.toObject(Pokemon.class);
                        ImageView img = findViewById(R.id.imageView2);
                        if (pokemon.sprites.size() > 0) {
                            Glide
                                    .with(context)
                                    .load(pokemon.sprites.get(pokemon.sprites.size() - 1))
                                    .apply(new RequestOptions()
                                            .fitCenter()
                                            .format(DecodeFormat.PREFER_ARGB_8888)
                                            .override(Target.SIZE_ORIGINAL))
                                    .placeholder(R.drawable.pokeapi_256)
                                    .into(img);
                        }
                        atcTxt.setText(pokemon.stats.get("Attack") + "");
                        dfsTxt.setText(pokemon.stats.get("Defense") + "");
                        hpTxt.setText(pokemon.stats.get("Hp") + "");
                        speedTxt.setText(pokemon.stats.get("Speed") + "");
                        nameTxt.setText(pokemon.name);
                    } else {
                        progress.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Error");
                        builder.setMessage("The pokemon seems to be lost!");
                        builder.setPositiveButton("Ok", null);

                        AlertDialog dialog = builder.create();
                        dialog.show();

                        Intent intent = new Intent(getBaseContext(), Login.class);
                        startActivity(intent);
                    }
                } else {
                    progress.dismiss();
                    Log.d(TAG, "get failed with ", task.getException());
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Error");
                    builder.setMessage("Unexpected error when reaching database");
                    builder.setPositiveButton("Ok", null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
//
                    Intent intent = new Intent(getBaseContext(), Login.class);
                    startActivity(intent);
                }
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


                db.collection("users").document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                progress.dismiss();
                                User user = document.toObject(User.class);
                                Pokemon poke = user.pokemons.stream()
                                        .filter(pokemon -> pokeID == pokemon.id)
                                        .findAny()
                                        .orElse(null);

                                if (poke != null) {
                                    user.pokemons.remove(poke);
                                }
                                Log.d(TAG, user.username);
                                db.collection("users").document(user.username)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                progress.dismiss();
                                                Intent intent = new Intent(getBaseContext(), HomeScreen.class);
                                                intent.putExtra("user_name", user.username);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                builder.setTitle("Error");
                                                builder.setMessage("The pokemon does not want to be released!");
                                                builder.setPositiveButton("Ok", null);

                                                AlertDialog dialog = builder.create();
                                                dialog.show();

                                                progress.dismiss();
                                                Intent intent = new Intent(getBaseContext(), HomeScreen.class);
                                                intent.putExtra("user_name", user.username);
                                                startActivity(intent);
                                            }
                                        });
                            } else {
                                progress.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Error");
                                builder.setMessage("The pokemon seems to be lost!");
                                builder.setPositiveButton("Ok", null);

                                AlertDialog dialog = builder.create();
                                dialog.show();

                                Intent intent = new Intent(getBaseContext(), Login.class);
                                startActivity(intent);
                            }
                        } else {
                            progress.dismiss();
                            Log.d(TAG, "get failed with ", task.getException());
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Error");
                            builder.setMessage("Unexpected error when reaching database");
                            builder.setPositiveButton("Ok", null);

                            AlertDialog dialog = builder.create();
                            dialog.show();
//
                            Intent intent = new Intent(getBaseContext(), Login.class);
                            startActivity(intent);
                        }
                    }
                });


            }
        });

    }
}