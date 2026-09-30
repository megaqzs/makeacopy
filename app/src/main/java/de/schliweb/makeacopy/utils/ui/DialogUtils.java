/*
 * Copyright 2025 Christian Kierdorf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package de.schliweb.makeacopy.utils.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.schliweb.makeacopy.R;
import lombok.experimental.UtilityClass;

/** Small dialog-related helpers to reduce UI code duplication. */
@UtilityClass
public final class DialogUtils {

  /**
   * In dark mode, some AlertDialog button colors can be low contrast depending on theme. This
   * method adjusts button text colors to white to improve readability. Safe to call on dialog's
   * onShow.
   */
  public static void improveAlertDialogButtonContrastForNight(
      androidx.appcompat.app.AlertDialog dialog, Context ctx) {
    if (dialog == null || ctx == null) return;
    try {
      int nightModeFlags =
          ctx.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
      if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
        try {
          int white = androidx.core.content.ContextCompat.getColor(ctx, android.R.color.white);
          if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) != null) {
            dialog
                .getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(white);
          }
          if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog
                .getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(white);
          }
          if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(white);
          }
        } catch (Exception ignored) {
          // Best-effort; failure is non-critical
        }
      }
    } catch (Throwable ignored) {
      // Best-effort; failure is non-critical
    }
  }

  /**
   * Builds a Material 3 bottom sheet hosting an options view with a title and a Cancel/Confirm
   * button row. Used by the camera/export option dialogs instead of AlertDialogs so options appear
   * as bottom sheets (reachable, M3-styled via the app-wide {@code bottomSheetDialogTheme}).
   *
   * @param ctx the context
   * @param title the sheet title
   * @param content the options content view (typically an inflated ScrollView)
   * @param onConfirm invoked when the user taps Confirm; the sheet is dismissed afterwards
   * @return the configured, not-yet-shown bottom sheet dialog
   */
  public static com.google.android.material.bottomsheet.BottomSheetDialog createOptionsBottomSheet(
      Context ctx, CharSequence title, android.view.View content, Runnable onConfirm) {
    com.google.android.material.bottomsheet.BottomSheetDialog dialog =
        new com.google.android.material.bottomsheet.BottomSheetDialog(ctx);
    float density = ctx.getResources().getDisplayMetrics().density;
    int pad = (int) (16 * density);

    LinearLayout container = new LinearLayout(ctx);
    container.setOrientation(LinearLayout.VERTICAL);
    container.setPadding(pad, pad, pad, pad);

    TextView titleView = new TextView(ctx);
    titleView.setText(title);
    titleView.setTextAppearance(
        com.google.android.material.R.style.TextAppearance_Material3_TitleLarge);
    titleView.setPadding(0, 0, 0, (int) (8 * density));
    androidx.core.view.ViewCompat.setAccessibilityHeading(titleView, true);
    container.addView(titleView);

    container.addView(
        content,
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f /* weight: scrollable content */));

    LinearLayout buttonRow = new LinearLayout(ctx);
    buttonRow.setOrientation(LinearLayout.HORIZONTAL);
    buttonRow.setGravity(android.view.Gravity.END);
    buttonRow.setPadding(0, (int) (8 * density), 0, 0);

    com.google.android.material.button.MaterialButton cancelButton =
        new com.google.android.material.button.MaterialButton(
            ctx, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
    cancelButton.setText(R.string.cancel);
    cancelButton.setOnClickListener(v -> dialog.dismiss());
    buttonRow.addView(cancelButton);

    com.google.android.material.button.MaterialButton confirmButton =
        new com.google.android.material.button.MaterialButton(ctx);
    confirmButton.setText(R.string.confirm);
    LinearLayout.LayoutParams confirmLp =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    confirmLp.setMarginStart((int) (8 * density));
    confirmButton.setLayoutParams(confirmLp);
    confirmButton.setOnClickListener(
        v -> {
          try {
            if (onConfirm != null) onConfirm.run();
          } finally {
            dialog.dismiss();
          }
        });
    buttonRow.addView(confirmButton);

    container.addView(buttonRow);
    dialog.setContentView(container);

    // Long option lists: open fully expanded so the action row is visible right away.
    // The sheet height is capped by the screen; the weighted content view scrolls while
    // title and buttons stay pinned. Skip the collapsed state entirely — a half-open
    // sheet hides the Cancel/Confirm row and only confuses.
    com.google.android.material.bottomsheet.BottomSheetBehavior<?> behavior = dialog.getBehavior();
    behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
    behavior.setSkipCollapsed(true);
    // Not draggable: pulling down at the top of a long option list used to dismiss the sheet
    // mid-way through editing. Cancel, the system back gesture and a tap outside still close it.
    behavior.setDraggable(false);
    return dialog;
  }
}
