package com.workshop.controller.repository;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public final class Audio {

    private static final String[] EXTENSIONS = {".ogg", ".mp3", ".wav"};

    private static final Map<String, Music> cache = new HashMap<>();
    private static Music currentMusic;
    private static String currentKey;

    private Audio() {}

    private static FileHandle resolve(String pathNoExt) {
        FileHandle root = Textures.assetsRoot();
        for (String ext : EXTENSIONS) {
            FileHandle candidate = root.child(pathNoExt + ext);
            if (candidate.exists()) return candidate;
        }
        return null;
    }

    private static Music getMusic(String pathNoExt) {
        if (cache.containsKey(pathNoExt)) return cache.get(pathNoExt);

        FileHandle file = resolve(pathNoExt);
        if (file == null) {
            Gdx.app.error("Audio", "Not found: " + pathNoExt
                + " (tried .ogg/.mp3/.wav under " + Textures.assetsRoot().file().getAbsolutePath() + ")");
            cache.put(pathNoExt, null);
            return null;
        }

        Music music = Gdx.audio.newMusic(file);
        cache.put(pathNoExt, music);
        return music;
    }

    public static void playMusic(String pathNoExt, boolean loop) {
        if (pathNoExt.equals(currentKey) && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }

        Music music = getMusic(pathNoExt);
        if (music == null) return;

        if (currentMusic != null) {
            currentMusic.stop();
        }

        music.setLooping(loop);
        music.play();
        currentMusic = music;
        currentKey = pathNoExt;
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = null;
        currentKey = null;
    }

    public static void dispose() {
        for (Music music : cache.values()) {
            if (music != null) music.dispose();
        }
        cache.clear();
        currentMusic = null;
        currentKey = null;
    }
}
