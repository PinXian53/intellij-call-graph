package com.pino.intellijcodetrace.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent settings state for Code Trace plugin.
 * Stores user preferences that persist between IDE restarts.
 */
@State(
    name = "com.pino.intellijcodetrace.settings.CodeTraceSettingsState",
    storages = {@Storage("CodeTraceSettings.xml")}
)
public class CodeTraceSettingsState implements PersistentStateComponent<CodeTraceSettingsState> {
    // Include options
    public boolean includeFullName = true;
    public boolean includeShortName = true;
    public boolean includeClassName = true;
    public boolean includeMethodName = true;
    public boolean includeAccessLevel = true;
    public boolean includeReturnType = true;
    public boolean includePosition = true;

    // Output options
    public String fileNamePrefix = "code-trace-";

    public static CodeTraceSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(CodeTraceSettingsState.class);
    }

    @Nullable
    @Override
    public CodeTraceSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CodeTraceSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
