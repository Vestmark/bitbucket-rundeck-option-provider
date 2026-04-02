package com.vestmark.bitbucket.plugins.rundeck.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import com.atlassian.bitbucket.util.PageRequest;

@SuppressWarnings("unchecked")
public class RundeckOptionModelResourceTest
{

  private ProjectService projectService;
  private RepositoryService repositoryService;
  private RefService refService;
  private RundeckOptionModelResource resource;

  @Before
  public void setUp()
  {
    projectService = mock(ProjectService.class);
    repositoryService = mock(RepositoryService.class);
    refService = mock(RefService.class);
    resource = new RundeckOptionModelResource(projectService, repositoryService, refService);
  }

  @Test
  public void testGetProjects()
  {
    Project project = mock(Project.class);
    when(project.getName()).thenReturn("Project 1");
    when(project.getKey()).thenReturn("PROJ1");
    Page<Project> page = mock(Page.class);
    when(page.getValues()).thenReturn(Collections.singletonList(project));
    when(projectService.findAll(any(PageRequest.class))).thenReturn(page);

    Response response = resource.getProjects(null);

    assertThat(response.getStatus()).isEqualTo(200);
    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).getName()).isEqualTo("Project 1");
    assertThat(entries.get(0).getValue()).isEqualTo("PROJ1");
    assertThat(entries.get(0).isSelected()).isFalse();
  }

  @Test
  public void testGetProjectsWithSelected()
  {
    Project project = mock(Project.class);
    when(project.getName()).thenReturn("Project 1");
    when(project.getKey()).thenReturn("PROJ1");
    Page<Project> page = mock(Page.class);
    when(page.getValues()).thenReturn(Collections.singletonList(project));
    when(projectService.findAll(any(PageRequest.class))).thenReturn(page);

    Response response = resource.getProjects("Project 1");

    assertThat(response.getStatus()).isEqualTo(200);
    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).isSelected()).isTrue();
  }

  @Test
  public void testGetProjectsWithSelectedRegex()
  {
    Project p1 = mock(Project.class);
    when(p1.getName()).thenReturn("Alpha");
    when(p1.getKey()).thenReturn("ALPHA");
    Project p2 = mock(Project.class);
    when(p2.getName()).thenReturn("Beta");
    when(p2.getKey()).thenReturn("BETA");
    Page<Project> page = mock(Page.class);
    when(page.getValues()).thenReturn(java.util.Arrays.asList(p1, p2));
    when(projectService.findAll(any(PageRequest.class))).thenReturn(page);

    Response response = resource.getProjects("alpha");

    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries.stream().filter(RundeckOptionModelEntry::isSelected).count()).isEqualTo(1);
    assertThat(entries.stream().filter(RundeckOptionModelEntry::isSelected).findFirst().get().getValue())
        .isEqualTo("ALPHA");
  }

  @Test
  public void testGetRepositoriesByProject()
  {
    Repository repo = mock(Repository.class);
    when(repo.getName()).thenReturn("repo1");
    when(repo.getSlug()).thenReturn("repo_1");
    Page<Repository> page = mock(Page.class);
    when(page.getValues()).thenReturn(Collections.singletonList(repo));
    when(repositoryService.findByProjectKey(eq("PROJ1"), any(PageRequest.class))).thenReturn(page);

    Response response = resource.getRepositoriesByProject("PROJ1", null);

    assertThat(response.getStatus()).isEqualTo(200);
    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).getName()).isEqualTo("repo1");
    assertThat(entries.get(0).getValue()).isEqualTo("repo_1");
  }

  @Test
  public void testGetRepositoriesByProjectBlankKey()
  {
    assertThat(resource.getRepositoriesByProject("", null).getStatus()).isEqualTo(400);
    assertThat(resource.getRepositoriesByProject(null, null).getStatus()).isEqualTo(400);
    assertThat(resource.getRepositoriesByProject("   ", null).getStatus()).isEqualTo(400);
  }

  @Test
  public void testGetRefsReturns404WhenRepoNotFound()
  {
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(null);

    Response response = resource.getRefs("PROJ1", "repo_1", null);

    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testGetRefs()
  {
    Repository repo = mock(Repository.class);
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(repo);

    Branch branch = mock(Branch.class);
    when(branch.getDisplayId()).thenReturn("main");
    Page<Branch> branchPage = mock(Page.class);
    when(branchPage.getValues()).thenReturn(Collections.singletonList(branch));
    when(refService.getBranches(any(RepositoryBranchesRequest.class), any(PageRequest.class))).thenReturn(branchPage);

    Tag tag = mock(Tag.class);
    when(tag.getDisplayId()).thenReturn("v1.0");
    Page<Tag> tagPage = mock(Page.class);
    when(tagPage.getValues()).thenReturn(Collections.singletonList(tag));
    when(refService.getTags(any(RepositoryTagsRequest.class), any(PageRequest.class))).thenReturn(tagPage);

    Response response = resource.getRefs("PROJ1", "repo_1", null);

    assertThat(response.getStatus()).isEqualTo(200);
    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries).hasSize(2);
    assertThat(entries).extracting(RundeckOptionModelEntry::getName).containsExactlyInAnyOrder("main", "v1.0");
  }

  @Test
  public void testGetRefsWithSelected()
  {
    Repository repo = mock(Repository.class);
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(repo);

    Branch branch = mock(Branch.class);
    when(branch.getDisplayId()).thenReturn("main");
    Page<Branch> branchPage = mock(Page.class);
    when(branchPage.getValues()).thenReturn(Collections.singletonList(branch));
    when(refService.getBranches(any(RepositoryBranchesRequest.class), any(PageRequest.class))).thenReturn(branchPage);

    Page<Tag> tagPage = mock(Page.class);
    when(tagPage.getValues()).thenReturn(Collections.emptyList());
    when(refService.getTags(any(RepositoryTagsRequest.class), any(PageRequest.class))).thenReturn(tagPage);

    Response response = resource.getRefs("PROJ1", "repo_1", "main");

    List<RundeckOptionModelEntry> entries = (List<RundeckOptionModelEntry>) response.getEntity();
    assertThat(entries.get(0).isSelected()).isTrue();
  }

  @Test
  public void testGetRefsBlankProjectKey()
  {
    assertThat(resource.getRefs("", "repo_1", null).getStatus()).isEqualTo(400);
    assertThat(resource.getRefs(null, "repo_1", null).getStatus()).isEqualTo(400);
  }

  @Test
  public void testGetRefsBlankRepoSlug()
  {
    assertThat(resource.getRefs("PROJ1", "", null).getStatus()).isEqualTo(400);
    assertThat(resource.getRefs("PROJ1", null, null).getStatus()).isEqualTo(400);
  }
}
