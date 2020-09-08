package com.vestmark.bitbucket.plugins.rundeck.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.Repository;

public class RundeckOptionModelMapperTest
{

  @Test
  public void testMapProject()
  {
    Project project = mock(Project.class);
    when(project.getName()).thenReturn("Project 1");
    when(project.getKey()).thenReturn("PROJECT_1");
    RundeckOptionModelEntry entry = RundeckOptionModelMapper.map(project);
    assertThat(entry).hasFieldOrPropertyWithValue("name", "Project 1")
        .hasFieldOrPropertyWithValue("value", "PROJECT_1");
  }

  @Test
  public void testMapRepository()
  {
    Repository repo = mock(Repository.class);
    when(repo.getName()).thenReturn("repo1");
    when(repo.getSlug()).thenReturn("repo_1");
    RundeckOptionModelEntry entry = RundeckOptionModelMapper.map(repo);
    assertThat(entry).hasFieldOrPropertyWithValue("name", "repo1").hasFieldOrPropertyWithValue("value", "repo_1");
  }

  @Test
  public void testMapRef()
  {
    MinimalRef ref = mock(MinimalRef.class);
    when(ref.getDisplayId()).thenReturn("some/tag");
    RundeckOptionModelEntry entry = RundeckOptionModelMapper.map(ref);
    assertThat(entry).hasFieldOrPropertyWithValue("name", "some/tag").hasFieldOrPropertyWithValue("value", "some/tag");
  }
}
