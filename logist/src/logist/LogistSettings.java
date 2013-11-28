package logist;

import java.awt.Color;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import static logist.LogistSettings.ColorKey.*;
import static logist.LogistSettings.SizeKey.*;
import static logist.LogistSettings.TimeoutKey.*;
import static logist.LogistSettings.FileKey.*;
import static logist.LogistSettings.FlagKey.*;

/**
 * The global settings of the platform.
 * 
 * @author Robin Steiger
 */
public class LogistSettings {

    /** A size stored as <tt>int</tt>. */
    public enum SizeKey {
        WORLD_WIDTH, WORLD_HEIGHT, CITY_RADIUS, ROUTE_WIDTH, NUMBER_OF_TASKS, NUMBER_OF_AGENTS
    }
    /** A color stored as <tt>{@link java.awt.Color}</tt>. */
    public enum ColorKey {
        BACKGROUND, FOREGROUND, CITY, CITY_NAME, CITY_CIRCUMFERENCE, TASK_TEXT, TASK_PICKUP, TASK_DELIVER, TASK_INDICATOR, ROUTE
    }
    /** A timeout stored as <tt>long</tt>. */
    public enum TimeoutKey {
        // INIT, RESET, KILL, SETUP, AUCTION
        SETUP, BID, PLAN
    }
    /** A flag stored as <tt>boolean</tt>. */
    public enum FlagKey {
        SHOW_UI, SHOW_TASKS
    }
    /** A file path stored as <tt>java.io.File</tt>. */
    public enum FileKey {
        CONFIGURATION, HISTORY
    }

    private static <K extends Enum<K>, V> Map<K, V> newmap(Class<K> key) {
        return new EnumMap<K, V>(key);
    }

    private final Map<SizeKey, Integer> sizes = newmap(SizeKey.class);
    private final Map<ColorKey, Color> colors = newmap(ColorKey.class);
    private final Map<TimeoutKey, Long> timeouts = newmap(TimeoutKey.class);
    private final Map<FileKey, File> files = newmap(FileKey.class);
    private final Map<FlagKey, Boolean> flags = newmap(FlagKey.class);

    /* Getters */
    
    public int get(SizeKey key) {
        return sizes.get(key);
    }

    public Color get(ColorKey key) {
        return colors.get(key);
    }

    public long get(TimeoutKey key) {
        // 1% tolerance
        long timeout = timeouts.get(key);
        return timeout + (timeout / 100);
    }
    
    public File get(FileKey key) {
        return files.get(key);
    }
    
    public boolean get(FlagKey key) {
        return flags.get(key);
    }

    /* Setters */
    
    public void set(SizeKey key, int value) {
        sizes.put(key, value);
    }

    public void set(ColorKey key, Color value) {
        colors.put(key, value);
    }

    public void set(TimeoutKey key, long value) {
        timeouts.put(key, value);
    }

    public void set(FileKey key, File value) {
        files.put(key, value);
    }

    public void set(FlagKey key, boolean value) {
        flags.put(key, value);
    }

    /**
     * Creates a new setting with default values.
     */
    public LogistSettings() {
        // Sizes
        sizes.put(WORLD_WIDTH, 640);
        sizes.put(WORLD_HEIGHT, 480);
        sizes.put(CITY_RADIUS, 8);
        sizes.put(ROUTE_WIDTH, 3);
        sizes.put(NUMBER_OF_TASKS, 5);

        // Colors
        colors.put(BACKGROUND, Color.WHITE);
        colors.put(FOREGROUND, Color.BLACK);

        colors.put(CITY, Color.GREEN);
        colors.put(CITY_CIRCUMFERENCE, Color.BLACK);
        colors.put(CITY_NAME, Color.BLACK);

        colors.put(TASK_TEXT, Color.BLACK);
        colors.put(TASK_PICKUP, Color.RED);
        colors.put(TASK_DELIVER, Color.BLUE);
        colors.put(TASK_INDICATOR, Color.BLACK);

        colors.put(ROUTE, Color.LIGHT_GRAY);

        // Timeouts
        timeouts.put(SETUP, 30000L); // 30 seconds
        timeouts.put(BID, 30000L); // 30 seconds
        timeouts.put(PLAN, 30000L); // 30 seconds
        
        // File
        files.put(HISTORY, new File("history.xml"));
        
        // Flags
        flags.put(SHOW_UI, true);
        flags.put(SHOW_TASKS, true);
    }

}
