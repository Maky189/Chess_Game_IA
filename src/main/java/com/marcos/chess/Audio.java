package com.marcos.chess;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioSource.Status;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Audio {
    private static Audio instance;
    private final AssetManager assetManager;
    private AudioNode currentMusic;
    private final Random random = new Random();
    private boolean isMuted = false;
    private float volume = 1.0f;
    private final List<String> musicTracks;
    private boolean isInitialized = false;
    
    private Audio(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.musicTracks = new ArrayList<>();
        loadMusicTracks();
    }

    public static Audio getInstance(AssetManager assetManager) {
        if (instance == null) {
            instance = new Audio(assetManager);
        }
        return instance;
    }

    private void loadMusicTracks() {
        musicTracks.add("Sounds/Music/music1.ogg");
        musicTracks.add("Sounds/Music/music2.ogg");
        musicTracks.add("Sounds/Music/music3.ogg");
        musicTracks.add("Sounds/Music/music4.ogg");
        musicTracks.add("Sounds/Music/music5.ogg");
    }

    public void initializeIfNeeded() {
        if (!isInitialized) {
            playRandomMusic();
            isInitialized = true;
        }
    }

    public void playRandomMusic() {
        try {
            if (currentMusic != null) {
                currentMusic.stop();
            }

            String randomTrack = musicTracks.get(random.nextInt(musicTracks.size()));
            System.out.println("Attempting to play: " + randomTrack);

            currentMusic = new AudioNode(assetManager, randomTrack, DataType.Stream);
            currentMusic.setLooping(false);
            currentMusic.setVolume(isMuted ? 0 : volume);
            currentMusic.setPositional(false);
            currentMusic.play();

            System.out.println("Music started playing");

            new Thread(() -> {
                while (currentMusic != null && currentMusic.getStatus() == Status.Playing) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                if (currentMusic != null) {
                    playRandomMusic();
                }
            }).start();

        } catch (Exception e) {
            return;
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (currentMusic != null) {
            currentMusic.setVolume(isMuted ? 0 : volume);
        }
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (currentMusic != null && !isMuted) {
            currentMusic.setVolume(volume);
        }
    }

    public void cleanup() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
        isInitialized = false;
    }
}