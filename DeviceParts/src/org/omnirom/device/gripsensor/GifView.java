package org.omnirom.device.gripsensor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

public class GifView extends View {
    private Movie mMovie;
    private long mMovieStart;

    public GifView(Context context) {
        super(context);
        init(context);
    }

    public GifView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public GifView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        setFocusable(true);
        setLayerType(1, null);
    }

    public void setGifResource(Context context, int i) {
        this.mMovie = Movie.decodeStream(context.getResources().openRawResource(i));
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        long uptimeMillis = SystemClock.uptimeMillis();
        if (0 == this.mMovieStart) {
            this.mMovieStart = uptimeMillis;
        }
        Movie movie = this.mMovie;
        if (movie == null) {
            super.onDraw(canvas);
            return;
        }
        int duration = movie.duration();
        if (duration == 0) {
            duration = 5000;
        }
        this.mMovie.setTime((int) ((uptimeMillis - this.mMovieStart) % ((long) duration)));
        float min = Math.min(((float) getHeight()) / ((float) this.mMovie.height()), ((float) getWidth()) / ((float) this.mMovie.width()));
        int width = (getWidth() / 2) - (((int) (((float) this.mMovie.width()) * min)) / 2);
        int height = getHeight() - ((int) (((float) this.mMovie.height()) * min));
        if (((double) min) < 0.5d) {
            canvas.scale(0.2f, 0.2f);
        } else {
            canvas.scale(min, min);
        }
        this.mMovie.draw(canvas, (float) width, (float) height);
        invalidate();
    }
}
