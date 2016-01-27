/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import org.geotools.util.logging.Logging;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Utils {

    private static final Logger LOGGER = Logging.getLogger(Utils.class);

    private Utils() {
    }

    public static void info(Logger logger, String message, Object... messageArguments) {
        logger.info(() -> String.format(message, messageArguments));
    }

    public static void debug(Logger logger, String message, Object... messageArguments) {
        logger.fine(() -> String.format(message, messageArguments));
    }

    public static void error(Logger logger, Throwable cause, String message, Object... messageArguments) {
        logger.log(Level.SEVERE, cause, () -> String.format(message, messageArguments));
    }

    public static void checkCondition(boolean condition, String failMessage, Object... failMessageArguments) {
        if (!condition) {
            throw exception(failMessage, failMessageArguments);
        }
    }

    public static <T> T withDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static ParamsExtractorException exception(String message, Object... messageArguments) {
        return new ParamsExtractorException(null, message, messageArguments);
    }

    public static ParamsExtractorException exception(Throwable cause, String message, Object... messageArguments) {
        return new ParamsExtractorException(cause, message, messageArguments);
    }

    private final static class ParamsExtractorException extends RuntimeException {

        public ParamsExtractorException(Throwable cause, String message, Object... messageArguments) {
            super(String.format(message, messageArguments), cause);
        }
    }

    public static <T extends Closeable> void closeQuietly(T closable) {
        try {
            closable.close();
        } catch (Exception exception) {
            Utils.error(LOGGER, exception, "Something bad happen when closing.");
        }
    }
}
