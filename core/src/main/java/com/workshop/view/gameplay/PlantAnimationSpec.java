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
        setClip(PlantAnimationState.IDLE, pickClip(available, "idle", "idle1", "idle_stage1", "loop"));
        String attack = pickClipOrNull(
            available,
            "attack", "attack1", "attack2", "attack3",
            "punch", "bite", "chomp", "chew",
            "smash", "whip", "hit", "strike", "melee",
            "special", "special1", "shoot"
        );
        setClip(PlantAnimationState.ATTACK, attack);
        setClip(PlantAnimationState.SPECIAL, pickClipOrNull(available, "special", "special1", "chew", "digest"));
        setClip(PlantAnimationState.PLANTFOOD, pickClipOrNull(available, "plantfood", "plantfood1", "plant_food"));
    }

    private static String pickClipOrNull(List<String> available, String... preferred) {
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

        for (String want : preferred) {
            String needle = want.toLowerCase(Locale.ROOT);
            for (String clip : available) {
                if (clip != null && clip.toLowerCase(Locale.ROOT).contains(needle)) {
                    return clip;
                }
            }
        }

        return null;
    }

    private static String pickClip(List<String> available, String... preferred) {
        String found = pickClipOrNull(available, preferred);
        if (found != null) {
            return found;
        }
        return available == null || available.isEmpty() ? null : available.get(0);
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
