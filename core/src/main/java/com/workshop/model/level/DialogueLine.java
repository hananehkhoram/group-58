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

    public DialogueLine(String speakerName, String portraitPath, String text) {
        this(speakerName, text, portraitPath, null, null, null);
    }

    public DialogueLine(String speakerName, String text) {
        this(speakerName, text, null, null, null, null);
    }

    /** پرتره از یک آی‌دی منبع داخلیِ بازی (TextureBank/RESOURCES.json). */
    public static DialogueLine withResourcePortrait(
        String speakerName,
        String resourceId,
        String text
    ) {
        return new DialogueLine(speakerName, text, null, resourceId, null, null);
    }

    /** پرتره از یک انیمیشن PAM (مثل DaveWinnie_NarrationIcons). */
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
