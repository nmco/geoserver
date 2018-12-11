/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.hive.jdbc.store;

import java.io.IOException;
import org.geotools.filter.FilterCapabilities;
import org.geotools.jdbc.PreparedFilterToSQL;
import org.geotools.jdbc.SQLDialect;
import org.opengis.filter.NativeFilter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.DWithin;

public final class HiveFilterToSql extends PreparedFilterToSQL {

    protected Object visitBinarySpatialOperator(
            BinarySpatialOperator filter,
            PropertyName property,
            Literal geometry,
            boolean swapped,
            Object extraData) {
        return visitBinarySpatialOperatorz(filter, property, geometry, swapped, extraData);
    }
    
    protected Object visitBinarySpatialOperator(
            BinarySpatialOperator filter, Expression e1, Expression e2, Object extraData) {
        return visitBinarySpatialOperatorz(filter, e1, e2, false, extraData);
    }

    protected Object visitBinarySpatialOperatorz(
            BinarySpatialOperator filter,
            Expression e1,
            Expression e2,
            boolean swapped,
            Object extraData) {
        if (!(filter instanceof BBOX)) {
            throw new RuntimeException("Filter no supported !");
        }
        try {
            // TODO handle swaped
            /**e1 = clipToWorld(filter, e1);
            e2 = clipToWorld(filter, e2);

            if (filter instanceof Beyond || filter instanceof DWithin)
                doSDODistance(filter, e1, e2, extraData);
            else if (filter instanceof BBOX && looseBBOXEnabled) {
                doSDOFilter(filter, e1, e2, extraData);
            } else doSDORelate(filter, e1, e2, swapped, extraData);**/
            BBOX bbox = (BBOX) filter;
            out.write(String.format(
                    "longitude > %f and longitude < %f and latitude > %f and longitude < %f", 
                    bbox.getBounds().getMinX(), bbox.getBounds().getMaxX(), bbox.getBounds().getMinY(), bbox.getBounds().getMaxY()));
            //out.write("SDO_FILTER(");
            //7e1.accept(this, extraData);
            //out.write(", ");
            //e2.accept(this, extraData);
            // for backwards compatibility with Oracle 9 we add the mask and querytypes params
            //out.write(", 'mask=anyinteract querytype=WINDOW') = 'TRUE' ");
            
            
        } catch (IOException ioe) {
            throw new RuntimeException(IO_ERROR, ioe);
        }
        return extraData;
    }

    @Override
    public FilterCapabilities createFilterCapabilities() {
        FilterCapabilities caps = new FilterCapabilities();
        caps.addAll(SQLDialect.BASE_DBMS_CAPABILITIES);

        // adding the spatial filters support
        caps.addType(BBOX.class);

        // native filter support
        caps.addType(NativeFilter.class);

        return caps;
    }
}
