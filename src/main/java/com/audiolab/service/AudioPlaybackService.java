package com.audiolab.service;

import com.audiolab.model.AudioMetadata;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Plays PCM preview audio using javax.sound.sampled.Clip. */
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
        byte[] pcm = new AudioIOService().samplesToPcmBytes(samples);
        clip = AudioSystem.getClip();
        clip.open(format, pcm, 0, pcm.length);
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
            clip.removeLineListener(ignored -> {});
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
            clip = null;
        }
        playing.set(false);
        playingStateListener.accept(false);
    }

    public boolean isSupported() {
        return true;
    }
}
