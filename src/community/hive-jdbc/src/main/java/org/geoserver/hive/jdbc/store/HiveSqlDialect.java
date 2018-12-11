/* (c) 2019 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.hive.jdbc.store;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

public final class HiveSqlDialect extends BasicSQLDialect {

    protected HiveSqlDialect(JDBCDataStore dataStore) {
        super(dataStore);
    }

    @Override
    public void encodeGeometryValue(Geometry value, int dimension, int srid, StringBuffer sql)
            throws IOException {}

    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn, StringBuffer sql) {}

    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column, Connection cx)
            throws SQLException, IOException {
        return null;
    }

    @Override
    public Geometry decodeGeometryValue(
            GeometryDescriptor descriptor,
            ResultSet rs,
            String column,
            GeometryFactory factory,
            Connection cx,
            Hints hints)
            throws IOException, SQLException {
        return null;
    }

    @Override
    public SimpleFeatureType postCreateFeatureTypeCallback(
            SimpleFeatureType featureType,
            DatabaseMetaData metadata,
            String schemaName,
            Connection cx) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init(featureType);
        builder.add("geometry", Point.class, DefaultGeographicCRS.WGS84);
        builder.setDefaultGeometry("geometry");
        return builder.buildFeatureType();
    }

    public FilterToSQL createFilterToSQL() {
        FilterToSQL f2s = new HiveFilterToSql();
        return f2s;
    }

    public String getNameEscape() {
        return "";
    }
}
