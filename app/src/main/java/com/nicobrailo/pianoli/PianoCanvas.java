package com.nicobrailo.pianoli;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Map;


class PianoCanvas extends SurfaceView implements SurfaceHolder.Callback {

    final Piano piano;

    // Change in color when pressing a key
    final int KEY_COLOR_PRESS_DELTA = 60;
    final int[][] KEY_COLORS = new int[][]{
            {148,   0, 211},    // Violet
            {75,    0, 130},    // Indigo
            {0,     0, 255},    // Blue
            {0,   255,   0},    // Green
            {255, 255,   0},    // Yellow
            {255, 127,   0},    // Orange
            {255,   0 ,  0},    // Red
    };

    public PianoCanvas(Context context, AttributeSet as, int defStyle) {
        this(context, as);
    }

    public PianoCanvas(Context context, AttributeSet as) {
        this(context);
    }

    public PianoCanvas(Context context) {
        super(context);
        this.setFocusable(true);
        this.getHolder().addCallback(this);

        final Point screen_size = new Point();
        final AppCompatActivity ctx;
        try {
            ctx = (AppCompatActivity) context;
            Display display = ctx.getWindowManager().getDefaultDisplay();
            display.getSize(screen_size);
        } catch (ClassCastException ex) {
            Log.e("PianOli::DrawingCanvas", "Can't read screen size");
            throw ex;
        }

        this.piano = new Piano(context, screen_size.x, screen_size.y);

        Log.d("PianOli::DrawingCanvas", "Display is " + screen_size.x + "x" + screen_size.y +
                                  ", there are " + piano.get_keys_count() + " keys");
    }

    void draw_all_keys(final Canvas canvas) {
        /* Reset canvas */ {
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            canvas.drawPaint(p);
        }

        for (int i=0; i < piano.get_keys_count(); i+=2) {
            // Draw big key
            final int col_idx = (i/2) % KEY_COLORS.length;
            Paint big_key_paint = new Paint();
            final int d = piano.is_key_pressed(i) ? KEY_COLOR_PRESS_DELTA : 0;
            big_key_paint.setARGB(255, Math.max(KEY_COLORS[col_idx][0] - d, 0),
                    Math.max(KEY_COLORS[col_idx][1] - d, 0),
                    Math.max(KEY_COLORS[col_idx][2] - d, 0));
            draw_key(canvas, piano.get_area_for_key(i), big_key_paint);
        }

        // Small keys drawn after big keys to ensure z-index
        for (int i=1; i < piano.get_keys_count(); i+=2) {
            // Draw small key
            Paint flat_key_paint = new Paint();
            flat_key_paint.setColor(piano.is_key_pressed(i)? Color.GRAY : Color.BLACK);
            if (piano.get_area_for_flat_key(i) != null) {
                draw_key(canvas, piano.get_area_for_flat_key(i), flat_key_paint);
            }
        }

        Drawable icon = ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_preferences);
        draw_icon_on_key(canvas, icon, piano.get_area_for_flat_key(1), 70, 70);
        draw_icon_on_key(canvas, icon, piano.get_area_for_flat_key(6), 70, 70);
        draw_icon_on_key(canvas, icon, piano.get_area_for_flat_key(8), 70, 70);
    }

    void draw_key(final Canvas canvas, final Piano.Key rect, final Paint p) {
        Rect r = new Rect();
        r.left = rect.x_i;
        r.right = rect.x_f;
        r.top = rect.y_i;
        r.bottom = rect.y_f;
        canvas.drawRect(r, p);
    }

    void draw_icon_on_key(final Canvas canvas, final Drawable icon, final Piano.Key key,
                            final int icon_width, final int icon_height) {
        int icon_x = ((key.x_f - key.x_i) / 2) + key.x_i;
        int icon_y = 30;

        Rect r = new Rect();
        r.left = icon_x - (icon_width / 2);
        r.right = icon_x + (icon_width / 2);
        r.top = icon_y;
        r.bottom = icon_y + icon_height;

        icon.setBounds(r);
        icon.draw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        redraw(surfaceHolder);
    }

    public void redraw() {
        redraw(getHolder());
    }

    public void redraw(SurfaceHolder surfaceHolder) {
        Canvas canvas = surfaceHolder.lockCanvas();
        draw_all_keys(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    void on_key_up(int key_idx) {
        Log.d("PianOli::DrawingCanvas", "Key " + key_idx + " is now UP");
        piano.on_key_up(key_idx);
        redraw();
    }

    void on_key_down(int key_idx) {
        Log.d("PianOli::DrawingCanvas", "Key " + key_idx + " is now DOWN");
        piano.on_key_down(key_idx);
        redraw();
    }

    Map<Integer, Integer> touch_pointer_to_keys = new HashMap<>();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int ptr_id = event.getPointerId(event.getActionIndex());
        // final int ptr_idx = event.findPointerIndex(ptr_id);
        // ptr_pos = event.getX(ptr_id), event.getY(ptr_id)
        final int key_idx = piano.pos_to_key_idx(event.getX(event.getActionIndex()),
                                                 event.getY(event.getActionIndex()));

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:           // fallthrough
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (touch_pointer_to_keys.containsKey(ptr_id)) {
                    Log.e("PianOli::DrawingCanvas", "Touch-track error: Repeated touch-down event received");
                    return super.onTouchEvent(event);
                }

                // Mark key down ptr_id
                touch_pointer_to_keys.put(ptr_id, key_idx);
                on_key_down(key_idx);

                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!touch_pointer_to_keys.containsKey(ptr_id)) {
                    Log.e("PianOli::DrawingCanvas", "Touch-track error: Missed touch-up event");
                    return super.onTouchEvent(event);
                }

                // check if key changed
                if (touch_pointer_to_keys.get(ptr_id) != key_idx) {
                    Log.d("PianOli::DrawingCanvas", "Moved to another key");
                    // Release key before storing new key_idx for new key down
                    on_key_up(touch_pointer_to_keys.get(ptr_id));
                    touch_pointer_to_keys.put(ptr_id, key_idx);
                    on_key_down(key_idx);
                }

                return true;
            }
            case MotionEvent.ACTION_POINTER_UP:     // fallthrough
            case MotionEvent.ACTION_UP: {
                if (!touch_pointer_to_keys.containsKey(ptr_id)) {
                    Log.e("PianOli::DrawingCanvas", "Touch-track error: Repeated touch-up event received");
                    return super.onTouchEvent(event);
                }

                touch_pointer_to_keys.remove(ptr_id);
                on_key_up(key_idx);

                return true;
            }

            default:
                return super.onTouchEvent(event);
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

}
