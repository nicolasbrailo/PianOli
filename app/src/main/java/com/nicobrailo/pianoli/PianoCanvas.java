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
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import com.nicobrailo.pianoli.melodies.Melody;
import com.nicobrailo.pianoli.melodies.MultipleSongsMelodyPlayer;
import com.nicobrailo.pianoli.sound.MelodicKeySoundMaker;
import com.nicobrailo.pianoli.sound.SampledSoundSet;
import com.nicobrailo.pianoli.sound.SoundSet;
import com.nicobrailo.pianoli.sound.StraightKeySoundMaker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renderer/View for our {@link Piano}.
 */
class PianoCanvas extends View implements PianoListener {
    /** Relative draw-size of gear icon on next-expected config key */
    public static final float CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO = 0.5f;
    /** Relative draw-size of gear icon on already-held config keys */
    public static final float CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO_PRESSED = 0.4f;
    /** Relative width of the 3D-effect edges of keys */
    public static final float BEVEL_RATIO = 0.1f;

    private final float bevelWidth;

    private Piano piano;
    final AppConfigTrigger appConfigTrigger;

    private final int screen_size_y, screen_size_x;
    private final Drawable gearIcon;
    private Theme theme;

    private final Map<Integer, Integer> touch_pointer_to_keys = new HashMap<>();
    private SoundSet soundSet;

    public PianoCanvas(Context context, AttributeSet as) {
        this(context, as, 0);
    }

    public PianoCanvas(Context context, AttributeSet as, int defStyle) {
        super(context, as, defStyle);
        this.setFocusable(true);

        final Point screen_size = new Point();
        final AppCompatActivity ctx;
        try {
            ctx = (AppCompatActivity) context;
            Display display = ctx.getWindowManager().getDefaultDisplay();
            display.getSize(screen_size);
        } catch (ClassCastException ex) {
            Log.e("PianOli::PianoCanvas", "Can't read screen size");
            throw ex;
        }

        screen_size_x = screen_size.x;
        screen_size_y = screen_size.y;
        final String prefSoundset = Preferences.selectedSoundSet(context);

        // DANGER ZONE: !! delicate ordering dependencies in this block !!
        // Bind to the main Looper explicitly rather than calling View.getHandler() at
        // schedule-time: getHandler() returns null while the view is detached from a
        // window, which would NPE if a timer is ever scheduled off-screen.
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        appConfigTrigger = new AppConfigTrigger() {
            @Override
            protected void scheduleTimer(int delayMs, int timerID) {
                mainHandler.postDelayed(() -> appConfigTrigger.onTimer(timerID), delayMs);
            }
        };
        // gotcha: gets just-created appConfigHandler via field
        reInitPiano(context, prefSoundset); // danger: impl leaks `this` pointer before ctor finished
        // gotcha: needs piano field that was just initialised by reInitPiano above
        this.bevelWidth = piano.get_keys_width() * BEVEL_RATIO;

        this.gearIcon = ContextCompat.getDrawable(context, R.drawable.ic_settings);
        if (this.gearIcon == null) {
            Log.wtf("PianOli::DrawingCanvas", "Config icon doesn't exist");
        }

        // log successful completion of ctor, since this a highly non-trivial one.
        Log.d("PianOli::DrawingCanvas", "Display is " + screen_size.x + "x" + screen_size.y +
                ", there are " + piano.get_keys_count() + " keys");
    }

    public void reInitPiano(Context context, String prefSoundset) {
        Log.i("PianOli::PianoCanvas", "re-initialising Piano");
        this.piano = new Piano(screen_size_x, screen_size_y);

        String prefTheme = Preferences.selectedTheme(context);
        this.theme = Theme.fromPreference(prefTheme);

        // for config trigger updates
        piano.addListener(appConfigTrigger);
        appConfigTrigger.reset();

        // to redraw on key-touches, must be after config handler to ensure its input is also drawn
        piano.addListener(this);

        if (soundSet != null) {
            soundSet.close();
        }
        // Respond musically to key-presses: listen with a "soundMaker"
        // Use "strategy pattern" to deal with the two possible key-to-note mappings:
        soundSet = new SampledSoundSet(context, prefSoundset);
        PianoListener soundMaker;
        if (Preferences.areMelodiesEnabled(context)) {
            // "melodic" strategy: next note is determined by melody
            List<Melody> selectedMelodies = Preferences.selectedMelodies(context);
            MultipleSongsMelodyPlayer melodyPlayer = new MultipleSongsMelodyPlayer(selectedMelodies);
            soundMaker = new MelodicKeySoundMaker(soundSet, melodyPlayer);
        } else {
            // "straight" strategy:
            soundMaker = new StraightKeySoundMaker(soundSet);
        }
        piano.addListener(soundMaker);

        piano.doAllKeysUp();
        Log.i("PianOli::PianoCanvas", "re-initialising Piano - DONE");
    }

    /**
     * Dependency injection for context-handling stuff: switching to settings activity, and showing toasts.
     *
     * @see AppConfigTrigger#setConfigRequestCallback(AppConfigTrigger.AppConfigCallback)
     */
    public void setConfigRequestCallback(@NonNull AppConfigTrigger.AppConfigCallback cb) {
        this.appConfigTrigger.setConfigRequestCallback(cb);
    }

    /**
     * Overlays gear icons onto the currently-held and next expected flat keys.
     */
    void drawConfigGears(Canvas androidCanvas) {
        // draw already-held keys with shrunken icon
        int pressedSize = (int) (piano.get_keys_flat_width() * CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO_PRESSED);
        for (int cfgKey : appConfigTrigger.getPressedConfigKeys()) {
            draw_icon_on_black_key(androidCanvas, gearIcon, cfgKey, pressedSize, pressedSize);
        }

        // draw next expected key with large icon, for more user-attention.
        int key = appConfigTrigger.getNextExpectedKey();
        if (key >= 0) {
            int normalSize = (int) (piano.get_keys_flat_width() * CONFIG_ICON_SIZE_TO_FLAT_KEY_RATIO);
            draw_icon_on_black_key(androidCanvas, gearIcon, key, normalSize, normalSize);
        }
    }

    void drawKey(final Canvas canvas, final int i) {
        Key rect = piano.getAreaForKey(i);
        if (rect == Key.CANT_TOUCH_THIS) {
            return; // don't waste performance drawing the skipped black keys.
        }

        Paint p = new Paint();
        p.setColor(theme.getColorForKey(i, piano.is_key_pressed(i)));

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
    void draw_icon_on_black_key(final Canvas canvas, final Drawable icon, int key_idx,
                                final int icon_width, final int icon_height) {
        final Key key = piano.getAreaForKey(key_idx);
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

    public void redraw() {
        invalidate();
    }

    private final Paint backgroundPaint = new Paint();
    {
        backgroundPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawPaint(backgroundPaint);

        // draw main, big keys (even key index)
        for (int i = 0; i < piano.get_keys_count(); i += 2) {
            drawKey(canvas, i);
        }

        // Small keys drawn after big keys to ensure z-index (odd key index)
        for (int i = 1; i < piano.get_keys_count(); i += 2) {
            drawKey(canvas, i);
        }

        // Gear icons drawn after small keys, since they go on top of those.
        drawConfigGears(canvas);
    }

    @Override
    public void onKeyUp(int keyIdx) {
        redraw();
    }

    @Override
    public void onKeyDown(int keyIdx) {
        redraw();
    }

    // This is called through piano.doAllKeysUp().
    @Override
    public void onAllKeysUp() {
        touch_pointer_to_keys.clear();
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
                    piano.doAllKeysUp();
                    return super.onTouchEvent(event);
                }

                // Mark key down ptr_id
                touch_pointer_to_keys.put(ptr_id, key_idx);
                piano.doKeyDown(key_idx);

                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                // action_move is special, there is no *single* `getActionIndex`,
                // as *multiple* pointers could have moved, so we must check *each* pointer.
                // https://developer.android.com/develop/ui/views/touch-and-input/gestures/multi
                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    ptr_id = event.getPointerId(i);  // cf precalculated ptr_id above switch
                    key_idx = piano.pos_to_key_idx(
                            event.getX(i),
                            event.getY(i));

                    if (!touch_pointer_to_keys.containsKey(ptr_id)) {
                        Log.e("PianOli::DrawingCanvas", "Touch-track error: Missed touch-up event");
                        piano.doAllKeysUp();
                        return super.onTouchEvent(event);
                    }
                    // check if key changed
                    int prevKeyIdx = touch_pointer_to_keys.get(ptr_id);
                    if (prevKeyIdx != key_idx) {
                        Log.d("PianOli::DrawingCanvas", "Moved to another key");
                        // Release key before storing new key_idx for new key down
                        piano.doKeyUp(prevKeyIdx);
                        touch_pointer_to_keys.put(ptr_id, key_idx);
                        piano.doKeyDown(key_idx);
                    }
                }

                return true;
            }
            case MotionEvent.ACTION_POINTER_UP:     // fallthrough
            case MotionEvent.ACTION_UP: {
                if (!touch_pointer_to_keys.containsKey(ptr_id)) {
                    Log.e("PianOli::DrawingCanvas", "Touch-track error: Repeated touch-up event received");
                    piano.doAllKeysUp();
                    return super.onTouchEvent(event);
                }

                touch_pointer_to_keys.remove(ptr_id);
                piano.doKeyUp(key_idx);

                // We are worried about missing touch events, since it would leave us in an invalid
                // state. We assume, that getting MotionEvents may not be reliable,
                // but that the information contained within these events is reliable.
                // So if we detect that there are no touches on the screen right now,
                // we call the onAllKeysUp signal (via piano) - it should not do anything,
                // but it will fix our state if it is broken.
                if (event.getPointerCount() <= 1) {
                    piano.doAllKeysUp();
                }

                return true;
            }

            default:
                return super.onTouchEvent(event);
        }
    }
}
