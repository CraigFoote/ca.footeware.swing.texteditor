/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ca.footeware.swing.textify;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.FocusManager;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
    private boolean hideFiles = true;

    /**
     * Creates and opens a new {@link Textify} window.
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
            this.editor.setDocument(document);
        }
        this.setIconImage(new ImageIcon(Textify.class
                .getResource("/images/textify.png")).getImage());
        this.editor.setComponentPopupMenu(getCutCopyPastePopupMenu());
        installKeyboardMonitor();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(getWindowListener());
        this.setVisible(true);
        this.editor.requestFocus();
    }

    /**
     * Listens for window closing and prompts user to save if changes to editor
     * text.
     *
     * @return {@link WindowListener}
     */
    private WindowListener getWindowListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (changed) {
                    int response;
                    if (file != null) {
                        response = JOptionPane.showConfirmDialog(Textify.this,
                                "Do you want to discard unsaved changes to file '"
                                + file.getName() + "'?", "Unsaved Changes",
                                JOptionPane.YES_NO_OPTION);
                    } else {
                        response = JOptionPane.showConfirmDialog(Textify.this,
                                "Do you want to discard unsaved changes?",
                                "Unsaved Changes", JOptionPane.YES_NO_OPTION);
                    }
                    if (response == JOptionPane.NO_OPTION) {
                        saveChanges();
                    } else if (response == JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            }
        };
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
            editor.cut();
        });
        menu.add(item);
        item = new JMenuItem("Copy");
        item.addActionListener((ActionEvent e) -> {
            editor.copy();
        });
        menu.add(item);
        item = new JMenuItem("Paste");
        item.addActionListener((ActionEvent e) -> {
            editor.paste();
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
            Logger.getLogger(Textify.class
                    .getName()).log(Level.INFO,
                            "Too many args, only one is accepted. It should be a filename that may already exist.");
        } else if (args.length == 1 && args[0] != null) {
            LOGGER.log(Level.INFO, "Found one arg: {0}", args[0]);
            this.file = new File(args[0]);
            if (!this.file.exists()) {
                try {
                    this.file.createNewFile();
                    LOGGER.log(Level.INFO, "File created.");
                    this.setTitle(this.file.getAbsolutePath());
                    this.editor.requestFocus();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "An error occurred creating file: "
                            + this.file.getAbsolutePath(), e);
                }
            } else {
                LOGGER.log(Level.INFO, "Opening file.");
                Path path = this.file.toPath();
                try {
                    String mimeType = Files.probeContentType(path);
                    if (mimeType == null || "text/plain".equals(mimeType)) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(
                                new FileInputStream(this.file)));
                        this.editor.read(input, "Reading file.");
                        this.editor.getDocument().addDocumentListener(listener);
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
                                response = JOptionPane.showConfirmDialog(this,
                                        "Do you want to save changes to '" + file.getName() + "' before closing?",
                                        "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
                            } else {
                                response = JOptionPane.showConfirmDialog(this,
                                        "Do you want to save changes before closing?",
                                        "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
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
            LOGGER.log(Level.INFO, "file={0}", file.getName());
            write = true;
        } else {
            JFileChooser fileChooser = getFileChooser("Save");
            int response = fileChooser.showSaveDialog(this);
            if (response == JFileChooser.APPROVE_OPTION) {
                LOGGER.log(Level.INFO, "saving");
                File chosen = fileChooser.getSelectedFile();
                LOGGER.log(Level.INFO, "chosen={0}", chosen);
                if (!chosen.exists()) {
                    write = true;
                    this.file = chosen;
                } else {
                    LOGGER.log(Level.INFO, "chosen exists");
                    int response2 = JOptionPane.showConfirmDialog(this,
                            "Overwrite?", "File Exists", JOptionPane.YES_NO_OPTION);
                    switch (response2) {
                        case JOptionPane.YES_OPTION -> {
                            this.file = chosen;
                            write = true;
                            LOGGER.log(Level.INFO, "overwriting {0}", file.getName());
                            break;
                        }
                        case JOptionPane.NO_OPTION -> {
                            break;
                        }
                    }
                }
            }
        }

        LOGGER.log(Level.INFO, "write={0}", write);
        LOGGER.log(Level.INFO, "this.file={0}", file);
        if (write && this.file != null) {
            try {
                if (!this.file.exists()) {
                    LOGGER.log(Level.INFO, "creating file {0}", file.getName());
                    this.file.createNewFile();
                }
                FileWriter writer = new FileWriter(this.file);
                try (BufferedWriter bw = new BufferedWriter(writer)) {
                    this.editor.write(bw);
                    LOGGER.log(Level.INFO, "{0} written", file.getName());
                }
                this.setTitle(this.file.getAbsolutePath());
                this.editor.requestFocus();
                this.changed = false;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
                        "Error Saving", JOptionPane.ERROR_MESSAGE);
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

        scroller = new javax.swing.JScrollPane();
        editor = new javax.swing.JEditorPane();
        newButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        hamburger = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("textify");
        setIconImages(null);

        scroller.setViewportBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        editor.setFont(new java.awt.Font("Ubuntu Mono", 0, 18)); // NOI18N
        editor.setName("editor"); // NOI18N
        scroller.setViewportView(editor);

        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-new-document-24.png"))); // NOI18N
        newButton.setToolTipText("New");
        newButton.setName("newButton"); // NOI18N
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-open-24.png"))); // NOI18N
        openButton.setToolTipText("Open");
        openButton.setName("openButton"); // NOI18N
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-save-24.png"))); // NOI18N
        saveButton.setToolTipText("Save");
        saveButton.setName("saveButton"); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        saveAsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-save-as-24.png"))); // NOI18N
        saveAsButton.setToolTipText("Save As");
        saveAsButton.setName("saveAsButton"); // NOI18N
        saveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsButtonActionPerformed(evt);
            }
        });

        hamburger.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icons8-hamburger-menu-24.png"))); // NOI18N
        hamburger.setToolTipText("Save As");
        hamburger.setName("hamburgerButton"); // NOI18N
        hamburger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hamburgerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(openButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveAsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 715, Short.MAX_VALUE)
                .addComponent(hamburger)
                .addContainerGap())
            .addComponent(scroller)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveAsButton)
                    .addComponent(newButton)
                    .addComponent(openButton)
                    .addComponent(saveButton)
                    .addComponent(hamburger))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroller, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Save button pressed.
     *
     * @param evt
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveChanges();
    }//GEN-LAST:event_saveButtonActionPerformed

    /**
     * Save As button clicked.
     *
     * @param evt
     */
    private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsButtonActionPerformed
        JFileChooser fileChooser = getFileChooser("Save As");
        int response = fileChooser.showSaveDialog(this);
        if (response == JFileChooser.APPROVE_OPTION) {
            this.file = fileChooser.getSelectedFile();
            saveChanges();
        }
    }//GEN-LAST:event_saveAsButtonActionPerformed

    /**
     * Open button clicked.
     *
     * @param evt
     */
    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        if (changed) {
            int response;
            if (file != null) {
                response = JOptionPane.showConfirmDialog(this,
                        "Do you want to discard unsaved changes to file '?"
                        + file.getName() + "'?", "Unsaved Changes",
                        JOptionPane.YES_NO_OPTION);
            } else {
                response = JOptionPane.showConfirmDialog(this,
                        "Do you want to discard unsaved changes?",
                        "Unsaved Changes", JOptionPane.YES_NO_OPTION);
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
                    LOGGER.log(Level.SEVERE, null, ex);
                }
                if (mimeType == null || "text/plain".equals(mimeType)) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(
                            new FileInputStream(this.file)));
                    this.editor.read(input, "Reading file.");
                    this.editor.getDocument().addDocumentListener(listener);
                    this.setTitle(this.file.getAbsolutePath());
                    this.editor.requestFocus();
                    this.changed = false;
                } else {
                    LOGGER.log(Level.SEVERE, "File is not text/plain: {0}", mimeType);
                    JOptionPane.showMessageDialog(this, "File is not 'text/plain'.");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
                        "Error", JOptionPane.OK_OPTION);
            }
        }
    }//GEN-LAST:event_openButtonActionPerformed

    /**
     * New button clicked.
     *
     * @param evt
     */
    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        if (this.changed) {
            int response;
            if (this.file != null) {
                response = JOptionPane.showConfirmDialog(this,
                        "Do you want to discard unsaved changes to file '" + file.getName() + "'?",
                        "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            } else {
                response = JOptionPane.showConfirmDialog(this,
                        "Do you want to discard unsaved changes?",
                        "Unsaved Changes", JOptionPane.YES_NO_OPTION);
            }
            if (response == JOptionPane.NO_OPTION) {
                this.editor.requestFocus();
                return;
            }
        }
        PlainDocument document = new PlainDocument();
        this.editor.setDocument(document);
        document.addDocumentListener(listener);
        this.file = null;
        this.setTitle("Textify");
        this.editor.requestFocus();
        this.changed = false;
    }//GEN-LAST:event_newButtonActionPerformed

    /**
     * Hamburger menu accessed.
     *
     * @param evt
     */
    private void hamburgerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hamburgerActionPerformed
        JPopupMenu hamburgerPopup = new JPopupMenu();
        hamburgerPopup.setName("hamburgerPopup");

        // dark mode
        JCheckBox darkModeCheckBox = new JCheckBox("Dark Mode", dark);
        darkModeCheckBox.setMargin(new Insets(10, 10, 10, 10));
        darkModeCheckBox.addItemListener((evt2) -> {
            FlatLaf laf = dark ? new FlatLightLaf() : new FlatDarkLaf();
            try {
                UIManager.setLookAndFeel(laf);
                SwingUtilities.updateComponentTreeUI(this);
                dark = !dark;

            } catch (UnsupportedLookAndFeelException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });
        hamburgerPopup.add(darkModeCheckBox);

        // show hidden files in file chooser
        JCheckBox showHiddenFilesCheckBox = new JCheckBox("Show Hidden Files in File Chooser",
                hideFiles);
        showHiddenFilesCheckBox.setMargin(new Insets(10, 10, 10, 10));
        showHiddenFilesCheckBox.addItemListener((evt3) -> {
            this.hideFiles = !this.hideFiles;
        });
        hamburgerPopup.add(showHiddenFilesCheckBox);

        // about dialog
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setMargin(new Insets(10, 10, 10, 10));
        aboutMenuItem
                .addActionListener((ActionEvent e) -> {
                    // clickable image and text
                    ImageIcon icon = new ImageIcon(Textify.class
                            .getResource("/images/programmer.jpg"));
                    JLabel imgLabel = new JLabel(icon);
                    imgLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    JLabel msgLabel = new JLabel("Another fine mess by Footeware.ca");
                    msgLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    MouseListener listener = new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            try {
                                Desktop.getDesktop().browse(new URI("http://Footeware.ca"));
                            } catch (URISyntaxException | IOException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        }
                    };
                    imgLabel.addMouseListener(listener);
                    msgLabel.addMouseListener(listener);

                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                    imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panel.add(imgLabel, BorderLayout.NORTH);
                    panel.add(Box.createRigidArea(new Dimension(0, 20)));
                    panel.add(msgLabel, BorderLayout.SOUTH);
                    JOptionPane.showMessageDialog(Textify.this, panel,
                            "About", JOptionPane.PLAIN_MESSAGE);
                });
        hamburgerPopup.add(aboutMenuItem);

        hamburgerPopup.show(this.hamburger, 0, this.hamburger.getHeight());

    }//GEN-LAST:event_hamburgerActionPerformed

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
            new Textify(args);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane editor;
    private javax.swing.JButton hamburger;
    private javax.swing.JButton newButton;
    private javax.swing.JButton openButton;
    private javax.swing.JButton saveAsButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JScrollPane scroller;
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
