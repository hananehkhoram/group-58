package com.workshop.model.level;


public class DialogueLine {

    private final String speakerName;
    private final String text;

    private final String portraitPath;
    private final String portraitResourceId;
    private final String portraitPamPath;
    private final String portraitPamClip;

    private DialogueLine(
        String speakerName,
        String text,
        String portraitPath,
        String portraitResourceId,
        String portraitPamPath,
        String portraitPamClip
    ) {
        this.speakerName = speakerName;
        this.text = text;
        this.portraitPath = portraitPath;
        this.portraitResourceId = portraitResourceId;
        this.portraitPamPath = portraitPamPath;
        this.portraitPamClip = portraitPamClip;
    }

    public static DialogueLine withResourcePortrait(
        String speakerName,
        String resourceId,
        String text
    ) {
        return new DialogueLine(speakerName, text, null, resourceId, null, null);
    }

    public static DialogueLine withPamPortrait(
        String speakerName,
        String pamPath,
        String pamClip,
        String text
    ) {
        return new DialogueLine(speakerName, text, null, null, pamPath, pamClip);
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public String getText() {
        return text;
    }

    public String getPortraitPath() {
        return portraitPath;
    }

    public String getPortraitResourceId() {
        return portraitResourceId;
    }

    public String getPortraitPamPath() {
        return portraitPamPath;
    }

    public String getPortraitPamClip() {
        return portraitPamClip;
    }
}
