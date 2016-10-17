package bh.greenfoot.runner;

import bluej.Config;
import greenfoot.Greenfoot;
import greenfoot.World;
import greenfoot.export.GreenfootScenarioViewer;
import greenfoot.util.StandalonePropStringManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;

/**
 * A Runner class for a greenfoot project.
 */
public abstract class GreenfootRunner {
    private String scenarioName;
    protected static Class<? extends GreenfootRunner> mainClass;

    public static void main(String[] args) {
        try {
            mainClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected GreenfootRunner() {
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        initProperties();
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", scenarioName);
        final GreenfootScenarioViewer[] gsv = new GreenfootScenarioViewer[1];
        final JFrame[] frame = new JFrame[1];

        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    frame[0] = new JFrame(scenarioName);
                    gsv[0] = new GreenfootScenarioViewer(frame[0]);
                    frame[0].setDefaultCloseOperation(3);
                    frame[0].setResizable(false);
                    URL resource = this.getClass().getClassLoader().getResource("greenfoot.png");
                    if (resource != null) {
                        ImageIcon icon = new ImageIcon(resource);
                        frame[0].setIconImage(icon.getImage());
                    }
                }
            });
            gsv[0].init();
            EventQueue.invokeAndWait(() -> {
                frame[0].pack();
                frame[0].setVisible(true);
                if (Config.getPropBoolean("scenario.hideControls", false)) {
                    Greenfoot.start();
                }

            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initProperties() {
        if (scenarioName == null) {
            final Properties p = new Properties();

            try {
                final ClassLoader e = GreenfootRunner.class.getClassLoader();
                final Object is = e.getResourceAsStream("standalone.properties");
                if (is != null) {
                    p.load((InputStream) is);
                }

                final Configuration configuration = getConfiguration();
                p.put("project.name", configuration.projectName);
                p.put("main.class", configuration.worldClass.getName());
                p.put("scenario.lock", "" + configuration.lockScenario);
                p.put("scenario.hideControls", "" + configuration.hideControls);

                scenarioName = p.getProperty("project.name");
                Config.initializeStandalone(new StandalonePropStringManager(p));
                if (is != null) {
                    ((InputStream) is).close();
                }
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }
    }

    protected abstract Configuration getConfiguration();

    protected static final class Configuration {
        private final Class<? extends World> worldClass;
        private final String projectName;
        private final boolean lockScenario;
        private final boolean hideControls;

        private Configuration(final Class<? extends World> worldClass, final String projectName, final boolean lockScenario, final boolean hideControls) {
            this.worldClass = worldClass;
            this.projectName = projectName;
            this.lockScenario = lockScenario;
            this.hideControls = hideControls;
        }

        /**
         * Creates the configuration for this specific {@code worldClass}.
         *
         * @param worldClass the class used to create the world in your application.
         * @return the new configuration.
         */
        public static Configuration forWorld(final Class<? extends World> worldClass) {
            return new Configuration(worldClass, "No title", false, false);
        }

        /**
         * Configures a project name to be shown in the title of the window.
         * If not specified it'll be set to "No title".
         *
         * @param projectName the name of the project that will show in the title of the window.
         * @return the new configuration.
         */
        public Configuration projectName(final String projectName) {
            return new Configuration(worldClass, projectName, lockScenario, hideControls);
        }

        /**
         * Indicates if the scenario should be locked or not.
         * If not specified it'll allow the scenario to be run step-by-step, and to select the scenario speed.
         *
         * @param lockScenario if the scenario should be locked or not.
         * @return the new configuration.
         */
        public Configuration lockScenario(final boolean lockScenario) {
            return new Configuration(worldClass, projectName, lockScenario, hideControls);
        }

        /**
         * Indicates if the controls should be hidden.
         * If not specified it'll show the controls to allow the scenario run and reset.
         *
         * @param hideControls if the controls should be hidden.
         * @return the new configuration.
         */
        public Configuration hideControls(final boolean hideControls) {
            return new Configuration(worldClass, projectName, lockScenario, hideControls);
        }
    }
}
