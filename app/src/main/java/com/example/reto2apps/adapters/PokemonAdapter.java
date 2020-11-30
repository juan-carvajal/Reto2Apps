package com.example.reto2apps.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.reto2apps.HomeScreen;
import com.example.reto2apps.PokemonView;
import com.example.reto2apps.R;
import com.example.reto2apps.models.Pokemon;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class PokemonAdapter extends
        RecyclerView.Adapter<PokemonAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public Button messageButton;
        public ImageView imageView;


        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.poke_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.poke_name);
            messageButton = (Button) itemView.findViewById(R.id.message_button);
        }
    }

    private List<Pokemon> pokemons;
    private Activity activity;
    private String userName;

    public PokemonAdapter (List<Pokemon> pokemons , Activity activity , String userName){
        this.pokemons=pokemons;
        this.activity = activity;
        this.userName = userName;
    }


    @NonNull
    @Override
    public PokemonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View PokeView = inflater.inflate(R.layout.pokemon_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(PokeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonAdapter.ViewHolder holder, int position) {
        Pokemon pokemon = pokemons.get(position);
        holder.nameTextView.setText(pokemon.name);
        if(pokemon.sprites.size() > 0){
            Glide
                    .with(activity)
                    .load(pokemon.sprites.get(0))
                    .apply(new RequestOptions()
                            .fitCenter()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL))
                    .placeholder(R.drawable.pokeapi_256)
                    .into(holder.imageView);
        }

        holder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Snackbar.make(view, pokemon.name, Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                return;


                Intent intent = new Intent(activity.getBaseContext(), PokemonView.class);
                intent.putExtra("pokemon_id", pokemon.id);
                intent.putExtra("user_name", userName);
                activity.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return this.pokemons.size();
    }
}
