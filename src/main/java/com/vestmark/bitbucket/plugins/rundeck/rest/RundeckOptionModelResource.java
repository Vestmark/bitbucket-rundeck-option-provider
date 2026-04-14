package com.vestmark.bitbucket.plugins.rundeck.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PublicApi
@AnonymousAllowed
@Path("options-provider")
public class RundeckOptionModelResource {

  private static final Logger log = LoggerFactory.getLogger(RundeckOptionModelResource.class);

  @ComponentImport
  private final ProjectService projectService;

  @ComponentImport
  private final RepositoryService repositoryService;

  @ComponentImport
  private final RefService refService;

  public RundeckOptionModelResource(
      ProjectService projectService,
      RepositoryService repositoryService,
      RefService refService) {
    this.projectService = projectService;
    this.repositoryService = repositoryService;
    this.refService = refService;
  }

  @GET
  @Path("projects")
  @AnonymousAllowed
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProjects(@QueryParam("selected") String selected) {
    PageRequest pageRequest = new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT);
    Page<Project> projects = projectService.findAll(pageRequest);

    List<RundeckOptionModelEntry> entries =
        StreamSupport.stream(projects.getValues().spliterator(), false)
            .map(RundeckOptionModelMapper::map)
            .sorted()
            .collect(Collectors.toList());

    toggleSelected(entries, selected);
    return Response.ok(entries).build();
  }

  @GET
  @AnonymousAllowed
  @Path("projects/{projectKey}/repos")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRepositoriesByProject(
      @PathParam("projectKey") String projectKey,
      @QueryParam("selected") String selected) {

    PageRequest pageRequest = new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT);
    Page<Repository> repositories = repositoryService.findByProjectKey(projectKey, pageRequest);

    List<RundeckOptionModelEntry> entries =
        StreamSupport.stream(repositories.getValues().spliterator(), false)
            .map(RundeckOptionModelMapper::map)
            .sorted()
            .collect(Collectors.toList());

    toggleSelected(entries, selected);
    return Response.ok(entries).build();
  }

  @GET
  @AnonymousAllowed
  @Path("projects/{projectKey}/repos/{repoSlug}/refs")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRefs(
      @PathParam("projectKey") String projectKey,
      @PathParam("repoSlug") String repoSlug,
      @QueryParam("selected") String selected,
      @QueryParam("filter") String filter,
      @QueryParam("branches") @DefaultValue("true") boolean includeBranches,
      @QueryParam("tags") @DefaultValue("true") boolean includeTags) {

    Repository repository = repositoryService.getBySlug(projectKey, repoSlug);
    if (repository == null) {
      return Response.noContent().build();
    }

    PageRequest pageRequest = new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT);
    List<RundeckOptionModelEntry> entries = new ArrayList<>();

    if (includeBranches) {
      Page<Branch> branchPage =
          refService.getBranches(
              new RepositoryBranchesRequest.Builder(repository).build(),
              pageRequest);

      List<RundeckOptionModelEntry> branchEntries =
          StreamSupport.stream(branchPage.getValues().spliterator(), false)
              .map(RundeckOptionModelMapper::map)
              .filter(e -> e.getName() != null)
              .collect(Collectors.toList());

      entries.addAll(branchEntries);
    }

    if (includeTags) {
      Page<Tag> tagPage =
          refService.getTags(
              new RepositoryTagsRequest.Builder(repository).build(),
              pageRequest);

      List<RundeckOptionModelEntry> tagEntries =
          StreamSupport.stream(tagPage.getValues().spliterator(), false)
              .map(RundeckOptionModelMapper::map)
              .filter(e -> e.getName() != null)
              .collect(Collectors.toList());

      entries.addAll(tagEntries);
    }

    if (StringUtils.isNotBlank(filter)) {
      final Pattern filterPattern;
      try {
        filterPattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
      } catch (PatternSyntaxException e) {
        log.warn("Invalid filter regex: {}", filter, e);
        return Response.status(Response.Status.BAD_REQUEST)
            .entity("Invalid filter regex: " + filter)
            .build();
      }

      entries = entries.stream()
          .filter(e -> filterPattern.matcher(e.getName()).matches())
          .collect(Collectors.toList());
    }

    Collections.sort(entries);
    toggleSelected(entries, selected);
    return Response.ok(entries).build();
  }

  private void toggleSelected(List<RundeckOptionModelEntry> entries, String selected) {
    if (StringUtils.isNotBlank(selected)) {
      Pattern selectedPattern = Pattern.compile(selected, Pattern.CASE_INSENSITIVE);
      entries.forEach(x -> {
        if (x.getName() != null && selectedPattern.matcher(x.getName()).matches()) {
          x.setSelected(true);
        }
      });
    }
  }
}