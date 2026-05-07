package com.vestmark.bitbucket.plugins.rundeck.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryCloneLinksRequest;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.util.NamedLink;

public class RundeckRepositoryResourceTest
{

  private RepositoryService repositoryService;
  private RundeckRepositoryResource resource;

  @Before
  public void setUp()
  {
    repositoryService = mock(RepositoryService.class);
    resource = new RundeckRepositoryResource(repositoryService);
  }

  @Test
  public void testGetCloneLinksReturns404WhenRepoNotFound()
  {
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(null);

    Response response = resource.getRepositoryCloneLinks("PROJ1", "repo_1", "ssh");

    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void testGetCloneLinksReturns400WhenProjectKeyBlank()
  {
    assertThat(resource.getRepositoryCloneLinks("", "repo_1", "ssh").getStatus()).isEqualTo(400);
    assertThat(resource.getRepositoryCloneLinks(null, "repo_1", "ssh").getStatus()).isEqualTo(400);
    assertThat(resource.getRepositoryCloneLinks("   ", "repo_1", "ssh").getStatus()).isEqualTo(400);
  }

  @Test
  public void testGetCloneLinksReturns400WhenRepoSlugBlank()
  {
    assertThat(resource.getRepositoryCloneLinks("PROJ1", "", "ssh").getStatus()).isEqualTo(400);
    assertThat(resource.getRepositoryCloneLinks("PROJ1", null, "ssh").getStatus()).isEqualTo(400);
    assertThat(resource.getRepositoryCloneLinks("PROJ1", "   ", "ssh").getStatus()).isEqualTo(400);
  }

  @Test
  public void testGetCloneLinks()
  {
    Repository repo = mock(Repository.class);
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(repo);

    NamedLink link = mock(NamedLink.class);
    when(link.getName()).thenReturn("ssh");
    when(link.getHref()).thenReturn("ssh://git@localhost:7999/proj1/repo_1.git");
    when(repositoryService.getCloneLinks(any(RepositoryCloneLinksRequest.class)))
        .thenReturn(Collections.singleton(link));

    Response response = resource.getRepositoryCloneLinks("PROJ1", "repo_1", "ssh");

    assertThat(response.getStatus()).isEqualTo(200);
    RundeckRepositoryLinks links = (RundeckRepositoryLinks) response.getEntity();
    assertThat(links.getCloneLinks()).containsEntry("ssh", "ssh://git@localhost:7999/proj1/repo_1.git");
  }

  @Test
  public void testGetCloneLinksDefaultProtocolIsSsh()
  {
    Repository repo = mock(Repository.class);
    when(repositoryService.getBySlug("PROJ1", "repo_1")).thenReturn(repo);
    when(repositoryService.getCloneLinks(any(RepositoryCloneLinksRequest.class)))
        .thenReturn(Collections.emptySet());

    Response response = resource.getRepositoryCloneLinks("PROJ1", "repo_1", "ssh");

    assertThat(response.getStatus()).isEqualTo(200);
  }
}
