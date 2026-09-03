package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.workshop.controller.repository.Textures;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.Tag;
import com.workshop.model.projectile.BowlingWallnut;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.ProjectileVisualVariant;
import com.workshop.model.projectile.TrajectoryType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pvz.libpvz.pam.PamPlayer;

public final class ProjectileAnimationResolver {

    private static final String LOG_TAG = "ProjectileAnimationResolver";
    private static final String[] FIRE_PEA_PAMS = {
        "768/INITIAL/EFFECTS/T_FIRE_PEA/FIRE_PEASHOOTER.PAM",
        "768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM"
    };
    private static final String[] BOWLING_BULB_PAMS = {
        "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE1/BOWLINGBULB_PROJECTILE1.PAM",
        "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE2/BOWLINGBULB_PROJECTILE2.PAM",
        "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE3/BOWLINGBULB_PROJECTILE3.PAM"
    };
    private static final String BOWLING_BULB_PLANTFOOD_PAM =
        "768/FULL/EFFECTS/BOWLINGBULB_PLANTFOOD_PROJECTILE/BOWLINGBULB_PLANTFOOD_PROJECTILE.PAM";
    private static final Pattern PROJECTILE_NUMBER = Pattern.compile(
        "PROJECTILE(\\d+)",
        Pattern.CASE_INSENSITIVE
    );
    private static ProjectilePamCatalog sharedCatalog;

    private final ProjectilePamCatalog catalog;
    private final PlantAnimationResolver plantAnimationResolver;

    private final Map<String, ProjectileAnimationSpec> resolvedSpecs =
        new HashMap<>();
    private final Map<String, String> plantBodyPamPaths =
        new HashMap<>();
    private final Set<String> missingKeys = new HashSet<>();
    private final Set<String> ambiguousKeys = new HashSet<>();

    public ProjectileAnimationResolver() {
        PamPlayer player = Textures.getPamPlayer();
        catalog = getSharedCatalog(player);
        plantAnimationResolver = new PlantAnimationResolver();
    }

    private static synchronized ProjectilePamCatalog getSharedCatalog(
        PamPlayer player
    ) {
        if (sharedCatalog == null) {
            sharedCatalog = new ProjectilePamCatalog(player);
        }
        return sharedCatalog;
    }

    public ProjectileAnimationSpec resolve(Projectile projectile) {
        if (projectile == null || projectile.getOwnerPlant() == null) {
            return null;
        }

        String cacheKey = createCacheKey(projectile);
        ProjectileAnimationSpec cached = resolvedSpecs.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        ProjectileAnimationSpec spec = specFromPlantBody(projectile);
        if (spec == null) {
            spec = specFromBowlingBulb(projectile);
        }
        if (spec == null) {
            spec = specFromFirePea(projectile);
        }
        if (spec == null) {
            spec = specFromSprite(projectile);
        }
        if (spec == null) {
            Descriptor descriptor = Descriptor.from(projectile);
            String pamPath = findUniquePamPath(projectile, descriptor, cacheKey);
            if (pamPath != null && !isOwnerPlantAnimation(pamPath, projectile.getOwnerPlant())) {
                spec = new ProjectileAnimationSpec(
                    pamPath,
                    pickProjectileClip(pamPath),
                    1f,
                    0f,
                    0f
                );
                Gdx.app.log(LOG_TAG, "Resolved " + cacheKey + " -> " + pamPath);
            }
        }

        if (spec == null) {
            logMissing(projectile, cacheKey);
            return null;
        }

        if (projectile.getVisualVariant() == ProjectileVisualVariant.GIANT) {
            spec = withScale(spec, spec.getScale() * 2.75f);
        }

        resolvedSpecs.put(cacheKey, spec);
        return spec;
    }

    private String findUniquePamPath(
        Projectile projectile,
        Descriptor descriptor,
        String cacheKey
    ) {
        List<Match> matches = collectMatches(projectile, descriptor);
        if (matches.isEmpty()) {
            return null;
        }

        List<Match> strongest = removeQualityDominated(matches);
        String preferred = preferOwnerProjectilePath(strongest, descriptor);
        if (preferred != null) {
            return preferred;
        }
        if (strongest.size() == 1) {
            return strongest.get(0).candidate.getPath();
        }

        List<Match> minimal = removeDominatedByExtraTokens(strongest, descriptor);
        preferred = preferOwnerProjectilePath(minimal, descriptor);
        if (preferred != null) {
            return preferred;
        }
        if (minimal.size() == 1) {
            return minimal.get(0).candidate.getPath();
        }

        logAmbiguous(projectile, cacheKey, minimal);
        return null;
    }

    private String preferOwnerProjectilePath(
        List<Match> matches,
        Descriptor descriptor
    ) {
        if (descriptor.ownerCompact.isEmpty() || matches == null || matches.isEmpty()) {
            return null;
        }

        List<Match> ownerProjectiles = new ArrayList<>();
        for (Match match : matches) {
            String compactPath = match.candidate.getCompactPath();
            if (compactPath.contains(descriptor.ownerCompact)
                && ProjectilePamCatalog.isProjectileAssetPath(match.candidate.getPath())) {
                ownerProjectiles.add(match);
            }
        }

        if (ownerProjectiles.isEmpty()) {
            return null;
        }

        List<Match> flight = new ArrayList<>();
        for (Match match : ownerProjectiles) {
            String upper = match.candidate.getPath().toUpperCase(Locale.ROOT);
            if (upper.contains("SPLAT")
                || upper.contains("EXPLODE")
                || upper.contains("EXPLOSION")
                || upper.contains("HIT")
                || upper.contains("IMPACT")
                || upper.contains("PLANTFOOD")) {
                continue;
            }
            flight.add(match);
        }

        List<Match> chosen = flight.size() == 1 ? flight
            : flight.isEmpty() ? ownerProjectiles
            : flight;
        if (chosen.size() == 1) {
            return chosen.get(0).candidate.getPath();
        }
        return pickCanonicalProjectilePath(chosen);
    }

    private static String pickCanonicalProjectilePath(List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            return null;
        }

        Match best = null;
        int bestNumber = Integer.MAX_VALUE;
        boolean bestNumbered = false;
        for (Match match : matches) {
            int number = projectileAssetNumber(match.candidate.getPath());
            if (best == null) {
                best = match;
                bestNumber = number;
                bestNumbered = number >= 0;
                continue;
            }
            if (number < 0 && bestNumbered) {
                best = match;
                bestNumber = number;
                bestNumbered = false;
            } else if (number >= 0 && bestNumbered && number < bestNumber) {
                best = match;
                bestNumber = number;
            }
        }
        return best == null ? null : best.candidate.getPath();
    }

    private static int projectileAssetNumber(String path) {
        if (path == null) {
            return -1;
        }
        Matcher matcher = PROJECTILE_NUMBER.matcher(path);
        if (!matcher.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private List<Match> collectMatches(
        Projectile projectile,
        Descriptor descriptor
    ) {
        List<Match> matches = new ArrayList<>();
        String plantBodyPath = resolvePlantBodyPamPath(projectile.getOwnerPlant());

        for (ProjectilePamCatalog.Candidate candidate : catalog.getCandidates()) {
            if (isOwnerPlantAnimation(candidate.getPath(), projectile.getOwnerPlant())
                || isSameNormalizedPath(candidate.getPath(), plantBodyPath)) {
                continue;
            }

            Match match = evaluateCandidate(projectile, descriptor, candidate);
            if (match == null) {
                continue;
            }

            if (catalog.isPlantEntityAnimation(candidate)) {
                continue;
            }

            matches.add(match);
        }

        return matches;
    }

    private Match evaluateCandidate(
        Projectile projectile,
        Descriptor descriptor,
        ProjectilePamCatalog.Candidate candidate
    ) {
        Set<String> candidateTokens = candidate.getFullTokens();

        if (hasConflictingBulletEffect(projectile, descriptor, candidateTokens)) {
            return null;
        }

        int variantMatches = countMatches(descriptor.variantTokens, candidateTokens);
        if (!descriptor.variantTokens.isEmpty()
            && variantMatches != descriptor.variantTokens.size()) {
            return null;
        }

        boolean exactOwnerPath = descriptor.useOwnerIdentity
            && !descriptor.ownerCompact.isEmpty()
            && candidate.getCompactPath().contains(descriptor.ownerCompact);

        int ownerMatches = countMatches(descriptor.ownerTokens, candidateTokens);
        int tagMatches = countMatches(descriptor.tagTokens, candidateTokens);
        int bulletMatches = countMatches(descriptor.bulletTokens, candidateTokens);
        int classMatches = countMatches(descriptor.classTokens, candidateTokens);
        int trajectoryMatches = countMatches(descriptor.trajectoryTokens, candidateTokens);

        int identityMatches = variantMatches + ownerMatches + tagMatches + classMatches;
        if (!exactOwnerPath && identityMatches == 0) {
            return null;
        }

        MatchQuality quality = new MatchQuality(
            variantMatches,
            exactOwnerPath,
            ownerMatches,
            tagMatches,
            bulletMatches,
            classMatches,
            trajectoryMatches
        );

        return new Match(candidate, quality);
    }

    private List<Match> removeQualityDominated(List<Match> matches) {
        List<Match> survivors = new ArrayList<>();

        for (Match candidate : matches) {
            boolean dominated = false;

            for (Match other : matches) {
                if (candidate == other) {
                    continue;
                }

                if (other.quality.dominates(candidate.quality)) {
                    dominated = true;
                    break;
                }
            }

            if (!dominated) {
                survivors.add(candidate);
            }
        }

        return survivors;
    }

    private List<Match> removeDominatedByExtraTokens(
        List<Match> matches,
        Descriptor descriptor
    ) {
        List<Match> survivors = new ArrayList<>();

        for (Match candidate : matches) {
            Set<String> candidateExtras = extraLocalTokens(candidate, descriptor);
            boolean dominated = false;

            for (Match other : matches) {
                if (candidate == other) {
                    continue;
                }

                Set<String> otherExtras = extraLocalTokens(other, descriptor);
                if (isProperSubset(otherExtras, candidateExtras)) {
                    dominated = true;
                    break;
                }
            }

            if (!dominated) {
                survivors.add(candidate);
            }
        }

        return survivors;
    }

    private Set<String> extraLocalTokens(Match match, Descriptor descriptor) {
        Set<String> extras = new HashSet<>(match.candidate.getLocalTokens());
        extras.removeAll(descriptor.allSemanticTokens);
        return extras;
    }

    private boolean hasConflictingBulletEffect(
        Projectile projectile,
        Descriptor descriptor,
        Set<String> candidateTokens
    ) {
        for (BulletType type : BulletType.values()) {
            if (type == BulletType.NORMAL || type == projectile.getBulletType()) {
                continue;
            }

            if (type.name().contains("_")) {
                continue;
            }

            String token = type.name();
            if (candidateTokens.contains(token)
                && !descriptor.allSemanticTokens.contains(token)) {
                return true;
            }
        }

        return false;
    }

    private String resolvePlantBodyPamPath(Plant plant) {
        String name = plant.getName();
        if (name == null) {
            return null;
        }

        if (plantBodyPamPaths.containsKey(name)) {
            return plantBodyPamPaths.get(name);
        }

        PlantAnimationSpec spec = plantAnimationResolver.resolve(name);
        String path = spec == null ? null : spec.getPamPath();
        plantBodyPamPaths.put(name, path);
        return path;
    }

    private ProjectileAnimationSpec specFromPlantBody(Projectile projectile) {
        if (!(projectile instanceof BowlingWallnut)) {
            return null;
        }

        String path = resolvePlantBodyPamPath(projectile.getOwnerPlant());
        if (path == null) {
            return null;
        }

        return new ProjectileAnimationSpec(
            path,
            pickProjectileClip(path),
            1f,
            0f,
            0f
        );
    }

    private ProjectileAnimationSpec specFromBowlingBulb(Projectile projectile) {
        Plant owner = projectile.getOwnerPlant();
        if (owner == null || owner.getName() == null) {
            return null;
        }
        String compact = ProjectilePamCatalog.compact(owner.getName());
        if (!compact.contains("BOWLINGBULB")) {
            return null;
        }
        if (projectile.getBulletType() != BulletType.BOWLING_NUT
            && projectile.getTrajectory() != TrajectoryType.BOWLING) {
            return null;
        }

        String path = projectile.getVisualVariant() == ProjectileVisualVariant.GIANT
            ? BOWLING_BULB_PLANTFOOD_PAM
            : BOWLING_BULB_PAMS[bowlingBulbTier(projectile)];
        if (!pamFileExists(path)) {
            for (String fallback : BOWLING_BULB_PAMS) {
                if (pamFileExists(fallback)) {
                    path = fallback;
                    break;
                }
            }
        }
        if (!pamFileExists(path)) {
            return null;
        }

        return new ProjectileAnimationSpec(
            path,
            pickProjectileClip(path),
            1.15f,
            0f,
            0f
        );
    }

    private static int bowlingBulbTier(Projectile projectile) {
        int damage = projectile.getDamage();
        if (damage >= 160) {
            return 2;
        }
        if (damage >= 80) {
            return 1;
        }
        return 0;
    }

    private ProjectileAnimationSpec specFromFirePea(Projectile projectile) {
        if (!ProjectileLooks.isFirePeaLook(projectile)) {
            return null;
        }

        for (String path : FIRE_PEA_PAMS) {
            if (!pamFileExists(path)) {
                continue;
            }
            return new ProjectileAnimationSpec(
                path,
                pickProjectileClip(path),
                2.0f,
                0f,
                0f
            );
        }
        return null;
    }

    private static boolean pamFileExists(String relativeToImages) {
        FileHandle file = Textures.assetsRoot()
            .child("IMAGES")
            .child(relativeToImages.replace('\\', '/'));
        return file.exists() && !file.isDirectory();
    }

    private ProjectileAnimationSpec specFromSprite(Projectile projectile) {
        String imageId = ProjectileLooks.imageId(projectile);
        if (imageId == null) {
            return null;
        }
        return new ProjectileAnimationSpec(
            null,
            null,
            null,
            imageId,
            ProjectileLooks.spriteScale(projectile.getOwnerPlant()),
            0f,
            0f,
            false
        );
    }

    private static ProjectileAnimationSpec withScale(
        ProjectileAnimationSpec spec,
        float scale
    ) {
        return new ProjectileAnimationSpec(
            spec.getPamPath(),
            spec.getClip(),
            spec.getPart(),
            spec.getImageResourceId(),
            scale,
            spec.getOffsetX(),
            spec.getOffsetY(),
            spec.isFreezeFrame()
        );
    }

    private boolean isOwnerPlantAnimation(String path, Plant plant) {
        if (path == null || plant == null) {
            return false;
        }

        String upper = path.replace('\\', '/').toUpperCase(Locale.ROOT);
        if (upper.contains("/EFFECTS/")) {
            return false;
        }

        String ownerCompact = ProjectilePamCatalog.compact(plant.getName());
        String fileCompact = ProjectilePamCatalog.compact(fileNameWithoutExtension(path));
        if (!ownerCompact.isEmpty() && ownerCompact.equals(fileCompact)) {
            return true;
        }

        String bodyPath = plantBodyPamPaths.get(plant.getName());
        if (isSameNormalizedPath(path, bodyPath)) {
            return true;
        }

        return upper.contains("/PLANT/") && !isProjectileAssetPath(upper);
    }

    private static boolean isProjectileAssetPath(String upperPath) {
        return upperPath.contains("PROJECTILE")
            || upperPath.contains("/PEA/")
            || upperPath.endsWith("/PEA.PAM")
            || upperPath.contains("FIRE_PEA")
            || upperPath.contains("BULLET");
    }

    private String pickProjectileClip(String pamPath) {
        try {
            List<String> clips = Textures.getPamPlayer().clips(pamPath);
            if (clips == null || clips.isEmpty()) {
                return "idle";
            }
            for (String preferred : new String[]{"idle", "animation", "loop"}) {
                for (String clip : clips) {
                    if (clip != null && preferred.equalsIgnoreCase(clip.trim())) {
                        return clip;
                    }
                }
            }
            return clips.get(0);
        } catch (RuntimeException exception) {
            return "idle";
        }
    }

    private static String fileNameWithoutExtension(String path) {
        String normalized = path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String name = slash < 0 ? normalized : normalized.substring(slash + 1);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

    private static boolean isSameNormalizedPath(String first, String second) {
        return first != null
            && second != null
            && normalizePamPath(first).equals(normalizePamPath(second));
    }

    private static String normalizePamPath(String path) {
        String normalized = path.replace('\\', '/');
        if (normalized.regionMatches(true, 0, "IMAGES/", 0, 7)) {
            normalized = normalized.substring(7);
        }
        return normalized.replaceAll("/+", "/").toUpperCase(Locale.ROOT);
    }

    private String createCacheKey(Projectile projectile) {
        Plant owner = projectile.getOwnerPlant();
        String ownerName = owner == null ? "NO_OWNER" : owner.getName();

        return ownerName
            + "|" + projectile.getBulletType().name()
            + "|" + projectile.getTrajectory().name()
            + "|" + projectile.getVisualVariant().name()
            + "|" + projectile.getDamage()
            + "|" + projectile.getClass().getName();
    }

    private void logMissing(Projectile projectile, String cacheKey) {
        if (!missingKeys.add(cacheKey)) {
            return;
        }

        Gdx.app.error(
            LOG_TAG,
            "No exact, non-ambiguous PAM match for " + describe(projectile)
        );
    }

    private void logAmbiguous(
        Projectile projectile,
        String cacheKey,
        List<Match> matches
    ) {
        if (!ambiguousKeys.add(cacheKey)) {
            return;
        }

        Gdx.app.error(LOG_TAG, "Ambiguous PAM match for " + describe(projectile));
        for (Match match : matches) {
            Gdx.app.log(
                LOG_TAG,
                "Candidate " + match.quality + " -> " + match.candidate.getPath()
            );
        }
    }

    private String describe(Projectile projectile) {
        Plant owner = projectile.getOwnerPlant();
        String ownerName = owner == null ? "null" : owner.getName();

        return "owner=" + ownerName
            + ", bullet=" + projectile.getBulletType()
            + ", trajectory=" + projectile.getTrajectory()
            + ", variant=" + projectile.getVisualVariant();
    }

    private static int countMatches(Set<String> expected, Set<String> actual) {
        int count = 0;
        for (String token : expected) {
            if (actual.contains(token)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isProperSubset(Set<String> smaller, Set<String> larger) {
        return larger.containsAll(smaller) && !smaller.equals(larger);
    }

    private static boolean isConflictingEffectTag(
        String token,
        BulletType currentBullet
    ) {
        for (BulletType type : BulletType.values()) {
            if (type == BulletType.NORMAL || type.name().contains("_")) {
                continue;
            }

            if (type.name().equals(token) && type != currentBullet) {
                return true;
            }
        }
        return false;
    }

    private static BulletType configuredBulletType(Plant plant) {
        Map<String, String> params = plant.getAbilityParams();
        if (params == null) {
            return null;
        }

        String value = params.get("bulletType");
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return BulletType.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static final class Descriptor {

        private final boolean useOwnerIdentity;
        private final String ownerCompact;
        private final Set<String> ownerTokens;
        private final Set<String> tagTokens;
        private final Set<String> bulletTokens;
        private final Set<String> trajectoryTokens;
        private final Set<String> variantTokens;
        private final Set<String> classTokens;
        private final Set<String> allSemanticTokens;

        private Descriptor(
            boolean useOwnerIdentity,
            String ownerCompact,
            Set<String> ownerTokens,
            Set<String> tagTokens,
            Set<String> bulletTokens,
            Set<String> trajectoryTokens,
            Set<String> variantTokens,
            Set<String> classTokens
        ) {
            this.useOwnerIdentity = useOwnerIdentity;
            this.ownerCompact = ownerCompact;
            this.ownerTokens = ownerTokens;
            this.tagTokens = tagTokens;
            this.bulletTokens = bulletTokens;
            this.trajectoryTokens = trajectoryTokens;
            this.variantTokens = variantTokens;
            this.classTokens = classTokens;
            this.allSemanticTokens = mergeTokens(
                ownerTokens,
                tagTokens,
                bulletTokens,
                trajectoryTokens,
                variantTokens,
                classTokens
            );
        }

        private static Descriptor from(Projectile projectile) {
            Plant plant = projectile.getOwnerPlant();
            BulletType configuredType = configuredBulletType(plant);
            boolean transformed = configuredType != null
                && configuredType != projectile.getBulletType();

            String ownerCompact = transformed
                ? ""
                : ProjectilePamCatalog.compact(plant.getName());

            Set<String> ownerTokens = transformed
                ? Collections.emptySet()
                : new HashSet<>(ProjectilePamCatalog.tokenize(plant.getName()));
            if (!transformed && !ownerCompact.isEmpty()) {
                ownerTokens.add(ownerCompact);
            }

            Set<String> tagTokens = buildTagTokens(plant, projectile.getBulletType());
            Set<String> bulletTokens = buildBulletTokens(projectile.getBulletType());
            Set<String> trajectoryTokens = ProjectilePamCatalog.tokenize(
                projectile.getTrajectory().name()
            );
            Set<String> variantTokens = buildVariantTokens(projectile.getVisualVariant());
            Set<String> classTokens = buildClassTokens(projectile);

            return new Descriptor(
                !transformed,
                ownerCompact,
                ownerTokens,
                tagTokens,
                bulletTokens,
                trajectoryTokens,
                variantTokens,
                classTokens
            );
        }

        private static Set<String> buildTagTokens(Plant plant, BulletType bulletType) {
            Set<String> result = new HashSet<>();
            EnumSet<Tag> tags = plant.getTags();

            if (tags == null) {
                return result;
            }

            for (Tag tag : tags) {
                Set<String> tokens = ProjectilePamCatalog.tokenize(tag.name());
                for (String token : tokens) {
                    if (!isConflictingEffectTag(token, bulletType)) {
                        result.add(token);
                    }
                }
            }

            return result;
        }

        private static Set<String> buildBulletTokens(BulletType bulletType) {
            if (bulletType == BulletType.NORMAL) {
                return Collections.emptySet();
            }
            return ProjectilePamCatalog.tokenize(bulletType.name());
        }

        private static Set<String> buildVariantTokens(ProjectileVisualVariant variant) {
            if (variant == null || variant == ProjectileVisualVariant.DEFAULT) {
                return Collections.emptySet();
            }
            return ProjectilePamCatalog.tokenize(variant.name());
        }

        private static Set<String> buildClassTokens(Projectile projectile) {
            if (projectile.getClass() == Projectile.class) {
                return Collections.emptySet();
            }
            return ProjectilePamCatalog.tokenize(projectile.getClass().getSimpleName());
        }

        @SafeVarargs
        private static Set<String> mergeTokens(Set<String>... tokenSets) {
            Set<String> result = new HashSet<>();
            for (Set<String> tokenSet : tokenSets) {
                result.addAll(tokenSet);
            }
            return result;
        }
    }

    private static final class Match {

        private final ProjectilePamCatalog.Candidate candidate;
        private final MatchQuality quality;

        private Match(
            ProjectilePamCatalog.Candidate candidate,
            MatchQuality quality
        ) {
            this.candidate = candidate;
            this.quality = quality;
        }

    }

    private static final class MatchQuality {

        private final int variantMatches;
        private final boolean exactOwnerPath;
        private final int ownerMatches;
        private final int tagMatches;
        private final int bulletMatches;
        private final int classMatches;
        private final int trajectoryMatches;

        private MatchQuality(
            int variantMatches,
            boolean exactOwnerPath,
            int ownerMatches,
            int tagMatches,
            int bulletMatches,
            int classMatches,
            int trajectoryMatches
        ) {
            this.variantMatches = variantMatches;
            this.exactOwnerPath = exactOwnerPath;
            this.ownerMatches = ownerMatches;
            this.tagMatches = tagMatches;
            this.bulletMatches = bulletMatches;
            this.classMatches = classMatches;
            this.trajectoryMatches = trajectoryMatches;
        }

        private boolean dominates(MatchQuality other) {
            boolean noWorse = variantMatches >= other.variantMatches
                && Boolean.compare(exactOwnerPath, other.exactOwnerPath) >= 0
                && ownerMatches >= other.ownerMatches
                && tagMatches >= other.tagMatches
                && bulletMatches >= other.bulletMatches
                && classMatches >= other.classMatches
                && trajectoryMatches >= other.trajectoryMatches;

            boolean strictlyBetter = variantMatches > other.variantMatches
                || Boolean.compare(exactOwnerPath, other.exactOwnerPath) > 0
                || ownerMatches > other.ownerMatches
                || tagMatches > other.tagMatches
                || bulletMatches > other.bulletMatches
                || classMatches > other.classMatches
                || trajectoryMatches > other.trajectoryMatches;

            return noWorse && strictlyBetter;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof MatchQuality other)) {
                return false;
            }

            return variantMatches == other.variantMatches
                && exactOwnerPath == other.exactOwnerPath
                && ownerMatches == other.ownerMatches
                && tagMatches == other.tagMatches
                && bulletMatches == other.bulletMatches
                && classMatches == other.classMatches
                && trajectoryMatches == other.trajectoryMatches;
        }

        @Override
        public int hashCode() {
            int result = variantMatches;
            result = 31 * result + Boolean.hashCode(exactOwnerPath);
            result = 31 * result + ownerMatches;
            result = 31 * result + tagMatches;
            result = 31 * result + bulletMatches;
            result = 31 * result + classMatches;
            result = 31 * result + trajectoryMatches;
            return result;
        }

        @Override
        public String toString() {
            return "variant=" + variantMatches
                + ", ownerPath=" + exactOwnerPath
                + ", owner=" + ownerMatches
                + ", tags=" + tagMatches
                + ", bullet=" + bulletMatches
                + ", class=" + classMatches
                + ", trajectory=" + trajectoryMatches;
        }
    }
}
