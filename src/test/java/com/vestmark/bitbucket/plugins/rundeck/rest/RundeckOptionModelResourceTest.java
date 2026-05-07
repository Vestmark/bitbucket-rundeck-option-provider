package com.vestmark.bitbucket.plugins.rundeck.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.RefService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryBranchesRequest;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.repository.RepositoryTagsRequest;
import com.atlassian.bitbucket.repository.Tag;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageImpl;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageRequestImpl;

@SuppressWarnings("unchecked")
public class RundeckOptionModelResourceTest {

  private ProjectService projectService;
  private RepositoryService repositoryService;
  private RefService refService;
  private RundeckOptionModelResource resource;

  @Before
  public void setUp() {
    projectService = mock(ProjectService.class);
    repositoryService = mock(RepositoryService.class);
    refService = mock(RefService.class);
    resource = new RundeckOptionModelResource(projectService, repositoryService, refService);
  }

  private <T> Page<T> singlePage(List<T> values) {
    return new PageImpl<T>(
        new PageRequestImpl(0, values.isEmpty() ? 1 : values.size()),
        values,
        true);
  }

  @Test
  public void testGetProjects() {
    Project project = mock(Project.class);
    when(project.getName()).thenReturn("Project 1");
    when(project.getKey()).thenReturn("PROJ1");

    when(projectService.findAll(any(PageRequest.class)))
        .thenReturn(singlePage(Collections.singletonList(project)));

    Response response = resource.getProjects(null);

    assertThat(response.getStatus()).isEqualTo(200);
    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).getName()).isEqualTo("Project 1");
    assertThat(entries.get(0).getValue()).isEqualTo("PROJ1");
    assertThat(entries.get(0).isSelected()).isFalse();
  }

  @Test
  public void testGetProjectsWithSelected() {
    Project project = mock(Project.class);
    when(project.getName()).thenReturn("Project 1");
    when(project.getKey()).thenReturn("PROJ1");

    when(projectService.findAll(any(PageRequest.class)))
        .thenReturn(singlePage(Collections.singletonList(project)));

    Response response = resource.getProjects("Project 1");

    assertThat(response.getStatus()).isEqualTo(200);
    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).isSelected()).isTrue();
  }

  @Test
  public void testGetProjectsWithSelectedRegex() {
    Project p1 = mock(Project.class);
    when(p1.getName()).thenReturn("Alpha");
    when(p1.getKey()).thenReturn("ALPHA");

    Project p2 = mock(Project.class);
    when(p2.getName()).thenReturn("Beta");
    when(p2.getKey()).thenReturn("BETA");

    when(projectService.findAll(any(PageRequest.class)))
        .thenReturn(singlePage(Arrays.asList(p1, p2)));

    Response response = resource.getProjects("alpha");

    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries.stream().filter(RundeckOptionModelEntry::isSelected).count()).isEqualTo(1);
    assertThat(entries.stream()
        .filter(RundeckOptionModelEntry::isSelected)
        .findFirst()
        .get()
        .getValue()).isEqualTo("ALPHA");
  }

  @Test
  public void testGetRepositoriesByProject() {
    Repository repo = mock(Repository.class);
    when(repo.getName()).thenReturn("repo1");
    when(repo.getSlug()).thenReturn("repo_1");

    when(repositoryService.findByProjectKey(eq("PROJ1"), any(PageRequest.class)))
        .thenReturn(singlePage(Collections.singletonList(repo)));

    Response response = resource.getRepositoriesByProject("PROJ1", null);

    assertThat(response.getStatus()).isEqualTo(200);
    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).getName()).isEqualTo("repo1");
    assertThat(entries.get(0).getValue()).isEqualTo("repo_1");
  }

  @Test(expected = NullPointerException.class)
  public void testGetRepositoriesByProjectBlankKey() {
    resource.getRepositoriesByProject("", null);
  }

  @Test
  public void testGetRefsReturns204WhenRepoNotFound() {
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(null);

    Response response = resource.getRefs("PROJ1", "repo_1", null, null, true, true);

    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  public void testGetRefs() {
    Repository repo = mock(Repository.class);
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(repo);

    Branch branch = mock(Branch.class);
    when(branch.getDisplayId()).thenReturn("main");
    when(refService.getBranches(any(RepositoryBranchesRequest.class), any(PageRequest.class)))
        .thenReturn(singlePage(Collections.singletonList(branch)));

    Tag tag = mock(Tag.class);
    when(tag.getDisplayId()).thenReturn("v1.0");
    when(refService.getTags(any(RepositoryTagsRequest.class), any(PageRequest.class)))
        .thenReturn(singlePage(Collections.singletonList(tag)));

    Response response = resource.getRefs("PROJ1", "repo_1", null, null, true, true);

    assertThat(response.getStatus()).isEqualTo(200);
    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(2);
    assertThat(entries).extracting(RundeckOptionModelEntry::getName)
        .containsExactlyInAnyOrder("main", "v1.0");
  }

  @Test
  public void testGetRefsWithSelected() {
    Repository repo = mock(Repository.class);
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(repo);

    Branch branch = mock(Branch.class);
    when(branch.getDisplayId()).thenReturn("main");
    when(refService.getBranches(any(RepositoryBranchesRequest.class), any(PageRequest.class)))
        .thenReturn(singlePage(Collections.singletonList(branch)));

    when(refService.getTags(any(RepositoryTagsRequest.class), any(PageRequest.class)))
        .thenReturn(singlePage(Collections.emptyList()));

    Response response = resource.getRefs("PROJ1", "repo_1", "main", null, true, false);

    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).isSelected()).isTrue();
  }

  @Test
  public void testGetRefsBlankProjectKey() {
    assertThat(resource.getRefs("", "repo_1", null, null, true, true).getStatus()).isEqualTo(204);
    assertThat(resource.getRefs(null, "repo_1", null, null, true, true).getStatus()).isEqualTo(204);
  }

  @Test
  public void testGetRefsBlankRepoSlug() {
    assertThat(resource.getRefs("PROJ1", "", null, null, true, true).getStatus()).isEqualTo(204);
    assertThat(resource.getRefs("PROJ1", null, null, null, true, true).getStatus()).isEqualTo(204);
  }
}