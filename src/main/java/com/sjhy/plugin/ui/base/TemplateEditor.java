package com.sjhy.plugin.ui.base;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SeparatorFactory;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * 模板编辑
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/11 10:20
 */
@Data
public class TemplateEditor {
    /**
     * 项目对象
     */
    private Project project;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 文件类型
     */
    private FileType fileType;

    /**
     * 编辑器对象
     */
    private Editor editor;

    /**
     * 回调结构
     */
    private Callback callback;

    /**
     * 构造方法
     *
     * @param project     项目对象
     * @param name        模板名称
     * @param content     模板内容
     * @param description 描述内容
     * @param fileType    文件类型
     */
    public TemplateEditor(@NotNull Project project, String name, String content, String description, @NotNull FileType fileType) {
        this.project = project;
        this.name = name;
        this.content = content;
        this.description = description;
        this.fileType = fileType;
    }

    /**
     * 创建面板组件
     *
     * @return 面板组件
     */
    public JComponent createComponent() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        if (editor != null) {
            editorFactory.releaseEditor(editor);
        }
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        PsiFile psiFile = psiFileFactory.createFileFromText(name, fileType, content, 0, true);
        // 创建文档对象
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        assert document != null;
        // 创建编辑框
        editor = editorFactory.createEditor(document, project, fileType, false);

        // 添加修改事件
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                String text = editor.getDocument().getText();
                // 回调事件
                if (callback != null && !Objects.equals(text, content)) {
                    callback.call();
                }
            }
        });

        EditorSettings editorSettings = editor.getSettings();
        // 关闭虚拟空间
        editorSettings.setVirtualSpace(false);
        // 关闭标记位置（断点位置）
        editorSettings.setLineMarkerAreaShown(false);
        // 关闭缩减指南
        editorSettings.setIndentGuidesShown(false);
        // 显示行号
        editorSettings.setLineNumbersShown(true);
        // 支持代码折叠
        editorSettings.setFoldingOutlineShown(true);
        // 附加行，附加列（提高视野）
        editorSettings.setAdditionalColumnsCount(3);
        editorSettings.setAdditionalLinesCount(3);
        // 显示光标
        editorSettings.setCaretRowShown(true);

        // 描述信息
        JEditorPane editorPane = new JEditorPane();
        // html形式展示
        editorPane.setEditorKit(UIUtil.getHTMLEditorKit());
        // 仅查看
        editorPane.setEditable(false);
        editorPane.setText(description);
        editorPane.addHyperlinkListener(new BrowserHyperlinkListener());

        // 描述面板
        JPanel descriptionPanel = new JPanel(new GridBagLayout());
        descriptionPanel.add(SeparatorFactory.createSeparator(IdeBundle.message("label.description"), null),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        JBUI.insetsBottom(2), 0, 0));
        descriptionPanel.add(ScrollPaneFactory.createScrollPane(editorPane),
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        JBUI.insetsTop(2), 0, 0));

        // 包装编辑器
        JPanel panel = new JPanel(new BorderLayout());

        // 分割器
        Splitter splitter = new Splitter(true, 0.6F);
        splitter.setFirstComponent(editor.getComponent());
        splitter.setSecondComponent(descriptionPanel);

        panel.add(splitter, BorderLayout.CENTER);
        panel.setPreferredSize(JBUI.size(400, 300));
        return panel;
    }

    /**
     * 关闭回调方法
     */
    public void onClose() {
        if (editor != null) {
            EditorFactory.getInstance().releaseEditor(editor);
        }
        editor = null;
    }

    /**
     * 回调接口
     */
    public interface Callback {
        /**
         * 文档修改回调
         */
        void call();
    }
}