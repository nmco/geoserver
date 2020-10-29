package org.geoserver.appschema.smart.data.store.panel;

import static org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory.DOMAIN_MODEL_EXCLUSIONS;
import static org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory.POSTGIS_DATASTORE_METADATA;
import static org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory.ROOT_ENTITY;
import static org.geoserver.appschema.smart.data.PostgisSmartAppSchemaDataAccessFactory.DATASTORE_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.appschema.smart.data.store.NestedTreeDomainModelVisitor;
import org.geoserver.appschema.smart.domain.DomainModelBuilder;
import org.geoserver.appschema.smart.domain.DomainModelConfig;
import org.geoserver.appschema.smart.domain.entities.DomainModel;
import org.geoserver.appschema.smart.metadata.DataStoreMetadata;
import org.geoserver.appschema.smart.metadata.DataStoreMetadataConfig;
import org.geoserver.appschema.smart.metadata.DataStoreMetadataFactory;
import org.geoserver.appschema.smart.metadata.EntityMetadata;
import org.geoserver.appschema.smart.metadata.jdbc.JdbcDataStoreMetadataConfig;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.web.data.store.StoreEditPanel;
import org.geoserver.web.data.store.panel.TextParamPanel;
import org.geoserver.web.data.store.panel.WorkspacePanel;
import org.geoserver.web.util.MapModel;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;

/**
 * Implementation od StoreEditPanel for PostgisSmartAppSchemaDataAccessFactory.
 *
 * @author Jose Macchi - GeoSolutions
 */
@SuppressWarnings("serial")
public class PostgisSmartAppSchemaStoreEditPanel extends StoreEditPanel {

    // resources
    private ParamResourceModel postgisdatastoreResource =
            new ParamResourceModel("PostGisSmartAppSchemaStoreEditPanel.postgisdatastore", this);
    private ParamResourceModel rootentitiesResource =
            new ParamResourceModel("PostGisSmartAppSchemaStoreEditPanel.rootentities", this);
    private ParamResourceModel domainmodelResource =
            new ParamResourceModel("PostGisSmartAppSchemaStoreEditPanel.domainmodel", this);
    private ParamResourceModel exclusionsResource =
            new ParamResourceModel("PostGisSmartAppSchemaStoreEditPanel.exclusions", this);
    private ParamResourceModel datastorenameResource =
            new ParamResourceModel("PostGisSmartAppSchemaStoreEditPanel.datastorename", this);

    // view components
    private NestedTreePanel domainModelTree;
    private SimpleDropDownChoiceParamPanel datastores;
    private SimpleDropDownChoiceParamPanel availableRootEntities;
    private WorkspacePanel workspacePanel;
    @SuppressWarnings("rawtypes")
    private TextParamPanel datastoreNamePanel;
    @SuppressWarnings("rawtypes")
    private TextParamPanel exclusions;
    @SuppressWarnings("rawtypes")
    private TextParamPanel datastorename;

    // models
    private DataStoreInfo smartAppSchemaDataStoreInfo;

    @SuppressWarnings("rawtypes")
    private final IModel model;

    // internal use
    private String selectedPostgisDataStoreName = "";
    private String selectedRootEntityName = "";
    private String excludedObjectCodesList = "";

    @SuppressWarnings("unused")
    private String selectedWorkspaceName = "";

    public PostgisSmartAppSchemaStoreEditPanel(final String componentId, final Form storeEditForm) {
        super(componentId, storeEditForm);
        model = storeEditForm.getModel();
        setDefaultModel(model);
        smartAppSchemaDataStoreInfo = ((DataStoreInfo) storeEditForm.getModel().getObject());
        // set helper variables
        selectedPostgisDataStoreName = getDataStoreInfoParam(POSTGIS_DATASTORE_METADATA.key);
        selectedRootEntityName = getDataStoreInfoParam(ROOT_ENTITY.key);
        excludedObjectCodesList = getDataStoreInfoParam(DOMAIN_MODEL_EXCLUSIONS.key);
        // build connection parameters panel
        buildPostgisDropDownPanel(model);
        // build rootentity selector panel
        buildRootEntitySelectionPanel(model);
        // build entities, attributes and relations selector panel
        buildDomainModelTreePanel(model);
        // build exclusions panel (it's hidden)
        buildHiddenParametersPanel(model);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        // search for the workspace panel
        workspacePanel =
                (WorkspacePanel)
                        PostgisSmartAppSchemaStoreEditPanel.this
                                .getPage()
                                .get("dataStoreForm:workspacePanel");
        // attach behavior on form component selector, so we can filter list of available postgis
        // related datastores
        workspacePanel
                .getFormComponent()
                .add(
                        new AjaxFormComponentUpdatingBehavior("change") {
                            @SuppressWarnings({"rawtypes", "unchecked"})
                            protected void onUpdate(AjaxRequestTarget target) {
                                WorkspaceInfo wi =
                                        ((WorkspaceInfo)
                                                workspacePanel.getFormComponent().getModelObject());
                                selectedWorkspaceName =
                                        ((WorkspaceInfo)
                                                        workspacePanel
                                                                .getFormComponent()
                                                                .getModelObject())
                                                .getName();
                                List<String> list = getPostgisDataStores(wi);
                                datastores.getFormComponent().setChoices(list);
                                availableRootEntities
                                        .getFormComponent()
                                        .setChoices(Collections.EMPTY_LIST);
                                selectedRootEntityName = "";
                                // clear list of exclusions
                                excludedObjectCodesList = "";
                                exclusions.modelChanging();
                                smartAppSchemaDataStoreInfo
                                        .getConnectionParameters()
                                        .put(DOMAIN_MODEL_EXCLUSIONS.key, excludedObjectCodesList);
                                exclusions.modelChanged();
                                // rebuild tree
                                IModel iModel = new PropertyModel(model, "connectionParameters");
                                buildDomainModelTreePanel(iModel);
                                target.add(domainModelTree);
                                target.add(availableRootEntities);
                                target.add(datastores);
                            }
                        });
        
        // search for the datastorename panel
        datastoreNamePanel =
                (TextParamPanel)
                        PostgisSmartAppSchemaStoreEditPanel.this
                                .getPage()
                                .get("dataStoreForm:dataStoreNamePanel");
        // attach datastore name to hidden text component used to share datastorename with the dataaccessfactory
        datastoreNamePanel.getFormComponent().add(
                new AjaxFormComponentUpdatingBehavior("change") {
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						String name =
                                ((String)
                                        datastoreNamePanel.getFormComponent().getModelObject());
						datastorename.modelChanging();
                        smartAppSchemaDataStoreInfo
                                .getConnectionParameters()
                                .put(DATASTORE_NAME.key, name);
                        datastorename.modelChanged();
                        target.add(datastorename);
					}
                });
        
    }

    /** Helper method that creates dropdown for postgis datastore selection. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void buildPostgisDropDownPanel(final IModel model) {
        IModel iModel = new PropertyModel(model, "connectionParameters");
        List<String> postgisDSs = getPostgisDataStores(getWorkspaceInfo());
        datastores =
                new SimpleDropDownChoiceParamPanel(
                        "postgisdatastore",
                        new MapModel(iModel, POSTGIS_DATASTORE_METADATA.key),
                        postgisdatastoreResource,
                        postgisDSs,
                        true);
        datastores
                .getFormComponent()
                .add(
                        new AjaxFormComponentUpdatingBehavior("click") {
                            protected void onUpdate(AjaxRequestTarget target) {
                                selectedPostgisDataStoreName =
                                        (String) datastores.getFormComponent().getModelObject();
                                DataStoreInfo postgisDS = null;
                                if (selectedPostgisDataStoreName != null
                                        && !selectedPostgisDataStoreName.isEmpty()) {
                                    postgisDS =
                                            getDataStoreInfoByName(selectedPostgisDataStoreName);
                                }
                                List<String> list = new ArrayList<String>();
                                if (postgisDS != null) {
                                    list = getAvailableRootEntities(postgisDS);
                                }
                                availableRootEntities.getFormComponent().setChoices(list);
                                target.add(availableRootEntities);
                            }
                        });
        datastores.setOutputMarkupId(true);
        add(datastores);
    }

    private List<String> getPostgisDataStores(WorkspaceInfo wi) {
        List<String> postgisDSs = new ArrayList<String>();
        List<DataStoreInfo> dsList = getCatalog().getDataStoresByWorkspace(wi);
        // need to keep only those related to postgis
        for (DataStoreInfo ds : dsList) {
            String dbtype = ds.getConnectionParameters().get("dbtype").toString().toLowerCase();
            if (dbtype.contains("postgis")) {
                // it's postgis related
                postgisDSs.add(ds.getName());
            }
        }
        return postgisDSs;
    }

    /** Helper method that creates the rootentity selector dropdown. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void buildRootEntitySelectionPanel(final IModel model) {
        IModel iModel = new PropertyModel(model, "connectionParameters");
        List<String> list = new ArrayList<String>();
        String postgisDSName = (String) datastores.getDefaultModel().getObject();
        if (postgisDSName != null) {
            DataStoreInfo postgisDS = this.getDataStoreInfoByName(postgisDSName);
            list = getAvailableRootEntities(postgisDS);
        }
        availableRootEntities =
                new SimpleDropDownChoiceParamPanel(
                        "rootentities",
                        new MapModel(iModel, ROOT_ENTITY.key),
                        rootentitiesResource,
                        list,
                        true);
        availableRootEntities
                .getFormComponent()
                .add(
                        new AjaxFormComponentUpdatingBehavior("change") {
                            protected void onUpdate(AjaxRequestTarget target) {
                                selectedRootEntityName =
                                        (String)
                                                availableRootEntities
                                                        .getFormComponent()
                                                        .getModelObject();
                                // clear list of exclusions
                                excludedObjectCodesList = "";
                                exclusions.modelChanging();
                                smartAppSchemaDataStoreInfo
                                        .getConnectionParameters()
                                        .put(DOMAIN_MODEL_EXCLUSIONS.key, excludedObjectCodesList);
                                exclusions.modelChanged();
                                // rebuild tree
                                buildDomainModelTreePanel(iModel);
                                target.add(domainModelTree);
                            }
                        });
        availableRootEntities.setOutputMarkupId(true);
        add(availableRootEntities);
    }

    /** Helper method that creates the DomainModel tree panel */
    protected void buildDomainModelTreePanel(final IModel model) {
        domainModelTree =
                new NestedTreePanel("domainmodel", null, domainmodelResource, null, false);
        domainModelTree.setOutputMarkupId(true);
        addOrReplace(domainModelTree);
        // avoid loading tree if rootentity was not selected
        if (selectedRootEntityName != null && !selectedRootEntityName.isEmpty()) {
            DataStoreInfo postgisDS = getDataStoreInfoByName(selectedPostgisDataStoreName);
            if (postgisDS != null) {
                // build DomainModel based on parameters
                DataStoreMetadata dsm = getDataStoreMetadata(postgisDS);
                DomainModelConfig dmc = new DomainModelConfig();
                dmc.setRootEntityName(selectedRootEntityName);
                DomainModelBuilder dmb = new DomainModelBuilder(dsm, dmc);
                DomainModel dm = dmb.buildDomainModel();
                // visit DomainModel with NestedTree
                NestedTreeDomainModelVisitor dmv = new NestedTreeDomainModelVisitor();
                dm.accept(dmv);
                // get the NestedTree representation
                DefaultTreeModel dtm = dmv.getTreeModel();
                // build treePanel with DomainModel and list of checkedNodes
                Set<DefaultMutableTreeNode> nodes = getNodes(dtm);
                Set<DefaultMutableTreeNode> checkedNodes = getCheckedNodes(dtm);
                domainModelTree.buildTree(dtm, checkedNodes);
                domainModelTree.add(
                        new AjaxEventBehavior(("click")) {
                            @Override
                            protected void onEvent(AjaxRequestTarget target) {
                                // build list of exclusions based on tree selection
                                StringBuilder stringBuilder = new StringBuilder();
                                for (DefaultMutableTreeNode node : nodes) {
                                    if (!checkedNodes.contains(node)) {
                                        if (node.getParent() != null) {
                                            stringBuilder.append(
                                                    node.getParent().toString()
                                                            + "."
                                                            + node.toString());
                                        } else {
                                            stringBuilder.append(node.toString());
                                        }
                                        stringBuilder.append(",");
                                    }
                                }
                                String exclusionList = stringBuilder.toString();
                                int size = exclusionList.length();
                                String fullExclusionList = "";
                                if (size > 0) {
                                    fullExclusionList = exclusionList.substring(0, size - 1);
                                }
                                // set exclusionList value to exclusionsPanel (model)
                                exclusions.getFormComponent().modelChanging();
                                smartAppSchemaDataStoreInfo
                                        .getConnectionParameters()
                                        .put(DOMAIN_MODEL_EXCLUSIONS.key, fullExclusionList);
                                exclusions.getFormComponent().modelChanged();
                                target.add(exclusions);
                            }
                        });
            }
        }
    }

    /**
     * Helper method that creates a hidden parameters panel, that allows to set the exclusion DomainModel objects (used internally in
     * the form to keep list of exclusions based on tree selection) and the datastore name that will be shared to the factory.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void buildHiddenParametersPanel(final IModel model) {
        IModel iModel = new PropertyModel(model, "connectionParameters");
        exclusions =
                new TextParamPanel(
                        "exclusions",
                        new MapModel(iModel, DOMAIN_MODEL_EXCLUSIONS.key),
                        exclusionsResource,
                        false);
        exclusions.setOutputMarkupId(true);
        exclusions.getFormComponent().setEnabled(false);
        exclusions.setVisible(false);
        add(exclusions);
        
        iModel = new PropertyModel(model, "connectionParameters");
        datastorename =
                new TextParamPanel(
                        "datastorename",
                        new MapModel(iModel, DATASTORE_NAME.key),
                        datastorenameResource,
                        false);
        datastorename.setOutputMarkupId(true);
        datastorename.getFormComponent().setEnabled(false);
        datastorename.setVisible(false);
        add(datastorename);
    }

    /**
     * Helper that includes all the detected nodes in the DomainModel and uncheck those listed in
     * the exclusion list
     */
    private Set<DefaultMutableTreeNode> getCheckedNodes(DefaultTreeModel dtm) {
        // get all nodes
        final Set<DefaultMutableTreeNode> allNodes = getNodes(dtm);
        // create empty set and add those that are in checked
        final Set<DefaultMutableTreeNode> checkedNodes = new HashSet<DefaultMutableTreeNode>();
        String[] elements = {};
        if (excludedObjectCodesList != null) {
            elements = excludedObjectCodesList.split(",");
        }
        List<String> excludedObjectsList = Arrays.asList(elements);
        for (DefaultMutableTreeNode node : allNodes) {
            StringBuilder name = new StringBuilder();
            if (node.getParent() != null) {
                name.append(node.getParent().toString() + "." + node.toString());
            } else {
                name.append(node.toString());
            }
            if (!excludedObjectsList.contains(name.toString())) {
                checkedNodes.add(node);
            }
        }
        return checkedNodes;
    }

    /**
     * Helper method to get the list of all the available entities that can be defined as root
     * entity for a DomainModel.
     */
    private List<String> getAvailableRootEntities(DataStoreInfo ds) {
        DataStoreMetadata dsm = this.getDataStoreMetadata(ds);
        @SuppressWarnings("unchecked")
        List<String> choiceList = new ArrayList<String>();
        List<EntityMetadata> entities = dsm.getDataStoreEntities();
        for (EntityMetadata e : entities) {
            String name = e.getName();
            choiceList.add(name);
        }
        return choiceList;
    }

    /** Helper method to get Postgis-related DataStoreMetadata. */
    private DataStoreMetadata getDataStoreMetadata(DataStoreInfo ds) {
        PostgisNGDataStoreFactory factory = new PostgisNGDataStoreFactory();
        JDBCDataStore jdbcDataStore = null;
        DataStoreMetadata dsm = null;
        try {
            jdbcDataStore = factory.createDataStore(ds.getConnectionParameters());
            DataStoreMetadataConfig config =
                    new JdbcDataStoreMetadataConfig(
                            jdbcDataStore, ds.getConnectionParameters().get("passwd").toString());
            dsm = (new DataStoreMetadataFactory()).getDataStoreMetadata(config);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving metadata from DB.");
        }
        jdbcDataStore.dispose();
        return dsm;
    }

    /** Helper that includes all the detected nodes in the DomainModel */
    private Set<DefaultMutableTreeNode> getNodes(DefaultTreeModel dtm) {
        final Set<DefaultMutableTreeNode> nodes = new HashSet<DefaultMutableTreeNode>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
        convertTreeToSet(root, nodes);
        return nodes;
    }

    /** Helper method that allows to get a DataStoreInfo based on workspace and datastore name. */
    private DataStoreInfo getDataStoreInfoByName(String name) {
        WorkspaceInfo wi = getWorkspaceInfo();
        DataStoreInfo ds = getCatalog().getDataStoreByName(wi.getName(), name);
        return ds;
    }

    /** Helper method that allows to get the related workspace based on the form datastore model. */
    private WorkspaceInfo getWorkspaceInfo() {
        DataStoreInfo dsi = ((DataStoreInfo) storeEditForm.getModel().getObject());
        WorkspaceInfo wi = dsi.getWorkspace();
        return wi;
    }

    /** Helper method that returns a form datastore model parameter value based on the param key. */
    private String getDataStoreInfoParam(String key) {
        String param = (String) smartAppSchemaDataStoreInfo.getConnectionParameters().get(key);
        return param;
    }

    /**
     * Helped method that return list of nodes based on a TreeNode (recursively get full list of
     * nodes)
     */
    private static void convertTreeToSet(
            DefaultMutableTreeNode aNode, Set<DefaultMutableTreeNode> nodes) {
        for (int i = 0; i < aNode.getChildCount(); i++) {
            convertTreeToSet((DefaultMutableTreeNode) aNode.getChildAt(i), nodes);
        }
        nodes.add(aNode);
    }
}
