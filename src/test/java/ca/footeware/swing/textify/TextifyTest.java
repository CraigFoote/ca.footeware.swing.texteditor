/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ca.footeware.swing.textify;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link Textify}.
 *
 * @author http://footeware.ca
 */
public class TextifyTest {

    @AfterEach
    public void afterEach() {
        File file = new File("test");
        if (file.exists()) {
            file.delete();
            file = null;
        }
    }

    @Test
    public void testEditorPaneOpensEmpty() {
        Textify textify = new Textify(new String[]{});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());
    }

    @Test
    public void testEditorPaneOpensWithNonexistantFileArg() {
        Textify textify = new Textify(new String[]{"test"});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());
    }

    @Test
    public void testEditorPaneOpensWithExistingFileArg() throws IOException {
        // create file
        File file = new File("test");
        file.createNewFile();
        // open text.txt in testify
        Textify textify = new Textify(new String[]{"test"});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        // confirm file is empty
        assertEquals(0, editor.getText().length());
        // make changes to test.txt
        editor.setText("testtest");
        assertEquals("testtest", editor.getText());
        // save file
        JButton saveButton = (JButton) TestUtils.getChildNamed(textify, "saveButton");
        saveButton.doClick();
        textify.dispose();
        // reopen textify with test.txt
        textify = new Textify(new String[]{"test"});
        editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        // confirm test.txt now has content
        assertEquals("testtest", editor.getText());
    }

    @Test
    public void testNewButtonWhenEmpty() {
        Textify textify = new Textify(new String[]{"test"});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());

        JButton newButton = (JButton) TestUtils.getChildNamed(textify, "newButton");
        assertNotNull(newButton);
        newButton.doClick();
        assertEquals(0, editor.getText().length());
    }

    @Test
    public void testNewButtonWhenNotEmpty() throws InterruptedException {
        // open new file
        Textify textify = new Textify(new String[]{"test"});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        // put in content
        editor.setText("testtest");
        assertEquals("testtest", editor.getText());
        // click the New button
        JButton newButton = (JButton) TestUtils.getChildNamed(textify, "newButton");
        assertNotNull(newButton);

        JButton yesButton = null;
        int tries = 0;
        SwingUtilities.invokeLater(() -> newButton.doClick());
        // the dialog box will show up shortly
        while (yesButton == null && tries < 50) {
            Thread.sleep(200);
            // use index to find OK button of dialog
            yesButton = (JButton) TestUtils.getChildIndexed(textify, "JButton", 0);
            tries++;
        }
        assertNotNull(yesButton);
        // click dialog's OK button
        yesButton.doClick();
        SwingUtilities.invokeLater(() -> {
            // confirm editor now empty
            assertEquals(0, editor.getText().length());
        });
    }

    @Test
    public void testSaveButton() throws InterruptedException {
        // open empty
        Textify textify = new Textify(new String[]{});
        // add content
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        // put in content
        editor.setText("testtest");
        // get and click save button
        JButton saveButton = (JButton) TestUtils.getChildNamed(textify, "saveButton");
        assertNotNull(saveButton);
        SwingUtilities.invokeLater(() -> saveButton.doClick());
        // the dialog box will show up shortly
        int tries = 0;
        JFileChooser chooser = null;
        JButton saveDialogButton = null;
        while ((chooser == null || saveDialogButton == null) && tries < 20) {
            Thread.sleep(200);
            // use index to find save button of dialog
            if (chooser == null) {
                chooser = (JFileChooser) TestUtils.getChildIndexed(textify, "JFileChooser", 0);
            }
            if (saveDialogButton == null) {
                saveDialogButton = (JButton) TestUtils.getChildIndexed(textify, "JButton", 3);
            }
            tries++;
        }
        assertNotNull(chooser);
        chooser.setSelectedFile(new File("test"));
        assertNotNull(saveDialogButton);
        saveDialogButton.doClick();
        // confirm editor still has content
        assertNotNull(editor);
        assertEquals(8, editor.getText().length());
        // close and reopen file
        textify.dispose();
        textify = null;
        textify = new Textify(new String[]{"test"});
        // confirm editor still has content
        final JEditorPane editor2 = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor2);
        SwingUtilities.invokeLater(() -> assertEquals("testtest", editor2.getText()));
    }
}
