package com.pino.intellijcodetrace.action;

import com.pino.intellijcodetrace.model.CodeTrace;
import com.pino.intellijcodetrace.utils.FileUtils;
import com.pino.intellijcodetrace.utils.MethodUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CypherCodeTraceAction extends BaseCodeTraceAction {

    @Override
    String getOutputFileName(LocalDateTime startTime) {
        var timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "CodeTrace-" + timeFormatter.format(startTime) + ".cypher";
    }

    @Override
    void writeOutputFile(List<CodeTrace> codeTraceList, Path outputFilePath) {
        var outputContent = new StringBuilder();
        codeTraceList.forEach(codeTrace -> {
            var callee = codeTrace.getCallee();
            codeTrace.getCallers().forEach(caller -> {
                var line = "MERGE (a:method {short_name: \"%s\", full_name: \"%s\"}) MERGE (b:method {short_name: \"%s\", full_name: \"%s\"}) MERGE (a)-[:call_method]->(b);".formatted(
                        MethodUtils.getShortName(caller),
                        MethodUtils.getFullName(caller),
                        MethodUtils.getShortName(callee),
                        MethodUtils.getFullName(callee)
                );
                outputContent.append(line).append("\n");
            });
        });

        FileUtils.write(outputFilePath, outputContent.toString());
    }
}
