package com.nicobrailo.pianoli;

/**
 * Area taken up by a Piano key.
 *
 * <p>
 * for both X and Y, <code>i <= f</code> (you can have keys with zero width/height, e.g. for the non-existing flats.
 * </p>
 */
class Key {
    /** null-object for keys: zero-area, so cannot be touched. (It's Hammer Time!) */
    public static final Key CANT_TOUCH_THIS = new Key(0, 0, 0, 0);

    /** Height of a flat key in relation to a regular key. */
    public static final double FLAT_HEIGHT_RATIO = 0.55;
    /** Width of a flat key in relation to a regular key. */
    public static final double FLAT_WIDTH_RATIO = 0.6;

    int x_i, x_f, y_i, y_f;

    Key(int x_i, int x_f, int y_i, int y_f) {
        this.x_i = x_i;
        this.x_f = x_f;
        this.y_i = y_i;
        this.y_f = y_f;
    }

    boolean contains(float pos_x, float pos_y) {
        return (pos_x > x_i && pos_x < x_f) &&
                (pos_y > y_i && pos_y < y_f);
    }
}
