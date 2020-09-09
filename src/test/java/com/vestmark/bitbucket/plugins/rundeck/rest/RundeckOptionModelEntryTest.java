package com.vestmark.bitbucket.plugins.rundeck.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RundeckOptionModelEntryTest
{

  @Test
  public void testCompareTo()
  {
    RundeckOptionModelEntry entryOne = new RundeckOptionModelEntry("name", "value");
    RundeckOptionModelEntry entryTwo = new RundeckOptionModelEntry("name2", "value");
    RundeckOptionModelEntry entryThree = new RundeckOptionModelEntry(null, "value");
    assertThat(entryOne.compareTo(entryTwo)).isLessThan(0);
    assertThat(entryOne.compareTo(entryThree)).isLessThan(0);
    assertThat(entryTwo.compareTo(entryOne)).isGreaterThan(0);
    assertThat(entryThree.compareTo(entryTwo)).isGreaterThan(0);
    assertThat(entryOne.compareTo(entryOne)).isEqualTo(0);
    assertThat(entryThree.compareTo(entryThree)).isEqualTo(0);
  }
}
