package com.example.syntaxappproject;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling bullet point input in EditText fields.
 * Automatically starts with a bullet point and adds new bullets when Enter is pressed.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Auto-starts with "• " when the field is empty</li>
 *   <li>Pressing Enter adds a new bullet point on the next line</li>
 *   <li>Pressing Enter on an empty bullet point removes that line</li>
 *   <li>Prevents deleting the initial bullet point completely</li>
 *   <li>Provides utility methods for converting between formatted and plain text</li>
 * </ul>
 */
public class BulletPointHelper {

    /** The bullet point symbol followed by a space. */
    private static final String BULLET_POINT = "• ";

    /** New line character for separating bullet points. */
    private static final String NEW_LINE = "\n";

    /**
     * Sets up the EditText with bullet point behavior.
     */
    public static void setupBulletPointField(EditText editText) {
        if (editText == null) {
            throw new NullPointerException("EditText cannot be null");
        }

        editText.setText(BULLET_POINT);
        editText.setSelection(BULLET_POINT.length());

        editText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;
            private String previousText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting) return;
                previousText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) return;

                String currentText = s.toString();
                int cursorPos = editText.getSelectionStart();

                if (count == 1 && before == 0 && currentText.length() == previousText.length() + 1) {
                    char addedChar = currentText.charAt(cursorPos - 1);

                    // If Enter was pressed
                    if (addedChar == '\n') {
                        isFormatting = true;

                        int lineStart = currentText.lastIndexOf(NEW_LINE, cursorPos - 2) + 1;
                        String currentLine = currentText.substring(lineStart, cursorPos - 1);

                        if (currentLine.trim().equals(BULLET_POINT.trim())) {
                            String newText;
                            if (lineStart == 0) {
                                newText = "";
                            } else {
                                newText = currentText.substring(0, lineStart - 1);
                            }
                            editText.setText(newText);
                            editText.setSelection(newText.length());
                        } else {
                            String newText = currentText.substring(0, cursorPos) + BULLET_POINT + currentText.substring(cursorPos);
                            editText.setText(newText);
                            editText.setSelection(cursorPos + BULLET_POINT.length());
                        }

                        isFormatting = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                String text = s.toString();

                if (text.isEmpty()) {
                    isFormatting = true;
                    editText.setText(BULLET_POINT);
                    editText.setSelection(BULLET_POINT.length());
                    isFormatting = false;
                    return;
                }

                if (!text.startsWith(BULLET_POINT)) {
                    isFormatting = true;
                    editText.setText(BULLET_POINT + text);
                    editText.setSelection(editText.getSelectionStart() + BULLET_POINT.length());
                    isFormatting = false;
                }
            }
        });
    }

    /**
     * Gets the text without bullet point formatting for storage.
     */
    public static String getPlainText(String bulletText) {
        if (bulletText == null || bulletText.isEmpty()) return "";

        String[] lines = bulletText.split(NEW_LINE);
        StringBuilder plainText = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.startsWith(BULLET_POINT.trim())) {
                    trimmed = trimmed.substring(BULLET_POINT.trim().length()).trim();
                }
                if (plainText.length() > 0) {
                    plainText.append(NEW_LINE);
                }
                plainText.append(trimmed);
            }
        }

        return plainText.toString();
    }

    /**
     * Formats plain text with bullet points for display.
     */
    public static String formatWithBullets(String plainText) {
        if (plainText == null || plainText.isEmpty()) return "";

        String[] lines = plainText.split(NEW_LINE);
        StringBuilder bulletText = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                if (i > 0) {
                    bulletText.append(NEW_LINE);
                }
                bulletText.append(BULLET_POINT).append(line);
            }
        }

        return bulletText.toString();
    }

    /**
     * Parses bullet point text into a list of individual points without bullet symbols.
     */
    public static List<String> parseBulletPoints(String bulletText) {
        List<String> points = new ArrayList<>();

        if (bulletText == null || bulletText.isEmpty()) return points;

        String[] lines = bulletText.split(NEW_LINE);
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.startsWith(BULLET_POINT.trim())) {
                    trimmed = trimmed.substring(BULLET_POINT.trim().length()).trim();
                }
                points.add(trimmed);
            }
        }

        return points;
    }
}