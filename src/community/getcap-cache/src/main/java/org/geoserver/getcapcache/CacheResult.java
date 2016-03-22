/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.geoserver.ows.Request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Container that will be used to cache a request results.
 * Currently we store the mime type, the output stream and the update sequence.
 * </p>
 */
final class CacheResult {

    private final static Pattern UPDATE_SEQUENCE_PATTERN = Pattern.compile(".*?updateSequence=\"(\\d+)\".*", Pattern.CASE_INSENSITIVE);

    // response mime type
    private final String mimeType;
    // the content that should be copied to the response output
    private final ByteArrayOutputStream output;
    // the update sequence of the current response content
    private final int updateSequence;
    // the associated request info container
    private final RequestInfo requestInfo;

    CacheResult(RequestInfo requestInfo, String mimeType, ByteArrayOutputStream output) {
        Utils.checkNotNull(requestInfo, "Request info cannot be NULL.");
        Utils.checkNotNull(mimeType, "Mime type cannot be NULL.");
        Utils.checkNotNull(output, "Output byte array stream cannot be NULL.");
        this.requestInfo = requestInfo;
        this.mimeType = mimeType;
        this.output = output;
        this.updateSequence = getUpdateSequenceFromResponse(output);
    }

    RequestInfo getRequestInfo() {
        return requestInfo;
    }

    String getMimeType() {
        return mimeType;
    }

    ByteArrayOutputStream getOutput() {
        return output;
    }

    int getUpdateSequence() {
        return updateSequence;
    }

    /**
     * Helper method that extract the update sequence from a response content.
     */
    private int getUpdateSequenceFromResponse(ByteArrayOutputStream byteArrayOutputStream) {
        Matcher matcher = UPDATE_SEQUENCE_PATTERN.matcher(byteArrayOutputStream.toString());
        if (matcher.find()) {
            // we return the content update sequence
            return Integer.parseInt(matcher.group(1));
        } else {
            // if the client request contains an update sequence it will always be lower than this
            return Integer.MAX_VALUE;
        }
    }
}
