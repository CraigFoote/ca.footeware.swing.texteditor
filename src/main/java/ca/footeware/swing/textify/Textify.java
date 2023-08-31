/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ca.footeware.swing.textify;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.FocusManager;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.PlainDocument;

/**
 * A minimal text editor.
 *
 * @author http://footeware.ca
 */
public class Textify extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(Textify.class.getName());
    private File file;
    private boolean dark = true;
    private boolean changed = false;
    private final DocumentListenerImpl listener;
    private JPopupMenu hamburgerPopup;
    private boolean hideFiles = true;

    /**
     * Creates new {@link Textify}.
     *
     * @param args
     */
    public Textify(String[] args) {
        this.listener = new DocumentListenerImpl();
        initComponents();
        boolean loaded = handleArgs(args);
        if (!loaded) {
            PlainDocument document = new PlainDocument();
            document.addDocumentListener(this.listener);
            this.jEditorPane.setDocument(document);
        }
        this.setIconImage(new ImageIcon(Textify.class
                .getResource("/images/textify.png")).getImage());
        this.jEditorPane.setComponentPopupMenu(getCutCopyPastePopupMenu());
        installKeyboardMonitor();
        this.setLocationRelativeTo(null);
        this.jEditorPane.requestFocus();
    }

    /**
     * Gets a popup menu with cut, copy and paste actions.
     *
     * @return {@link JPopupMenu}
     */
    private JPopupMenu getCutCopyPastePopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Cut");
        item.addActionListener((ActionEvent e) -> {
            jEditorPane.cut();
        });
        menu.add(item);
        item = new JMenuItem("Copy");
        item.addActionListener((ActionEvent e) -> {
            jEditorPane.copy();
        });
        menu.add(item);
        item = new JMenuItem("Paste");
        item.addActionListener((ActionEvent e) -> {
            jEditorPane.paste();
        });
        menu.add(item);
        return menu;
    }

    /**
     * Handle command line arguments.
     *
     * @param args
     * @return boolean true if a file was loaded and displayed
     */
    private boolean handleArgs(String[] args) {
        boolean loaded = false;
        if (args.length > 1) {
            Logger.getLogger(Textify.class.getName()).log(Level.INFO, "Too many args, only one is accepted. It should be a filename that may already exist.");
        } else if (args.length == 1 && args[0] != null) {
            LOGGER.log(Level.INFO, "Found one arg: {0}", args[0]);
            this.file = new File(args[0]);
            if (!this.file.exists()) {
                try {
                    this.file.createNewFile();
                    LOGGER.log(Level.INFO, "File created.");
                    this.setTitle(this.file.getAbsolutePath());
                    this.jEditorPane.requestFocus();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "An error occurred creating file: " + this.file.getAbsolutePath(), e);
                }
            } else {
                LOGGER.log(Level.INFO, "Opening file.");
                Path path = this.file.toPath();
                try {
                    String mimeType = Files.probeContentType(path);
                    if (mimeType == null || "text/plain".equals(mimeType)) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(
                                new FileInputStream(this.file)));
                        this.jEditorPane.read(input, "Reading file.");
                        this.jEditorPane.getDocument().addDocumentListener(listener);
                        this.setTitle(this.file.getAbsolutePath());
                        loaded = true;
                    } else {
                        LOGGER.log(Level.SEVERE, "File is not text/plain.");
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, null, e);
                }
            }
        }
        return loaded;
    }

    /**
     * Listens for CTRL+W to close window and CTRL+s to save file.
     */
    private void installKeyboardMonitor() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((KeyEvent ke) -> {
            Window window = FocusManager.getCurrentManager().getActiveWindow();
            if (ke.getID() == KeyEvent.KEY_PRESSED) {
                if (ke.isControlDown()) {
                    if (ke.getKeyCode() == KeyEvent.VK_W) {
                        if (this.changed) {
                            int response;
                            if (this.file != null) {
                                response = JOptionPane.showConfirmDialog(this, "Do you want to save changes to '" + file.getName() + "' before closing?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
                            } else {
                                response = JOptionPane.showConfirmDialog(this, "Do you want to save changes before closing?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
                            }

                            switch (response) {
                                case JOptionPane.CANCEL_OPTION -> {
                                    return true;
                                }
                                case JOptionPane.YES_OPTION -> {
                                    saveChanges();
                                }
                                case JOptionPane.NO_OPTION -> {
                                    // allow window to close
                                }
                            }
                        }
                        if (window != null) {
                            window.dispose();
                        }
                        return true;
                    } else if (ke.getKeyCode() == KeyEvent.VK_S) {
                        saveChanges();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * Get a standardized {@link JFileChooser} with provided title.
     *
     * @param title {@link String}
     * @return {@link JFileChooser}
     */
    private JFileChooser getFileChooser(String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Document", "txt"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
        fileChooser.setFileHidingEnabled(!hideFiles);
        fileChooser.setDialogTitle(title);
        return fileChooser;
    }

    /**
     * Save file to disk.
     */
    private void saveChanges() {
        boolean write = false;
        if (this.file != null) {
            LOGGER.log(Level.INFO, "file=" + file.getName());
            write = true;
        } else {
            JFileChooser fileChooser = getFileChooser("Save");
            int response = fileChooser.showSaveDialog(this);
            if (response == JFileChooser.APPROVE_OPTION) {
                LOGGER.log(Level.INFO, "saving " + file.getName());
                File chosen = fileChooser.getSelectedFile();
                LOGGER.log(Level.INFO, "chosen=" + chosen);
                if (!chosen.exists()) {
                    write = true;
                    this.file = chosen;
                } else {
                    LOGGER.log(Level.INFO, "chosen exists");
                    int response2 = JOptionPane.showConfirmDialog(this, "Overwrite?", "File Exists", JOptionPane.YES_NO_OPTION);
                    switch (response2) {
                        case JOptionPane.YES_OPTION -> {
                            this.file = chosen;
                            write = true;
                            LOGGER.log(Level.INFO, "overwriting " + file.getName());
                            break;
                        }
                        case JOptionPane.NO_OPTION -> {
                            break;
                        }
                    }
                }
            }
        }

        LOGGER.log(Level.INFO, "write=" + write);
        LOGGER.log(Level.INFO, "this.file=" + file);
        if (write && this.file != null) {
            try {
                if (!this.file.exists()) {
                    LOGGER.log(Level.INFO, "creating file " + file.getName());
                    this.file.createNewFile();
                }
                FileWriter writer = new FileWriter(this.file);
                try (BufferedWriter bw = new BufferedWriter(writer)) {
                    this.jEditorPane.write(bw);
                    LOGGER.log(Level.INFO, file.getName() + " written");
                }
                this.setTitle(this.file.getAbsolutePath());
                this.jEditorPane.requestFocus();
                this.changed = false;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error Saving", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane = new javax.swing.JScrollPane();
        jEditorPane = new javax.swing.JEditorPane();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("textify");
        setIconImages(null);

        jScrollPane.setViewportBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jEditorPane.setFont(new java.awt.Font("Ubuntu Mono", 0, 18)); // NOI18N
        jEditorPane.setName("editor"); // NOI18N
        jScrollPane.setViewportView(jEditorPane);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-new-document-24.png"))); // NOI18N
        jButton1.setToolTipText("New");
        jButton1.setName("newButton"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-open-24.png"))); // NOI18N
        jButton2.setToolTipText("Open");
        jButton2.setName("openButton"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-save-24.png"))); // NOI18N
        jButton3.setToolTipText("Save");
        jButton3.setName("saveButton"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-save-as-24.png"))); // NOI18N
        jButton4.setToolTipText("Save As");
        jButton4.setName("saveAsButton"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-hamburger-menu-24.png"))); // NOI18N
        jButton5.setToolTipText("Save As");
        jButton5.setName("hamburgerButton"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 715, Short.MAX_VALUE)
                .addComponent(jButton5)
                .addContainerGap())
            .addComponent(jScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                .addGap(21, 21, 21))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Save button pressed.
     *
     * @param evt
     */
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        saveChanges();
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * Save As button clicked.
     *
     * @param evt
     */
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        JFileChooser fileChooser = getFileChooser("Save As");
        int response = fileChooser.showSaveDialog(this);
        if (response == JFileChooser.APPROVE_OPTION) {
            this.file = fileChooser.getSelectedFile();
            saveChanges();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    /**
     * Open button clicked.
     *
     * @param evt
     */
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (changed) {
            int response;
            if (file != null) {
                response = JOptionPane.showConfirmDialog(this, "Do you want to discard unsaved changes to file '" + file.getName() + "'?", "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            } else {
                response = JOptionPane.showConfirmDialog(this, "Do you want to discard unsaved changes?", "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            }
            if (response == JOptionPane.NO_OPTION) {
                return;
            }
        }
        JFileChooser fileChooser = getFileChooser("Open");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            this.file = fileChooser.getSelectedFile();
            Path path = this.file.toPath();
            try {
                String mimeType = null;
                try {
                    mimeType = Files.probeContentType(path);

                } catch (IOException ex) {
                    Logger.getLogger(Textify.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                if (mimeType == null || "text/plain".equals(mimeType)) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(
                            new FileInputStream(this.file)));
                    this.jEditorPane.read(input, "Reading file.");
                    this.jEditorPane.getDocument().addDocumentListener(listener);
                    this.setTitle(this.file.getAbsolutePath());
                    this.jEditorPane.requestFocus();
                    this.changed = false;
                } else {
                    LOGGER.log(Level.SEVERE, "File is not text/plain: {0}", mimeType);
                    JOptionPane.showMessageDialog(this, "File is not 'text/plain'.");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error", JOptionPane.OK_OPTION);
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * New button clicked.
     *
     * @param evt
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (this.changed) {
            int response;
            if (this.file != null) {
                response = JOptionPane.showConfirmDialog(this, "Do you want to discard unsaved changes to file '" + file.getName() + "'?", "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            } else {
                response = JOptionPane.showConfirmDialog(this, "Do you want to discard unsaved changes?", "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            }
            if (response == JOptionPane.NO_OPTION) {
                this.jEditorPane.requestFocus();
                return;
            }
        }
        PlainDocument document = new PlainDocument();
        this.jEditorPane.setDocument(document);
        document.addDocumentListener(listener);
        this.file = null;
        this.setTitle("Textify");
        this.jEditorPane.requestFocus();
        this.changed = false;
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        this.hamburgerPopup = new JPopupMenu();
        JCheckBox darkModeCheckBox = new JCheckBox("Dark Mode", dark);
        darkModeCheckBox.setMargin(new Insets(10, 10, 10, 10));
        darkModeCheckBox.addItemListener((evt2) -> {
            FlatLaf laf = dark ? new FlatLightLaf() : new FlatDarkLaf();
            try {
                UIManager.setLookAndFeel(laf);
                SwingUtilities.updateComponentTreeUI(this);
                dark = !dark;

            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(Textify.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        });
        this.hamburgerPopup.add(darkModeCheckBox);

        JCheckBox showHiddenFilesCheckBox = new JCheckBox("Show Hidden Files in File Chooser", hideFiles);
        showHiddenFilesCheckBox.setMargin(new Insets(10, 10, 10, 10));
        showHiddenFilesCheckBox.addItemListener((evt3) -> {
            this.hideFiles = !this.hideFiles;
        });
        this.hamburgerPopup.add(showHiddenFilesCheckBox);
        this.hamburgerPopup.show(this.jButton5, 0, this.jButton5.getHeight());
    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * Main.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatDarkLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
        java.awt.EventQueue.invokeLater(() -> {
            Textify textify = new Textify(args);
            textify.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JEditorPane jEditorPane;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Listens for changes to current document and sets 'changed' local boolean
     * variable.
     */
    private class DocumentListenerImpl implements DocumentListener, Serializable {

        private static final long serialVersionUID = 1L;

        public DocumentListenerImpl() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changed = true;
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changed = true;
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changed = true;
        }
    }
}
