package de.hbt.pwr.view.service;

import de.hbt.pwr.view.model.ViewProfile;
import de.hbt.pwr.view.model.entries.Project;
import de.hbt.pwr.view.model.skill.Skill;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
public class ViewProfileProjectSortServiceTest {

    private ViewProfile viewProfile;

    private ViewProfileSortService viewProfileSortService;
    private Skill skill1;
    private Skill skill2;
    private Skill skill3;
    private Skill skill4;


    @Before
    public void setUp() {
        viewProfile = new ViewProfile();
        viewProfileSortService = new ViewProfileSortService();
    }

    private void addSkillProjectData() {
        skill1 = Skill.builder().name("1").rating(1).build();
        skill2 = Skill.builder().name("2").rating(2).build();
        skill3 = Skill.builder().name("3").rating(3).build();
        skill4 = Skill.builder().name("4").rating(4).build();
        Project project = Project.builder().skills(new ArrayList<>(Arrays.asList(skill4, skill3, skill1, skill2))).name("Proj").build();
        viewProfile.getProjects().add(project);
    }


    @Test
    public void shouldBeSortedByNameInProject() {
        addSkillProjectData();
        viewProfileSortService.sortSkillsInProjectByName(viewProfile, 0, true);
        assertThat(viewProfile.getProjects().get(0).getSkills()).containsExactly(skill1, skill2, skill3, skill4);
        viewProfileSortService.sortSkillsInProjectByName(viewProfile, 0, false);
        assertThat(viewProfile.getProjects().get(0).getSkills()).containsExactly(skill4, skill3, skill2, skill1);
    }

    @Test
    public void shouldBeSortedByRatingInProject() {
        addSkillProjectData();
        viewProfileSortService.sortSkillsInProjectByRating(viewProfile, 0, true);
        assertThat(viewProfile.getProjects().get(0).getSkills()).containsExactly(skill1, skill2, skill3, skill4);
        viewProfileSortService.sortSkillsInProjectByRating(viewProfile, 0, false);
        assertThat(viewProfile.getProjects().get(0).getSkills()).containsExactly(skill4, skill3, skill2, skill1);
    }

    @Test
    public void shouldHaveMovedInProject() {
        addSkillProjectData();
        // s4, s3, s1, s2
        // 3 -> 1
        // s4, s2, s3, s1
        viewProfileSortService.moveSkillInProject(viewProfile, 0, 3, 1);
        assertThat(viewProfile.getProjects().get(0).getSkills()).containsExactly(skill4, skill2, skill3, skill1);
        // s4, s2, s3, s1
        // 1 -> 3
        // s4, s3, s1, s2
        viewProfileSortService.moveSkillInProject(viewProfile, 0, 1, 3);
        assertThat(viewProfile.getProjects().get(0).getSkills()).containsExactly(skill4, skill3, skill1, skill2);
    }
}