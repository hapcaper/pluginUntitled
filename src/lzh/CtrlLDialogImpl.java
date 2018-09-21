package lzh;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 *
 * @since 2018/9/17
 */
public class CtrlLDialogImpl extends CtrlLDialog {
    private final Editor myEditor;

    public CtrlLDialogImpl(Project project, Editor editor) {
        super(project,editor);
        this.myEditor = editor;
        this.init();
    }

	@Override
	protected double getPercent() {
		double count = myEditor.getDocument().getLineCount();
		double column = myEditor.getCaretModel().getLogicalPosition().column;
		return column / count;
	}


	@Override
    protected void doOKAction() {
        CtrlLDialogImpl.Coordinates coordinates = this.getCoordinates();
        if (coordinates != null) {
            LogicalPosition position = new LogicalPosition(coordinates.row, coordinates.column);
            this.myEditor.getCaretModel().removeSecondaryCarets();
            this.myEditor.getCaretModel().moveToLogicalPosition(position);
            this.myEditor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
            this.myEditor.getSelectionModel().removeSelection();
            IdeFocusManager.getGlobalInstance().requestFocus(this.myEditor.getContentComponent(), true);
            super.doOKAction();
        }
    }

    @Override
    protected int getLine() {
        return myEditor.getCaretModel().getLogicalPosition().line;
    }

    @Override
    protected int getColumn() {
        return myEditor.getCaretModel().getLogicalPosition().column;
    }

    @Override
    protected int getOffset() {
        return myEditor.getCaretModel().getOffset();
    }

    @Override
    protected int getMaxOffset() {
        return myEditor.getDocument().getTextLength();
    }

    @Override
    protected int coordinatesToOffset(@NotNull Coordinates coordinates) {
        LogicalPosition position = new LogicalPosition(coordinates.row, coordinates.column);
        return this.myEditor.logicalPositionToOffset(position);
    }

    @NotNull
    @Override
    protected Coordinates offsetToCoordinates(int offset) {
        LogicalPosition position = this.myEditor.offsetToLogicalPosition(offset);
        CtrlLDialog.Coordinates coordinates = new CtrlLDialog.Coordinates(position.line, position.column);
        if (coordinates == null) {
            return new Coordinates(0, 0);
        }
        return coordinates;
    }
}
