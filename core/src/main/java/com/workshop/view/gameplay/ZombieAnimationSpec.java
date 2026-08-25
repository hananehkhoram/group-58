package com.workshop.view.gameplay;

import java.util.EnumMap;
import java.util.Map;

public final class ZombieAnimationSpec {

    private final String pamPath;
    private String ashPamPath;

    private final Map<ZombieAnimationState, String> clips =
        new EnumMap<>(ZombieAnimationState.class);

    public ZombieAnimationSpec(String pamPath, String idleClip) {
        this.pamPath = pamPath;
        clips.put(ZombieAnimationState.IDLE, idleClip);
    }

    public String getPamPath() {
        if (pamPath != null) {
            String clean = pamPath.toUpperCase().trim().replace("ZOMBIE_", "").replace("_ZOMBIE", "").replace("ZOMBIE", "").replace(" ", "").replace("_", "");
            if (clean.equals("RA")) {
                return "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM";
            }
            if (clean.contains("BARRELROLLER")){
                return "768/FULL/ZOMBIE/ZOMBIE_PIRATE_BARREL_PUSHER/ZOMBIE_PIRATE_BARREL_PUSHER.PAM";
            }
        }
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

    public String getAshPamPath() {
        return ashPamPath;
    }

    public void setAshPamPath(String ashPamPath) {
        this.ashPamPath = ashPamPath;
    }
}
