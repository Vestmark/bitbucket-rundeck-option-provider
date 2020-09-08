package com.vestmark.bitbucket.plugins.rundeck.rest;

import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.repository.MinimalRef;
import com.atlassian.bitbucket.repository.Repository;

public class RundeckOptionModelMapper
{

  static RundeckOptionModelEntry map(Project project)
  {
    return new RundeckOptionModelEntry(project.getName(), project.getKey());
  }

  static RundeckOptionModelEntry map(Repository repository)
  {
    return new RundeckOptionModelEntry(repository.getName(), repository.getSlug());
  }

  static RundeckOptionModelEntry map(MinimalRef ref)
  {
    return new RundeckOptionModelEntry(ref.getDisplayId(), ref.getDisplayId());
  }
}
