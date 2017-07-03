package org.cytoscape.ndex.internal.cx_reader;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.cxio.aspects.datamodels.Mapping;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.ndex.internal.singletons.CyObjectManager;
import org.cytoscape.ndex.io.cxio.CxUtil;
import org.cytoscape.ndex.io.cxio.Settings;
import org.cytoscape.ndex.io.cxio.TimingUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public final class ViewMaker {

    public static final Pattern DIRECT_NET_PROPS_PATTERN = Pattern
            .compile("GRAPH_VIEW_(ZOOM|CENTER_(X|Y))|NETWORK_(WIDTH|HEIGHT|SCALE_FACTOR|CENTER_(X|Y|Z)_LOCATION)");

    public final static  CyNetworkView  makeView(final CyNetwork network,
                                               final CxToCy cx_to_cy,
                                               final String network_collection_name,
                                               final CyNetworkViewFactory networkview_factory,
                                               final RenderingEngineManager rendering_engine_manager,
                                               final VisualMappingManager visual_mapping_manager,
                                               final VisualStyleFactory visual_style_factory,
                                               boolean doLayout) 
                                            		   throws IOException {

   // 		final Map<CyNetworkView, Boolean> hasLayoutMap = new HashMap<>();
    		
        final long t0 = System.currentTimeMillis();
        final VisualElementCollectionMap collection = cx_to_cy.getVisualElementCollectionMap();
        final CyNetworkView view = networkview_factory.createNetworkView(network);
    //    hasLayoutMap.put(view, Boolean.FALSE);
        
        if ((collection == null) || collection.isEmpty()) {
             return ViewMaker.applyStyle(visual_mapping_manager.getDefaultVisualStyle(), view, doLayout);
        }

        final Long network_id = cx_to_cy.getNetworkSuidToNetworkRelationsMap().get(network.getSUID());

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // PLEASE NOTE
        // -----------
        // For now, Cytoscape has only one view per
        // sub-network, but in the future this
        // might change, which means instead of "get(0)" we will need a
        // loop.
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if (network_id == null || !cx_to_cy.getSubNetworkToViewsMap().containsKey(network_id)) {
				return ViewMaker.applyStyle(visual_mapping_manager.getDefaultVisualStyle(), view, doLayout);
		}

        final Long view_id = cx_to_cy.getSubNetworkToViewsMap().get(network_id).get(0);
        if (Settings.INSTANCE.isDebug()) {
            System.out.println("making view " + view_id + " (network " + network_id + ")");
        }

        final boolean have_default_visual_properties = ((collection.getNetworkVisualPropertiesElement(view_id) != null)
                || (collection.getNodesDefaultVisualPropertiesElement(view_id) != null) || (collection
                        .getEdgesDefaultVisualPropertiesElement(view_id) != null));

        VisualStyle new_visual_style = visual_mapping_manager.getDefaultVisualStyle();
        if (have_default_visual_properties) {
            int counter = 1;
            final VisualStyle default_visual_style = visual_mapping_manager.getDefaultVisualStyle();
            new_visual_style = visual_style_factory.createVisualStyle(default_visual_style);
            final String viz_style_title_base = ViewMaker.createTitleForNewVisualStyle(network_collection_name);
            String viz_style_title = viz_style_title_base;
            while (counter < 101 && ViewMaker.containsVisualStyle(viz_style_title, visual_mapping_manager)) {
                    viz_style_title = viz_style_title_base + "-" + counter;
                    counter++;
            }
 //           ViewMaker.removeVisualStyle(viz_style_title, visual_mapping_manager);
            new_visual_style.setTitle(viz_style_title);
        }
        final VisualLexicon lexicon = rendering_engine_manager.getDefaultVisualLexicon();

        if (collection.getNetworkVisualPropertiesElement(view_id) != null) {
            ViewMaker.setDefaultVisualPropertiesAndMappings(lexicon,
                                                            collection.getNetworkVisualPropertiesElement(view_id),
                                                            new_visual_style,
                                                            CyNetwork.class);
        }

        if (collection.getNodesDefaultVisualPropertiesElement(view_id) != null) {
            ViewMaker.setDefaultVisualPropertiesAndMappings(lexicon,
                                                            collection.getNodesDefaultVisualPropertiesElement(view_id),
                                                            new_visual_style,
                                                            CyNode.class);
        }

        if (collection.getEdgesDefaultVisualPropertiesElement(view_id) != null) {
            ViewMaker.setDefaultVisualPropertiesAndMappings(lexicon,
                                                            collection.getEdgesDefaultVisualPropertiesElement(view_id),
                                                            new_visual_style,
                                                            CyEdge.class);
        }

        ViewMaker.setNodeVisualProperties(view, lexicon, collection, view_id, cx_to_cy.getNodesWithVisualProperties());

        ViewMaker.setEdgeVisualProperties(view, lexicon, collection, view_id, cx_to_cy.getEdgesWithVisualProperties());

        final Map<CyNode, CartesianLayoutElement> position_map_for_view = collection
                .getCartesianLayoutElements(view_id);

        if ((position_map_for_view != null) && (view != null)) {
            for (final CyNode node : position_map_for_view.keySet()) {
                if (node != null) {
                    final CartesianLayoutElement e = position_map_for_view.get(node);
                    if (e != null) {
                        final View<CyNode> node_view = view.getNodeView(node);
                        if (node_view != null) {
                            node_view.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, Double.valueOf(e.getX()));
                            node_view.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, Double.valueOf(e.getY()));
                            if (e.isZset()) {
                                node_view.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION,
                                                            Double.valueOf(e.getZ()));
                            }
                        }
                    }
                }
            }
   //         hasLayoutMap.put(view, Boolean.TRUE);
        }

        if (have_default_visual_properties) {
        		// Simply add & assign style.  VMM automatically apply this later. 
            visual_mapping_manager.addVisualStyle(new_visual_style);
            visual_mapping_manager.setVisualStyle(new_visual_style, view);
        }
        
        if (Settings.INSTANCE.isTiming()) {
            TimingUtil.reportTimeDifference(t0, "time to make view", -1);
        }
        
        return ViewMaker.applyStyle(new_visual_style, view, doLayout);
        
   /*     CyNetworkView cyNetworkView = null;
        for ( CyNetworkView v : cyNetworkViewMap.keySet()) {
        	cyNetworkView = v;
        	break;
        }
        if( doLayout ) // && !stopLayout)
        {
            CyLayoutAlgorithmManager lam = CyObjectManager.INSTANCE.getLayoutAlgorithmManager();
            CyLayoutAlgorithm algorithm = lam.getLayout("force-directed");
            TaskIterator ti = algorithm.createTaskIterator(cyNetworkView, algorithm.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
            DialogTaskManager tm = CyObjectManager.INSTANCE.getTaskManager();
            tm.execute(ti);
            cyNetworkView.updateView();
        }
        VisualStyle currentStyle = vmm.getCurrentVisualStyle();
        currentStyle.apply(cyNetworkView);
        if( cyNetworkView != null )
            cyNetworkView.updateView();
        
        return hasLayoutMap; */
    }

    private static CyNetworkView applyStyle (VisualStyle style, CyNetworkView networkView, boolean doLayout) {
        if( doLayout ) // && !stopLayout)
        {
            CyLayoutAlgorithmManager lam = CyObjectManager.INSTANCE.getLayoutAlgorithmManager();
            CyLayoutAlgorithm algorithm = lam.getLayout("force-directed");
            TaskIterator ti = algorithm.createTaskIterator(networkView, algorithm.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
            DialogTaskManager tm = CyObjectManager.INSTANCE.getTaskManager();
            tm.execute(ti);
            networkView.updateView();
        }
        style.apply(networkView);
        networkView.updateView();	
        return networkView;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final static void addContinuousMapping(final VisualStyle style,
                                                  final VisualProperty vp,
                                                  final StringParser sp,
                                                  final String col,
                                                  final String type,
                                                  final Class<?> type_class) {
        final ContinuousMapping cmf = (ContinuousMapping) CyObjectManager.INSTANCE.getVisualMappingFunctionContinuousFactory()
                .createVisualMappingFunction(col, type_class, vp);

        if (cmf != null) {
            int counter = 0;
            while (true) {
                final String ov = sp.get("OV=" + counter);
                if (ov == null) {
                    break;
                }
                final String l = sp.get("L=" + counter);
                final String e = sp.get("E=" + counter);
                final String g = sp.get("G=" + counter);
                if ((l != null) && (e != null) && (g != null)) {
                    final Object lp = vp.parseSerializableString(l);
                    final Object ep = vp.parseSerializableString(e);
                    final Object gp = vp.parseSerializableString(g);
                    if ((lp != null) && (ep != null) && (gp != null)) {
                        final BoundaryRangeValues point = new BoundaryRangeValues(lp, ep, gp);
                        cmf.addPoint(ViewMaker.toTypeValue(ov, type), point);
                    }
                    else {
                        System.out.println("could not parse from string in continuous mapping for col '" + col + "'");
                    }
                }
                else {
                    System.out.println("could not get expected values in continuous mapping for col '" + col + "'");
                }
                counter++;
            }
            style.addVisualMappingFunction(cmf);
        }
        else {
            System.out.println("could not create continuous mapping for col '" + col + "'");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final static void addDiscreteMapping(final VisualStyle style,
                                                final VisualProperty vp,
                                                final StringParser sp,
                                                final String col,
                                                final String type,
                                                final Class<?> type_class) {
        final DiscreteMapping dmf = (DiscreteMapping) CyObjectManager.INSTANCE.getVisualMappingFunctionDiscreteFactory()
        		.createVisualMappingFunction(col, type_class, vp);
        if (dmf != null) {
            int counter = 0;
            while (true) {
                final String k = sp.get("K=" + counter);
                if (k == null) {
                    break;
                }
                final String v = sp.get("V=" + counter);
                if (v != null) {
                    final Object pv = vp.parseSerializableString(v);
                    if (pv != null) {
                        dmf.putMapValue(ViewMaker.toTypeValue(k, type), pv);
                    }
                    else {
                        System.out.println("could not parse serializable string from discrete mapping value '" + v
                                + "'");
                    }
                }
                else {
                    System.out.println("error: discrete mapping function string is corrupt");
                }
                counter++;
            }
            style.addVisualMappingFunction(dmf);
        }
        else {
            System.out.println("could not create discrete mapping for col '" + col + "'");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addPasstroughMapping(final VisualStyle style,
                                                  final VisualProperty vp,
                                                  final String col,
                                                  final Class<?> type_class) throws IOException {
        final PassthroughMapping pmf = (PassthroughMapping) CyObjectManager.INSTANCE
        		.getVisualMappingFunctionPassthroughFactory().createVisualMappingFunction(col,
                                                       type_class,
                                                       vp);
        if (pmf != null) {
            style.addVisualMappingFunction(pmf);
        }
        else {
            throw new IOException("could not create passthrough mapping for col '" + col + "'");

        }
    }

    private final static String createTitleForNewVisualStyle(final String network_collection_name) {
        String viz_style_title = "new-Style";
        if (network_collection_name != null) {
            if (network_collection_name.toLowerCase().endsWith(".cx")) {
                viz_style_title = String.format("%s-Style", network_collection_name.substring(0,
                                                                                              network_collection_name
                                                                                                      .length() - 3));
            }
            else {
                viz_style_title = String.format("%s-Style", network_collection_name);
            }
        }
        return viz_style_title;
    }

 /*   private final static void removeVisualStyle(final String viz_style_title,
                                               final VisualMappingManager visual_mapping_manager) {
        final Iterator<VisualStyle> it = visual_mapping_manager.getAllVisualStyles().iterator();
        while (it.hasNext()) {
            final VisualStyle vs = it.next();
            if (vs.getTitle().equalsIgnoreCase(viz_style_title)) {
                visual_mapping_manager.removeVisualStyle(vs);
                break;
            }
        }
    } */

    private final static boolean containsVisualStyle(final String viz_style_title,
                                                    final VisualMappingManager visual_mapping_manager) {
        final Iterator<VisualStyle> it = visual_mapping_manager.getAllVisualStyles().iterator();
        while (it.hasNext()) {
            final VisualStyle vs = it.next();
            if (vs.getTitle().equalsIgnoreCase(viz_style_title)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final static void setDefaultVisualPropertiesAndMappings(final VisualLexicon lexicon,
                                                                   final CyVisualPropertiesElement cy_visual_properties_element,
                                                                   final VisualStyle style,
                                                                   final Class my_class)
                                                                           throws IOException {

        final SortedMap<String, String> props = cy_visual_properties_element.getProperties();
        final SortedMap<String, Mapping> maps = cy_visual_properties_element.getMappings();
        final SortedMap<String, String> dependencies = cy_visual_properties_element.getDependencies();

        if (props != null) {
            for (final Map.Entry<String, String> entry : props.entrySet()) {

                final String key = entry.getKey();
                // vvvvvvvvvvvvvvvvvvvvvvvvvvv remove me
                boolean is_mapping = false;
                char mapping = '?';
                String mapping_key = null;
                if (key.startsWith(CxUtil.PASSTHROUGH_MAPPING)) {
                    is_mapping = true;
                    mapping = 'p';
                    mapping_key = key.substring(20);
                }
                else if (key.startsWith(CxUtil.CONTINUOUS_MAPPING)) {
                    is_mapping = true;
                    mapping = 'c';
                    mapping_key = key.substring(19);
                }
                else if (key.startsWith(CxUtil.DISCRETE_MAPPING)) {
                    is_mapping = true;
                    mapping = 'd';
                    mapping_key = key.substring(17);
                }
                if (is_mapping) {
                    final VisualProperty vp = lexicon.lookup(my_class, mapping_key);
                    final StringParser sp = new StringParser(entry.getValue());
                    final String col = sp.get(CxUtil.VM_COL);
                    final String type = sp.get(CxUtil.VM_TYPE);
                    final Class<?> type_class = ViewMaker.toClass(type);
                    if (vp != null) {
                        if (mapping == 'p') {
                            addPasstroughMapping(style, vp, col, type_class);
                        }
                        else if (mapping == 'c') {
                            addContinuousMapping(style, vp, sp, col, type, type_class);
                        }
                        else if (mapping == 'd') {
                            addDiscreteMapping(style, vp, sp, col, type, type_class);
                        }
                        else {
                            throw new IllegalStateException("unknown mapping type: " + mapping);
                        }
                    }
                    // TODO
                    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^ remove me
                }
                else {
                    // vvvvvvvvvvvvvvvvvvvvvvvvvvv remove me
                    if (key.equals(CxUtil.ARROW_COLOR_MATCHES_EDGE)
                            || key.equals(CxUtil.NODE_CUSTOM_GRAPHICS_SIZE_SYNC) || key.equals(CxUtil.NODE_SIZE_LOCKED)) {
                        for (final VisualPropertyDependency<?> d : style.getAllVisualPropertyDependencies()) {
                            if (d.getIdString().equals(key)) {
                                d.setDependency(Boolean.parseBoolean(entry.getValue()));
                            }
                        }
                    }
                    // TODO
                    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^ remove me
                    else {
                        final VisualProperty vp = lexicon.lookup(my_class, key);
                        if (vp != null) {
                            Object parsed_value = null;
                            try {
                                parsed_value = vp.parseSerializableString(entry.getValue());
                            }
                            catch (final Exception e) {
                                throw new IOException("could not parse serializable string from '" + entry.getValue()
                                        + "' for '" + key + "'");
                            }
                            if (parsed_value != null) {
                                style.setDefaultValue(vp, parsed_value);
                            }
                        }
                    }
                }
            }
        }

        if (maps != null) {
            for (final Entry<String, Mapping> entry : maps.entrySet()) {
                final String mapping_target = entry.getKey();
                final Mapping mapping = entry.getValue();
                final String mapping_type = mapping.getType();
                final VisualProperty vp = lexicon.lookup(my_class, mapping_target);
                final StringParser sp = new StringParser(mapping.getDefintion());
                String col = sp.get(CxUtil.VM_COL);
                if ( cy_visual_properties_element.getProperties_of().equals("edges:default") && col.equals("interaction") ) 
                	col = "shared interaction";
                final String type = sp.get(CxUtil.VM_TYPE);
                final Class<?> type_class = ViewMaker.toClass(type);
                if (vp != null) {
                    if (mapping_type.equals(CxUtil.PASSTHROUGH)) {
                        addPasstroughMapping(style, vp, col, type_class);
                    }
                    else if (mapping_type.equals(CxUtil.CONTINUOUS)) {
                        addContinuousMapping(style, vp, sp, col, type, type_class);
                    }
                    else if (mapping_type.equals(CxUtil.DISCRETE)) {
                        addDiscreteMapping(style, vp, sp, col, type, type_class);
                    }
                    else {
                        throw new IOException("unknown mapping type: " + mapping_type);
                    }
                }
            }
        }
        if (dependencies != null) {
            for (final Entry<String, String> entry : dependencies.entrySet()) {
                final String k = entry.getKey();
                final String v = entry.getValue();
                if ((k != null) && (v != null)) {
                    for (final VisualPropertyDependency<?> d : style.getAllVisualPropertyDependencies()) {
                        if (d.getIdString().equals(k)) {
                            try {
                                d.setDependency(Boolean.parseBoolean(v));
                            }
                            catch (final Exception e) {
                                throw new IOException("could not parse boolean from '" + v + "'");
                            }
                        }
                    }
                }
            }
        }
    }

    private final static void setEdgeVisualProperties(final CyNetworkView view,
                                                     final VisualLexicon lexicon,
                                                     final VisualElementCollectionMap collection,
                                                     final Long subnetwork_id,
                                                     final Collection<CyEdge> edges_with_visual_properties) {

        if (edges_with_visual_properties != null) {
            for (final CyEdge edge : edges_with_visual_properties) {
                final Map<CyEdge, CyVisualPropertiesElement> evpm = collection
                        .getEdgeVisualPropertiesElementsMap(subnetwork_id);
                final CyVisualPropertiesElement vpe = evpm.get(edge);

                if (vpe != null) {
                    final SortedMap<String, String> props = vpe.getProperties();
                    if (props != null) {
                        final View<CyEdge> v = view.getEdgeView(edge);
                        ViewMaker.setVisualProperties(lexicon, props, v, CyEdge.class);
                    }
                }
            }
        }
    }

    private final static void setNodeVisualProperties(final CyNetworkView view,
                                                     final VisualLexicon lexicon,
                                                     final VisualElementCollectionMap collection,
                                                     final Long subnetwork_id,
                                                     final Collection<CyNode> nodes_with_visual_properties) {

        if (nodes_with_visual_properties != null) {
            for (final CyNode node : nodes_with_visual_properties) {
                final Map<CyNode, CyVisualPropertiesElement> nvpm = collection
                        .getNodeVisualPropertiesElementsMap(subnetwork_id);
                final CyVisualPropertiesElement vpe = nvpm.get(node);

                if (vpe != null) {
                    final SortedMap<String, String> props = vpe.getProperties();
                    if (props != null) {
                        final View<CyNode> v = view.getNodeView(node);
                        ViewMaker.setVisualProperties(lexicon, props, v, CyNode.class);
                    }
                }
            }
        }
    }

    private final static void setVisualProperties(final VisualLexicon lexicon,
                                                 final SortedMap<String, String> props,
                                                 final View<?> view,
                                                 final Class<?> my_class) {
        if (props != null) {
            for (final Map.Entry<String, String> entry : props.entrySet()) {
                final VisualProperty<?> vp = lexicon.lookup(my_class, entry.getKey());

                if (vp != null) {
                    final Object parsed_value = vp.parseSerializableString(entry.getValue());
                    if (parsed_value != null) {
                        if (ViewMaker.shouldSetAsLocked(vp)) {
                            view.setLockedValue(vp, parsed_value);
                        }
                        else {
                            view.setVisualProperty(vp, parsed_value);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private final static boolean shouldSetAsLocked(final VisualProperty vp) {
        if (vp.getTargetDataType() == CyNode.class) {
            if ((vp == BasicVisualLexicon.NODE_X_LOCATION) || (vp == BasicVisualLexicon.NODE_Y_LOCATION)
                    || (vp == BasicVisualLexicon.NODE_Z_LOCATION)) {
                return false;
            }
        }
        else if (vp.getTargetDataType() == CyNetwork.class) { // TODO //FIXME
            return !DIRECT_NET_PROPS_PATTERN.matcher(vp.getIdString()).matches();
        }
        return true;
    }

    private final static Class<?> toClass(final String type) {
        if (type.equals("string")) {
            return String.class;
        }
        else if (type.equals("integer")) {
            return Integer.class;
        }
        else if (type.equals("long")) {
            return Long.class;
        }
        else if (type.equals("double") || type.equals("float")) {
            return Double.class;
        }
        else if (type.equals("boolean")) {
            return Boolean.class;
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

    private final static Object toTypeValue(final String s, final String type) {
        if (type.equals("string")) {
            return s;
        }
        else if (type.equals("integer")) {
            return Double.valueOf(s);
        }
        else if (type.equals("long")) {
            return Double.valueOf(s);
        }
        else if (type.equals("double") || type.equals("float")) {
            return Double.valueOf(s);
        }
        else if (type.equals("boolean")) {
            return Boolean.valueOf(s);
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

}
