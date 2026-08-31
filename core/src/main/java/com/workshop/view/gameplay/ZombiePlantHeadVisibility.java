package com.workshop.view.gameplay;

import pvz.libpvz.pam.PamPlayer;

import java.util.HashSet;
import java.util.Set;

final class ZombiePlantHeadVisibility {

    private static final String PEASHOOTER_PAM =
        "768/INITIAL/PLANT/PEASHOOTER/PEASHOOTER.PAM";

    private static final Set<String> parts = new HashSet<>();

    private ZombiePlantHeadVisibility(){}


    static boolean pamHasPart(
        PamPlayer pamPlayer,
        String part
    ){
        return names(pamPlayer).contains(part);
    }


    private static Set<String> names(
        PamPlayer pamPlayer
    ){

        if(!parts.isEmpty()){
            return parts;
        }

        PamPlayer.AnimationPart root =
            pamPlayer.getParts(PEASHOOTER_PAM);

        if(root != null){
            collect(root, parts);
        }

        return parts;
    }


    private static void collect(
        PamPlayer.AnimationPart part,
        Set<String> result
    ){

        if(part.name != null && !part.name.isEmpty()){
            result.add(part.name);
        }

        if(part.children != null){
            for(PamPlayer.AnimationPart child : part.children){
                collect(child, result);
            }
        }
    }
}
