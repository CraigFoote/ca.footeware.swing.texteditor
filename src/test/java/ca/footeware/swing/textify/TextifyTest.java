/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ca.footeware.swing.textify;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static final String TEST_PATH = "/home/" + 
            System.getProperty("user.name") + "/test";

    @AfterEach
    public void afterEach() {
        File file = new File(TEST_PATH);
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
        Textify textify = new Textify(new String[]{TEST_PATH});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());
    }

    @Test
    public void testEditorPaneOpensWithExistingFileArg() throws IOException {
        // create file
        File file = new File(TEST_PATH);
        file.createNewFile();
        // open text.txt in testify
        Textify textify = new Textify(new String[]{TEST_PATH});
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
        textify = new Textify(new String[]{TEST_PATH});
        editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        // confirm test.txt now has content
        assertEquals("testtest", editor.getText());
    }

    @Test
    public void testNewButtonWhenEmpty() {
        Textify textify = new Textify(new String[]{TEST_PATH});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());

        JButton newButton = (JButton) TestUtils.getChildNamed(textify, "newButton");
        assertNotNull(newButton);
        newButton.doClick();
        assertEquals(0, editor.getText().length());
    }

    /**
     * Open new file, add content, click New button, get prompted to discard
     * content, click No, confirm content still displayed.
     */
    @Test
    public void testNewButtonWhenNotEmptyDoNotOverwrite() throws InterruptedException, InvocationTargetException {
        // open new file
        Textify textify = new Textify(new String[]{TEST_PATH});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(
                textify, "editor");
        assertNotNull(editor);
        // put in content
        editor.setText("testtest");
        assertEquals("testtest", editor.getText());
        // click the New button
        JButton newButton = (JButton) TestUtils.getChildNamed(
                textify, "newButton");
        assertNotNull(newButton);

        SwingUtilities.invokeLater(() -> newButton.doClick());
        // wait for dialog
        JButton noButton = null;
        int tries = 0;
        // the dialog box will show up shortly
        while (noButton == null && tries < 10) {
            Thread.sleep(200);
            // looking for No button to not overwrite
            noButton = (JButton) TestUtils.getChildIndexed(
                    textify, "JButton", 1);
            tries++;
        }
        assertNotNull(noButton);
        // click dialog's No button to save content
        noButton.doClick();

        String content = null;
        tries = 0;
        while (content == null && tries < 20) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(TextifyTest.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            String text = editor.getText();
            if (text != null && !text.isEmpty()) {
                content = text;
            }
            tries++;
        }
        assertEquals("testtest", content);
    }

    /**
     * Open new file, add content, click New button, get prompted to discard
     * content, click Yes, confirm empty content displayed.
     */
    @Test
    public void testNewButtonWhenNotEmptyOverwrite() throws InterruptedException, InvocationTargetException {
        // open new file
        Textify textify = new Textify(new String[]{TEST_PATH});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(
                textify, "editor");
        assertNotNull(editor);
        // put in content
        editor.setText("testtest");
        assertEquals("testtest", editor.getText());
        // click the New button
        JButton newButton = (JButton) TestUtils.getChildNamed(
                textify, "newButton");
        assertNotNull(newButton);

        SwingUtilities.invokeLater(() -> newButton.doClick());

        // wait for dialog
        JButton yesButton = null;
        int tries = 0;
        // the dialog box will show up shortly
        while (yesButton == null && tries < 20) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(TextifyTest.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            // use index to find OK button of dialog
            yesButton = (JButton) TestUtils.getChildIndexed(
                    textify, "JButton", 0);
            tries++;
        }
        assertNotNull(yesButton);
        // click dialog's Yes button to overwrite content displaying empty editor
        yesButton.doClick();

        String content = null;
        tries = 0;
        while (content == null && tries < 20) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(TextifyTest.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            String text = editor.getText();
            if (text != null) {
                content = text;
            }
            tries++;
        }
        assertEquals("", content);
    }

    @Test
    public void testSaveButton() throws InterruptedException {
        // open empty
        Textify textify = new Textify(new String[]{});
        // add content to editor
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        editor.setText("testtest");
        // get and click Save button
        JButton saveButton = (JButton) TestUtils.getChildNamed(textify, "saveButton");
        assertNotNull(saveButton);

        // click on Save async
        SwingUtilities.invokeLater(() -> saveButton.doClick());

        // the dialog box will show up shortly, get the filechooser when ready
        JFileChooser chooser = null;
        int tries = 0;
        while (chooser == null || tries < 20) {
            Thread.sleep(200);
            // use index to find filechooser of dialog
            if (chooser == null) {
                chooser = (JFileChooser) TestUtils.getChildIndexed(
                        textify, "JFileChooser", 0);
            }
            tries++;
        }
        assertNotNull(chooser);
        chooser.setSelectedFile(new File(TEST_PATH));

        JButton saveDialogButton = null;
        tries = 0;
        while (saveDialogButton == null || tries < 20) {
            Thread.sleep(200);
            // use index to find save button of dialog
            if (saveDialogButton == null) {
                saveDialogButton = (JButton) TestUtils.getChildIndexed(
                        textify, "JButton", 3);
            }
            tries++;
        }

        assertNotNull(saveDialogButton);
        final JButton finalSaveDialogButton = saveDialogButton;
        SwingUtilities.invokeLater(() -> finalSaveDialogButton.doClick());

        // confirm editor still has content
        assertNotNull(editor);
        assertEquals(8, editor.getText().length());
        // close and reopen file
        textify.dispose();
        textify = null;
        // restart textify
        textify = new Textify(new String[]{TEST_PATH});
        // confirm new editor has the content
        editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        String content = null;
        tries = 0;
        while (content == null || tries < 20) {
            String text = editor.getText();
            if (text != null && !text.isEmpty()) {
                content = text;
            }
            tries++;
        }
        assertEquals("testtest", content);
    }

    @Test
    public void testSaveAsButton() throws InterruptedException {
        // open empty
        Textify textify = new Textify(new String[]{});
        // add content
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        // put in content
        editor.setText("testtest");
        // get and click save button
        JButton saveAsButton = (JButton) TestUtils.getChildNamed(textify, "saveAsButton");
        assertNotNull(saveAsButton);
        SwingUtilities.invokeLater(() -> saveAsButton.doClick());
        // the dialog box will show up shortly
        int tries = 0;
        JFileChooser chooser = null;
        JButton saveDialogButton = null;
        while ((chooser == null || saveDialogButton == null) && tries < 20) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(TextifyTest.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            // use index to find save button of dialog
            if (chooser == null) {
                chooser = (JFileChooser) TestUtils.getChildIndexed(
                        textify, "JFileChooser", 0);
            }
            if (saveDialogButton == null) {
                saveDialogButton = (JButton) TestUtils.getChildIndexed(
                        textify, "JButton", 3);
            }
            tries++;

            assertNotNull(chooser);
            chooser.setSelectedFile(new File(TEST_PATH));
            assertNotNull(saveDialogButton);
            saveDialogButton.doClick();
            // confirm editor still has content
            JEditorPane editor2 = (JEditorPane) TestUtils.getChildNamed(
                    textify, "editor");
            assertNotNull(editor2);
            assertEquals(8, editor.getText().length());
            // close and reopen file
            textify.dispose();
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(TextifyTest.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            textify = new Textify(new String[]{TEST_PATH});
            // confirm editor still has content
            editor = (JEditorPane) TestUtils.getChildNamed(
                    textify, "editor");
            assertNotNull(editor);
            String content = null;
            tries = 0;
            while (content == null && tries < 20) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TextifyTest.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                if (content == null || content.isEmpty()) {
                    content = editor.getText();
                }
                tries++;
            }
            assertEquals("testtest", content);
        }
    }
}
