package com.vestmark.bitbucket.plugins.rundeck.rest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.annotations.PublicApi;
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
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

@PublicApi
@AnonymousAllowed
@Path("options-provider")
public class RundeckOptionModelResource
{

  @ComponentImport
  private ProjectService projectService;
  @ComponentImport
  private RepositoryService repositoryService;
  @ComponentImport
  private RefService refService;

  public RundeckOptionModelResource(
      ProjectService projectService,
      RepositoryService repositoryService,
      RefService refService)
  {
    this.projectService = projectService;
    this.repositoryService = repositoryService;
    this.refService = refService;
  }

  @GET
  @Path("projects")
  @AnonymousAllowed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProjects()
  {
    PageRequest pageRequest = new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT);
    Page<Project> projects = projectService.findAll(pageRequest);
    List<RundeckOptionModelEntry> entries = StreamSupport.stream(projects.getValues().spliterator(), false)
        .map(RundeckOptionModelMapper::map)
        .sorted()
        .collect(Collectors.toList());
    return Response.ok(entries).build();
  }

  @GET
  @AnonymousAllowed
  @Path("projects/{projectKey}/repos")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRepositoriesByProject(@PathParam("projectKey") String projectKey)
  {
    PageRequest pageRequest = new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT);
    Page<Repository> repositories = repositoryService.findByProjectKey(projectKey, pageRequest);
    List<RundeckOptionModelEntry> entries = StreamSupport.stream(repositories.getValues().spliterator(), false)
        .map(RundeckOptionModelMapper::map)
        .collect(Collectors.toList());
    return Response.ok(entries).build();
  }

  @GET
  @AnonymousAllowed
  @Path("projects/{projectKey}/repos/{repoSlug}/refs")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRefs(@PathParam("projectKey") String projectKey, @PathParam("repoSlug") String repoSlug)
  {
    Repository repository = repositoryService.getBySlug(projectKey, repoSlug);
    if (repository == null) {
      return Response.noContent().build();
    }
    RepositoryBranchesRequest branchesRequest = new RepositoryBranchesRequest.Builder(repository).build();
    PageRequest pageRequest = new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT);
    Page<Branch> branches = refService.getBranches(branchesRequest, pageRequest);
    RepositoryTagsRequest tagsRequest = new RepositoryTagsRequest.Builder(repository).build();
    Page<Tag> tags = refService.getTags(tagsRequest, pageRequest);
    List<RundeckOptionModelEntry> entries = StreamSupport.stream(branches.getValues().spliterator(), false)
        .map(RundeckOptionModelMapper::map)
        .collect(Collectors.toList());
    entries.addAll(
        StreamSupport.stream(tags.getValues().spliterator(), false)
            .map(RundeckOptionModelMapper::map)
            .collect(Collectors.toList()));
    Collections.sort(entries);
    return Response.ok(entries).build();
  }
}
