/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ca.footeware.swing.textify;

import javax.swing.JEditorPane;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author craig
 */
public class TextifyTest {

    @Test
    public void testEditorPane() {
        Textify textify = new Textify(new String[]{});
        JEditorPane editor = (JEditorPane) TestUtils.getChildNamed(textify, "editor");
        assertNotNull(editor);
        assertEquals(0, editor.getText().length());
    }
}
