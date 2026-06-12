package com.audiolab.service;

import com.audiolab.model.AudioMetadata;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class AudioPlaybackService {

    private Clip clip;
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private Consumer<Boolean> playingStateListener = ignored -> {};

    public void setPlayingStateListener(Consumer<Boolean> listener) {
        this.playingStateListener = listener != null ? listener : ignored -> {};
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public void play(short[] samples, AudioMetadata metadata) throws Exception {
        stop();
        AudioFormat format = metadata.audioFormat();
        int frameSize = format.getFrameSize();
        if (frameSize <= 0) {
            throw new IllegalArgumentException("Unsupported audio format");
        }
        byte[] pcm = new AudioIOService().samplesToPcmBytes(samples);
        long frameLength = pcm.length / frameSize;
        if (frameLength <= 0) {
            throw new IllegalArgumentException("No audio samples to play");
        }

        AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(pcm), format, frameLength);
        clip = AudioSystem.getClip();
        clip.open(stream);
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                playing.set(false);
                playingStateListener.accept(false);
            }
        });
        clip.start();
        playing.set(true);
        playingStateListener.accept(true);
    }

    public void stop() {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
            clip = null;
        }
        playing.set(false);
        playingStateListener.accept(false);
    }

}
