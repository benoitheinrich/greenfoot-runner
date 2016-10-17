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
        } catch (InvocationTargetException | InterruptedException var4) {
            var4.printStackTrace();
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
        final String projectName;
        final Class<? extends World> worldClass;
        final boolean lockScenario;
        final boolean hideControls;

        private Configuration() {
            projectName = "No title";
            worldClass = World.class;
            lockScenario = false;
            hideControls = false;
        }

        private Configuration(final String projectName, final Class<? extends World> worldClass, final boolean lockScenario, final boolean hideControls) {
            this.projectName = projectName;
            this.worldClass = worldClass;
            this.lockScenario = lockScenario;
            this.hideControls = hideControls;
        }

        public static Configuration prepare() {
            return new Configuration();
        }

        public Configuration projectName(final String projectName) {
            return new Configuration(projectName, worldClass, lockScenario, hideControls);
        }

        public Configuration worldClass(final Class<? extends World> worldClass) {
            return new Configuration(projectName, worldClass, lockScenario, hideControls);
        }

        public Configuration lockScenario(final boolean lockScenario) {
            return new Configuration(projectName, worldClass, lockScenario, hideControls);
        }

        public Configuration hideControls(final boolean hideControls) {
            return new Configuration(projectName, worldClass, lockScenario, hideControls);
        }
    }
}
