package com.workshop.view.gameplay;

import java.util.EnumMap;
import java.util.Map;

public final class ZombieAnimationSpec {

    private final String pamPath;

    private final Map<ZombieAnimationState, String> clips =
        new EnumMap<>(ZombieAnimationState.class);

    public ZombieAnimationSpec(String pamPath, String idleClip) {
        this.pamPath = pamPath;
        clips.put(ZombieAnimationState.IDLE, idleClip);
    }

    public String getPamPath() {
        return pamPath;
    }

    public String getIdleClip() {
        return getClip(ZombieAnimationState.IDLE);
    }

    public String getClip(ZombieAnimationState state) {
        return clips.get(state);
    }

    public void setClip(
        ZombieAnimationState state,
        String clip
    ) {
        if (clip == null) {
            clips.remove(state);
        } else {
            clips.put(state, clip);
        }
    }

    public boolean hasClip(ZombieAnimationState state) {
        return clips.containsKey(state);
    }
}
