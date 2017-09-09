package org.geoserver.status.monitoring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.util.tester.TagTester;
import org.geoserver.status.monitoring.web.SystemStatusMonitorPanel;
import org.geoserver.web.GeoServerWicketTestSupport;
import org.geoserver.web.admin.StatusPage;
import org.junit.Before;
import org.junit.Test;

public class SystemStatusMonitorPanelTest extends GeoServerWicketTestSupport {

    @Before
    public void setupTests() {
        login();
        tester.getApplication().getMarkupSettings().setStripWicketTags(false);
        tester.startPage(StatusPage.class);
    }

    @Test
    public void testLoad() throws Exception {
        tester.assertRenderedPage(StatusPage.class);
        tester.clickLink("tabs:tabs-container:tabs:2:link", true);
        tester.assertContains("Updated at");
    }

    @Test
    public void testUpdate() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(SystemStatusMonitorPanel.datePattern);
        tester.assertRenderedPage(StatusPage.class);
        tester.clickLink("tabs:tabs-container:tabs:2:link", true);
        TagTester time1 = tester.getTagByWicketId("time");
        assertNotNull(time1);
        Date firstTime = formatter.parse(time1.getValue());
        System.out.println(firstTime.getTime());
        // Execute timer
        tester.executeAllTimerBehaviors(tester.getLastRenderedPage());
        TagTester time2 = tester.getTagByWicketId("time");
        assertNotNull(time2);
        Date secondTime = formatter.parse(time2.getValue());
        // Check if update time is changed (use 500ms due to time imprecision)
        assertTrue(secondTime.getTime() >= firstTime.getTime() + (500));
        tester.executeAllTimerBehaviors(tester.getLastRenderedPage());
        TagTester time3 = tester.getTagByWicketId("time");
        assertNotNull(time3);
        Date thirdTime = formatter.parse(time3.getValue());
        // Check if update time is changed (use 500ms due to time imprecision)
        assertTrue(thirdTime.getTime() >= secondTime.getTime() + (500));
    }
}
