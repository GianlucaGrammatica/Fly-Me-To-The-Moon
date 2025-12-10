package com.abistudy.flymetothemoon;
public class Astronaut {
    private String name;
    private String colorHex;
    private String imageName;

    public Astronaut(String name, String colorHex, String imageName) {
        this.name = name;
        this.colorHex = colorHex;
        this.imageName = imageName;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getImageName() {
        return imageName;
    }

    public int getColorInt() {
        return android.graphics.Color.parseColor(colorHex);
    }

}