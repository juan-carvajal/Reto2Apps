package com.example.reto2apps;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.reto2apps.adapters.PokemonAdapter;
import com.example.reto2apps.models.Pokemon;
import com.example.reto2apps.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeScreen extends AppCompatActivity {

    private static final String TAG = "HOME";
    private String username;

    private FirebaseFirestore db;

    private ArrayList<Pokemon> pokemons;

    private Context context;

    private User loggedUser;

    private PokemonAdapter adapter;

    private EditText pokemonName;

    private Button pokemonAdd;


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void searchPokemon(String pokemonName) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setTitle("Loading");
        progress.setMessage("Finding your pokemon...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();


        Pokemon poke = pokemons.stream()
                .filter(pokemon -> pokemonName.equals(pokemon.name))
                .findAny()
                .orElse(null);

        if (poke != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Error");
            builder.setMessage("You already have this pokemon!");
            builder.setPositiveButton("Ok", null);

            AlertDialog dialog = builder.create();
            dialog.show();
            progress.dismiss();
            return;
        }


        db.collection("pokemons")
                .whereEqualTo("name", pokemonName).limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                progress.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Error");
                                builder.setMessage("This pokemon does not exist!");
                                builder.setPositiveButton("Ok", null);

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {
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
                                progress.dismiss();
                                return;
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
                        }
                    }
                });

//        db.collection("pokemons").document(pokemonName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Pokemon pokemon = document.toObject(Pokemon.class);
//                        loggedUser.pokemons.add(pokemon);
//                        db.collection("users").document(loggedUser.username)
//                                .set(loggedUser)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        Log.d(TAG, "DocumentSnapshot successfully written!");
//                                        pokemons.clear();
//                                        pokemons.addAll(loggedUser.pokemons);
//                                        adapter.notifyDataSetChanged();
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                                        builder.setTitle("Error");
//                                        builder.setMessage("The pokemon got away!");
//                                        builder.setPositiveButton("Ok", null);
//
//                                        AlertDialog dialog = builder.create();
//                                        dialog.show();
//                                    }
//                                });
//
//                    } else {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                        builder.setTitle("Error");
//                        builder.setMessage("This pokemon does not exist!");
//                        builder.setPositiveButton("Ok", null);
//
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setTitle("Error");
//                    builder.setMessage("Unexpected error when reaching database");
//                    builder.setPositiveButton("Ok", null);
//
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                }
//
//                progress.dismiss();
//            }
//        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        context = this;
        this.pokemons = new ArrayList<Pokemon>();
        Log.d(TAG, "Initialized app");
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "created db instance");
        setContentView(R.layout.activity_home_screen);

        pokemonName = (EditText) findViewById(R.id.editTextTextPersonName2);
        pokemonAdd = (Button) findViewById(R.id.button2);
        pokemonAdd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                searchPokemon(pokemonName.getText().toString());
            }
        });


        RecyclerView rvPoke = (RecyclerView) findViewById(R.id.rvPoke);
        adapter = new PokemonAdapter(this.pokemons, this, username);
        rvPoke.setAdapter(adapter);
        rvPoke.setLayoutManager(new LinearLayoutManager(this));

        // adapter.notifyDataSetChanged();


        this.username = getIntent().getStringExtra("user_name");
        Log.d(TAG, "Current username: " + this.username);
        ProgressDialog progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setTitle("Loading");
        progress.setMessage("Finding your pokemon...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        DocumentReference docRef = db.collection("users").document(this.username);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        loggedUser = document.toObject(User.class);
                        pokemons.clear();
                        pokemons.addAll(loggedUser.pokemons);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        User user = new User();
                        user.username = username;
                        loggedUser = user;
                        db.collection("users").document(username).set(user);
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }

                progress.dismiss();
            }
        });
//        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
//                Pokemon poke = documentSnapshot.toObject(Pokemon.class);
//            }
//        });


        //        ProgressDialog progress = new ProgressDialog(this);
//        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progress.setTitle("Loading");
//        progress.setMessage("Wait while loading...");
//        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
//        progress.show();
    }
}