/*******************************************************************************
* Copyright (c) 2013-2017 IBM Corp.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*    http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.acmeair.service;

import com.acmeair.AirportCodeMapping;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public abstract class FlightService {
    
  @Inject 
  @ConfigProperty(name = "USE_FLIGHT_DATA_RELATED_CACHING", defaultValue="true") 
  protected Boolean useFlightDataRelatedCaching;
 
  private static final JsonReaderFactory factory = Json.createReaderFactory(null);
  protected static final Logger logger =  Logger.getLogger(FlightService.class.getName());  

  protected static ConcurrentHashMap<String, String> originAndDestPortToSegmentCache = 
          new ConcurrentHashMap<String,String>();
  protected static ConcurrentHashMap<String, List<String>> flightSegmentAndDataToFlightCache = 
          new ConcurrentHashMap<String,List<String>>();
  protected static ConcurrentHashMap<String, String> flightPKtoFlightCache = 
          new ConcurrentHashMap<String, String>();
  protected static ConcurrentHashMap<String, Long> flightSegmentIdtoRewardsCache = 
          new ConcurrentHashMap<String, Long>();
    
  @PostConstruct
  protected void init() {
    System.out.println("useFlightDataRelatedCaching : " + useFlightDataRelatedCaching);
  }
  
  /**
   * Get flight.
   */
  public String getFlightByFlightId(String flightId, String flightSegment) {
    try {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Book flights with " + flightId + " and " + flightSegment);
      }
      if (useFlightDataRelatedCaching) {
        String flight = flightPKtoFlightCache.get(flightId);
        if (flight == null) {
          flight = getFlight(flightId, flightSegment);
          if (flightId != null && flight != null) {
            flightPKtoFlightCache.putIfAbsent(flightId, flight);
          }
        }
        return flight;
      } else {
        return getFlight(flightId, flightSegment);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  
  protected abstract String getFlight(String flightId, String flightSegment);
  
  /**
   * Get Flight.
   */
  public List<String> getFlightByAirportsAndDepartureDate(String fromAirport,
          String toAirport, Date deptDate) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Search for flights from " + fromAirport + " to " + toAirport + " on " 
              + deptDate.toString());
    }

    String originPortAndDestPortQueryString = fromAirport + toAirport;
    String segment = null;
    if (useFlightDataRelatedCaching) {
      segment = originAndDestPortToSegmentCache.get(originPortAndDestPortQueryString);

      if (segment == null) {
        segment = getFlightSegment(fromAirport, toAirport);
        originAndDestPortToSegmentCache.putIfAbsent(originPortAndDestPortQueryString, segment);
      }
    } else {
      segment = getFlightSegment(fromAirport, toAirport);
    }
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Segment " + segment);
    }
    // cache flights that not available (checks against sentinel value above indirectly)
    try {
      if (segment == "") {
        return new ArrayList<String>();
      }
      
      JsonReader jsonReader = factory.createReader(new StringReader(segment));
      JsonObject segmentJson = jsonReader.readObject();
      jsonReader.close();
      
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Segment in JSON " + segmentJson);
      }
      
      String segId = segmentJson.getString("_id");
      if (segId == null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Segment is null");
        }
        
        return new ArrayList<String>(); 
      }

      String flightSegmentIdAndScheduledDepartureTimeQueryString = segId + deptDate.toString();
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("flightSegmentIdAndScheduledDepartureTimeQueryString " 
                + flightSegmentIdAndScheduledDepartureTimeQueryString);
      }
      if (useFlightDataRelatedCaching) {
        List<String> flights = flightSegmentAndDataToFlightCache
                .get(flightSegmentIdAndScheduledDepartureTimeQueryString);
        if (flights == null) {
          flights = getFlightBySegment(segment, deptDate);
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("flights search results if flights cache is null " + flights.toString());
          }

          flightSegmentAndDataToFlightCache.putIfAbsent(
                  flightSegmentIdAndScheduledDepartureTimeQueryString, flights);
        }
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("Returning " + flights);
        }
        return flights;
      } else {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("useFlightDataRelatedCaching is false ");
        }

        List<String> flights = getFlightBySegment(segment, deptDate);
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("Returning " + flights);
        }
        return flights;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Get flight by aiports. Not cached
   */
  public List<String> getFlightByAirports(String fromAirport, String toAirport) {
    String segment = getFlightSegment(fromAirport, toAirport);
    if (segment == null) {
      return new ArrayList<String>(); 
    }
    return getFlightBySegment(segment, null);
  }

  protected abstract String getFlightSegment(String fromAirport, String toAirport);
  
  protected abstract Long getRewardMilesFromSegment(String segmentId);
    
  protected abstract List<String> getFlightBySegment(String segment, Date deptDate);  
  
  public abstract void storeAirportMapping(AirportCodeMapping mapping);

  public abstract AirportCodeMapping createAirportCodeMapping(String airportCode, 
          String airportName);
  
  public abstract void createNewFlight(String flightSegmentId,
          Date scheduledDepartureTime, Date scheduledArrivalTime,
          int firstClassBaseCost, int economyClassBaseCost,
          int numFirstClassSeats, int numEconomyClassSeats,
          String airplaneTypeId);

  public abstract void storeFlightSegment(String flightSeg);
  
  public abstract void storeFlightSegment(String flightName, String origPort, String destPort, 
          int miles);
  
  public abstract Long countFlightSegments();
  
  public abstract Long countFlights();
  
  public abstract Long countAirports();

  public abstract void dropFlights();

  public abstract String getServiceType();
    
  /**
   * Get reward miles.
   */
  public Long getRewardMiles(String segmentId) {
        
    if (useFlightDataRelatedCaching) {
            
      Long miles = flightSegmentIdtoRewardsCache.get(segmentId);
            
      if (miles == null) {               
        miles = getRewardMilesFromSegment(segmentId);
        if (miles != null && miles != null) {
          flightSegmentIdtoRewardsCache.putIfAbsent(segmentId,miles);
        }
      }
      return miles;
    } else {
      return getRewardMilesFromSegment(segmentId);
    }
        
  }


  public abstract boolean isPopulated();
  
  public void invalidateFlightDataCaches() {
    if (useFlightDataRelatedCaching) {
     originAndDestPortToSegmentCache.clear(); 
     flightSegmentAndDataToFlightCache.clear();
     flightPKtoFlightCache.clear();
     flightSegmentIdtoRewardsCache.clear();
    }
  }

  public abstract boolean isConnected();

  //USER ADDED CODE
  public abstract Long getBaseCostById(String flightId);
  public abstract List<Long> getCostAndMilesById(String flightId);
}