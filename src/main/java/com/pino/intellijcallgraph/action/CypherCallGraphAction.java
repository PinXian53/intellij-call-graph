package com.pino.intellijcallgraph.action;

import com.pino.intellijcallgraph.model.CallGraph;
import com.pino.intellijcallgraph.utils.FileUtils;
import com.pino.intellijcallgraph.utils.MethodUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CypherCallGraphAction extends BaseCallGraphAction {

    @Override
    String getOutputFileName(LocalDateTime startTime) {
        var timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "CallGraph-" + timeFormatter.format(startTime) + ".cypher";
    }

    @Override
    void writeOutputFile(List<CallGraph> callGraphList, Path outputFilePath) {
        var outputContent = new StringBuilder();
        callGraphList.forEach(callGraph -> {
            var callee = callGraph.getCallee();
            callGraph.getCallers().forEach(caller -> {
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
