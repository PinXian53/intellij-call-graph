package com.pino.intellijcallgraph.action;

import com.pino.intellijcallgraph.model.CallGraph;
import com.pino.intellijcallgraph.utils.FileUtils;
import com.pino.intellijcallgraph.utils.MethodUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvCallGraphAction extends BaseCallGraphAction {

    @Override
    String getOutputFileName(LocalDateTime startTime) {
        var timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "CallGraph-" + timeFormatter.format(startTime) + ".csv";
    }

    @Override
    void writeOutputFile(List<CallGraph> callGraphList, Path outputFilePath) {
        var outputContent = new StringBuilder();
        callGraphList.forEach(callGraph -> {
            var callee = callGraph.getCallee();
            callGraph.getCallers().forEach(caller -> {
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
