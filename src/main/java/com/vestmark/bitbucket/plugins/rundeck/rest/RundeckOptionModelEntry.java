package com.vestmark.bitbucket.plugins.rundeck.rest;

import javax.xml.bind.annotation.XmlElement;

public class RundeckOptionModelEntry
    implements Comparable<RundeckOptionModelEntry>
{

  private String name;
  private String value;

  public RundeckOptionModelEntry()
  {
  }

  public RundeckOptionModelEntry(String name, String value)
  {
    this.name = name;
    this.value = value;
  }

  @XmlElement
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @XmlElement
  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  @Override
  public int compareTo(RundeckOptionModelEntry o)
  {
    if (name == null) {
      if (o.getName() == null) {
        return 0;
      }
      return 1;
    }
    else if (o.getName() == null) {
      return -1;
    }
    return name.compareTo(o.getName());
  }
}
