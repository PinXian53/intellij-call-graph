package com.pino.intellijcallgraph.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PsiMethodUtils {

    private PsiMethodUtils() {
    }

    public static String getMethodSignature(PsiMethod method) {
        var methodName = method.getName();
        var parameters = method.getParameterList().getParameters();

        var params = Arrays.stream(parameters)
                .map(p -> {
                    var type = p.getType();
                    // 取得簡短型別名稱（例如 java.lang.String -> String）
                    return type.getPresentableText();
                })
                .collect(Collectors.joining(", "));
        return methodName + "(" + params + ")";
    }

    public static List<PsiMethod> findAllMethods(Project project, VirtualFile virtualFile) {
        var psiFile = PsiManager.getInstance(project).findFile(virtualFile);

        if (!(psiFile instanceof PsiJavaFile javaFile)) {
            return List.of();
        }

        var methods = new ArrayList<PsiMethod>();
        for (var psiClass : javaFile.getClasses()) {
            methods.addAll(List.of(psiClass.getMethods()));
        }
        return methods;
    }

    public static List<PsiMethod> findAllCallers(PsiMethod method) {
        var allReferences = ReferencesSearch.search(method).findAll();
        for (var anInterface : method.getContainingClass().getInterfaces()) {
            var methodBySignature = anInterface.findMethodBySignature(method, false);
            if (methodBySignature != null) {
                allReferences.addAll(ReferencesSearch.search(methodBySignature).findAll());
            }
        }
        var callers = new ArrayList<PsiMethod>();
        for (var reference : allReferences) {
            var callReference = reference.getElement();
            var caller = PsiTreeUtil.getParentOfType(callReference, PsiMethod.class);
            if (caller != null) {
                callers.add(caller);
            }
        }
        return callers;
    }
}
