package de.hbt.pwr.view.aspects;

import de.hbt.pwr.view.model.ViewProfile;
import de.hbt.pwr.view.model.skill.Category;
import de.hbt.pwr.view.model.skill.Skill;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@Aspect @Component public class ViewProfileRestoreAspect {

    private static final Logger LOG = LogManager.getLogger(ViewProfileRestoreAspect.class);

    void restore(ViewProfile viewProfile) {

        viewProfile.getDisplayCategories().forEach(category -> {
            category.getDisplaySkills().forEach(skill -> {
                skill.setDisplayCategory(category);
                if (skill.getVersions() == null) {
                    skill.setVersions(new ArrayList<>());
                }
            });
        });
        // merge duplicates categories
        viewProfile.setDisplayCategories(mergeDuplicates(viewProfile.getDisplayCategories()));
        // delete empty categories
        viewProfile.setDisplayCategories(viewProfile.getDisplayCategories().stream()
                .filter(category -> category.getDisplaySkills().size() > 0)
                .collect(Collectors.toList()));

    }

    private Category mergeChildren(Category c1, Category c2) {
        List<Skill> skills = Stream.of(c1.getDisplaySkills(), c2.getDisplaySkills())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        c1.setDisplaySkills(skills);
        return c1;
    }

    private List<Category> mergeDuplicates(Collection<Category> displayCategories) {
        Map<Long, Category> merged = displayCategories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity(), this::mergeChildren));
        return displayCategories.stream()
                .map(Category::getId)
                .distinct()
                .map(merged::get)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused") @Pointcut("this(de.hbt.pwr.view.repo.ViewProfileRepository)")
    private void isCrudRepo() {
    } //NOSONAR

    @AfterReturning(value = "isCrudRepo()", returning = "viewProfile", argNames = "joinPoint,viewProfile")
    private void anyViewProfileRestorable(JoinPoint joinPoint, Optional<?> viewProfile) { //NOSONAR
        LOG.debug(ViewProfileRestoreAspect.class + " invoked after returning from " + joinPoint
                .getSignature().toString() + ". Performing bi reference restoring...");
        if (viewProfile.isPresent()) {
            Object mayBeViewProfile = viewProfile.get();
            if (mayBeViewProfile instanceof ViewProfile) {
                LOG.debug("... done");
                restore((ViewProfile) mayBeViewProfile);
            } else {
                LOG.debug("... failed because of wrong class");
            }
        } else {
            LOG.debug("... failed because not present");
        }
    }
}
