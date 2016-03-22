/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.getcapcache;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.*;
import java.net.URL;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

final class TestUtils {

    private TestUtils() {
    }

    /**
     * Helper method that writes a string to a byte array output stream.
     */
    static ByteArrayOutputStream stringToByteArray(String string) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(string.getBytes());
        return output;
    }

    /**
     * Helper method that converts an input stream into a String.
     */
    static String inputStreamToString(InputStream inputStream) throws Exception {
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    /**
     * Helper method that finds a resource in the class path.
     */
    static File findResource(String resourcePath) {
        URL resource = TestUtils.class.getClassLoader().getResource(resourcePath);
        assertThat(resource, notNullValue());
        File file = new File(resource.getFile());
        assertThat(file.exists(), is(true));
        return file;
    }

    /**
     * Helper class that helps doing something with a resource without worrying about opening or closing it.
     */
    static abstract class DoWorkWithResource {

        public DoWorkWithResource(String resourcePath) {
            new Utils.DoWorkWithFile(findResource(resourcePath)) {
                @Override
                public void doWork(InputStream inputStream) {
                    DoWorkWithResource.this.doWork(inputStream);
                }
            };
        }

        public abstract void doWork(InputStream inputStream);
    }
}
