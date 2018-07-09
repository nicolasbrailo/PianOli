package com.nicobrailo.pianoli;

class Piano {
    class KeyArea {
        int x_i, x_f, y_i, y_f;
        KeyArea(int x_i, int x_f, int y_i, int y_f) {
            this.x_i = x_i;
            this.x_f = x_f;
            this.y_i = y_i;
            this.y_f = y_f;
        }
    };

    private static final double KEYS_FLAT_HEIGHT_RATIO = 0.55;
    private static final int KEYS_WIDTH = 220;
    private static final int KEYS_FLAT_WIDTH = 130;

    private final int keys_height;
    private final int keys_flats_height;

    private final int keys_count;
    private boolean key_pressed[];

    Piano(int screen_size_x, int screen_size_y) {
        keys_height = screen_size_y;
        keys_flats_height = (int) (screen_size_y * KEYS_FLAT_HEIGHT_RATIO);

        // Round up for possible half-key display
        keys_count = 1 + (screen_size_x / KEYS_WIDTH);

        key_pressed = new boolean[keys_count];
        for (int i=0; i < key_pressed.length; ++i) key_pressed[i] = false;
    }

    public int get_keys_count() {
        return keys_count;
    }

    public int pos_to_key_idx(float pos_x, float pos_y) {
        final int big_key_idx = (int) pos_x / KEYS_WIDTH;
        if (pos_y > keys_flats_height) return big_key_idx;
        return 0;
    }

    public boolean is_key_pressed(int key_idx) {
        return key_pressed[key_idx];
    }

    public void on_key_down(int key_idx) {
        key_pressed[key_idx] = true;
    }

    public void on_key_up(int key_idx) {
        key_pressed[key_idx] = false;
    }

    public KeyArea get_area_for_key(int key_idx) {
        int x_i = key_idx * KEYS_WIDTH;
        return new KeyArea(x_i,  x_i + KEYS_WIDTH, 0, keys_height);
    }

    public KeyArea get_area_for_flat_key(int key_idx) {
        final int octave_idx = key_idx % 7;
        if (octave_idx == 2 || octave_idx == 6) {
            return null;
        }

        final int offset = KEYS_WIDTH - (KEYS_FLAT_WIDTH / 2);
        int x_i = key_idx * KEYS_WIDTH + offset;
        return new KeyArea(x_i, x_i + KEYS_FLAT_WIDTH, 0, keys_flats_height);
    }
};
