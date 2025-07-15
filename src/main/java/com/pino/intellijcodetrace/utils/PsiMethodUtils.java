package com.pino.intellijcodetrace.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
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
                    // get the short type name（ex: java.lang.String -> String）
                    return type.getPresentableText();
                })
                .collect(Collectors.joining(", "));
        return methodName + "(" + params + ")";
    }

    public static String getAccessLevel(PsiMethod psiMethod) {
        if (psiMethod.hasModifierProperty(PsiModifier.PUBLIC)) {
            return "public";
        } else if (psiMethod.hasModifierProperty(PsiModifier.PROTECTED)) {
            return "protected";
        } else if (psiMethod.hasModifierProperty(PsiModifier.PRIVATE)) {
            return "private";
        } else {
            return "package";
        }
    }

    public static String getReturnType(PsiMethod psiMethod) {
        return psiMethod.getReturnType() != null
                ? psiMethod.getReturnType().getPresentableText()
                : "";
    }

    public static String getPosition(PsiMethod psiMethod) {
        if (psiMethod.getContainingFile() == null) {
            return "";
        }

        var document = PsiDocumentManager.getInstance(psiMethod.getProject()).getDocument(psiMethod.getContainingFile());
        if (document == null) {
            return "";
        }

        var fileName = psiMethod.getContainingFile().getVirtualFile().getName();
        var offset = psiMethod.getTextOffset();
        if (offset < 0) {
            return "";
        }

        var lineNumber = document.getLineNumber(offset) + 1;
        return fileName + ":" + lineNumber;
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
