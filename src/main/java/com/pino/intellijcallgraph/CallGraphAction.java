package com.pino.intellijcallgraph;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CallGraphAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        var project = e.getProject();
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (selectedFile == null) {
            Messages.showErrorDialog(project, "No file or folder selected", "Error");
            return;
        }

        var startTime = LocalDateTime.now();
        var formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        var outputFileName = "CallGraph-" + formatter.format(startTime) + ".txt";
        new Task.Backgroundable(project, "Call graph processing...", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                var sb = new StringBuilder();
                ApplicationManager.getApplication().runReadAction(() -> {
                    indicator.setIndeterminate(false);

                    var javaFiles = collectJavaFiles(selectedFile);

                    if (javaFiles.isEmpty()) {
                        Messages.showWarningDialog(project, "Java files not found.", "Warn");
                        return;
                    }

                    var callGraphList = new ArrayList<CallGraph>();
                    var total = javaFiles.size();
                    for (int i = 0; i < total; i++) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        indicator.setFraction((double) i / total); // 設定目前進度
                        indicator.setText("Call graph processing(" + (i + 1) + "/" + total + ")..."); // 顯示文字訊息

                        var javaFile = javaFiles.get(i);
                        callGraphList.addAll(generateCallGraph(project, javaFile));
                    }

                    callGraphList.forEach(callGraph -> {
                        var callee = callGraph.getCallee();
                        callGraph.getCallers().forEach(caller -> {
                            sb.append(caller.getClassQualifiedName()).append(".").append(caller.getMethodSignature())
                                    .append(" -> ")
                                    .append(callee.getClassQualifiedName()).append(".").append(callee.getMethodSignature()).append("\n");
                        });
                    });
                });

                writeResult(project, outputFileName, sb.toString());
            }

            @Override
            public void onSuccess() {
                var endTime = LocalDateTime.now();
                var duration = Duration.between(startTime, endTime);

                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;

                var spendTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                Messages.showInfoMessage(project, "Processing completed.\nSpend time: " + spendTime + ".\nOutput: " + outputFileName, "Completed");
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                Messages.showErrorDialog(project, "Error: " + error.getMessage(), "Error");
            }
        }.queue();
    }

    private List<String> collectJavaFiles(VirtualFile file) {
        List<String> javaFiles = new ArrayList<>();
        if (file.isDirectory()) {
            for (VirtualFile child : file.getChildren()) {
                javaFiles.addAll(collectJavaFiles(child));
            }
        } else if (isJavaFile(file)) {
            javaFiles.add(file.getPath());
        }
        return javaFiles;
    }

    private boolean isJavaFile(VirtualFile file) {
        return "java".equalsIgnoreCase(file.getExtension());
    }

    private List<CallGraph> generateCallGraph(Project project, String javaFilePath) {
        var virtualFile = LocalFileSystem.getInstance().findFileByPath(javaFilePath);
        var methods = listAllMethods(project, virtualFile);
        return methods.stream().map(method -> {
            var callee = toMethod(method);
            var callers = findCallers(method);
            return CallGraph.builder()
                    .callee(callee)
                    .callers(callers)
                    .build();
        }).toList();
    }

    public static List<PsiMethod> listAllMethods(Project project, VirtualFile virtualFile) {
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

    private List<Method> findCallers(PsiMethod method) {
        Collection<PsiReference> allReferences = ReferencesSearch.search(method).findAll();
        for (var anInterface : method.getContainingClass().getInterfaces()) {
            var methodBySignature = anInterface.findMethodBySignature(method, false);
            if (methodBySignature != null) {
                allReferences.addAll(ReferencesSearch.search(methodBySignature).findAll());
            }
        }
        var callers = new ArrayList<Method>();
        for (var reference : allReferences) {
            var callReference = reference.getElement();
            var caller = PsiTreeUtil.getParentOfType(callReference, PsiMethod.class);
            if (caller != null) {
                callers.add(toMethod(caller));
            }
        }
        return callers;
    }

    private Method toMethod(PsiMethod psiMethod) {
        var methodSignature = getMethodSignature(psiMethod);
        var callerClazz = psiMethod.getContainingClass();
        return Method.builder()
                .classQualifiedName(callerClazz.getQualifiedName())
                .className(callerClazz.getName())
                .methodSignature(methodSignature)
                .methodName(psiMethod.getName())
                .build();
    }

    private String getMethodSignature(PsiMethod method) {
        var methodName = method.getName();
        var parameters = method.getParameterList().getParameters();

        var params = Arrays.stream(parameters)
                .map(p -> {
                    var type = p.getType();
                    // 嘗試取得簡短型別名稱（例如 java.lang.String -> String）
                    var typeName = type.getPresentableText();
                    return typeName;
                })
                .collect(Collectors.joining(", "));

        return methodName + "(" + params + ")";
    }

    private void writeResult(Project project, String fileName, String content) {
        VirtualFile baseDir = project.getBaseDir();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                VirtualFile existing = baseDir.findChild(fileName);
                if (existing != null) {
                    existing.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                } else {
                    baseDir.createChildData(null, fileName)
                            .setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    private static class CallGraph {
        /**
         * 呼叫者
         */
        private List<Method> callers;
        /**
         * 被呼叫者
         */
        private Method callee;
    }

    @Builder
    @Data
    private static class Method {
        private String classQualifiedName;
        private String className;
        private String methodSignature;
        private String methodName;
    }
}
