package org.matsim.pt2matsim.gtfs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.geometry.transformations.CH1903LV03PlustoWGS84;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.pt2matsim.gtfs.lib.Service;
import org.matsim.pt2matsim.gtfs.lib.Stop;
import org.matsim.pt2matsim.gtfs.lib.StopImpl;
import org.matsim.pt2matsim.tools.GtfsTools;
import org.matsim.pt2matsim.tools.ScheduleToolsTest;
import org.matsim.pt2matsim.tools.ShapeToolsTest;
import org.matsim.pt2matsim.tools.lib.RouteShape;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author polettif
 */
public class GtfsFeedImplTest {

	private GtfsFeed feed;

	@Before
	public void prepare() {
		feed = new GtfsFeedImpl("test/gtfs-feed/");
	}

	@Test
	public void compareShapes() {
		feed.transform(TransformationFactory.CH1903_LV03_Plus);
		for(Map.Entry<Id<RouteShape>, RouteShape> entry : ShapeToolsTest.initShapes().entrySet()) {
			RouteShape feedShape = feed.getShapes().get(entry.getKey());
			for(Map.Entry<Integer, Coord> coordEntry : entry.getValue().getCoordsSorted().entrySet()) {
				Assert.assertEquals(coordEntry.getValue(), feedShape.getCoordsSorted().get(coordEntry.getKey()));
			}
		}
	}

	@Test
	public void transformData() {
		CH1903LV03PlustoWGS84 ct = new CH1903LV03PlustoWGS84();
		for(Map.Entry<Id<RouteShape>, RouteShape> entry : ShapeToolsTest.initShapes().entrySet()) {
			for(Map.Entry<Integer, Coord> integerCoordEntry : entry.getValue().getCoordsSorted().entrySet()) {
				Coord c = ct.transform(integerCoordEntry.getValue());
				// System.out.println(entry.getKey().toString() + "," + c.getX() + "," + c.getY() + "," + integerCoordEntry.getKey());
			}
		}
		for(TransitStopFacility stopFacility : ScheduleToolsTest.initUnmappedSchedule().getFacilities().values()) {
			Coord c = ct.transform(stopFacility.getCoord());
			System.out.println(stopFacility.getId().toString() + "," + c.getY() + "," + c.getX());
		}
	}

	@Test
	public void statistics() {
		Assert.assertEquals(6, feed.getStops().size());
		Assert.assertEquals(2, feed.getRoutes().size());
		Assert.assertEquals(4, feed.getServices().size());
		Assert.assertEquals(3, feed.getShapes().size());
		Assert.assertEquals(6, feed.getTrips().size());
		Assert.assertEquals(2, feed.getTransfers().size());
		Assert.assertEquals(TransformationFactory.WGS84, feed.getCurrentCoordSystem());
		Assert.assertTrue(feed.usesFrequencies());
	}

	@Test
	public void stopsEqualAfterTransform() {
		Stop testStop = feed.getStops().values().stream().findFirst().
				<Stop>map(s -> new StopImpl(s.getId(), s.getName(), s.getLon(), s.getLat(), s.getLocationType(), s.getParentStationId())).
				orElse(null);
		Assert.assertNotNull(testStop);
		feed.transform(TransformationFactory.ATLANTIS);
		Assert.assertEquals(testStop, feed.getStops().get(testStop.getId()));
		Assert.assertEquals(TransformationFactory.ATLANTIS, feed.getCurrentCoordSystem());
	}

	@Test
	public void coordEqualAfterRetransform() {
		Map<String, Coord> stopCoords1 = cloneFeedStopCoords();

		// transform
		feed.transform(TransformationFactory.CH1903_LV03_Plus);
		Map<String, Coord> stopCoords2 = cloneFeedStopCoords();
		testCoordsEqual(stopCoords1, stopCoords2, false);

		// retransform
		feed.transform(TransformationFactory.WGS84);
		Map<String, Coord> stopCoords3 = cloneFeedStopCoords();
		testCoordsEqual(stopCoords1, stopCoords3, true);
	}

	private Map<String, Coord> cloneFeedStopCoords() {
		Map<String, Coord> cloned = new HashMap<>();
		for(Stop stop : this.feed.getStops().values()) {
			cloned.put(stop.getId(), new Coord(stop.getCoord().getX(), stop.getCoord().getY()));
		}
		return cloned;
	}

	private void testCoordsEqual(Map<String, Coord> stopCoordsExpected, Map<String, Coord> stopCoordsActual, boolean expectedBool) {
		double delta = 0.000001;
		for(Map.Entry<String, Coord> entry : stopCoordsActual.entrySet()) {
			Coord orig = stopCoordsExpected.get(entry.getKey());
			Coord compare = entry.getValue();

			if(expectedBool) {
				Assert.assertEquals(orig.getX(), compare.getX(), delta);
				Assert.assertEquals(orig.getY(), compare.getY(), delta);
			} else {
				Assert.assertNotEquals(orig.getX(), compare.getX(), delta);
				Assert.assertNotEquals(orig.getY(), compare.getY(), delta);
			}
		}
	}

	@Test
	public void testServices() {
		Service emptService = feed.getServices().get("EMPT");
		Assert.assertEquals(1, emptService.getAdditions().size());
		Assert.assertEquals(1, emptService.getExceptions().size());
		Assert.assertEquals(LocalDate.of(2018, 10, 2), emptService.getAdditions().first());
		Assert.assertEquals(LocalDate.of(2018, 10, 1), emptService.getExceptions().first());
		Assert.assertEquals(LocalDate.of(2018, 10, 7), emptService.getEndDate());

		Set<LocalDate> covered = new TreeSet<>();
		covered.add(LocalDate.of(2018, 10, 2));
		covered.add(LocalDate.of(2018, 10, 3));
		covered.add(LocalDate.of(2018, 10, 4));
		covered.add(LocalDate.of(2018, 10, 6));
		Assert.assertEquals(covered, emptService.getCoveredDays());
	}

	@Test
	public void gtfsShapesGeojson() {
		GtfsTools.writeShapesToGeojson(feed, "test/shapes.geojson");
		new File("test/shapes.geojson").delete();
	}


}