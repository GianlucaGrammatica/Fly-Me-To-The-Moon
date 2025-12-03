package com.abistudy.flymetothemoon;

public class CompassHelper {

    public static final float ALPHA = 0.15f;

    public static float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            // Formula Low-Pass Filter: output[i] = output[i] + ALPHA * (input[i] - output[i])
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}