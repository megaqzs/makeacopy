/*
 * Copyright 2025 Christian Kierdorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package de.schliweb.makeacopy.ui.library;

import static org.junit.Assert.assertEquals;

import de.schliweb.makeacopy.R;
import org.junit.Test;

public class LibraryOptionsPanelTest {

  @Test
  public void policiesRoundTripThroughTheirRadioButtons() {
    for (String policy : new String[] {"NONE", "MAX_AGE", "MAX_COUNT", "MAX_STORAGE", "COMBINED"}) {
      assertEquals(
          policy, LibraryOptionsPanel.policyFor(LibraryOptionsPanel.policyRadioId(policy)));
    }
  }

  @Test
  public void unknownValuesFallBackToNone() {
    assertEquals(R.id.dialog_cleanup_policy_none, LibraryOptionsPanel.policyRadioId(null));
    assertEquals(R.id.dialog_cleanup_policy_none, LibraryOptionsPanel.policyRadioId("legacy"));
    assertEquals("NONE", LibraryOptionsPanel.policyFor(-1));
  }
}
