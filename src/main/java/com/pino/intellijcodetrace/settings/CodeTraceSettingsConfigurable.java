package com.pino.intellijcodetrace.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides controller functionality for application settings.
 * Connects the settings UI with the persistent settings state.
 */
public class CodeTraceSettingsConfigurable implements Configurable {

    private CodeTraceSettingsComponent settingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Code Trace";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new CodeTraceSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        CodeTraceSettingsState settings = CodeTraceSettingsState.getInstance();

        return !settingsComponent.getFileNamePrefix().equals(settings.fileNamePrefix) ||
                settingsComponent.getIncludeFullName() != settings.includeFullName ||
                settingsComponent.getIncludeShortName() != settings.includeShortName ||
                settingsComponent.getIncludePosition() != settings.includePosition ||
                settingsComponent.getIncludeReturnType() != settings.includeReturnType ||
                settingsComponent.getIncludeAccessLevel() != settings.includeAccessLevel ||
                settingsComponent.getIncludeClassName() != settings.includeClassName ||
                settingsComponent.getIncludeMethodName() != settings.includeMethodName;
    }

    @Override
    public void apply() throws ConfigurationException {
        CodeTraceSettingsState settings = CodeTraceSettingsState.getInstance();

        // Validate that at least one trace option is selected
        boolean anySelected = settingsComponent.getIncludeFullName() ||
                settingsComponent.getIncludeShortName() ||
                settingsComponent.getIncludePosition() ||
                settingsComponent.getIncludeReturnType() ||
                settingsComponent.getIncludeAccessLevel() ||
                settingsComponent.getIncludeClassName() ||
                settingsComponent.getIncludeMethodName();

        if (!anySelected) {
            throw new ConfigurationException("At least one trace option must be selected.");
        }

        if (settingsComponent.getFileNamePrefix().isBlank()) {
            throw new ConfigurationException("File name prefix cannot be blank.");
        }

        // Apply include options
        settings.includeFullName = settingsComponent.getIncludeFullName();
        settings.includeShortName = settingsComponent.getIncludeShortName();
        settings.includePosition = settingsComponent.getIncludePosition();
        settings.includeReturnType = settingsComponent.getIncludeReturnType();
        settings.includeAccessLevel = settingsComponent.getIncludeAccessLevel();
        settings.includeClassName = settingsComponent.getIncludeClassName();
        settings.includeMethodName = settingsComponent.getIncludeMethodName();

        // Apply output settings
        settings.fileNamePrefix = settingsComponent.getFileNamePrefix();
    }

    @Override
    public void reset() {
        CodeTraceSettingsState settings = CodeTraceSettingsState.getInstance();

        // Reset include options
        settingsComponent.setIncludeFullName(settings.includeFullName);
        settingsComponent.setIncludeShortName(settings.includeShortName);
        settingsComponent.setIncludePosition(settings.includePosition);
        settingsComponent.setIncludeReturnType(settings.includeReturnType);
        settingsComponent.setIncludeAccessLevel(settings.includeAccessLevel);
        settingsComponent.setIncludeClassName(settings.includeClassName);
        settingsComponent.setIncludeMethodName(settings.includeMethodName);

        // Reset output settings
        settingsComponent.setFileNamePrefix(settings.fileNamePrefix);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
