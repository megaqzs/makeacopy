/*
 * Copyright 2026 Christian Kierdorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package de.schliweb.makeacopy.ui.library;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import de.schliweb.makeacopy.R;
import de.schliweb.makeacopy.settings.SettingsCatalog;
import de.schliweb.makeacopy.utils.ui.UIUtils;

/**
 * The library options (how completed scans are cleaned up) as one reusable block: {@code
 * panel_library_options.xml} plus the code that fills it from the saved settings and writes the
 * choices back. Read by {@code CacheCleanupService}, which applies the policy.
 */
public final class LibraryOptionsPanel {

  static final String KEY_POLICY = "completed_scans_cleanup_policy";
  static final String KEY_MAX_AGE_DAYS = "completed_scans_max_age_days";
  static final String KEY_MAX_COUNT = "completed_scans_max_count";
  static final String KEY_MAX_STORAGE_MB = "completed_scans_max_storage_mb";

  private final View root;
  private final RadioGroup policies;
  private final EditText maxAge;
  private final EditText maxCount;
  private final EditText maxStorage;

  public LibraryOptionsPanel(@NonNull View root) {
    this.root = root;
    policies = root.findViewById(R.id.dialog_cleanup_policy_group);
    maxAge = root.findViewById(R.id.dialog_cleanup_max_age);
    maxCount = root.findViewById(R.id.dialog_cleanup_max_count);
    maxStorage = root.findViewById(R.id.dialog_cleanup_max_storage);
  }

  /** Fills the controls from the saved settings. */
  public void bind(@NonNull Context ctx) {
    SharedPreferences prefs = prefs(ctx);
    policies.check(policyRadioId(prefs.getString(KEY_POLICY, "NONE")));
    maxAge.setText(String.valueOf(prefs.getInt(KEY_MAX_AGE_DAYS, 30)));
    maxCount.setText(String.valueOf(prefs.getInt(KEY_MAX_COUNT, 100)));
    maxStorage.setText(String.valueOf(prefs.getInt(KEY_MAX_STORAGE_MB, 500)));
    showLimitsFor(policies.getCheckedRadioButtonId());
    policies.setOnCheckedChangeListener((group, checkedId) -> showLimitsFor(checkedId));
  }

  /** Persists the choices. A limit that is not a number keeps its previous value. */
  public void apply(@NonNull Context ctx) {
    SharedPreferences.Editor editor = prefs(ctx).edit();
    editor.putString(KEY_POLICY, policyFor(policies.getCheckedRadioButtonId()));
    putIntIfValid(editor, KEY_MAX_AGE_DAYS, maxAge);
    putIntIfValid(editor, KEY_MAX_COUNT, maxCount);
    putIntIfValid(editor, KEY_MAX_STORAGE_MB, maxStorage);
    editor.apply();
  }

  private static SharedPreferences prefs(Context ctx) {
    return ctx.getSharedPreferences(SettingsCatalog.PREFS_CLEANUP, Context.MODE_PRIVATE);
  }

  private static void putIntIfValid(SharedPreferences.Editor editor, String key, EditText field) {
    try {
      int value = Integer.parseInt(field.getText().toString().trim());
      if (value > 0) editor.putInt(key, value);
    } catch (NumberFormatException ignore) {
      // keep existing
    }
  }

  /** Greys out the limits the chosen policy does not use. */
  private void showLimitsFor(int checkedId) {
    String policy = policyFor(checkedId);
    boolean combined = "COMBINED".equals(policy);
    enable(R.id.dialog_cleanup_max_age_label, maxAge, combined || "MAX_AGE".equals(policy));
    enable(R.id.dialog_cleanup_max_count_label, maxCount, combined || "MAX_COUNT".equals(policy));
    enable(
        R.id.dialog_cleanup_max_storage_label,
        maxStorage,
        combined || "MAX_STORAGE".equals(policy));
  }

  private void enable(int labelId, EditText field, boolean enabled) {
    UIUtils.setEnabledWithAlpha(root.findViewById(labelId), enabled);
    UIUtils.setEnabledWithAlpha(field, enabled);
  }

  // ---- saved policy <-> radio button (pure mappings) ----

  @VisibleForTesting
  static int policyRadioId(String policy) {
    if ("MAX_AGE".equals(policy)) return R.id.dialog_cleanup_policy_max_age;
    if ("MAX_COUNT".equals(policy)) return R.id.dialog_cleanup_policy_max_count;
    if ("MAX_STORAGE".equals(policy)) return R.id.dialog_cleanup_policy_max_storage;
    if ("COMBINED".equals(policy)) return R.id.dialog_cleanup_policy_combined;
    return R.id.dialog_cleanup_policy_none;
  }

  @VisibleForTesting
  static String policyFor(int radioId) {
    if (radioId == R.id.dialog_cleanup_policy_max_age) return "MAX_AGE";
    if (radioId == R.id.dialog_cleanup_policy_max_count) return "MAX_COUNT";
    if (radioId == R.id.dialog_cleanup_policy_max_storage) return "MAX_STORAGE";
    if (radioId == R.id.dialog_cleanup_policy_combined) return "COMBINED";
    return "NONE";
  }
}
