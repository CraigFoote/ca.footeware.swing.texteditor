/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ca.footeware.swing.textify;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link Textify}.
 *
 * @author http://footeware.ca
 */
public class TextifyTest {

    private Textify textify;

    @BeforeEach
    public void beforeEach() {
        cleanupFiles();
    }

    @AfterAll
    public static void afterAll() {
        cleanupFiles();
    }

    private static void cleanupFiles() {
        File file = new File("test.txt");
        if (file.exists()) {
            file.delete();
        }
        file = new File("test2.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    @AfterEach
    public void tearDown() {
        if (textify != null) {
            textify.dispose();
            textify = null;
        }
    }

    @Test
    public void testEditorPaneOpensEmpty() {
        textify = new Textify(new String[]{});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());
    }

    @Test
    public void testEditorPaneOpensWithNonexistantFileArg() {
        textify = new Textify(new String[]{"test.txt"});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());
    }

    @Test
    public void testEditorPaneOpensWithExistingFileArg() {
        File file = new File("test.txt");
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(TextifyTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        textify = new Textify(new String[]{"test.txt"});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        editor.setText("test");
        assertEquals(4, editor.getText().length());
        JButton saveButton = (JButton) TestUtils.getChildNamed(textify, "saveButton");
        saveButton.doClick();
        textify.dispose();

        textify = new Textify(new String[]{"test.txt"});
        editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(4, editor.getText().length());
    }

    @Test
    public void testNewButtonWhenEmpty() {
        textify = new Textify(new String[]{"test.txt"});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());

        JButton newButton = (JButton) TestUtils.getChildNamed(textify, "newButton");
        assertNotNull(newButton);
        newButton.doClick();
        assertEquals(0, editor.getText().length());
    }

    @Test
    public void testNewButtonWhenNotEmpty() {
        textify = new Textify(new String[]{"test.txt"});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        editor.setText("test");
        assertEquals(4, editor.getText().length());

        JButton newButton = (JButton) TestUtils.getChildNamed(textify, "newButton");
        assertNotNull(newButton);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                newButton.doClick();
            }
        });

        JButton okButton = null;
        // The dialog box will show up shortly
        int tries = 0;
        while (okButton == null && tries < 20) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(TextifyTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            okButton = (JButton) TestUtils.getChildIndexed(textify, "JButton", 0);
            tries++;
        }
        assertNotNull(okButton);
        okButton.doClick();
        SwingUtilities.invokeLater(() -> {
            assertEquals(0, editor.getText().length());
        });
    }
}
