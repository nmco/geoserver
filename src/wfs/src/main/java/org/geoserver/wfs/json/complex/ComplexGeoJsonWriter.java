package org.geoserver.wfs.json.complex;

import org.geoserver.wfs.json.GeoJSONBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.PropertyType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ComplexGeoJsonWriter {

    private final boolean relaxed = Boolean.parseBoolean(System.getProperty("encoder.relaxed", "true"));

    private final GeoJSONBuilder jsonWriter;

    private boolean geomtryFound = false;
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
        // TODO: encode geometry
        jsonWriter.key("properties");
        jsonWriter.object();
        encodeProperties(feature.getProperties());
        jsonWriter.endObject(); // end the properties
        jsonWriter.endObject(); // end the feature
    }

    private void encodeProperties(Collection<Property> properties) {
        Map<PropertyType, List<Property>> index = indexPropertiesByType(properties);
        for (Map.Entry<PropertyType, List<Property>> entry : index.entrySet()) {
            encodePropertiesByType(entry.getKey(), entry.getValue());
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

    private void encodePropertiesByType(PropertyType type, List<Property> properties) {
        if (properties.size() == 1) {
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
                jsonWriter.key(chainedFeatures.get(0).getName().getLocalPart());
                jsonWriter.array();
                for (Feature feature : chainedFeatures) {
                    jsonWriter.object();
                    encodeProperties(feature.getProperties());
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
            }
        }
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
        encodeProperties(attribute.getProperties());
        jsonWriter.endObject();
    }

    public boolean geometryFound() {
        return geomtryFound;
    }

    public CoordinateReferenceSystem foundCrs() {
        return crs;
    }
}