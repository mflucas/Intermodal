package ch.ethz.matsim.r5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import com.conveyal.r5.api.util.StreetEdgeInfo;
import com.conveyal.r5.api.util.StreetSegment;
import com.conveyal.r5.profile.Path;
import com.conveyal.r5.streets.EdgeStore;
import com.conveyal.r5.streets.EdgeStore.Edge;
import com.conveyal.r5.streets.StreetLayer;
import com.conveyal.r5.transit.ExtendedTransitLayer;
import com.conveyal.r5.transit.RouteInfo;
import com.conveyal.r5.transit.RouteTopology;
import com.conveyal.r5.transit.TransitLayer;
import com.conveyal.r5.transit.TransportNetwork;
import com.conveyal.r5.transit.TripPattern;
import com.conveyal.r5.transit.TripSchedule;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;


//This class will write both the transit network and the walking/biking network for Switzerland. Cutting off the links outside the study area is done afterwards

public class RLnetworkwriter {

	public static void main(String[] args) throws Exception {
          
		  RLnetworkwriter.writenetwork(args);
	  }
	  
	public static void writenetwork (String[] args) throws Exception {

		File file = new File(args[0]);
		  
		TransportNetwork transportNetwork =  TransportNetwork.read(file);

		  
		    BufferedWriter writerTransit = new BufferedWriter(new FileWriter(args[1]));
		    BufferedWriter writerSlow = new BufferedWriter(new FileWriter(args[2]));
			writerTransit.write("RouteID;RouteIndex;Type;PatternID;TripID;ScheduleNo;Edge;DirectionID;FromStop;ToStop;FromX;FromY;ToX;ToY;TravelTime;DepartureTime;Headway");
		    writerSlow.write("LinkID,FromNode,ToNode,Length,Speed,fromLat,fromLon,toLat,toLon,OSMid"); 

		    writerTransit.newLine();
		    writerSlow.newLine();

			
	      
	      //get route ID for a tripPattern and then get the route info
	     String routeID_pattern = transportNetwork.transitLayer.tripPatterns.get(0).routeId;
	     int routeIndex_pattern = transportNetwork.transitLayer.tripPatterns.get(0).routeIndex;     
	     String routeID_route = transportNetwork.transitLayer.routes.get(0).route_id;
	     
	     int sizeOfRoutes = transportNetwork.transitLayer.routes.size();
	     int sizeOfTrips = transportNetwork.transitLayer.tripPatterns.size();
	     int sizeOfStops = transportNetwork.transitLayer.getStopCount();

	     int[] stopsInTripPrevious = null;
	     List<int[]> stopsArray = new ArrayList<int[]>();
	     int p = 0;
	    		int k=0;
	    		int r=0;

	    		//Initialize Link numbers
	    	     List<Integer> travelTimeLink = new ArrayList<>();
	    		
	    	    int largerThan1 = 0;
	    	    int departure = 0;
	    	    int depOther = 0;
	    	    int minDiff = 0;
	    	    String routeID ="";
	    	    String previousID ="";
	    	    int soma =0;
	    	    int seinao=0;
	    	    
	    		 List<String> routeIDs;
	    		 routeIDs= new ArrayList<>(transportNetwork.transitLayer.routes.size());
	    		 
	    		 System.out.println("has schedules "+ transportNetwork.transitLayer.hasSchedules);
	    		 System.out.println("has frequencies "+ transportNetwork.transitLayer.hasFrequencies);
	    		 
	    
	   	 int a=0;
//____________________________________________________________________________________________________________________________________________________________________________________//
	   	 // SECTION FOR WRITING THE TRANSIT NETWORK //
//____________________________________________________________________________________________________________________________________________________________________________________//

	     for(Iterator<TripPattern> patternIterator = transportNetwork.transitLayer.tripPatterns.iterator(); patternIterator.hasNext();) {
	    	 TripPattern pattern = patternIterator.next();
		    int routeIndex = pattern.routeIndex; 
		     int type = transportNetwork.transitLayer.routes.get(routeIndex).route_type;
	    	routeID = pattern.routeId; 
	    	List<LineString> geom = pattern.getHopGeometries(transportNetwork.transitLayer);
	    	int scheduleNo =1;
	    	ArrayList<Integer> depDiff = new ArrayList<Integer>();
	    	int headway = 0;
		    ArrayList<Integer> deeep = new ArrayList<Integer>();
		   	 int dep = 0;   

	    	
	    	
	    	for(Iterator<TripSchedule> scheduleIterator = pattern.tripSchedules.iterator(); scheduleIterator.hasNext(); ) {
	    		
		    int depAux=dep;
	    	TripSchedule schedule = scheduleIterator.next();
	    	
	    	int departure1 = schedule.departures[0];
	    	dep = schedule.departures[0];
	    	int annorlu = dep-depAux;
	    	if(annorlu !=0 && annorlu<7200)
	    	deeep.add(Math.abs(dep-depAux));
	    	
	    	//Add here another scheduleIterator to check the other departure times
	    	for(Iterator<TripSchedule> scheduleIterator2 = pattern.tripSchedules.iterator(); scheduleIterator2.hasNext();) {
	    		TripSchedule schedule2 = scheduleIterator2.next();
	    		int departure2 = schedule2.departures[0];

	    		if(departure1 != departure2) {
	    			if(Math.abs(departure2-departure1) !=0) {
	    			depDiff.add(Math.abs(departure2-departure1));
	    			}
	    		}	
	    	}
	    	try {
	    	headway = Collections.min(depDiff);
	    	} catch (NoSuchElementException e) {
	    		headway=-99;
	    	}
	    	
	    	
	    	for (Iterator<LineString> geomIt = geom.iterator(); geomIt.hasNext();) {
		    	int iteration=1;

	    	LineString gege = geomIt.next();
	    	double fromX = gege.getStartPoint().getX();
	    	double fromY = gege.getStartPoint().getY();
	    	double toX = gege.getEndPoint().getX();
	    	double toY = gege.getEndPoint().getY();
	    	int fromStop = pattern.stops[iteration-1];
	    	
	    	int toStop = pattern.stops[iteration];
	    	int travelTime = schedule.arrivals[iteration]-schedule.departures[iteration-1];
	    	int departureTime = schedule.departures[iteration-1];
	    	String tripId = schedule.tripId;
	    	
	    	double heaaad = calculateAverage(deeep);
	    	
	    	writerTransit.write(routeID + ";"+ routeIndex+ ";"+ type+ ";" + pattern.originalId + ";" +tripId + ";"+ scheduleNo+ ";" + iteration +";"+ pattern.directionId + ";" + fromStop +";" +toStop +";"+
	    	fromX + ";" + fromY + ";" + toX + ";" + toY + ";" + travelTime+ ";" +departureTime + ";" + heaaad);
	    	writerTransit.newLine();


	    	iteration++;
	    	scheduleNo++;
	    	}
	    	}		
	     }
	  
//____________________________________________________________________________________________________________________________________________________________________________________//
	   	 // SECTION FOR WRITING THE TRANSIT NETWORK :: END//
//____________________________________________________________________________________________________________________________________________________________________________________//

	     
	     //NOW WRITE OUT THE STREET NETWORK
			
			TIntIterator IDiterator = transportNetwork.streetLayer.edgeStore.fromVertices.iterator();

					  
			
			int edgecounter=0;
			for (int i=0; i<transportNetwork.streetLayer.edgeStore.nEdges();i++) {
				Edge edge=transportNetwork.streetLayer.edgeStore.getCursor(i);

				// Edge Flags is  ALLOWS_PEDESTRIAN ALLOWS_BIKE ALLOWS_CAR ALLOWS_WHEELCHAIR LINKABLE BIKE_LTS_4 //
				
			//  if (edge.getFlagsAsString().contains("ALLOWS_PEDESTRIAN") || edge.getFlagsAsString().contains("ALLOWS_BIKE")) {
				if (edge.getFlagsAsString().contains("ALLOWS_CAR")) {
				
					int LinkID = edge.getEdgeIndex();
					double length = edge.getLengthM();
					int from = edge.getFromVertex();
					int to =edge.getToVertex(); 
					float speed = edge.getCarSpeedMetersPerSecond();
					int toLat = transportNetwork.streetLayer.vertexStore.getCursor(to).getFixedLat();
					int toLon = transportNetwork.streetLayer.vertexStore.getCursor(to).getFixedLon();
					int fromLat = transportNetwork.streetLayer.vertexStore.getCursor(from).getFixedLat();
					int fromLon = transportNetwork.streetLayer.vertexStore.getCursor(from).getFixedLon();
					
					boolean test=false;
					if(toLat>=47.3072*10000000) {
						test=true;
					}
					

					
					
					
					
					if((toLat>47.2960*10000000 && toLat<47.4940*10000000) && (toLon>8.3019*10000000 && toLon<8.6541*10000000)) {
					System.out.println("Edge index is "+ LinkID+ " iteration is "+ i +" waytag is ");
					 writerSlow.write(LinkID +","+ from +","+ to +","+ length +","+ speed+ "," + fromLat + ","+ fromLon+ "," + toLat + "," + toLon + ",");
					  	
					 
					 writerSlow.newLine();
					 edgecounter += 1;
					}
					}
			}
			
		
			

			writerSlow.flush();
			writerSlow.close();
			writerTransit.flush();
			writerTransit.close();
		   
	  }
	
	private static double calculateAverage(List <Integer> marks) {
		  Integer sum = 0;
		  if(!marks.isEmpty()) {
		    for (Integer mark : marks) {
		        sum += mark;
		    }
		    return sum.doubleValue() / marks.size();
		  }
		  return sum;
		}
	
}
