package org.geoserver.wfs.json.complex;

import com.vividsolutions.jts.geom.Geometry;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDParticle;
import org.geoserver.wfs.json.GeoJSONBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.geotools.xml.Schemas;
import org.geotools.xml.gml.GMLComplexTypes;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ComplexGeoJsonWriter {

    private final GeoJSONBuilder jsonWriter;

    private boolean geometryFound = false;
    private CoordinateReferenceSystem crs;

    public ComplexGeoJsonWriter(GeoJSONBuilder jsonWriter) {
        this.jsonWriter = jsonWriter;
    }

    public void write(List<FeatureCollection> collections) {
        for (FeatureCollection collection : collections) {
            try (FeatureIterator iterator = collection.features()) {
                encodeFeaturesCollection(iterator);
            }
        }
    }

    private void encodeFeaturesCollection(FeatureIterator iterator) {
        while (iterator.hasNext()) {
            encodeFeature(iterator.next());
        }
    }

    private void encodeFeature(Feature feature) {
        jsonWriter.object();
        jsonWriter.key("type").value("Feature");
        jsonWriter.key("id").value(feature.getIdentifier().getID());
        encodeGeometry(feature);
        jsonWriter.key("properties");
        jsonWriter.object();
        encodeProperties(feature.getType(), feature.getProperties());
        jsonWriter.endObject(); // end the properties
        jsonWriter.endObject(); // end the feature
    }

    private void encodeGeometry(Feature feature) {
        GeometryDescriptor geometryType = feature.getType().getGeometryDescriptor();
        GeometryAttribute geometryAttribute = feature.getDefaultGeometryProperty();
        if(geometryType != null) {
            CoordinateReferenceSystem crs = geometryType.getCoordinateReferenceSystem();
            jsonWriter.setAxisOrder(CRS.getAxisOrder(crs));
            if (crs != null) {
                this.crs = crs;
            }
        } else  {
            jsonWriter.setAxisOrder(CRS.AxisOrder.EAST_NORTH);
        }
        jsonWriter.key("geometry");
        Geometry geometry = (Geometry) geometryAttribute.getValue();
        if (geometry != null) {
            jsonWriter.writeGeom(geometry);
            geometryFound = true;
        } else {
            jsonWriter.value(null);
        }
    }

    private void encodeProperties(PropertyType parentType, Collection<Property> properties) {
        Map<PropertyType, List<Property>> index = indexPropertiesByType(properties);
        for (Map.Entry<PropertyType, List<Property>> entry : index.entrySet()) {
            encodePropertiesByType(parentType, entry.getKey(), entry.getValue());
        }
    }

    private Map<PropertyType, List<Property>> indexPropertiesByType(Collection<Property> properties) {
        Map<PropertyType, List<Property>> index = new HashMap<>();
        for (Property property : properties) {
            List<Property> propertiesWithSameType = index.get(property.getType());
            if (propertiesWithSameType == null) {
                propertiesWithSameType = new ArrayList<>();
                index.put(property.getType(), propertiesWithSameType);
            }
            propertiesWithSameType.add(property);
        }
        return index;
    }

    private void encodePropertiesByType(PropertyType parentType, PropertyType type, List<Property> properties) {
        PropertyDescriptor multipleType = isMultipleType(parentType, type);
        if (multipleType == null) {
            // simple json object
            encodeProperty(properties.get(0));
        } else {
            // possible chained features
            List<Feature> chainedFeatures = getChainedFeatures(properties);
            if (chainedFeatures == null || chainedFeatures.isEmpty()) {
                // no chained features
                for (Property property : properties) {
                    encodeProperty(property);
                }
            } else {
                // chained features
                jsonWriter.key(multipleType.getName().getLocalPart());
                jsonWriter.array();
                for (Feature feature : chainedFeatures) {
                    jsonWriter.object();
                    encodeProperties(feature.getType(), feature.getProperties());
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
            }
        }
    }

    private PropertyDescriptor isMultipleType(PropertyType parentType, PropertyType type) {
        if (!(parentType instanceof ComplexType)) {
            return null;
        }
        ComplexType complexType = (ComplexType) parentType;
        PropertyDescriptor foundType = null;
        for (PropertyDescriptor descriptor : complexType.getDescriptors()) {
            if (descriptor.getType().equals(type)) {
                foundType = descriptor;
            }
        }
        if (foundType == null) {
            return null;
        }
        if(foundType.getMaxOccurs() > 1) {
            return foundType;
        }
        return null;
    }

    private List<Feature> getChainedFeatures(List<Property> properties) {
        List<Feature> features = new ArrayList<>();
        for (Property property : properties) {
            if (!(property instanceof ComplexAttribute)) {
                return null;
            }
            ComplexAttribute complexProperty = (ComplexAttribute) property;
            Collection<Property> subProperties = complexProperty.getProperties();
            if (subProperties.size() > 1) {
                return null;
            }
            Property subProperty = getElementAt(subProperties, 0);
            if (!(subProperty instanceof Feature)) {
                return null;
            }
            features.add((Feature) subProperty);
        }
        return features;
    }

    private <T> T getElementAt(Collection<T> collection, int index) {
        Iterator<T> iterator = collection.iterator();
        T element = null;
        for (int i = 0; i <= index && iterator.hasNext(); i++) {
            element = iterator.next();
        }
        return element;
    }

    private void encodeProperty(Property property) {
        if (property instanceof ComplexAttribute) {
            encodeComplexAttribute((ComplexAttribute) property);
        } else if (property instanceof Attribute) {
            encodeSimpleAttribute((Attribute) property);
        } else {
            throw new RuntimeException(String.format("Invalid property '%s' type '%s', only 'Attribute' and " +
                            "'ComplexAttribute' properties types are supported.",
                    property.getName(), property.getClass().getCanonicalName()));
        }
    }

    private void encodeSimpleAttribute(Attribute attribute) {
        Object value = attribute.getValue();
        String name = attribute.getName().getLocalPart();
        jsonWriter.key(name).value(value);
    }

    private void encodeComplexAttribute(ComplexAttribute attribute) {
        String name = attribute.getName().getLocalPart();
        jsonWriter.key(name);
        jsonWriter.object();
        encodeProperties(attribute.getType(), attribute.getProperties());
        jsonWriter.endObject();
    }

    public boolean geometryFound() {
        return geometryFound;
    }

    public CoordinateReferenceSystem foundCrs() {
        return crs;
    }
}