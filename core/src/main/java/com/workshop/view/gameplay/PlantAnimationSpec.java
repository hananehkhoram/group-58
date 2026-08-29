package com.workshop.view.gameplay;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pvz.libpvz.pam.PamPlayer;

public final class PlantAnimationSpec {

    private final String pamPath;
    private boolean clipsBound;

    private final Map<PlantAnimationState, String> clips =
        new EnumMap<>(PlantAnimationState.class);

    public PlantAnimationSpec(String pamPath, String idleClip) {
        this.pamPath = pamPath;
        clips.put(PlantAnimationState.IDLE, idleClip);
    }

    public void ensureClipsBound(PamPlayer pamPlayer) {
        if (clipsBound || pamPlayer == null || pamPath == null) {
            return;
        }

        try {
            bindAvailableClips(pamPlayer.clips(pamPath));
        } catch (RuntimeException ignored) {
            clipsBound = true;
        }
    }

    void bindAvailableClips(List<String> available) {
        if (clipsBound) {
            return;
        }
        clipsBound = true;
        setClip(PlantAnimationState.IDLE, pickClip(available, "idle", "intro"));
        setClip(PlantAnimationState.ATTACK, pickClip(available, "attack", "special", "intro"));
        setClip(PlantAnimationState.SPECIAL, pickClip(available, "special"));
        setClip(PlantAnimationState.PLANTFOOD, pickClip(available, "plantfood"));
    }

    private static String pickClip(List<String> available, String... preferred) {
        if (available == null || available.isEmpty()) {
            return null;
        }

        for (String want : preferred) {
            for (String clip : available) {
                if (clip != null && want.equalsIgnoreCase(clip)) {
                    return clip;
                }
            }
        }

        for (String want : preferred) {
            String prefix = want.toLowerCase(Locale.ROOT);
            for (String clip : available) {
                if (clip != null && clip.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    return clip;
                }
            }
        }

        return available.get(0);
    }

    public String getPamPath() {
        return pamPath;
    }

    public String getIdleClip() {
        return getClip(PlantAnimationState.IDLE);
    }

    public String getClip(PlantAnimationState state) {
        return clips.get(state);
    }

    public void setClip(
        PlantAnimationState state,
        String clip
    ) {
        if (clip == null) {
            clips.remove(state);
        } else {
            clips.put(state, clip);
        }
    }

    public boolean hasClip(PlantAnimationState state) {
        return clips.containsKey(state);
    }
}
