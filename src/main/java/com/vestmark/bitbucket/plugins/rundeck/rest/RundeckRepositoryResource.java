package com.vestmark.bitbucket.plugins.rundeck.rest;

import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.annotations.PublicApi;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryCloneLinksRequest;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.util.NamedLink;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

@PublicApi
@AnonymousAllowed
@Path("resources")
public class RundeckRepositoryResource
{

  @ComponentImport
  private RepositoryService repositoryService;

  public RundeckRepositoryResource(RepositoryService repositoryService)
  {
    this.repositoryService = repositoryService;
  }

  @GET
  @AnonymousAllowed
  @Path("projects/{projectKey}/repos/{repoSlug}/links")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRepositoryCloneLinks(
      @PathParam("projectKey") String projectKey,
      @PathParam("repoSlug") String repoSlug,
      @QueryParam("protocol") @DefaultValue("ssh") String protocol)
  {
    Repository repository = repositoryService.getBySlug(projectKey, repoSlug);
    if (repository == null) {
      return Response.noContent().build();
    }
    RepositoryCloneLinksRequest cloneLinksRequest = new RepositoryCloneLinksRequest.Builder().repository(repository)
        .protocol(protocol)
        .build();
    Set<NamedLink> links = repositoryService.getCloneLinks(cloneLinksRequest);
    return Response.ok(new RundeckRepositoryLinks(links)).build();
  }
}
