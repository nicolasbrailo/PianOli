package com.nicobrailo.pianoli;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import java.util.HashMap;
import java.util.Map;

class PianoCanvas extends SurfaceView implements SurfaceHolder.Callback {

    private static final int BORDER_WIDTH = 24;

    Piano piano;
    final AppConfigTrigger appConfigHandler;
    final int screen_size_y, screen_size_x;

    // Change in color when pressing a key
    final int KEY_COLOR_PRESS_DELTA = 60;
    final int[][] KEY_COLORS = new int[][]{
            {148, 0, 211},    // Violet
            {75, 0, 130},    // Indigo
            {0, 0, 255},    // Blue
            {0, 255, 0},    // Green
            {255, 255, 0},    // Yellow
            {255, 127, 0},    // Orange
            {255, 0, 0},    // Red
    };
    Map<Integer, Integer> touch_pointer_to_keys = new HashMap<>();

    public PianoCanvas(Context context, AttributeSet as) {
        this(context, as, 0);
    }

    public PianoCanvas(Context context, AttributeSet as, int defStyle) {
        super(context, as, defStyle);
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

        screen_size_x = screen_size.x;
        screen_size_y = screen_size.y;
        final String soundset = Preferences.selectedSoundSet(context);
        this.piano = new Piano(context, screen_size_x, screen_size_y, soundset);
        this.appConfigHandler = new AppConfigTrigger(ctx);

        Log.d("PianOli::DrawingCanvas", "Display is " + screen_size.x + "x" + screen_size.y +
                ", there are " + piano.get_keys_count() + " keys");
    }

    public void selectSoundset(final Context context, final String selected_soundset) {
        this.piano = new Piano(context, screen_size_x, screen_size_y, selected_soundset);
    }

    public void setConfigRequestCallback(AppConfigTrigger.AppConfigCallback cb) {
        this.appConfigHandler.setConfigRequestCallback(cb);
    }

    void draw_all_keys(final Canvas canvas) {
        /* Reset canvas */
        {
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            canvas.drawPaint(p);
        }

        for (int i = 0; i < piano.get_keys_count(); i += 2) {
            // Draw big key
            final int col_idx = (i / 2) % KEY_COLORS.length;
            Paint big_key_paint = new Paint();
            final int d = piano.is_key_pressed(i) ? KEY_COLOR_PRESS_DELTA : 0;
            big_key_paint.setARGB(255, Math.max(KEY_COLORS[col_idx][0] - d, 0),
                    Math.max(KEY_COLORS[col_idx][1] - d, 0),
                    Math.max(KEY_COLORS[col_idx][2] - d, 0));
            draw_key(canvas, piano.get_area_for_key(i), big_key_paint);
        }

        // Small keys drawn after big keys to ensure z-index
        for (int i = 1; i < piano.get_keys_count(); i += 2) {
            // Draw small key
            Paint flat_key_paint = new Paint();
            flat_key_paint.setColor(piano.is_key_pressed(i) ? Color.GRAY : 0xFF333333);
            if (piano.get_area_for_flat_key(i) != null) {
                draw_key(canvas, piano.get_area_for_flat_key(i), flat_key_paint);
            }
        }

        appConfigHandler.onPianoRedrawFinish(this, canvas);
    }

    void draw_key(final Canvas canvas, final Piano.Key rect, final Paint p) {
        // Draw the main (solid) background of the key.

        Rect r = new Rect();
        r.left = rect.x_i;
        r.right = rect.x_f;
        r.top = rect.y_i;
        r.bottom = rect.y_f;
        canvas.drawRect(r, p);

        // Now draw the bevels around the edge of each key.
        // Just the left, bottom, and right. The top of the key doesn't have a bevel.

        // Adjust this colour brighter or darker for the bevel.
        int base = p.getColor();

        // Left bevel
        // +---+
        // |   |
        // |   |
        // |   |
        // |   |
        // |   *
        // | *
        // *

        Path left = new Path();
        left.moveTo(r.left, r.top);
        left.lineTo(r.left, r.bottom);
        left.lineTo(r.left + BORDER_WIDTH, r.bottom - BORDER_WIDTH);
        left.lineTo(r.left + BORDER_WIDTH, r.top);
        left.lineTo(r.left, r.top);

        p.setColor(ColorUtils.blendARGB(base, Color.BLACK, 0.3f));
        canvas.drawPath(left, p);

        // Right bevel
        // +---+
        // |   |
        // |   |
        // |   |
        // |   |
        // *   |
        //   * |
        //     *

        Path right = new Path();
        right.moveTo(r.right, r.top);
        right.lineTo(r.right, r.bottom);
        right.lineTo(r.right - BORDER_WIDTH, r.bottom - BORDER_WIDTH);
        right.lineTo(r.right - BORDER_WIDTH, r.top);
        right.lineTo(r.right, r.top);

        p.setColor(ColorUtils.blendARGB(base, Color.WHITE, 0.2f));
        canvas.drawPath(right, p);

        //         Bottom bevel
        //          *---------*
        //       *                *
        //    *----------------------+

        Path bottom = new Path();
        bottom.moveTo(r.left, r.bottom);
        bottom.lineTo(r.right, r.bottom);
        bottom.lineTo(r.right - BORDER_WIDTH, r.bottom - BORDER_WIDTH);
        bottom.lineTo(r.left + BORDER_WIDTH, r.bottom - BORDER_WIDTH);
        bottom.lineTo(r.left, r.bottom);

        p.setColor(ColorUtils.blendARGB(base, Color.BLACK, 0.1f));
        canvas.drawPath(bottom, p);
    }

    /**
     * Draw something on a black key. Undefined if key_idx isn't black.
     */
    void draw_icon_on_black_key(final Canvas canvas, final Drawable icon, Integer key_idx,
                                final int icon_width, final int icon_height) {
        final Piano.Key key = piano.get_area_for_flat_key(key_idx);
        int icon_x = ((key.x_f - key.x_i) / 2) + key.x_i;
        int icon_y = 30;

        Rect r = new Rect();
        r.left = icon_x - (icon_width / 2);
        r.right = icon_x + (icon_width / 2);
        r.top = icon_y;
        r.bottom = icon_y + icon_height;

        icon.setBounds(r);
        icon.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
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
        if (surfaceHolder == null) return;
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) return;

        draw_all_keys(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    void on_key_up(int key_idx) {
        Log.d("PianOli::DrawingCanvas", "Key " + key_idx + " is now UP");
        piano.on_key_up(key_idx);
        appConfigHandler.onKeyUp(key_idx);
        redraw();
    }

    void on_key_down(int key_idx) {
        Log.d("PianOli::DrawingCanvas", "Key " + key_idx + " is now DOWN");
        piano.on_key_down(key_idx);
        appConfigHandler.onKeyPress(key_idx);
        redraw();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
        // Override this method to make linter happy
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int ptr_id = event.getPointerId(event.getActionIndex());
        // final int ptr_idx = event.findPointerIndex(ptr_id);
        // ptr_pos = event.getX(ptr_id), event.getY(ptr_id)
        final int key_idx = piano.pos_to_key_idx(event.getX(event.getActionIndex()),
                event.getY(event.getActionIndex()));

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_BUTTON_PRESS:   // fallthrough
                performClick();
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
