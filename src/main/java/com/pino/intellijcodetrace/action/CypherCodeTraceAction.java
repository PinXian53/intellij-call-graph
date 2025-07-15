package com.pino.intellijcodetrace.action;

import com.pino.intellijcodetrace.model.CodeTrace;
import com.pino.intellijcodetrace.model.Method;
import com.pino.intellijcodetrace.settings.CodeTraceSettingsState;
import com.pino.intellijcodetrace.utils.FileUtils;
import com.pino.intellijcodetrace.utils.MethodUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CypherCodeTraceAction extends BaseCodeTraceAction {

    @Override
    String getOutputFileName(LocalDateTime startTime) {
        var settings = CodeTraceSettingsState.getInstance();
        var timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return settings.fileNamePrefix + timeFormatter.format(startTime) + ".cypher";
    }

    @Override
    void writeOutputFile(List<CodeTrace> codeTraceList, Path outputFilePath) {
        var settings = CodeTraceSettingsState.getInstance();
        var outputContent = new StringBuilder();

        codeTraceList.forEach(codeTrace -> {
            var callee = codeTrace.getCallee();
            codeTrace.getCallers().forEach(caller -> {
                var callerProp = genMethodProperties(caller, settings);
                var calleeProp = genMethodProperties(callee, settings);
                // Create Cypher query
                var line = String.format("MERGE (a:method {%s}) MERGE (b:method {%s}) MERGE (a)-[:call_method]->(b);",
                        callerProp,
                        calleeProp
                );
                outputContent.append(line).append("\n");
            });
        });

        FileUtils.write(outputFilePath, outputContent.toString());
    }

    private String genMethodProperties(Method method, CodeTraceSettingsState settings) {
        List<String> properties = new ArrayList<>();

        if (settings.includeShortName) {
            properties.add(String.format("short_name: \"%s\"", MethodUtils.getShortName(method)));
        }
        if (settings.includeFullName) {
            properties.add(String.format("full_name: \"%s\"", MethodUtils.getFullName(method)));
        }
        if (settings.includeClassName) {
            properties.add(String.format("class_name: \"%s\"", escapeString(method.getClassName())));
        }
        if (settings.includeMethodName) {
            properties.add(String.format("method_name: \"%s\"", escapeString(method.getMethodName())));
        }
        if (settings.includeAccessLevel) {
            properties.add(String.format("access_level: \"%s\"", escapeString(method.getAccessLevel())));
        }
        if (settings.includeReturnType) {
            properties.add(String.format("return_type: \"%s\"", escapeString(method.getReturnType())));
        }
        if (settings.includePosition) {
            properties.add(String.format("position: \"%s\"", escapeString(method.getPosition())));
        }

        return String.join(", ", properties);
    }

    /**
     * Escapes special characters in strings for Cypher queries
     */
    private String escapeString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.replace("\"", "\\\"");
    }
}
