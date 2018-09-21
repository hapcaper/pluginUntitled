package lzh;

import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.PatternUtil;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 *
 * @since 2018/9/17
 */
public abstract class CtrlLDialog extends DialogWrapper {
    private final Pattern myPattern = PatternUtil.compileSafe("\\s*(\\d+)?\\s*(?:[,:]?\\s*(\\d+)?)?\\s*", (Pattern)null);
    private final Pattern myPattern1 = PatternUtil.compileSafe("\\s*[.]?\\s*(0*)?([123456789]*)?\\s*[.]?\\s*", (Pattern)null);
    private final Pattern myPattern2 = PatternUtil.compileSafe("\\s*[%]?\\s*(\\d+)?\\s*[%]?\\s*", (Pattern)null);
	private Editor editor = null;
	private JTextField myField;
    private JTextField myOffsetField;

    public CtrlLDialog(Project project) {
        super(project, true);
        this.setTitle("Super Go to Line/Column");
    }
	public CtrlLDialog(Project project,Editor editor) {
		super(project, true);
		this.editor = editor;
		this.setTitle("Super Go to Line/Column");
	}
    private static boolean isInternal() {
        return ApplicationManagerEx.getApplicationEx().isInternal();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return this.myField;
    }

    @Override
    protected JComponent createCenterPanel() {
        return null;
    }

    private String getText() {
        return this.myField.getText();
    }

    @Nullable
    protected final CtrlLDialog.Coordinates getCoordinates() {
        Matcher m = this.myPattern.matcher(this.getText());
	    Matcher m1 = this.myPattern1.matcher(this.getText());
	    Matcher m2 = this.myPattern2.matcher(this.getText());

        if (!m.matches()&&!m1.matches()) {
            return null;
        } else if (m.matches()) {
	        int l = StringUtil.parseInt(m.group(1), this.getLine() + 1);
	        int c = StringUtil.parseInt(m.group(2), -1);
	        return l > 0 ? new CtrlLDialog.Coordinates(l - 1, Math.max(0, c - 1)) : null;
        } else if (m1.matches()) {
	        String oStr = m1.group(1);
	        String num = m1.group(2);
	        String percentStr = "0." + oStr + num;
	        double percent = StringUtil.parseDouble(percentStr, this.getPercent());
	        int l = (int) (percent * editor.getDocument().getLineCount());
	        return l > 0 ? new CtrlLDialog.Coordinates(l - 1, 0) : null;
        } else if (m2.matches()) {
	        String numStr = m1.group(1);
	        double percent = Double.parseDouble(numStr)/100;
	        int l = (int) (percent * editor.getDocument().getLineCount());
	        return l > 0 ? new CtrlLDialog.Coordinates(l - 1, 0) : null;
        } else {
	        return null;
        }
    }

	protected abstract double getPercent();

	protected abstract int getLine();

    protected abstract int getColumn();

    protected abstract int getOffset();

    protected abstract int getMaxOffset();

    protected abstract int coordinatesToOffset(@NotNull CtrlLDialog.Coordinates var1);

    @NotNull
    protected abstract CtrlLDialog.Coordinates offsetToCoordinates(int var1);

    @Override
    protected JComponent createNorthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.insets = JBUI.insets(4, 0, 8, 8);
        gbConstraints.fill = 3;
        gbConstraints.weightx = 0.0D;
        gbConstraints.weighty = 1.0D;
        gbConstraints.anchor = 13;
        JLabel label = new JLabel("[Line] [:column]:");
        panel.add(label, gbConstraints);
        gbConstraints.fill = 1;
        gbConstraints.weightx = 1.0D;

        class MyTextField extends JTextField {
            public MyTextField() {
                super("");
                this.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (!e.isTemporary()) {
                            MyTextField.this.selectAll();
                        }

                    }
                });
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(200, d.height);
            }
        }

        this.myField = new MyTextField();
        panel.add(this.myField, gbConstraints);
        this.myField.setText(String.format("%d:%d", this.getLine() + 1, this.getColumn() + 1));
        if (isInternal()) {
            gbConstraints.gridy = 1;
            gbConstraints.weightx = 0.0D;
            gbConstraints.weighty = 1.0D;
            gbConstraints.anchor = 13;
            JLabel offsetLabel = new JLabel("Offset:");
            panel.add(offsetLabel, gbConstraints);
            gbConstraints.fill = 1;
            gbConstraints.weightx = 1.0D;
            this.myOffsetField = new MyTextField();
            panel.add(this.myOffsetField, gbConstraints);
            this.myOffsetField.setText(String.valueOf(this.getOffset()));
            DocumentAdapter valueSync = new DocumentAdapter() {
                boolean inSync;

                @Override
                protected void textChanged(DocumentEvent e) {
                    if (!this.inSync) {
                        this.inSync = true;
                        String s = "<invalid>";
                        JTextField f = null;

                        try {
                            if (e.getDocument() == CtrlLDialog.this.myField.getDocument()) {
                                f = CtrlLDialog.this.myOffsetField;
                                CtrlLDialog.Coordinates p = CtrlLDialog.this.getCoordinates();
                                s = p == null ? s : String.valueOf(CtrlLDialog.this.coordinatesToOffset(p));
                            } else {
                                f = CtrlLDialog.this.myField;
                                int offset = StringUtil.parseInt(CtrlLDialog.this.myOffsetField.getText(), -1);
                                CtrlLDialog.Coordinates px = offset >= 0 ? CtrlLDialog.this.offsetToCoordinates(Math.min(CtrlLDialog.this.getMaxOffset() - 1, offset)) : null;
                                s = px == null ? s : String.format("%d:%d", px.row + 1, px.column + 1);
                            }

                            f.setText(s);
                        } catch (IndexOutOfBoundsException var9) {
                            if (f != null) {
                                f.setText(s);
                            }
                        } finally {
                            this.inSync = false;
                        }

                    }
                }
            };
            this.myField.getDocument().addDocumentListener(valueSync);
            this.myOffsetField.getDocument().addDocumentListener(valueSync);
        }

        return panel;
    }

    protected static class Coordinates {
        public final int row;
        public final int column;

        public Coordinates(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }
}
