package de.hbt.pwr.view.service;


import de.hbt.pwr.view.client.profile.ProfileServiceClient;
import de.hbt.pwr.view.client.profile.model.*;
import de.hbt.pwr.view.client.skill.SkillServiceClient;
import de.hbt.pwr.view.model.LanguageLevel;
import de.hbt.pwr.view.model.ViewProfile;
import de.hbt.pwr.view.model.entries.*;
import de.hbt.pwr.view.model.entries.sort.NameComparableEntryType;
import de.hbt.pwr.view.model.entries.sort.StartEndDateComparableEntryType;
import de.hbt.pwr.view.model.skill.Skill;
import de.hbt.pwr.view.repo.ViewProfileRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * Validates that the view profile import works for all
 * profiles entries (Everything except skills). Skills get an extra test class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ViewProfileImporterEntryTest {

    private ViewProfileImporter viewProfileImporter;

    @MockBean
    private ProfileServiceClient profileServiceClient;

    @MockBean
    private SkillServiceClient skillServiceClient;

    @MockBean
    private ViewProfileRepository viewProfileRepository;

    @MockBean
    private ViewProfileSortService viewProfileSortService;

    private final String initials = "eu";

    private Profile profileToReturn;

    private ViewProfile viewProfile;

    private static final AtomicLong idCounter = new AtomicLong(0);

    private static LocalDate defaultDate = LocalDate.of(2017, 3, 3);
    private static LocalDate defaultEndDate = LocalDate.of(2017, 4, 27);
    private static String defaultDegree = "Blumentopferde";

    private final String entryName1 = "Entry1";
    private final String entryName2 = "Entry2";
    private final List<String> entryNames = Arrays.asList(entryName1, entryName2);

    @Before
    public void setUp() throws Exception {
        viewProfile = new ViewProfile();
        profileToReturn = new Profile();
        given(profileServiceClient.getSingleProfile(initials)).willReturn(profileToReturn);
        viewProfileImporter = new ViewProfileImporter(profileServiceClient, skillServiceClient, null,
                viewProfileRepository, viewProfileSortService);
    }

    @After
    public void tearDown() throws Exception {
        profileToReturn = new Profile();
        BDDMockito.reset(profileServiceClient);
    }


    private void addTestLanguageSkills() {
        for (String entryName : entryNames) {
            LanguageSkill languageSkill =
                    new LanguageSkill(idCounter.getAndIncrement(), NameEntity.builder().name(entryName).build() , LanguageLevel.ADVANCED);
            profileToReturn.getLanguages().add(languageSkill);
        }

    }

    private void invokeImport() {
        viewProfile = viewProfileImporter.importViewProfile(initials);
    }

    private void addTestQualificationEntries() {
        for (String entryName : entryNames) {
            QualificationEntry qualificationEntry = new QualificationEntry();
            qualificationEntry.setDate(defaultDate);
            qualificationEntry.setId(idCounter.getAndIncrement());
            qualificationEntry.setNameEntity(NameEntity.builder().name(entryName).build());
            profileToReturn.getQualification().add(qualificationEntry);
        }
    }

    private void addTestTrainingEntries() {
        for (String entryName : entryNames) {
            StepEntry stepEntry = new StepEntry();
            stepEntry.setEndDate(defaultEndDate);
            stepEntry.setStartDate(defaultDate);
            stepEntry.setId(idCounter.getAndIncrement());
            stepEntry.setNameEntity(NameEntity.builder().name(entryName).build());
            profileToReturn.getTrainingEntries().add(stepEntry);
        }
    }

    private void addTestEducationEntries() {
        for (String entryName: entryNames) {
            EducationEntry educationEntry = new EducationEntry();
            educationEntry.setDegree(defaultDegree);
            educationEntry.setId(idCounter.getAndIncrement());
            educationEntry.setNameEntity(NameEntity.builder().name(entryName).build());
            educationEntry.setEndDate(defaultEndDate);
            educationEntry.setStartDate(defaultDate);
            profileToReturn.getEducation().add(educationEntry);
        }
    }

    private void addTestSectorEntries() {
        for (String entryName: entryNames) {
            ProfileEntry profileEntry = new ProfileEntry();
            profileEntry.setId(idCounter.getAndIncrement());
            profileEntry.setNameEntity(NameEntity.builder().name(entryName).build());
            profileToReturn.getSectors().add(profileEntry);
        }
    }

    private void addTestCareerEntries() {
        for (String entryName: entryNames) {
            StepEntry stepEntry = new StepEntry();
            stepEntry.setNameEntity(NameEntity.builder().name(entryName).build());
            stepEntry.setId(idCounter.getAndIncrement());
            stepEntry.setStartDate(defaultDate);
            stepEntry.setEndDate(defaultEndDate);
            profileToReturn.getCareerEntries().add(stepEntry);
        }
    }

    private void addTestKeySkillEntries() {
        for (String entryName: entryNames) {
            ProfileEntry profileEntry = new ProfileEntry();
            profileEntry.setId(idCounter.getAndIncrement());
            profileEntry.setNameEntity(NameEntity.builder().name(entryName).build());
            profileToReturn.getKeySkillEntries().add(profileEntry);
        }
    }


    @Test
    public void shouldHaveCorrectDescription() {
        String description = "sad+**dx#saf";
        profileToReturn.setDescription(description);
        invokeImport();
        assertThat(viewProfile.getDescription()).isEqualTo(description);
    }

    /**
     * Checks that languages are correctly important and are enabled.
     */
    @Test
    public void shouldHaveCorrectAndEnabledLanguages() {
        addTestLanguageSkills();
        Language expected1 = Language.builder().level(LanguageLevel.ADVANCED).name(entryName1).enabled(true).build();
        Language expected2 = Language.builder().level(LanguageLevel.ADVANCED).name(entryName2).enabled(true).build();
        invokeImport();
        assertThat(viewProfile.getLanguages()).containsExactlyInAnyOrder(expected1, expected2);
    }

    @Test
    public void shouldHaveCorrectAndEnabledQualifications() {
        addTestQualificationEntries();
        Qualification expected1 = Qualification.builder().date(defaultDate).enabled(true).name(entryName1).build();
        Qualification expected2 = Qualification.builder().date(defaultDate).enabled(true).name(entryName2).build();
        invokeImport();
        assertThat(viewProfile.getQualifications()).containsExactlyInAnyOrder(expected1, expected2);
    }

    @Test
    public void shouldHaveCorrectAndEnabledTrainings() {
        addTestTrainingEntries();
        Training training1 = Training.builder().startDate(defaultDate).endDate(defaultEndDate).enabled(true).name(entryName1).build();
        Training training2 = Training.builder().startDate(defaultDate).endDate(defaultEndDate).enabled(true).name(entryName2).build();
        invokeImport();
        assertThat(viewProfile.getTrainings()).containsExactlyInAnyOrder(training1, training2);
    }

    @Test
    public void shouldHaveCorrectAndEnabledEducations() {
        addTestEducationEntries();
        Education education1 = Education.builder().endDate(defaultEndDate).startDate(defaultDate).name(entryName1).degree(defaultDegree).enabled(true).build();
        Education education2 = Education.builder().endDate(defaultEndDate).startDate(defaultDate).name(entryName2).degree(defaultDegree).enabled(true).build();
        invokeImport();
        assertThat(viewProfile.getEducations()).containsExactlyInAnyOrder(education1, education2);
    }

    @Test
    public void shouldHaveCorrectAndEnabledSectors() {
        addTestSectorEntries();
        Sector sector1 = Sector.builder().enabled(true).name(entryName1).build();
        Sector sector2 = Sector.builder().enabled(true).name(entryName2).build();
        invokeImport();
        assertThat(viewProfile.getSectors()).containsExactlyInAnyOrder(sector1, sector2);
    }

    @Test
    public void shouldHaveCorrectAndEnabledCareers() {
        addTestCareerEntries();
        Career career1 = Career.builder().enabled(true).endDate(defaultEndDate).startDate(defaultDate).name(entryName1).build();
        Career career2 = Career.builder().enabled(true).endDate(defaultEndDate).startDate(defaultDate).name(entryName2).build();
        invokeImport();
        assertThat(viewProfile.getCareers()).containsExactlyInAnyOrder(career1, career2);
    }

    @Test
    public void shouldHaveCorrectAndEnabledKeySkills() {
        addTestKeySkillEntries();
        KeySkill keySkill1 = KeySkill.builder().enabled(true).name(entryName1).build();
        KeySkill keySkill2 = KeySkill.builder().enabled(true).name(entryName2).build();
        invokeImport();
        assertThat(viewProfile.getKeySkills()).containsExactlyInAnyOrder(keySkill1, keySkill2);
    }

    @Test
    public void shouldHaveCorrectAndEnabledProjects() {
        ProfileProject profileProject = new ProfileProject();
        profileProject.getSkills().add(new ProfileSkill("Skill1"));
        profileProject.getSkills().add(new ProfileSkill("Skill2"));
        profileProject.setBroker(NameEntity.builder().name("Broker").build());
        profileProject.setClient(NameEntity.builder().name("Client").build());
        profileProject.setDescription("Description");
        profileProject.setEndDate(defaultEndDate);
        profileProject.setStartDate(defaultDate);
        profileProject.setName("ProjectName");
        profileProject.getProjectRoles().add(NameEntity.builder().name("Role1").build());
        profileProject.getProjectRoles().add(NameEntity.builder().name("Role2").build());
        profileToReturn.getProjects().add(profileProject);
        invokeImport();
        Project project = Project.builder()
                .name("ProjectName")
                .skills(Arrays.asList(Skill.builder().name("Skill1").enabled(true).build(), Skill.builder().name("Skill2").enabled(true).build()))
                .enabled(true)
                .projectRoles(Arrays.asList(ProjectRole.builder().name("Role1").enabled(true).build(), ProjectRole.builder().name("Role2").enabled(true).build()))
                .broker("Broker")
                .client("Client")
                .description("Description")
                .endDate(defaultEndDate)
                .startDate(defaultDate)
                .name("ProjectName")
                .build();
        assertThat(viewProfile.getProjects()).containsExactly(project);
    }

    @Test
    public void shouldHaveEnabledProjectRoles() {
        ProfileProject profileProject1 = new ProfileProject();
        profileProject1.getProjectRoles().add(NameEntity.builder().name("Role1").build());
        profileProject1.getProjectRoles().add(NameEntity.builder().name("Role2").build());

        ProfileProject profileProject2 = new ProfileProject();
        profileProject1.getProjectRoles().add(NameEntity.builder().name("Role2").build());
        profileProject1.getProjectRoles().add(NameEntity.builder().name("Role3").build());

        profileToReturn.getProjects().add(profileProject1);
        profileToReturn.getProjects().add(profileProject2);

        invokeImport();
        ProjectRole expected1 = ProjectRole.builder().name("Role1").enabled(true).build();
        ProjectRole expected2 = ProjectRole.builder().name("Role2").enabled(true).build();
        ProjectRole expected3 = ProjectRole.builder().name("Role3").enabled(true).build();

        assertThat(viewProfile.getProjectRoles()).containsExactlyInAnyOrder(expected1, expected2, expected3);
    }

    private void testNameSorting(NameComparableEntryType type) {
        invokeImport();
        then(viewProfileSortService)
                .should(times(1))
                .sortEntryByName(viewProfile, type, true);
    }

    private void testStartDateSorting(StartEndDateComparableEntryType type) {
        invokeImport();
        then(viewProfileSortService)
                .should(times(1))
                .sortEntryByStartDate(viewProfile,type, true);
    }

    /**
     * Newly imported view profiles have their sectors sorted from by name from 'a' - 'z' (alpha asc.)
     */
    @Test
    public void shouldSortSectorsByNameAscending() {
        testNameSorting(NameComparableEntryType.SECTOR);
    }

    /**
     * Newly imported view profile have their career sorted from oldest to newest by start date
     */
    @Test
    public void shouldSortCareersByStartDateAscending() {
        testStartDateSorting(StartEndDateComparableEntryType.CAREER);
    }

    @Test
    public void shouldSortEducationsByStartDateAscending() {
        testStartDateSorting(StartEndDateComparableEntryType.EDUCATION);
    }

    @Test
    public void shouldSortKeySkillsByNameAscending() {
        testNameSorting(NameComparableEntryType.KEY_SKILL);
    }

    @Test
    public void shouldSortLanguagesByNameAscending() {
        testNameSorting(NameComparableEntryType.LANGUAGE);
    }

    @Test
    public void shouldSortQualificationByNameAscending() {
        testNameSorting(NameComparableEntryType.QUALIFICATION);
    }

    @Test
    public void shouldSortTrainingByNameAscending() {
        testNameSorting(NameComparableEntryType.TRAINING);
    }

    @Test
    public void shouldSortProjectRolesByNameAscending() {
        testNameSorting(NameComparableEntryType.PROJECT_ROLE);
    }

    @Test
    public void shouldSortProejctByStartDateAsc() {
        testStartDateSorting(StartEndDateComparableEntryType.PROJECT);
    }

    @Test
    public void shouldSortDisplayCategoriesByNameAsc() {
        testNameSorting(NameComparableEntryType.DISPLAY_CATEGORY);
    }

    @Test
    public void shouldSortSkillsInProjectsByNameAsc() {
        profileToReturn.getProjects().add(new ProfileProject());
        invokeImport();
        then(viewProfileSortService)
                .should(times(1))
                .sortSkillsInProjectByName(viewProfile, 0, true);
    }
}