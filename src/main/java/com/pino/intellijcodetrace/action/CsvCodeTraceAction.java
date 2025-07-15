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

public class CsvCodeTraceAction extends BaseCodeTraceAction {

    @Override
    String getOutputFileName(LocalDateTime startTime) {
        var settings = CodeTraceSettingsState.getInstance();
        var timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return settings.fileNamePrefix + timeFormatter.format(startTime) + ".csv";
    }

    @Override
    void writeOutputFile(List<CodeTrace> codeTraceList, Path outputFilePath) {
        var settings = CodeTraceSettingsState.getInstance();
        var headers = buildHeader(settings);
        var bodyFormat = buildBodyFormat(headers.size());
        var outputContent = new StringBuilder();

        codeTraceList.forEach(codeTrace -> {
            var callee = codeTrace.getCallee();
            codeTrace.getCallers().forEach(caller -> {
                var values = new ArrayList<String>();
                // Add caller and callee values
                addMethodValues(values, caller,  settings);
                addMethodValues(values, callee,  settings);
                // Format the line using the dynamic format string
                var line = String.format(bodyFormat, values.toArray());
                outputContent.append(line).append("\n");
            });
        });

        // Write output to file
        writeToFile(outputFilePath, String.join(",", headers), outputContent.toString());
    }

    private List<String> buildHeader(CodeTraceSettingsState settings) {
        List<String> headerParts = new ArrayList<>();
        // Add header parts for caller
        addHeaderParts(headerParts, "caller", settings);
        // Add header parts for callee
        addHeaderParts(headerParts, "callee", settings);

        return headerParts;
    }

    private String buildBodyFormat(int columnCount) {
        var formatString = "%s,".repeat(columnCount);
        if (formatString.endsWith(",")) {
            formatString = formatString.substring(0, formatString.length() - 1);
        }
        return formatString;
    }

    /**
     * Adds header parts for a method (caller or callee)
     */
    private void addHeaderParts(List<String> headerParts, String prefix, CodeTraceSettingsState settings) {
        if (settings.includeShortName) {
            headerParts.add(prefix + "_short_name");
        }
        if (settings.includeFullName) {
            headerParts.add(prefix + "_full_name");
        }
        if (settings.includeClassName) {
            headerParts.add(prefix + "_class_name");
        }
        if (settings.includeMethodName) {
            headerParts.add(prefix + "_method_name");
        }
        if (settings.includeAccessLevel) {
            headerParts.add(prefix + "_access_level");
        }
        if (settings.includeReturnType) {
            headerParts.add(prefix + "_return_type");
        }
        if (settings.includePosition) {
            headerParts.add(prefix + "_position");
        }
    }

    /**
     * Adds method values to the values list based on settings
     */
    private void addMethodValues(List<String> values, Method method, CodeTraceSettingsState settings) {
        if (settings.includeShortName) {
            values.add(toSafeCsvString(MethodUtils.getShortName(method)));
        }
        if (settings.includeFullName) {
            values.add(toSafeCsvString(MethodUtils.getFullName(method)));
        }
        if (settings.includeClassName) {
            values.add(toSafeCsvString(method.getClassName()));
        }
        if (settings.includeMethodName) {
            values.add(toSafeCsvString(method.getMethodName()));
        }
        if (settings.includeAccessLevel) {
            values.add(toSafeCsvString(""));  // Access level not available yet
        }
        if (settings.includeReturnType) {
            values.add(toSafeCsvString(""));  // Return type not available yet
        }
        if (settings.includePosition) {
            values.add(toSafeCsvString(""));  // Position not available yet
        }
    }

    private void writeToFile(Path outputFilePath, String headerString, String content) {
        if (!outputFilePath.toFile().exists()) {
            // Write header only if the file does not exist
            FileUtils.write(outputFilePath, headerString + "\n");
        }
        FileUtils.write(outputFilePath, content);
    }

    private String toSafeCsvString(String input) {
        if (input == null || input.isEmpty()) {
            return "\"\"";
        }
        return "\"" + input.replace("\"", "\"\"") + "\"";
    }
}
