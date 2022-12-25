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

    private static final float BEVEL_RATIO = 0.1f;

    private final float bevelWidth;

    Piano piano;
    final AppConfigTrigger appConfigHandler;
    final int screen_size_y, screen_size_x;

    final int[] KEY_COLORS = new int[]{
            Color.rgb(148, 0, 211),    // Violet
            Color.rgb(75, 0, 130),    // Indigo
            Color.rgb(0, 0, 255),    // Blue
            Color.rgb(0, 255, 0),    // Green
            Color.rgb(255, 255, 0),    // Yellow
            Color.rgb(255, 127, 0),    // Orange
            Color.rgb(255, 0, 0),    // Red
    };

    /**
     * Note that green and yellow have higher lightness than other colours, so adding just a little white doesn't have
     * the desired effect. That is why they have a larger proportion of white added in.
     */
    final int[] PRESSED_KEY_COLORS = new int[]{
            ColorUtils.blendARGB(KEY_COLORS[0], Color.WHITE, 0.5f),    // Violet
            ColorUtils.blendARGB(KEY_COLORS[1], Color.WHITE, 0.5f),    // Indigo
            ColorUtils.blendARGB(KEY_COLORS[2], Color.WHITE, 0.5f),    // Blue
            ColorUtils.blendARGB(KEY_COLORS[3], Color.WHITE, 0.7f),    // Green
            ColorUtils.blendARGB(KEY_COLORS[4], Color.WHITE, 0.8f),    // Yellow
            ColorUtils.blendARGB(KEY_COLORS[5], Color.WHITE, 0.6f),    // Orange
            ColorUtils.blendARGB(KEY_COLORS[6], Color.WHITE, 0.5f),    // Red
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
        this.bevelWidth = this.piano.get_keys_width() * BEVEL_RATIO;
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
            big_key_paint.setColor(piano.is_key_pressed(i) ? PRESSED_KEY_COLORS[col_idx] : KEY_COLORS[col_idx]);
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
        left.lineTo(r.left + bevelWidth, r.bottom - bevelWidth);
        left.lineTo(r.left + bevelWidth, r.top);
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
        right.lineTo(r.right - bevelWidth, r.bottom - bevelWidth);
        right.lineTo(r.right - bevelWidth, r.top);
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
        bottom.lineTo(r.right - bevelWidth, r.bottom - bevelWidth);
        bottom.lineTo(r.left + bevelWidth, r.bottom - bevelWidth);
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
        int icon_y = icon_height;

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

    void resetPianoState() {
        // Something has gone wrong with the piano or canvas state, and our state is out of sync
        // with the real state of the world (eg somehow we missed a touch down or up event).
        // Try to reset the state and hope the app survives.
        touch_pointer_to_keys.clear();;
        piano.resetState();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
        // Override this method to make linter happy
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int ptr_id = event.getPointerId(event.getActionIndex());
        int key_idx = piano.pos_to_key_idx(
                event.getX(event.getActionIndex()),
                event.getY(event.getActionIndex()));

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_BUTTON_PRESS:   // fallthrough
                performClick();
            case MotionEvent.ACTION_DOWN:           // fallthrough
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (touch_pointer_to_keys.containsKey(ptr_id)) {
                    Log.e("PianOli::DrawingCanvas", "Touch-track error: Repeated touch-down event received");
                    resetPianoState();
                    return super.onTouchEvent(event);
                }

                // Mark key down ptr_id
                touch_pointer_to_keys.put(ptr_id, key_idx);
                on_key_down(key_idx);

                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                // action_move is special, there is no *single* `getActionIndex`,
                // as *multiple* pointers could have moved, so we must check *each* pointer.
                // https://developer.android.com/develop/ui/views/touch-and-input/gestures/multi
                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    ptr_id = event.getPointerId(i);  // cf precalc'd ptr_id above switch
                    key_idx = piano.pos_to_key_idx(
                            event.getX(i),
                            event.getY(i));

                    if (!touch_pointer_to_keys.containsKey(ptr_id)) {
                        Log.e("PianOli::DrawingCanvas", "Touch-track error: Missed touch-up event");
                        resetPianoState();
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
                }

                return true;
            }
            case MotionEvent.ACTION_POINTER_UP:     // fallthrough
            case MotionEvent.ACTION_UP: {
                if (!touch_pointer_to_keys.containsKey(ptr_id)) {
                    Log.e("PianOli::DrawingCanvas", "Touch-track error: Repeated touch-up event received");
                    resetPianoState();
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
