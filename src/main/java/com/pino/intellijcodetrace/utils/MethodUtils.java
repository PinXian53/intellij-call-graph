package com.pino.intellijcodetrace.utils;

import com.pino.intellijcodetrace.model.Method;

public class MethodUtils {

    private MethodUtils() {
    }

    public static String getFullName(Method method) {
        return method.getClassQualifiedName() + "." + method.getMethodSignature();
    }

    public static String getShortName(Method method) {
        return method.getClassName() + "." + method.getMethodName();
    }

}
