/*
 NET-Simulator -- Network simulator.
 Copyright (C) 2006 Maxim Tereshin <maxim-tereshin@yandex.ru>

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
            
 This program is distributed in the hope that it will be useful, but 
 WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 General Public License for more details.
            
 You should have received a copy of the GNU General Public License along 
 with this program; if not, write to the Free Software Foundation, 
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
 */
package org.netsimulator.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
import javax.xml.transform.*;
import org.netsimulator.util.*;
import org.xml.sax.*;

public class MainFrame
        extends JFrame
        implements
        ActionListener,
        ErrorHandler,
        SerializingErrorHandler,
        ExportToHtmlStartCompleteListener,
        ProjectXMLSerializingCompleteListener,
        ProjectXMLLoadingCompleteListener {

    public static final String NET_SIMULATOR_TITLE = "NET-Simulator";

    public static Config config;

    private static final String DEFAULTSKIN = "basic";
    private static final JFileChooser fileChooserXML = new JFileChooser();
    private static final JFileChooser fileChooserHtml = new JFileChooser();
    private static final File confFile = new File("cfg/config.xml");
    private static final Logger logger
            = Logger.getLogger("org.netsimulator.gui.MainFrame");
    private static final ResourceBundle rsc
            = ResourceBundle.getBundle("netsimulator", Locale.getDefault());
    private final AboutDialog aboutDilaog = new AboutDialog(this, true);
    private final LicenseDialog licenseDilaog = new LicenseDialog(this, true);
    private final SetupDialog setupDialog = new SetupDialog(this, true);
    private File currentFile;
    private Thread serializingThread;
    private final ProjectPropertiesDialog propertiesDialog
            = new ProjectPropertiesDialog(this, true);
    private final ProgressDialog progressDialog
            = new ProgressDialog(this, true);
    private final JScrollPane projectScrollPane = new JScrollPane();

    private final JMenuBar menuBar;

    private final JMenu file_menu, devices_menu, service_menu, help_menu;
    private final JMenuItem exit_menu_item, new_menu_item, save_menu_item,
            save_as_menu_item, open_menu_item, preview_menu_item, print_menu_item,
            properties_menu_item, close_menu_item, export_to_html_menu_item;
    private final JMenuItem router_menu_item, desktop_menu_item, hub_menu_item,
            switch_menu_item, media_menu_item;
    private final JMenuItem about_menu_item, license_menu_item;
    private final JMenuItem setup_menu_item;

    private NetworkPanel networkPanel;

    public MainFrame(String language, String country) {
        setTitle(NET_SIMULATOR_TITLE);

        fileChooserXML.addChoosableFileFilter(new XMLFileFilter());
        fileChooserHtml.addChoosableFileFilter(new HtmlFileFilter());

        menuBar = new JMenuBar();

        file_menu = new JMenu(rsc.getString("Project"));
        new_menu_item = new JMenuItem(rsc.getString("Create"));
        new_menu_item.addActionListener(this);
        file_menu.add(new_menu_item);
        open_menu_item = new JMenuItem(rsc.getString("Open") + "...");
        open_menu_item.addActionListener(this);
        file_menu.add(open_menu_item);
        file_menu.addSeparator();
        close_menu_item = new JMenuItem(rsc.getString("Close"));
        close_menu_item.addActionListener(this);
        file_menu.add(close_menu_item);
        save_menu_item = new JMenuItem(rsc.getString("Save"));
        save_menu_item.addActionListener(this);
        file_menu.add(save_menu_item);
        save_as_menu_item = new JMenuItem(rsc.getString("Save as") + "...");
        save_as_menu_item.addActionListener(this);
        file_menu.add(save_as_menu_item);
        export_to_html_menu_item = new JMenuItem(rsc.getString("Export to html") + "...");
        export_to_html_menu_item.addActionListener(this);
        file_menu.add(export_to_html_menu_item);
        file_menu.addSeparator();
        preview_menu_item = null;
        //preview_menu_item = new JMenuItem(rsc.getString("Preview"));
        //preview_menu_item.addActionListener(this);
        //file_menu.add(preview_menu_item);
        print_menu_item = null;
        //print_menu_item = new JMenuItem(rsc.getString("Print")+"...");
        //print_menu_item.addActionListener(this);
        //file_menu.add(print_menu_item);
        //file_menu.addSeparator();
        properties_menu_item = new JMenuItem(rsc.getString("Properties") + "...");
        properties_menu_item.addActionListener(this);
        file_menu.add(properties_menu_item);
        file_menu.addSeparator();
        exit_menu_item = new JMenuItem(rsc.getString("Exit"));
        exit_menu_item.addActionListener(this);
        file_menu.add(exit_menu_item);
        menuBar.add(file_menu);

        devices_menu = new JMenu(rsc.getString("Devices"));
        router_menu_item = new JMenuItem(rsc.getString("Router"));
        router_menu_item.addActionListener(this);
        devices_menu.add(router_menu_item);
        desktop_menu_item = new JMenuItem(rsc.getString("Desktop"));
        desktop_menu_item.addActionListener(this);
        devices_menu.add(desktop_menu_item);
        hub_menu_item = new JMenuItem(rsc.getString("Concentrator (Hub)"));
        hub_menu_item.addActionListener(this);
        devices_menu.add(hub_menu_item);
        switch_menu_item = new JMenuItem(rsc.getString("Commutator (Switch)"));
        switch_menu_item.addActionListener(this);
        devices_menu.add(switch_menu_item);
        media_menu_item = new JMenuItem(rsc.getString("Pathcord"));
        media_menu_item.addActionListener(this);
        devices_menu.add(media_menu_item);
        menuBar.add(devices_menu);
        service_menu = new JMenu(rsc.getString("Service"));
        menuBar.add(service_menu);
        setup_menu_item = new JMenuItem(rsc.getString("Setup") + "...");
        setup_menu_item.addActionListener(this);
        service_menu.add(setup_menu_item);
        menuBar.add(Box.createHorizontalGlue());
        help_menu = new JMenu(rsc.getString("Help"));
        about_menu_item = new JMenuItem(rsc.getString("About") + "...");
        about_menu_item.addActionListener(this);
        help_menu.add(about_menu_item);
        license_menu_item = new JMenuItem(rsc.getString("License") + "...");
        license_menu_item.addActionListener(this);
        help_menu.add(license_menu_item);
        menuBar.add(help_menu);

        setJMenuBar(menuBar);

        getContentPane().add(projectScrollPane, BorderLayout.CENTER);
        getContentPane().setBackground(Color.LIGHT_GRAY);

//        JLabel l = new JLabel("Status");
//        l.setBorder(BorderFactory.createLoweredBevelBorder());
//        getContentPane().add(l, BorderLayout.SOUTH);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        pack();
        setSize(700, 500);
        setVisible(true);

        // config loading 
        if (!confFile.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    rsc.getString("Configuration file does not exists, using default settings."),
                    rsc.getString("Information"),
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            ConfigLoader configLoader = new ConfigLoader(new LoadingConfigErrorHandler(this));
            try {
                config = configLoader.load(confFile);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Unexpected exception.", ioe);
                JOptionPane.showMessageDialog(
                        this,
                        rsc.getString("Input/output error during configuration file loading."),
                        rsc.getString("Error"),
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            } catch (SAXException saxe) {
                logger.log(Level.SEVERE, "Unexpected exception.", saxe);
                JOptionPane.showMessageDialog(
                        this,
                        rsc.getString("Error during configuration file parsing."),
                        rsc.getString("Error"),
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        //setting looger
        initLoggers(config.getDebug());

        // skin setting
        String currentSkin = config.getCurrentSkin();
        if (currentSkin == null) {
            currentSkin = DEFAULTSKIN;
            logger.log(Level.WARNING, "Name of current skin is not set in config, using default ''{0}''.", DEFAULTSKIN);
        }

        HashMap<String, ArrayList<ShapeInfo>> skins = config.getSkins();
        if (skins == null) {
            logger.severe("Skins is not set in config.");
            System.exit(1);
        }

        ArrayList<ShapeInfo> skin = skins.get(currentSkin);
        if (skin == null && currentSkin.equals(DEFAULTSKIN)) {
            logger.log(Level.SEVERE, "Skin ''{0}'' is not set in config.", currentSkin);
            System.exit(1);
        }
        if (skin == null && !currentSkin.equals(DEFAULTSKIN)) {
            logger.log(Level.WARNING, "Skin ''{0}'' is not set in config. Trying default ''{1}''", new Object[]{currentSkin, DEFAULTSKIN});
            currentSkin = DEFAULTSKIN;
            skin = skins.get(currentSkin);
            if (skin == null) {
                logger.log(Level.SEVERE, "Skin ''{0}'' is not set in config.", currentSkin);
                System.exit(1);
            }
        }
        for (ShapeInfo shapeInfo : skin) {
            if (shapeInfo.getName().equals("desktop")) {
                DesktopNetworkShape.setSkin(currentSkin, shapeInfo);
            }
            if (shapeInfo.getName().equals("router")) {
                RouterNetworkShape.setSkin(currentSkin, shapeInfo);
            }
            if (shapeInfo.getName().equals("switch")) {
                SwitchNetworkShape.setSkin(currentSkin, shapeInfo);
            }
            if (shapeInfo.getName().equals("hub")) {
                HubNetworkShape.setSkin(currentSkin, shapeInfo);
            }
        }

        // project init
        String whatOpenAfterStart = config.getWhatOpenAfterStart();
        if (whatOpenAfterStart != null && whatOpenAfterStart.equals("lastProject")) {
            processNewMenuItem();
            String lastProject = config.getLastProject();
            currentFile = new File(lastProject);
            openFile();
        }

        if (whatOpenAfterStart == null || whatOpenAfterStart.equals("emptyProject")) {
            processNewMenuItem();
        }

        if (whatOpenAfterStart != null && whatOpenAfterStart.equals("nothing")) {
            setCurrentProjectDependentMenuItemsEnable(false);
        }

    }

    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            processExitMenuItem();
        }
        //super.processWindowEvent(e);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exit_menu_item) {
            processExitMenuItem();
        }

        if (e.getSource() == setup_menu_item) {
            processSetupMenuItem();
        }

        if (e.getSource() == desktop_menu_item) {
            networkPanel.createDesktop();
        }

        if (e.getSource() == media_menu_item) {
            networkPanel.createMedia();
        }

        if (e.getSource() == hub_menu_item) {
            networkPanel.createHub();
        }

        if (e.getSource() == switch_menu_item) {
            networkPanel.createSwitch();
        }

        if (e.getSource() == router_menu_item) {
            networkPanel.createRouter();
        }

        if (e.getSource() == close_menu_item) {
            processCloseMenuItem();
        }

        if (e.getSource() == save_menu_item) {
            processSaveMenuItem();
        }

        if (e.getSource() == save_as_menu_item) {
            processSaveAsMenuItem();
        }

        if (e.getSource() == export_to_html_menu_item) {
            processExportToHtmlMenuItem();
        }

        if (e.getSource() == open_menu_item) {
            processOpenMenuItem();
        }

        if (e.getSource() == new_menu_item) {
            processNewMenuItem();
        }

        if (e.getSource() == properties_menu_item) {
            propertiesDialog.setAuthor(networkPanel.getAuthor());
            propertiesDialog.setComment(networkPanel.getDescription());
            propertiesDialog.setTitle(rsc.getString("Properties"));
            propertiesDialog.setVisible(true);

            if (propertiesDialog.pressedOk()) {
                networkPanel.setAuthor(propertiesDialog.getAuthor());
                networkPanel.setDescription(propertiesDialog.getComment());
            }
            propertiesDialog.clear();
        }

        if (e.getSource() == about_menu_item) {
            aboutDilaog.moveToCenterOfParentComponent();
            aboutDilaog.setVisible(true);
        }

        if (e.getSource() == license_menu_item) {
            licenseDilaog.moveToCenterOfParentComponent();
            licenseDilaog.setVisible(true);
        }

    }

    public void warning(SAXParseException e) throws SAXException {
        loadingErrorProcess(e);
    }

    public void fatalError(SAXParseException e) throws SAXException {
        loadingErrorProcess(e);
    }

    public void error(SAXParseException e) throws SAXException {
        loadingErrorProcess(e);
    }

    private void loadingErrorProcess(SAXParseException e) {
        logger.log(Level.SEVERE, "Unexpected exception.", e);
        JOptionPane.showMessageDialog(
                this,
                rsc.getString("Error during project file parsing row ") + e.getLineNumber() + rsc.getString(", column ") + e.getColumnNumber(),
                rsc.getString("Error"),
                JOptionPane.ERROR_MESSAGE);
        progressDialog.setVisible(false);
    }

    public void fatalError(Exception e) {
        logger.log(Level.SEVERE, "Unexpected exception.", e);
        JOptionPane.showMessageDialog(
                this,
                rsc.getString("Input/output error during project file saving."),
                rsc.getString("Error"),
                JOptionPane.ERROR_MESSAGE);
        progressDialog.setVisible(false);
    }

    private void openFile() {
        try {
            progressDialog.setTitle(rsc.getString("Loading file: ") + currentFile);
            ProjectXMLLoader loader
                    = new ProjectXMLLoader(
                            networkPanel,
                            this,
                            progressDialog.getProgressBar());
            loader.setSourceFile(currentFile);
            loader.addProjectXMLLoadingCompleteListener(this);
            new Thread(loader).start();
            progressDialog.moveToCenterOfParentComponent();
            progressDialog.setVisible(true);
        } catch (IOException io) {
            logger.log(Level.SEVERE, "Unexpected exception.", io);
            JOptionPane.showMessageDialog(
                    this,
                    rsc.getString("Input/output error during project file loading."),
                    rsc.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (SAXException saxe) {
            logger.log(Level.SEVERE, "Unexpected exception.", saxe);
            JOptionPane.showMessageDialog(
                    this,
                    rsc.getString("Error during project file parsing."),
                    rsc.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
        }
        setTitle("NET-Simulator - " + currentFile);
    }

    private void saveFile() {
        if (currentFile == null) {
            return;
        }

        progressDialog.setTitle(rsc.getString("Saving file: ") + currentFile);
        ProjectXMLSerializer serializer
                = new ProjectXMLSerializer(
                        networkPanel,
                        progressDialog.getProgressBar(),
                        this);
        serializer.addProjectSerializingCompleteListener(this);
        serializer.setOutputFile(currentFile);
        Thread serializingThreadLocal = new Thread(serializer);
        serializingThreadLocal.start();
        progressDialog.moveToCenterOfParentComponent();
        progressDialog.setVisible(true);
        setTitle("NET-Simulator - " + currentFile);
        networkPanel.setSaved(true);
    }

    private void setCurrentProjectDependentMenuItemsEnable(boolean state) {
        save_menu_item.setEnabled(state);
        save_as_menu_item.setEnabled(state);
        close_menu_item.setEnabled(state);
        properties_menu_item.setEnabled(state);
        devices_menu.setEnabled(state);
        service_menu.setEnabled(state);
        export_to_html_menu_item.setEnabled(state);
    }

    private void processCloseMenuItem() {
        if (networkPanel == null) {
            return;
        }

        if (networkPanel.isSaved()) {
            closeProject();
        } else {
            Object[] options = {rsc.getString("Discard"),
                rsc.getString("Save"),
                rsc.getString("Cancel")};
            int n = JOptionPane.showOptionDialog(
                    this,
                    rsc.getString("The project is modified."),
                    rsc.getString("Save it?"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]
            );

            switch (n) {
                case JOptionPane.YES_OPTION:
                    closeProject();
                    break;
                case JOptionPane.NO_OPTION:
                    processSaveMenuItem();
                    closeProject();
                    break;
            }
        }

    }

    private boolean processSaveMenuItem() {
        boolean res;

        if (currentFile != null) {
            saveFile();
            res = true;
        } else {
            res = processSaveAsMenuItem();
        }

        return res;
    }

    private boolean processSaveAsMenuItem() {
        boolean res;
        int returnVal = fileChooserXML.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooserXML.getSelectedFile();
            saveFile();
            res = true;
        } else {
            res = false;
        }

        return res;
    }

    private void closeProject() {
        removeNetworkPanel(networkPanel);
        networkPanel = null;
        currentFile = null;
        setCurrentProjectDependentMenuItemsEnable(false);
        setTitle(NET_SIMULATOR_TITLE);
        getContentPane().setBackground(Color.LIGHT_GRAY);
        validate();
        repaint();
    }

    private void setNetworkPanel(NetworkPanel networkPanel) {
        projectScrollPane.setViewportView(networkPanel);
    }

    private void removeNetworkPanel(NetworkPanel networkPanel) {
        projectScrollPane.setViewportView(null);
    }

    private void processOpenMenuItem() {
        if (networkPanel != null) {
            processCloseMenuItem();
        }

        int returnVal = fileChooserXML.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            processNewMenuItem();
            currentFile = fileChooserXML.getSelectedFile();
            openFile();
            setCurrentProjectDependentMenuItemsEnable(true);
        }
    }

    private void processNewMenuItem() {
        if (networkPanel != null) {
            processCloseMenuItem();
        } else {
            networkPanel = new NetworkPanel(this);
            setNetworkPanel(networkPanel);
            networkPanel.setBackground(config.getBackground());
            setCurrentProjectDependentMenuItemsEnable(true);
            validate();
        }
    }

    private void processSetupMenuItem() {
        setupDialog.setConfig(config);
        setupDialog.moveToCenterOfParentComponent();
        setupDialog.setVisible(true);
        Config updatedConfig
                = setupDialog.getUpdatedConfig();
        if (!updatedConfig.isEmpty()) {
            enforceConfigUpdate(updatedConfig);

            config.merge(updatedConfig);

            try {
                ConfigSerializer serializer = new ConfigSerializer();
                serializer.serialize(config, confFile);
            } catch (IOException io) {
                logger.log(Level.SEVERE, "Unexpected exception.", io);
                JOptionPane.showMessageDialog(
                        this,
                        rsc.getString("Input/output error during configuration file saving."),
                        rsc.getString("Error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void enforceConfigUpdate(Config updatedConfig) {
        Color background = updatedConfig.getBackground();
        if (background != null) {
            networkPanel.setBackground(background);
        }

        LoggersConfig loggersConfig = updatedConfig.getDebug();
        if (loggersConfig != null) {
            for (Enumeration<String> e = LogManager.getLogManager().getLoggerNames(); e.hasMoreElements();) {
                String loggerName = e.nextElement();
                Level level = loggersConfig.getLevelByLoggerName(loggerName);
                if (level != null) {
                    Logger loggerLocal = Logger.getLogger(loggerName);
                    if (loggerLocal != null) {
                        loggerLocal.setLevel(level);
                        Handler handlers[] = loggerLocal.getHandlers();
                        for (int i = 0; i != handlers.length; i++) {
                            handlers[i].setLevel(level);
                        }
                    }
                }
            }
        }
    }

    private void processExitMenuItem() {
        if (currentFile != null) {
            config.setLastProject(currentFile.toString());
        }

        processCloseMenuItem();

        if (networkPanel == null) // project was closed
        {
            try {
                ConfigSerializer serializer = new ConfigSerializer();
                serializer.serialize(config, confFile);
            } catch (IOException io) {
                logger.log(Level.SEVERE, "Unexpected exception.", io);
                JOptionPane.showMessageDialog(
                        this,
                        rsc.getString("Input/output error during configuration file saving."),
                        rsc.getString("Error"),
                        JOptionPane.ERROR_MESSAGE);
            }

            System.exit(0);
        }
    }

    private void processExportToHtmlMenuItem() {
        if (currentFile != null) {
            config.setLastProject(currentFile.toString());
        }

        if (!networkPanel.isSaved()) {
            Object[] options = {rsc.getString("Save"),
                rsc.getString("Cancel")};
            int n = JOptionPane.showOptionDialog(
                    this,
                    rsc.getString("We must save the project befor."),
                    rsc.getString("Save it?"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            switch (n) {
                case JOptionPane.YES_OPTION:
                    if (processSaveMenuItem()) {
                        break;
                    }
                case JOptionPane.NO_OPTION:
                    return;
                default:
                    logger.log(Level.SEVERE, "Error: not valid switch value.");
                    return;
            }
        }

        int returnVal = fileChooserHtml.showOpenDialog(this);
        File htmlReportFile;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            htmlReportFile = fileChooserHtml.getSelectedFile();

            File imageFile = null;
            Rectangle contentBounds = networkPanel.getContentBounds();
            try {
                BufferedImage image
                        = new BufferedImage(
                                contentBounds.width,
                                contentBounds.height,
                                BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                Color reportBackground = Color.WHITE;
                Color networkPanelBackground = networkPanel.getBackground(); //save original background
                networkPanel.setBackground(reportBackground);
                g2.setColor(reportBackground);
                g2.fillRect(0, 0, contentBounds.width, contentBounds.height);
                g2.translate(-contentBounds.x, -contentBounds.y);
                networkPanel.paint(g2);
                g2.dispose();
                networkPanel.setBackground(networkPanelBackground); // restore original background

                imageFile = changeExtension(htmlReportFile, "png");
                ImageIO.write(image, "png", imageFile);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unexpected exception.", e);
            }

            HtmlExporter htmlExporter;
            try {
                htmlExporter = new HtmlExporter(
                        currentFile,
                        htmlReportFile,
                        new HtmlExporterErrorListener(this),
                        imageFile.getName());
            } catch (TransformerConfigurationException tce) {
                logger.log(Level.SEVERE, "Unexpected exception.", tce);
                JOptionPane.showMessageDialog(
                        this,
                        rsc.getString("Error during exporter configuration."),
                        rsc.getString("Error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            htmlExporter.addExportToHtmlStartCompleteListener(this);
            htmlExporter.setThreadToJoin(serializingThread);
            new Thread(htmlExporter).start();
            progressDialog.moveToCenterOfParentComponent();
            progressDialog.setVisible(true);
        }

    }

    public void ExportToHtmlComplete() {
        progressDialog.setVisible(false);
    }

    private File changeExtension(File source, String newExtention) {
        String s = source.getName();
        File parent = source.getParentFile();
        String clearName = s;

        int i = s.lastIndexOf('.');

        if (i > 0 && i < (s.length() - 1)) {
            clearName = s.substring(0, i);
        }

        return new File(parent + File.separator + clearName + '.' + newExtention);
    }

    public void ProjectSerializingComplete() {
        progressDialog.setVisible(false);
    }

    public void ExportToHtmlStart() {
        progressDialog.setTitle(rsc.getString("Exporting file: ") + currentFile);
        progressDialog.getProgressBar().setIndeterminate(true);
        progressDialog.getProgressBar().setStringPainted(false);
    }

    public void ProjectLoadingComplete() {
        progressDialog.setVisible(false);
    }

    private void initLoggers(LoggersConfig loggersConfig) {
        for (String loggerName : loggersConfig.getLoggerNames()) {
            Logger foundLogger = Logger.getLogger(loggerName);

            Level level = loggersConfig.getLevelByLoggerName(foundLogger.getName());
            if (level == null) {
                logger.log(Level.WARNING, "Can not find log level for logger ''{0}'', will use ALL by default.", foundLogger.getName());
                level = Level.ALL;
            }
            foundLogger.setLevel(level);
        }
    }

    class LoadingConfigErrorHandler implements ErrorHandler {

        Frame frame;

        public LoadingConfigErrorHandler(Frame frame) {
            this.frame = frame;
        }

        public void warning(SAXParseException e) throws SAXException {
            loadingErrorProcess(e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            loadingErrorProcess(e);
        }

        public void error(SAXParseException e) throws SAXException {
            loadingErrorProcess(e);
        }

        private void loadingErrorProcess(SAXParseException e) {
            logger.log(Level.SEVERE, "Unexpected exception.", e);
            JOptionPane.showMessageDialog(
                    frame,
                    rsc.getString("Error during configuration file parsing row ") + e.getLineNumber() + rsc.getString(", column") + e.getColumnNumber(),
                    rsc.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    class HtmlExporterErrorListener
            implements javax.xml.transform.ErrorListener {

        Frame frame;

        HtmlExporterErrorListener(Frame frame) {
            this.frame = frame;
        }

        public void warning(TransformerException exception) throws TransformerException {
            logger.log(Level.SEVERE, "Unexpected exception.", exception);
            JOptionPane.showMessageDialog(
                    frame,
                    "Warning during exporting.",
                    rsc.getString("Warning"),
                    JOptionPane.WARNING_MESSAGE);
        }

        public void fatalError(TransformerException exception) throws TransformerException {
            logger.log(Level.SEVERE, "Unexpected exception.", exception);
            JOptionPane.showMessageDialog(
                    frame,
                    rsc.getString("Fatal error during exporting."),
                    rsc.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
        }

        public void error(TransformerException exception) throws TransformerException {
            logger.log(Level.SEVERE, "Unexpected exception.", exception);
            JOptionPane.showMessageDialog(
                    frame,
                    rsc.getString("Error during exporting."),
                    rsc.getString("Error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
