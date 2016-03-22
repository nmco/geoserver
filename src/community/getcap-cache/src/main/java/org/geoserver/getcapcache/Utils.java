/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.apache.commons.io.FileUtils;
import org.geotools.util.logging.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * General propose utility methods.
 * </p>
 */
final class Utils {

    private final static Logger LOGGER = Logging.getLogger(Utils.class);

    private Utils() {
    }

    static void info(Logger LOGGER, String message, Object... arguments) {
        LOGGER.info(String.format(message, arguments));
    }

    static void debug(Logger LOGGER, String message, Object... arguments) {
        LOGGER.fine(String.format(message, arguments));
    }

    static void error(Logger LOGGER, Exception exception, String message, Object... arguments) {
        LOGGER.log(Level.SEVERE, String.format(message, arguments), exception);
    }

    static Object checkNotNull(Object value, String message, Object... arguments) {
        if (value == null) {
            throw new CacheException(String.format(message, arguments));
        }
        return value;
    }

    static <T> T withDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    static CacheException exception(Exception exception, String message, Object... arguments) {
        return new CacheException(String.format(message, arguments), exception);
    }
    static CacheException exception(String message, Object... arguments) {
        return new CacheException(String.format(message, arguments));
    }

    static class CacheException extends RuntimeException {

        private CacheException(String message) {
            super(message);
        }

        private CacheException(String message, Exception exception) {
            super(message, exception);
        }
    }

    /**
     * Helper class that helps doing something with a file without worrying about opening or closing it.
     */
    static abstract class DoWorkWithFile {

        public DoWorkWithFile(File file) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                doWork(inputStream);
            } catch (Exception exception) {
                throw Utils.exception(exception, "Error processing file '%s': %s", file.getPath(), exception.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception exception) {
                        Utils.error(LOGGER, exception, "Exception closing resource '%s'.", file.getPath());
                    }
                }
            }
        }

        public abstract void doWork(InputStream inputStream);
    }

    /**
     * Helper method that lookup for a file and create it if it don't exists.
     * If the file is create the new content will be writed.
     */
    static File getOrCreate(File directory, String fileName, String newContent) {
        File file = new File(directory, fileName);
        try {
            boolean created = file.createNewFile();
            if (created && newContent != null) {
                FileUtils.write(file, newContent);
            }
        } catch (Exception exception) {
            throw Utils.exception(exception, "Error creating file '%s': %s", file.getPath(), exception.getMessage());
        }
        return file;
    }
}
