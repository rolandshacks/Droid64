package ui;

/**
 * Created by roland on 03.12.2016.
 */

public class Color {

    public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f);
    public static final Color WHITE = new Color(1.0f, 1.0f, 1.0f);

    public float red;
    public float green;
    public float blue;
    public float alpha;

    public Color() {
        set(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public Color(float red, float green, float blue) {
        set(red, green, blue, 1.0f);
    }

    public Color(float red, float green, float blue, float alpha) {
        set(red, green, blue, alpha);
    }

    public void set(float red, float green, float blue) {
        set(red, green, blue, 1.0f);
    }

    public void set(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
