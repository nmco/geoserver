/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.params.extractor;

import java.util.logging.Logger;

final class Utils {

    private Utils() {
    }

    static void info(Logger logger, String message, Object... messageArguments) {
        logger.info(() -> String.format(message, messageArguments));
    }

    static void debug(Logger logger, String message, Object... messageArguments) {
        logger.fine(() -> String.format(message, messageArguments));
    }

    static void checkCondition(boolean condition, String failMessage, Object... failMessageArguments) {
        if (!condition) {
            throw exception(failMessage, failMessageArguments);
        }
    }

    static ParamsExtractorException exception(String message, Object... messageArguments) {
        return new ParamsExtractorException(null, message, messageArguments);
    }

    static ParamsExtractorException exception(Throwable cause, String message, Object... messageArguments) {
        return new ParamsExtractorException(cause, message, messageArguments);
    }

    private final static class ParamsExtractorException extends RuntimeException {

        public ParamsExtractorException(Throwable cause, String message, Object... messageArguments) {
            super(String.format(message, messageArguments), cause);
        }
    }
}
