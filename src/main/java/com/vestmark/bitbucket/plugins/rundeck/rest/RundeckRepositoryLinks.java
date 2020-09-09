package com.vestmark.bitbucket.plugins.rundeck.rest;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;

import com.atlassian.bitbucket.util.NamedLink;

public class RundeckRepositoryLinks
{

  private Map<String, String> cloneLinks;

  RundeckRepositoryLinks(Set<NamedLink> namedLinks)
  {
    cloneLinks = namedLinks.stream()
        .collect(Collectors.toMap(NamedLink::getName, NamedLink::getHref, (a, b) -> b, TreeMap::new));
  }

  @XmlElement
  public Map<String, String> getCloneLinks()
  {
    return cloneLinks;
  }

  public void setCloneLinks(Map<String, String> cloneLinks)
  {
    this.cloneLinks = cloneLinks;
  }
}
