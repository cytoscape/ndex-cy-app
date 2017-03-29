package org.cytoscape.ndex.internal.cx_reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.cxio.aspects.datamodels.NetworkAttributesElement;
import org.cxio.metadata.MetaDataCollection;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.ndex.io.cxio.CxImporter;
import org.cytoscape.ndex.io.cxio.CxUtil;
import org.cytoscape.ndex.io.cxio.Settings;
import org.cytoscape.ndex.io.cxio.TimingUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;
import org.ndexbio.model.cx.NiceCXNetwork;

@Deprecated
public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

    private final List<CyNetwork>              _networks;
    private String                             _network_collection_name;
    private CxToCy                             _cx_to_cy;
    private final InputStream                  _in;
    private final VisualMappingManager         _visual_mapping_manager;
    private final RenderingEngineManager       _rendering_engine_manager;
    private final CyNetworkViewFactory         _networkview_factory;
    private final boolean                      _perform_basic_integrity_checks;
    private final VisualStyleFactory           _visual_style_factory;
    private final VisualMappingFunctionFactory _vmf_factory_c;
    private final VisualMappingFunctionFactory _vmf_factory_d;
    private final VisualMappingFunctionFactory _vmf_factory_p;
    private final CyGroupFactory               _group_factory;
    
    private final CyLayoutAlgorithmManager layoutManager;
	private TaskMonitor parentTaskMonitor;

    private CytoscapeCxNetworkReader(final String network_collection_name,
                                    final InputStream input_stream,
                                    final CyApplicationManager application_manager,
                                    final CyNetworkFactory network_factory,
                                    final CyNetworkManager network_manager,
                                    final CyRootNetworkManager root_network_manager,
                                    final CyGroupFactory group_factory,
                                    final VisualMappingManager visual_mapping_manager,
                                    final VisualStyleFactory visual_style_factory,
                                    final RenderingEngineManager rendering_engine_manager,
                                    final CyNetworkViewFactory networkview_factory,
                                    final VisualMappingFunctionFactory vmf_factory_c,
                                    final VisualMappingFunctionFactory vmf_factory_d,
                                    final VisualMappingFunctionFactory vmf_factory_p,
                                    final boolean perform_basic_integrity_checks,
                                    final CyLayoutAlgorithmManager layoutManager) throws IOException {

        super(input_stream, networkview_factory, network_factory, network_manager, root_network_manager);

        if (input_stream == null) {
            throw new IllegalArgumentException("input stream must not be null");
        }
        _in = input_stream;
        _network_collection_name = network_collection_name;
        _visual_mapping_manager = visual_mapping_manager;
        _rendering_engine_manager = rendering_engine_manager;
        _networkview_factory = networkview_factory;
        _group_factory = group_factory;
        _networks = new ArrayList<>();
        _perform_basic_integrity_checks = perform_basic_integrity_checks;
        _visual_style_factory = visual_style_factory;
        _vmf_factory_c = vmf_factory_c;
        _vmf_factory_d = vmf_factory_d;
        _vmf_factory_p = vmf_factory_p;
        this.layoutManager = layoutManager;
    }

    @Override
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {

        CyNetworkView view = null;
        Boolean hasLayout = false;
        try {
            Map<CyNetworkView, Boolean> result = ViewMaker.makeView(network,
                                      _cx_to_cy,
                                      _network_collection_name,
                                      _networkview_factory,
                                      _rendering_engine_manager,
                                      _visual_mapping_manager,
                                      _visual_style_factory,
                                      _vmf_factory_c,
                                      _vmf_factory_d,
                                      _vmf_factory_p);
            view = result.keySet().iterator().next();
            hasLayout = result.get(view);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        
        if (view != null) {
			if (!hasLayout) {
				final CyLayoutAlgorithm layout = layoutManager.getDefaultLayout();
				TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
						CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
				Task nextTask = itr.next();
				parentTaskMonitor
						.setStatusMessage("Layout data was not available in the CX.  Applying default layout...");
				try {
					nextTask.run(parentTaskMonitor);
				} catch (Exception e) {
					throw new RuntimeException("Could not finish layout", e);
				}
			}
            view.updateView();
            view.fitContent();
        }
        return view;

    }

    @Override
    public CyNetwork[] getNetworks() {
        final CyNetwork[] results = new CyNetwork[_networks.size()];
        for (int i = 0; i < results.length; ++i) {
            results[i] = _networks.get(i);
        }
        return results;
    }

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {

    		this.parentTaskMonitor = taskMonitor;
    		

        final CxImporter cx_importer = new CxImporter();

        NiceCXNetwork niceCX = cx_importer.getCXNetworkFromStream(_in);

        final long t0 = System.currentTimeMillis();

        if (Settings.INSTANCE.isTiming()) {
            TimingUtil.reportTimeDifference(t0, "total time parsing", -1);
        }
  //      final AspectElementCounts counts = cxr.getAspectElementCounts();
        final MetaDataCollection pre = niceCX.getMetadata();
/*        final MetaDataCollection post = cxr.getPostMetaData();
        if (Settings.INSTANCE.isDebug()) {
            if (counts != null) {
                System.out.println("Aspects elements read in:");
                System.out.println(counts);
            }
            else {
            	System.out.println("No aspects elements read in (!)");
            }
            if (pre != null) {
                System.out.println("Pre metadata:");
                System.out.println(pre);
            }
            else {
            	System.out.println("No pre metadata");
            }
            if (post != null) {
                System.out.println("Post metadata:");
                System.out.println(post);
            }
            else {
            	System.out.println("No post metadata");
            }
        } */

        _cx_to_cy = new CxToCy();

        // Select the root collection name from the list.
        if (_network_collection_name != null) {
            final ListSingleSelection<String> root_list = getRootNetworkList();
            if (root_list.getPossibleValues().contains(_network_collection_name)) {
                // Collection already exists.
                root_list.setSelectedValue(_network_collection_name);
            }
        }

        final CyRootNetwork root_network = getRootNetwork();

        // Select Network Collection
        // 1. Check from Tunable
        // 2. If not available, use optional parameter

        if (root_network != null) {
            // Root network exists
            _networks.addAll(_cx_to_cy.createNetwork(niceCX,
                                                     root_network,
                                                     null,
                                                     _group_factory,
                                                     null,
                                                     _perform_basic_integrity_checks));
        }
        else {
            // Need to create new network with new root.
            if (Settings.INSTANCE.isAllowToUseNetworkCollectionNameFromNetworkAttributes()) {
                final String collection_name_from_network_attributes = getCollectionNameFromNetworkAttributes(niceCX);
                if (collection_name_from_network_attributes != null) {
                    _network_collection_name = collection_name_from_network_attributes;
                    if (Settings.INSTANCE.isDebug()) {
                        System.out.println("collection name from network attributes: " + _network_collection_name);
                    }
                }
            }
            _networks.addAll(_cx_to_cy.createNetwork(niceCX,
                                                     null,
                                                     cyNetworkFactory,
                                                     _group_factory,
                                                     _network_collection_name,
                                                     _perform_basic_integrity_checks));
        }

        if (Settings.INSTANCE.isTiming()) {
            System.out.println();
            TimingUtil.reportTimeDifference(t0, "total time to build network(s) (not views)", -1);
            System.out.println();
        }
    }
    
    private final static String getCollectionNameFromNetworkAttributes(final NiceCXNetwork res) {
        String collection_name_from_network_attributes = null;
        for (NetworkAttributesElement nae : res.getNetworkAttributes()) {
            if (nae.getSubnetwork() == null && nae.getName() != null
                        && nae.getDataType() == ATTRIBUTE_DATA_TYPE.STRING && nae.getName().equals(CxUtil.NAME_COL)
                        && nae.isSingleValue() && nae.getValue() != null && nae.getValue().length() > 0) {
                    if (collection_name_from_network_attributes == null) {
                        collection_name_from_network_attributes = nae.getValue();
                    }
                    else {
                        return null;
                    }
            }
            
        }
        return collection_name_from_network_attributes;
    }

}