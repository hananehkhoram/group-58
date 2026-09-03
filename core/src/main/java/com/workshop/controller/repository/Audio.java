package com.workshop.controller.repository;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public final class Audio {

    private static final String[] EXTENSIONS = {".ogg", ".mp3", ".wav"};
    private static final String PREFS_NAME = "pvz-audio-settings";

    private static final Map<String, Music> musicCache = new HashMap<>();
    private static final Map<String, Sound> sfxCache = new HashMap<>();

    private static Music currentMusic;
    private static String currentKey;

    private static Float musicVolume; // lazily loaded from Preferences
    private static Float sfxVolume;

    private Audio() {}

    // ---- volume ----

    public static float getMusicVolume() {
        if (musicVolume == null) {
            musicVolume = prefs().getFloat("musicVolume", 1f);
        }
        return musicVolume;
    }

    public static float getSfxVolume() {
        if (sfxVolume == null) {
            sfxVolume = prefs().getFloat("sfxVolume", 1f);
        }
        return sfxVolume;
    }

    public static void setMusicVolume(float volume) {
        musicVolume = clamp01(volume);
        prefs().putFloat("musicVolume", musicVolume).flush();

        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public static void setSfxVolume(float volume) {
        sfxVolume = clamp01(volume);
        prefs().putFloat("sfxVolume", sfxVolume).flush();
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static Preferences prefs() {
        return Gdx.app.getPreferences(PREFS_NAME);
    }

    // ---- file resolution ----

    private static FileHandle resolve(String pathNoExt) {
        FileHandle root = Textures.assetsRoot();
        for (String ext : EXTENSIONS) {
            FileHandle candidate = root.child(pathNoExt + ext);
            if (candidate.exists()) return candidate;
        }
        return null;
    }

    private static Music getMusic(String pathNoExt) {
        if (musicCache.containsKey(pathNoExt)) return musicCache.get(pathNoExt);

        FileHandle file = resolve(pathNoExt);
        if (file == null) {
            Gdx.app.error("Audio", "Not found: " + pathNoExt
                + " (tried .ogg/.mp3/.wav under " + Textures.assetsRoot().file().getAbsolutePath() + ")");
            musicCache.put(pathNoExt, null);
            return null;
        }

        Music music = Gdx.audio.newMusic(file);
        musicCache.put(pathNoExt, music);
        return music;
    }

    private static Sound getSound(String pathNoExt) {
        if (sfxCache.containsKey(pathNoExt)) return sfxCache.get(pathNoExt);

        FileHandle file = resolve(pathNoExt);
        if (file == null) {
            Gdx.app.error("Audio", "Not found: " + pathNoExt
                + " (tried .ogg/.mp3/.wav under " + Textures.assetsRoot().file().getAbsolutePath() + ")");
            sfxCache.put(pathNoExt, null);
            return null;
        }

        Sound sound = Gdx.audio.newSound(file);
        sfxCache.put(pathNoExt, sound);
        return sound;
    }

    // ---- playback ----

    /** Stops whatever is currently playing (if anything) and starts this track. No-op if already playing it. */
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
        music.setVolume(getMusicVolume());
        music.play();
        currentMusic = music;
        currentKey = pathNoExt;
    }

    /** Short one-shot sound effect — plays on top of whatever background music is already going. */
    public static void playSfx(String pathNoExt) {
        Sound sound = getSound(pathNoExt);
        if (sound != null) sound.play(getSfxVolume());
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = null;
        currentKey = null;
    }

    public static void dispose() {
        for (Music music : musicCache.values()) {
            if (music != null) music.dispose();
        }
        for (Sound sound : sfxCache.values()) {
            if (sound != null) sound.dispose();
        }
        musicCache.clear();
        sfxCache.clear();
        currentMusic = null;
        currentKey = null;
    }
}
