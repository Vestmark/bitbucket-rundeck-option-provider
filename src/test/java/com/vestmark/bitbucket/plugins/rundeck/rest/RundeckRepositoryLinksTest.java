package com.vestmark.bitbucket.plugins.rundeck.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;

import com.atlassian.bitbucket.util.NamedLink;

public class RundeckRepositoryLinksTest
{

  @Test
  public void testConstructor()
  {
    NamedLink link = mock(NamedLink.class);
    when(link.getName()).thenReturn("ssh");
    when(link.getHref()).thenReturn("ssh://git@localhost:7999/project_1/rep_1.git");
    RundeckRepositoryLinks links = new RundeckRepositoryLinks(Collections.singleton(link));
    assertThat(links.getCloneLinks()).containsExactly(entry("ssh", "ssh://git@localhost:7999/project_1/rep_1.git"));
  }

}
