package com.audiolab.ui;

import com.audiolab.theme.AppColors;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;

/** Draws a simple PCM waveform envelope for the loaded audio. */
public final class WaveformView extends Region {

    private short[] samples = new short[0];
    private int channels = 1;
    private final Canvas canvas = new Canvas();

    public WaveformView() {
        getChildren().add(canvas);
        widthProperty().addListener(obs -> redraw());
        heightProperty().addListener(obs -> redraw());
    }

    public void setSamples(short[] samples, int channels) {
        this.samples = samples != null ? samples : new short[0];
        this.channels = Math.max(1, channels);
        redraw();
    }

    public void clear() {
        setSamples(new short[0], 1);
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        redraw();
    }

    private void redraw() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.setFill(AppColors.WAVEFORM_BG);
        gc.fillRoundRect(0, 0, w, h, 12, 12);

        if (samples.length == 0) {
            return;
        }

        int frames = samples.length / channels;
        if (frames <= 0) {
            return;
        }

        int points = (int) Math.min(w, frames);
        double midY = h / 2.0;
        gc.setStroke(AppColors.WAVEFORM);
        gc.setLineWidth(1.2);
        gc.beginPath();
        for (int i = 0; i < points; i++) {
            int frame = (int) ((long) i * frames / points);
            short sample = samples[frame * channels];
            double x = i * w / points;
            double y = midY - (sample / 32768.0) * (h * 0.42);
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
    }
}
