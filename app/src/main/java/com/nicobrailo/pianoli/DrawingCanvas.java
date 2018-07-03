package com.nicobrailo.pianoli;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Map;


class DrawingCanvas extends SurfaceView implements SurfaceHolder.Callback {

    private final Point screen_size = new Point();

    Integer current_key_press = null;

    final int KEYS_WIDTH = 220;

    final int[][] KEY_COLORS = new int[][]{
            {148,   0, 211},    // Violet
            {75,    0, 130},    // Indigo
            {0,     0, 255},    // Blue
            {0,   255,   0},    // Green
            {255, 255,   0},    // Yellow
            {255, 127,   0},    // Orange
            {255,   0 ,  0},    // Red
    };

    void draw_keys(Canvas canvas) {

        /* Reset canvas */ {
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            canvas.drawPaint(p);
        }

        // Round up (draw half a key if needed)
        final int keys_count = 1 + (screen_size.x / KEYS_WIDTH);
        for (int i=0; i < keys_count; ++i) {
            Rect r = new Rect();
            r.left = i * KEYS_WIDTH;
            r.right = r.left + KEYS_WIDTH;
            r.top = 0;
            r.bottom = screen_size.y;

            final int col_idx = i % KEY_COLORS.length;
            Paint p = new Paint();
            final int alpha = (current_key_press != null && current_key_press == i)? 128 : 255;
            p.setARGB(alpha, KEY_COLORS[col_idx][0], KEY_COLORS[col_idx][1], KEY_COLORS[col_idx][2]);

            canvas.drawRect(r, p);
        }
    }

    public DrawingCanvas(AppCompatActivity context) {
        super(context);
        this.setFocusable(true);
        this.getHolder().addCallback(this);

        Display display = context.getWindowManager().getDefaultDisplay();
        display.getSize(screen_size);
        Log.d("XXXXXXXXX", "Display is " + screen_size.x + "x" + screen_size.y);
    }

    @Override
    public void draw(Canvas canvas)  {
        super.draw(canvas);
        Log.d("XXXXXXXXX", "DRAW");
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        redraw(surfaceHolder);
    }

    private void redraw() {
        redraw(getHolder());
    }

    public void redraw(SurfaceHolder surfaceHolder) {
        Canvas canvas = surfaceHolder.lockCanvas();
        draw_keys(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    void on_key_up(int key_idx) {
        Log.d("XXXXXXXXX", "Key " + key_idx + " is now UP");
    }

    void on_key_down(int key_idx) {
        Log.d("XXXXXXXXX", "Key " + key_idx + " is now DOWN");
    }

    Map<Integer, Integer> touch_pointer_to_keys = new HashMap<>();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int ptr_id = event.getPointerId(event.getActionIndex());
        // final int ptr_idx = event.findPointerIndex(ptr_id);
        // ptr_pos = event.getX(ptr_id), event.getY(ptr_id)
        final int key_idx = (int) event.getX(event.getActionIndex()) / KEYS_WIDTH;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:           // fallthrough
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (touch_pointer_to_keys.containsKey(ptr_id)) {
                    // throw new Exception("");
                    // XXX TODO
                    Log.e("XXXXXXXXX", "Bad things happened");
                }

                // Mark key down ptr_id
                touch_pointer_to_keys.put(ptr_id, key_idx);
                on_key_down(key_idx);

                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!touch_pointer_to_keys.containsKey(ptr_id)) {
                    // throw new Exception("");
                    // XXX TODO
                    Log.e("XXXXXXXXX", "Bad things happened");
                }

                // check if key changed
                if (touch_pointer_to_keys.get(ptr_id) != key_idx) {
                    touch_pointer_to_keys.put(ptr_id, key_idx);
                    on_key_up(touch_pointer_to_keys.get(ptr_id));
                    on_key_down(key_idx);
                }

                return true;
            }
            case MotionEvent.ACTION_POINTER_UP:     // fallthrough
            case MotionEvent.ACTION_UP: {
                if (!touch_pointer_to_keys.containsKey(ptr_id)) {
                    // throw new Exception("");
                    // XXX TODO
                    Log.e("XXXXXXXXX", "Bad things happened");
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
