package com.workshop.view.gameplay;

import java.util.EnumMap;
import java.util.Map;

public final class PlantAnimationSpec {

    private final String pamPath;

    private final Map<PlantAnimationState, String> clips =
        new EnumMap<>(PlantAnimationState.class);

    public PlantAnimationSpec(String pamPath, String idleClip) {
        this.pamPath = pamPath;
        clips.put(PlantAnimationState.IDLE, idleClip);
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
