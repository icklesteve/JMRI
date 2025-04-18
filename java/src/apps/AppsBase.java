package apps;

import apps.gui3.tabbedpreferences.TabbedPreferences;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import jmri.*;
import jmri.jmrit.logixng.LogixNGPreferences;
import jmri.jmrit.revhistory.FileHistory;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.script.JmriScriptEngineManager;
import jmri.util.FileUtil;
import jmri.util.ThreadingUtil;

import jmri.util.prefs.JmriPreferencesActionFactory;

import apps.util.Log4JUtil;

/**
 * Base class for the core of JMRI applications.
 * <p>
 * This provides a non-GUI base for applications. Below this is the
 * {@link apps.gui3.Apps3} subclass which provides basic Swing GUI support.
 * <p>
 * There are a series of steps in the configuration:
 * <dl>
 * <dt>preInit<dd>Initialize log4j, invoked from the main()
 * <dt>ctor<dd>Construct the basic application object
 * </dl>
 *
 * @author Bob Jacobsen Copyright 2009, 2010
 */
public abstract class AppsBase {

    private final static String CONFIG_FILENAME = System.getProperty("org.jmri.Apps.configFilename", "/JmriConfig3.xml");
    protected boolean configOK;
    protected boolean configDeferredLoadOK;
    protected boolean preferenceFileExists;
    static boolean preInit = false;

    /**
     * Initial actions before frame is created, invoked in the applications
     * main() routine.
     * <ul>
     * <li> Initialize logging
     * <li> Set application name
     * </ul>
     *
     * @param applicationName The application name as presented to the user
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="Info String always needs to be evaluated")
    static public void preInit(String applicationName) {
        Log4JUtil.initLogging();

        try {
            Application.setApplicationName(applicationName);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            log.error("Unable to set application name", ex);
        }

        log.info(Log4JUtil.startupInfo(applicationName));

        preInit = true;
    }

    /**
     * Create and initialize the application object.
     *
     * @param applicationName user-visible name of application
     * @param configFileDef   default config filename
     * @param args            arguments passed to application at launch
     */
    @SuppressFBWarnings(value = "SC_START_IN_CTOR",
            justification = "The thread is only called to help improve user experiance when opening the preferences, it is not critical for it to be run at this stage")
    public AppsBase(String applicationName, String configFileDef, String[] args) {

        if (!preInit) {
            preInit(applicationName);
            setConfigFilename(configFileDef, args);
        }

        Log4JUtil.initLogging();

        configureProfile();

        installConfigurationManager();

        installManagers();

        setAndLoadPreferenceFile();

        FileUtil.logFilePaths();

        if (Boolean.getBoolean("org.jmri.python.preload")) {
            new Thread(() -> {
                try {
                    JmriScriptEngineManager.getDefault().initializeAllEngines();
                } catch (Exception ex) {
                    log.error("Error initializing python interpreter", ex);
                }
            }, "initialize python interpreter").start();
        }

        // all loaded, initialize objects as necessary
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        jmri.jmrit.logixng.LogixNG_Manager logixNG_Manager =
                InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class);
        logixNG_Manager.setupAllLogixNGs();
        if (InstanceManager.getDefault(LogixNGPreferences.class).getStartLogixNGOnStartup()
                && InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).isStartLogixNGsOnLoad()) {
            logixNG_Manager.activateAllLogixNGs();
        }
    }

    /**
     * Configure the {@link jmri.profile.Profile} to use for this application.
     * <p>
     * Note that GUI-based applications must override this method, since this
     * method does not provide user feedback.
     */
    protected void configureProfile() {
        String profileFilename;
        FileUtil.createDirectory(FileUtil.getPreferencesPath());
        // Load permission manager
        InstanceManager.getDefault(PermissionManager.class);
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        File profileFile;
        profileFilename = getConfigFileName().replaceFirst(".xml", ".properties");
        // decide whether name is absolute or relative
        if (!new File(profileFilename).isAbsolute()) {
            // must be relative, but we want it to
            // be relative to the preferences directory
            profileFile = new File(FileUtil.getPreferencesPath() + profileFilename);
        } else {
            profileFile = new File(profileFilename);
        }
        ProfileManager.getDefault().setConfigFile(profileFile);
        // See if the profile to use has been specified on the command line as
        // a system property org.jmri.profile as a profile id.
        if (System.getProperties().containsKey(ProfileManager.SYSTEM_PROPERTY)) {
            ProfileManager.getDefault().setActiveProfile(System.getProperty(ProfileManager.SYSTEM_PROPERTY));
        }
        // @see jmri.profile.ProfileManager#migrateToProfiles Javadoc for conditions handled here
        if (!profileFile.exists()) { // no profile config for this app
            try {
                if (ProfileManager.getDefault().migrateToProfiles(getConfigFileName())) { // migration or first use
                    // GUI should show message here
                    log.info("Migrated {}",Bundle.getMessage("ConfigMigratedToProfile"));
                }
            } catch (IOException | IllegalArgumentException ex) {
                // GUI should show message here
                log.error("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
            }
        }
        try {
            // GUI should use ProfileManagerDialog.getStartingProfile here
            if (ProfileManager.getStartingProfile() != null) {
                // Manually setting the configFilename property since calling
                // Apps.setConfigFilename() does not reset the system property
                System.setProperty("org.jmri.Apps.configFilename", Profile.CONFIG_FILENAME);
                Profile profile = ProfileManager.getDefault().getActiveProfile();
                if (profile != null) {
                    log.info("Starting with profile {}", profile.getId());
                } else {
                    log.info("Starting without a profile");
                }
            } else {
                log.error("Specify profile to use as command line argument.");
                log.error("If starting with saved profile configuration, ensure the autoStart property is set to \"true\"");
                log.error("Profiles not configurable. Using fallback per-application configuration.");
            }
        } catch (IOException ex) {
            log.info("Profiles not configurable. Using fallback per-application configuration. Error: {}", ex.getMessage());
        }
    }

    protected void installConfigurationManager() {
        // install a Preferences Action Factory
        InstanceManager.store(new AppsPreferencesActionFactory(), JmriPreferencesActionFactory.class);
        ConfigureManager cm = new AppsConfigurationManager();
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        InstanceManager.store(cm, ConfigureManager.class);
        InstanceManager.setDefault(ConfigureManager.class, cm);
        log.debug("config manager installed");
    }

    protected void installManagers() {
        // record startup
        String appString = String.format("%s (v%s)", Application.getApplicationName(), Version.getCanonicalVersion());
        InstanceManager.getDefault(FileHistory.class).addOperation("app", appString, null);

        // install the abstract action model that allows items to be added to the, both
        // CreateButton and Perform Action Model use a common Abstract class
        InstanceManager.store(new CreateButtonModel(), CreateButtonModel.class);
    }

    /**
     * Invoked to load the preferences information, and in the process configure
     * the system. The high-level steps are:
     * <ul>
     * <li>Locate the preferences file based through
     * {@link FileUtil#getFile(String)}
     * <li>See if the preferences file exists, and handle it if it doesn't
     * <li>Obtain a {@link jmri.ConfigureManager} from the
     * {@link jmri.InstanceManager}
     * <li>Ask that ConfigureManager to load the file, in the process loading
     * information into existing and new managers.
     * <li>Do any deferred loads that are needed
     * <li>If needed, migrate older formats
     * </ul>
     * (There's additional handling for shared configurations)
     */
    protected void setAndLoadPreferenceFile() {
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        final File file;
        File sharedConfig = null;
        try {
            sharedConfig = FileUtil.getFile(FileUtil.PROFILE + Profile.SHARED_CONFIG);
            if (!sharedConfig.canRead()) {
                sharedConfig = null;
            }
        } catch (FileNotFoundException ex) {
            // ignore - this only means that sharedConfig does not exist.
        }
        if (sharedConfig != null) {
            file = sharedConfig;
            log.trace("Try preferences from sharedConfig {}", file.getPath());
        } else if (!new File(getConfigFileName()).isAbsolute()) {
            // must be relative, but we want it to
            // be relative to the preferences directory
            file = new File(FileUtil.getUserFilesPath() + getConfigFileName());
            log.trace("Try references from getUserFilesPath {}", file.getPath());
        } else {
            file = new File(getConfigFileName());
            log.trace("Try references from getConfigFileName {}", file.getPath());
        }
        // don't try to load if doesn't exist, but mark as not OK
        if (!file.exists()) {
            preferenceFileExists = false;
            configOK = false;
            log.info("No pre-existing config file found, searched for '{}'", file.getPath());
            return;
        }
        log.debug("Found preferences file '{}'", file.getPath());
        preferenceFileExists = true;

        // ensure the UserPreferencesManager has loaded. Done on GUI
        // thread as it can modify GUI objects
        ThreadingUtil.runOnGUI(() -> {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        });

        // now (attempt to) load the config file
        try {
            ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cm != null) {
                configOK = cm.load(file);
            } else {
                configOK = false;
            }
            log.debug("end load config file {}, OK={}", file.getName(), configOK);
        } catch (JmriException e) {
            configOK = false;
        }

        if (sharedConfig != null) {
            // sharedConfigs do not need deferred loads
            configDeferredLoadOK = true;
        } else if (SwingUtilities.isEventDispatchThread()) {
            // To avoid possible locks, deferred load should be
            // performed on the Swing thread
            configDeferredLoadOK = doDeferredLoad(file);
        } else {
            try {
                // Use invokeAndWait method as we don't want to
                // return until deferred load is completed
                SwingUtilities.invokeAndWait(() -> {
                    configDeferredLoadOK = doDeferredLoad(file);
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                log.error("Exception creating system console frame:", ex);
            }
        }
        if (sharedConfig == null && configOK == true && configDeferredLoadOK == true) {
            log.info("Migrating preferences to new format...");
            // migrate preferences
            InstanceManager.getOptionalDefault(TabbedPreferences.class).ifPresent(tp -> {
                //tp.init();
                tp.saveContents();
                InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> {
                    cm.storePrefs();
                });
                // notify user of change
                log.info("Preferences have been migrated to new format.");
                log.info("New preferences format will be used after JMRI is restarted.");
            });
        }
    }

    private boolean doDeferredLoad(File file) {
        boolean result;
        log.debug("start deferred load from config file {}", file.getName());
        try {
            ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cm != null) {
                result = cm.loadDeferred(file);
            } else {
                log.error("Failed to get default configure manager");
                result = false;
            }
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration:", e);
            result = false;
        }
        log.debug("end deferred load from config file {}, OK={}", file.getName(), result);
        return result;
    }

    /**
     * Final actions before releasing control of the application to the user,
     * invoked explicitly after object has been constructed in main().
     */
    protected void start() {
        log.debug("main initialization done");
    }

    /**
     * Set up the configuration file name at startup.
     * <p>
     * The Configuration File name variable holds the name used to load the
     * configuration file during later startup processing. Applications invoke
     * this method to handle the usual startup hierarchy:
     * <ul>
     * <li>If an absolute filename was provided on the command line, use it
     * <li>If a filename was provided that's not absolute, consider it to be in
     * the preferences directory
     * <li>If no filename provided, use a default name (that's application specific)
     * </ul>
     * This name will be used for reading and writing the preferences. It need
     * not exist when the program first starts up. This name may be proceeded
     * with <em>config=</em>.
     *
     * @param def  Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String[] args) {
        // skip if org.jmri.Apps.configFilename is set
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            return;
        }
        // save the configuration filename if present on the command line
        if (args.length >= 1 && args[0] != null && !args[0].equals("") && !args[0].contains("=")) {
            def = args[0];
            log.debug("Config file was specified as: {}", args[0]);
        }
        for (String arg : args) {
            String[] split = arg.split("=", 2);
            if (split[0].equalsIgnoreCase("config")) {
                def = split[1];
                log.debug("Config file was specified as: {}", arg);
            }
        }
        if (def != null) {
            setJmriSystemProperty("configFilename", def);
            log.debug("Config file set to: {}", def);
        }
    }

    // We will use the value stored in the system property
    static public String getConfigFileName() {
        if (System.getProperty("org.jmri.Apps.configFilename") != null) {
            return System.getProperty("org.jmri.Apps.configFilename");
        }
        return CONFIG_FILENAME;
    }

    static protected void setJmriSystemProperty(String key, String value) {
        try {
            String current = System.getProperty("org.jmri.Apps." + key);
            if (current == null) {
                System.setProperty("org.jmri.Apps." + key, value);
            } else if (!current.equals(value)) {
                log.warn("JMRI property {} already set to {}, skipping reset to {}", key, current, value);
            }
        } catch (Exception e) {
            log.error("Unable to set JMRI property {} to {}due to exception", key, value, e);
        }
    }

    /**
     * The application decided to quit, handle that.
     *
     * @return always returns false
     */
    static public boolean handleQuit() {
        log.debug("Start handleQuit");
        try {
            InstanceManager.getDefault(jmri.ShutDownManager.class).shutdown();
        } catch (Exception e) {
            log.error("Continuing after error in handleQuit", e);
        }
        return false;
    }

    /**
     * The application decided to restart, handle that.
     */
    static public void handleRestart() {
        log.debug("Start handleRestart");
        try {
            InstanceManager.getDefault(jmri.ShutDownManager.class).restart();
        } catch (Exception e) {
            log.error("Continuing after error in handleRestart", e);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppsBase.class);
}
