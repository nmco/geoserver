/* (c) 2014 - 2016 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2014 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.inspire.wmts;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.geoserver.web.services.BaseServiceAdminPage;

public class WMTSAdminPage extends BaseServiceAdminPage<WMTSInfo> {

    @Override
    protected Class<WMTSInfo> getServiceClass() {
        return WMTSInfo.class;
    }

    @Override
    protected String getServiceName() {
        return "WMTS";
    }

    @Override
    protected void build(IModel info, Form form) {
    }
}
