package com.pino.intellijcodetrace.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiMethod;
import com.pino.intellijcodetrace.model.CodeTrace;
import com.pino.intellijcodetrace.model.Method;
import com.pino.intellijcodetrace.utils.JavaUtils;
import com.pino.intellijcodetrace.utils.PsiMethodUtils;
import com.pino.intellijcodetrace.utils.TimeUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public abstract class BaseCodeTraceAction extends AnAction {

    abstract String getOutputFileName(LocalDateTime startTime);

    abstract void writeOutputFile(List<CodeTrace> codeTraceList, Path outputFilePath);

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
        var selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        if (selectedFiles == null || selectedFiles.length == 0) {
            Messages.showErrorDialog(project, "No file or folder selected", "Error");
            return;
        }

        final var startTime = LocalDateTime.now();
        final var baseDir = project.getBasePath();
        final var outputFileName = getOutputFileName(startTime);
        final var outputFilePath = Path.of(baseDir, outputFileName);

        new Task.Backgroundable(project, "Code trace processing...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ApplicationManager.getApplication().runReadAction(() -> {
                    indicator.setIndeterminate(false);

                    var javaFiles = JavaUtils.collectJavaFiles(selectedFiles);
                    if (javaFiles.isEmpty()) {
                        Messages.showWarningDialog(project, "Java files not found.", "Warn");
                        return;
                    }

                    var total = javaFiles.size();
                    for (int i = 0; i < total; i++) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        indicator.setFraction((double) i / total);
                        indicator.setText("Code trace processing(" + (i + 1) + "/" + total + ")...");

                        var javaFile = javaFiles.get(i);
                        var codeTraceList = generateCodeTrace(project, javaFile);
                        writeOutputFile(codeTraceList, outputFilePath);
                    }
                });
            }

            @Override
            public void onSuccess() {
                VirtualFileManager.getInstance().syncRefresh();
                var endTime = LocalDateTime.now();
                var spendTime = TimeUtils.getSpendTime(startTime, endTime);
                var file = LocalFileSystem.getInstance().findFileByIoFile(outputFilePath.toFile());
                if (file != null) {
                    FileEditorManager.getInstance(project).openFile(file, true);
                }
                Messages.showInfoMessage(project, "Processing completed.\nSpend time: " + spendTime + ".\nOutput: " + outputFileName, "Completed");
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                Messages.showErrorDialog(project, "Error: " + error.getMessage(), "Error");
            }
        }.queue();
    }

    private List<CodeTrace> generateCodeTrace(Project project, VirtualFile virtualFile) {
        var methods = PsiMethodUtils.findAllMethods(project, virtualFile);
        return methods.stream().map(method -> {
            var callee = toMethod(method);
            var callers = PsiMethodUtils.findAllCallers(method);
            return CodeTrace.builder()
                    .callee(callee)
                    .callers(toMethod(callers))
                    .build();
        }).toList();
    }

    private List<Method> toMethod(List<PsiMethod> psiMethods) {
        return psiMethods.stream().map(this::toMethod).toList();
    }

    private Method toMethod(PsiMethod psiMethod) {
        var methodSignature = PsiMethodUtils.getMethodSignature(psiMethod);
        var callerClazz = psiMethod.getContainingClass();
        return Method.builder()
                .classQualifiedName(callerClazz.getQualifiedName())
                .className(callerClazz.getName())
                .methodSignature(methodSignature)
                .methodName(psiMethod.getName())
                .build();
    }

}
