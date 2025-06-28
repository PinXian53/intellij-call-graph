package com.pino.intellijcallgraph.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Method {
    private String classQualifiedName;
    private String className;
    private String methodSignature;
    private String methodName;
}
