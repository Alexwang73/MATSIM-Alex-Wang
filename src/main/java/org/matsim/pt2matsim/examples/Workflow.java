package org.matsim.pt2matsim.examples;

import org.matsim.api.core.v01.network.Network;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt2matsim.config.OsmConverterConfigGroup;
import org.matsim.pt2matsim.config.PublicTransitMappingConfigGroup;
import org.matsim.pt2matsim.mapping.PTMapper;
import org.matsim.pt2matsim.run.CheckMappedSchedulePlausibility;
import org.matsim.pt2matsim.run.Gtfs2TransitSchedule;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;
import org.matsim.pt2matsim.run.gis.Network2Geojson;
import org.matsim.pt2matsim.run.gis.Schedule2Geojson;
import org.matsim.pt2matsim.tools.NetworkTools;
import org.matsim.pt2matsim.tools.ScheduleTools;

/**
 * This is an example workflow using config files. The network and schedule files are placeholders and
 * not part of the GitHub repository
 *
 * @author polettif
 */
public class Workflow {

	public static void main(String[] args) {
		// Convert Network
		OsmConverterConfigGroup osmConfig = OsmConverterConfigGroup.loadConfig("C:/Users/wangc/IdeaProjects/pt2matsim/src/main/java/org/matsim/pt2matsim/examples/osm_config.xml");
		Osm2MultimodalNetwork.run(osmConfig); // or just: Osm2MultimodalNetwork.run("osm2matsimConfig.xml");


	}
}
