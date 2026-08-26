package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.workshop.controller.repository.Textures;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.Tag;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.ProjectileVisualVariant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pvz.libpvz.pam.PamPlayer;

public final class ProjectileAnimationResolver {

    private static final String LOG_TAG = "ProjectileAnimationResolver";
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

        Descriptor descriptor = Descriptor.from(projectile);
        String pamPath = findUniquePamPath(projectile, descriptor, cacheKey);
        if (pamPath == null) {
            return null;
        }

        ProjectileAnimationSpec spec = new ProjectileAnimationSpec(
            pamPath,
            null,
            1f,
            0f,
            0f
        );

        resolvedSpecs.put(cacheKey, spec);
        Gdx.app.log(LOG_TAG, "Resolved " + cacheKey + " -> " + pamPath);
        return spec;
    }

    private String findUniquePamPath(
        Projectile projectile,
        Descriptor descriptor,
        String cacheKey
    ) {
        List<Match> matches = collectMatches(projectile, descriptor);
        if (matches.isEmpty()) {
            logMissing(projectile, cacheKey);
            return null;
        }

        List<Match> strongest = removeQualityDominated(matches);
        if (strongest.size() == 1) {
            return strongest.get(0).candidate.getPath();
        }

        List<Match> minimal = removeDominatedByExtraTokens(strongest, descriptor);
        if (minimal.size() == 1) {
            return minimal.get(0).candidate.getPath();
        }

        logAmbiguous(projectile, cacheKey, minimal);
        return null;
    }

    private List<Match> collectMatches(
        Projectile projectile,
        Descriptor descriptor
    ) {
        List<Match> matches = new ArrayList<>();
        String plantBodyPath = resolvePlantBodyPamPath(projectile.getOwnerPlant());

        for (ProjectilePamCatalog.Candidate candidate : catalog.getCandidates()) {
            if (isSamePath(candidate.getPath(), plantBodyPath)) {
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

    private String createCacheKey(Projectile projectile) {
        Plant owner = projectile.getOwnerPlant();
        String ownerName = owner == null ? "NO_OWNER" : owner.getName();

        return ownerName
            + "|" + projectile.getBulletType().name()
            + "|" + projectile.getTrajectory().name()
            + "|" + projectile.getVisualVariant().name()
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

    private static boolean isSamePath(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
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

            Set<String> ownerTokens = transformed
                ? Collections.emptySet()
                : ProjectilePamCatalog.tokenize(plant.getName());

            String ownerCompact = transformed
                ? ""
                : ProjectilePamCatalog.compact(plant.getName());

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
