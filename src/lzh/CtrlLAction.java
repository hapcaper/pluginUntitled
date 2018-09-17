package lzh;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class CtrlLAction extends AnAction {
    public CtrlLAction() {
        this.setEnabledInModalContext(true);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        Editor editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        CtrlLDialogImpl dialog = new CtrlLDialogImpl(project, editor);
        dialog.show();
    }
}
