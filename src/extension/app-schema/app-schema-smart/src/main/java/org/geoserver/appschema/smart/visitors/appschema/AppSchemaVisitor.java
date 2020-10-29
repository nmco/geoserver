package org.geoserver.appschema.smart.visitors.appschema;

import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.buildAppSchemaDocument;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.countLinkedAttributeMapping;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.createAttributeMapping;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.createDataStoreNode;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.createFeatureTypeMappingNode;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.createLinkedAttributeMapping;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.createSourceDataStoresNode;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.createTargetTypesNode;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.createTypeMappingsNode;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.getAttributeMappings;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.getFeatureTypeMapping;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.getSourceDataStoreId;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.getSourceDataStoresNode;
import static org.geoserver.appschema.smart.visitors.appschema.AppSchemaUtils.getTypeMappingsNode;
import static org.geoserver.appschema.smart.visitors.gml.GmlSchemaUtils.TABLE_SUFFIX;

import java.util.logging.Logger;
import org.geoserver.appschema.smart.domain.DomainModelVisitorImpl;
import org.geoserver.appschema.smart.domain.entities.DomainEntity;
import org.geoserver.appschema.smart.domain.entities.DomainEntitySimpleAttribute;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.domain.entities.DomainRelation;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geotools.util.logging.Logging;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** This visitor generates a valid AppSchema xml document for the domain model it will visit. */
public final class AppSchemaVisitor extends DomainModelVisitorImpl {

    private static final Logger LOGGER = Logging.getLogger(AppSchemaVisitor.class);

    private final String targetNamespacePrefix;

    private final String schemaUri;

    private final Document appDocument;

    private Node currentFeatureTypeMapping;

    public AppSchemaVisitor(
            String targetNamespacePrefix, String targetNamespaceUrl, String schemaUri) {
        this.targetNamespacePrefix = targetNamespacePrefix;
        this.schemaUri = schemaUri;
        // build and instantiate the gml schema document
        appDocument = buildAppSchemaDocument(targetNamespacePrefix, targetNamespaceUrl);
    }

    @Override
    public void visitDataStoreMetadata(DataStoreMetadata dataStoreMetadata) {
        Node sourceDataStoresNode = getSourceDataStoresNode(appDocument);
        // append dataStoresMetadata to sourceDataStores node
        Node dataStoreNode = createDataStoreNode(appDocument, dataStoreMetadata);
        sourceDataStoresNode.appendChild(dataStoreNode);
    }

    @Override
    public void visitDomainModel(DomainModel model) {
        // targetTypes (reference to gml files)
        Element targetTypesNode = createTargetTypesNode(appDocument, this.schemaUri);
        appDocument.getFirstChild().appendChild(targetTypesNode);
        // typeMappings (empty node, we will add entities here)
        Element typeMappingsNode = createTypeMappingsNode(appDocument);
        appDocument.getFirstChild().appendChild(typeMappingsNode);
        // sourceDataStores (empty node, we will add datastores here)
        Element sourceDataStoresNode = createSourceDataStoresNode(appDocument);
        appDocument.getFirstChild().appendChild(sourceDataStoresNode);
    }

    @Override
    public void visitDomainRootEntity(DomainEntity entity) {
        currentFeatureTypeMapping = handleEntity(entity);
    }

    @Override
    public void visitDomainChainedEntity(DomainEntity entity) {
        currentFeatureTypeMapping = handleEntity(entity);
    }

    @Override
    public void visitDomainRelation(DomainRelation relation) {

        String containingTargetElementValue =
                this.targetNamespacePrefix
                        + ":"
                        + relation.getContainingEntity().getName()
                        + TABLE_SUFFIX;
        String destinationTargetElementValue =
                this.targetNamespacePrefix
                        + ":"
                        + relation.getDestinationEntity().getName()
                        + TABLE_SUFFIX;

        int count = 1;
        Node destinationFeatureTypeMapping =
                getFeatureTypeMapping(appDocument, destinationTargetElementValue);
        if (destinationFeatureTypeMapping == null) {
            destinationFeatureTypeMapping = handleEntity(relation.getDestinationEntity());
            // add destination to appDocument typeMappings
            Node typeMappings = getTypeMappingsNode(appDocument);
            typeMappings.appendChild(destinationFeatureTypeMapping);
        }
        count = countLinkedAttributeMapping(appDocument, destinationTargetElementValue);
        String linkName = "FEATURE_LINK[" + Integer.toString(count) + "]";
        Node destinationAttributeMappingNode =
                createAttributeMapping(
                        appDocument, linkName, relation.getDestinationKeyAttribute().getName());
        Node destAttributesMapping = getAttributeMappings(destinationFeatureTypeMapping);
        destAttributesMapping.appendChild(destinationAttributeMappingNode);

        Node containingFeatureTypeMapping =
                getFeatureTypeMapping(appDocument, containingTargetElementValue);
        Node srcAttributeMappings = getAttributeMappings(containingFeatureTypeMapping);
        Node attributeMappingNode =
                createLinkedAttributeMapping(
                        appDocument,
                        destinationTargetElementValue,
                        relation.getContainingKeyAttribute().getName(),
                        destinationTargetElementValue,
                        linkName);
        srcAttributeMappings.appendChild(attributeMappingNode);
    }

    @Override
    public void visitDomainEntitySimpleAttribute(DomainEntitySimpleAttribute attribute) {
        Node featureTypeMapping = currentFeatureTypeMapping;
        // append AttributeMapping to the FeatureTypeMapping
        Node attributeMappings = getAttributeMappings(featureTypeMapping);
        String targetAttributeValue = this.targetNamespacePrefix + ":" + attribute.getName();
        String OCQLValue = attribute.getName();
        Node attributeMappingNode =
                createAttributeMapping(appDocument, targetAttributeValue, OCQLValue);
        attributeMappings.appendChild(attributeMappingNode);
    }

    private Node handleEntity(DomainEntity entity) {
        String sourceDataStoreValue = getSourceDataStoreId(appDocument);
        String sourceTypeValue = entity.getName();
        String targetElementValue =
                this.targetNamespacePrefix + ":" + entity.getName() + TABLE_SUFFIX;
        // check if featureTypeMapping node is present in document
        Node featureTypeMappingNode = getFeatureTypeMapping(appDocument, targetElementValue);
        if (featureTypeMappingNode != null) {
            return featureTypeMappingNode;
        }
        Node typeMappingsNode = appDocument.getElementsByTagName("typeMappings").item(0);
        // insert entity into typeMappings node
        featureTypeMappingNode =
                createFeatureTypeMappingNode(
                        appDocument, sourceDataStoreValue, sourceTypeValue, targetElementValue);
        typeMappingsNode.appendChild(featureTypeMappingNode);
        return featureTypeMappingNode;
    }

    public Document getDocument() {
        return (Document) appDocument;
    }
}
