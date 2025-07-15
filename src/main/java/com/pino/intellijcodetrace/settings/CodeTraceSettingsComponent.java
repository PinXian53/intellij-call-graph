package com.pino.intellijcodetrace.settings;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Component for Code Trace settings UI.
 * Creates and manages the settings panel with UI controls.
 */
public class CodeTraceSettingsComponent {
    private final JPanel myMainPanel;

    // Trace options checkboxes
    private final JBCheckBox includeFullNameCheckBox = new JBCheckBox("Full name");
    private final JBCheckBox includeShortNameCheckBox = new JBCheckBox("Short name");
    private final JBCheckBox includeClassNameCheckBox = new JBCheckBox("Class name");
    private final JBCheckBox includeMethodNameCheckBox = new JBCheckBox("Method name");
    private final JBCheckBox includeAccessLevelCheckBox = new JBCheckBox("Access level");
    private final JBCheckBox includeReturnTypeCheckBox = new JBCheckBox("Return type");
    private final JBCheckBox includePositionCheckBox = new JBCheckBox("Position");

    private final JBTextField fileNamePrefixField = new JBTextField();

    /**
     * Creates a panel with a checkbox and a question mark icon with a tooltip
     */
    private JPanel createCheckboxWithTooltip(JBCheckBox checkbox, String tooltipText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        // Add the checkbox
        panel.add(checkbox);

        // Create and add the question mark icon with tooltip
        JLabel helpIcon = new JLabel(AllIcons.General.ContextHelp);
        helpIcon.setToolTipText(tooltipText);
        panel.add(helpIcon);

        return panel;
    }

    public CodeTraceSettingsComponent() {
        // Configure trace options panel
        JPanel traceOptionsPanel = new JPanel(new GridLayout(0, 2));

        // Add checkboxes with tooltips to the options panel
        traceOptionsPanel.add(createCheckboxWithTooltip(includeFullNameCheckBox, "e.g., com.example.SampleClass.sampleMethod(String, int)"));
        traceOptionsPanel.add(createCheckboxWithTooltip(includeShortNameCheckBox, "e.g., SampleClass.sampleMethod"));
        traceOptionsPanel.add(createCheckboxWithTooltip(includeClassNameCheckBox, "e.g., SampleClass"));
        traceOptionsPanel.add(createCheckboxWithTooltip(includeMethodNameCheckBox, "e.g., sampleMethod"));
        traceOptionsPanel.add(createCheckboxWithTooltip(includeAccessLevelCheckBox, "e.g., public"));
        traceOptionsPanel.add(createCheckboxWithTooltip(includeReturnTypeCheckBox, "e.g., void"));
        traceOptionsPanel.add(createCheckboxWithTooltip(includePositionCheckBox, "e.g., SampleClass.java:42"));

        // Set default values
        includeFullNameCheckBox.setSelected(true);

        // Build the settings panel with proper spacing and layout
        myMainPanel = FormBuilder.createFormBuilder()
                .addVerticalGap(10)
                .addComponent(new JBLabel("Code Trace Settings"))
                .addVerticalGap(10)
                .addSeparator()
                .addVerticalGap(5)

                // Include Options section
                .addComponent(new JBLabel("Include Options"))
                .addVerticalGap(5)
                .addComponent(traceOptionsPanel)
                .addVerticalGap(10)
                .addSeparator()
                .addVerticalGap(5)

                // Output Options section
                .addComponent(new JBLabel("Output Options"))
                .addVerticalGap(5)
                .addLabeledComponent("File name prefix:", fileNamePrefixField)

                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        // Apply consistent padding
        myMainPanel.setBorder(JBUI.Borders.empty(10));
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return includeFullNameCheckBox;
    }

    @NotNull
    public String getFileNamePrefix() {
        return fileNamePrefixField.getText();
    }

    public void setFileNamePrefix(@NotNull String newText) {
        fileNamePrefixField.setText(newText);
    }

    public boolean getIncludeFullName() {
        return includeFullNameCheckBox.isSelected();
    }

    public void setIncludeFullName(boolean selected) {
        includeFullNameCheckBox.setSelected(selected);
    }

    public boolean getIncludeShortName() {
        return includeShortNameCheckBox.isSelected();
    }

    public void setIncludeShortName(boolean selected) {
        includeShortNameCheckBox.setSelected(selected);
    }

    public boolean getIncludePosition() {
        return includePositionCheckBox.isSelected();
    }

    public void setIncludePosition(boolean selected) {
        includePositionCheckBox.setSelected(selected);
    }

    public boolean getIncludeReturnType() {
        return includeReturnTypeCheckBox.isSelected();
    }

    public void setIncludeReturnType(boolean selected) {
        includeReturnTypeCheckBox.setSelected(selected);
    }

    public boolean getIncludeAccessLevel() {
        return includeAccessLevelCheckBox.isSelected();
    }

    public void setIncludeAccessLevel(boolean selected) {
        includeAccessLevelCheckBox.setSelected(selected);
    }

    public boolean getIncludeClassName() {
        return includeClassNameCheckBox.isSelected();
    }

    public void setIncludeClassName(boolean selected) {
        includeClassNameCheckBox.setSelected(selected);
    }

    public boolean getIncludeMethodName() {
        return includeMethodNameCheckBox.isSelected();
    }

    public void setIncludeMethodName(boolean selected) {
        includeMethodNameCheckBox.setSelected(selected);
    }

}
