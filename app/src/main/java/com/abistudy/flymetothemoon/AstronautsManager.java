package com.abistudy.flymetothemoon;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AstronautsManager {

    private final Context context;
    Random random = new Random();

    private List<Astronaut> astronauts;
    private int lastAstronautIndex = -1;
    public Astronaut currentAstronaut;

    public AstronautsManager(Context context) {
        this.context = context;
        astronauts = createAstronautsList();
        nextAstronaut();
    }


    private List<Astronaut> createAstronautsList() {
        List<Astronaut> list = new ArrayList<>();

        list.add(new Astronaut("Shiroe", "#e59f4", "astronaut_shiroe"));
        list.add(new Astronaut("Akemi", "#b12c2c", "astronaut_akemi"));
        list.add(new Astronaut("Uma", "#c25f28", "astronaut_ume"));
        list.add(new Astronaut("Hanae", "#d3b356", "astronaut_hanae"));
        list.add(new Astronaut("Midoriya", "#b3cf3e", "astronaut_midoriya"));
        list.add(new Astronaut("Aoi", "#89c0ed", "astronaut_aoi"));
        list.add(new Astronaut("Lunea", "#455eba", "astronaut_lunea"));
        list.add(new Astronaut("Kokona", "#694391", "astronaut_kokona"));
        list.add(new Astronaut("Ichika", "#c87faa", "astronaut_ichika"));
        list.add(new Astronaut("Tsukimi", "#a27869", "astronaut_tsukimi"));

        return list;
    }

    // Funzione per ottenere l'ID della risorsa Drawable (Ancora utile per caricare l'immagine!)
    public int getCurrentImageResourceId() {
        return context.getResources().getIdentifier(
                currentAstronaut.getImageName(), "drawable", context.getPackageName()
        );
    }

    //
    public void nextAstronaut() {
        int listLength = astronauts.size();
        int nextIndex;

        do {
            nextIndex = random.nextInt(listLength);
        } while (nextIndex == lastAstronautIndex);

        lastAstronautIndex = nextIndex;
        currentAstronaut = astronauts.get(nextIndex);
    }
}