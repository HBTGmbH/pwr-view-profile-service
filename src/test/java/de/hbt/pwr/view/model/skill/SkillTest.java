package de.hbt.pwr.view.model.skill;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SkillTest {

    @Test
    public void backReferenceShouldBeSetForDisplay() {
        Skill skill = Skill.builder().name("Skill1").build();

        Category category =  new Category("Category");

        skill.setDisplayCategory(category);
        assertThat(category.getDisplaySkills()).contains(skill);
    }
}