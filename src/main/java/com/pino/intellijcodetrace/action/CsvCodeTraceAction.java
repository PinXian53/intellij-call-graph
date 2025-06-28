package com.pino.intellijcodetrace.action;

import com.pino.intellijcodetrace.model.CodeTrace;
import com.pino.intellijcodetrace.utils.FileUtils;
import com.pino.intellijcodetrace.utils.MethodUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvCodeTraceAction extends BaseCodeTraceAction {

    @Override
    String getOutputFileName(LocalDateTime startTime) {
        var timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "CodeTrace-" + timeFormatter.format(startTime) + ".csv";
    }

    @Override
    void writeOutputFile(List<CodeTrace> codeTraceList, Path outputFilePath) {
        var outputContent = new StringBuilder();
        codeTraceList.forEach(codeTrace -> {
            var callee = codeTrace.getCallee();
            codeTrace.getCallers().forEach(caller -> {
                var line = "%s,%s,%s,%s".formatted(
                        toSafeCsvString(MethodUtils.getShortName(caller)),
                        toSafeCsvString(MethodUtils.getFullName(caller)),
                        toSafeCsvString(MethodUtils.getShortName(callee)),
                        toSafeCsvString(MethodUtils.getFullName(callee))
                );
                outputContent.append(line).append("\n");
            });
        });

        if (!outputFilePath.toFile().exists()) {
            // Write header only if the file does not exist
            FileUtils.write(outputFilePath, "caller_short_name,caller_full_name,callee_short_name,callee_full_name\n");
        }
        FileUtils.write(outputFilePath, outputContent.toString());
    }

    private String toSafeCsvString(String input) {
        if (input == null || input.isEmpty()) {
            return "\"\"";
        }
        return "\"" + input.replace("\"", "\"\"") + "\"";
    }
}
