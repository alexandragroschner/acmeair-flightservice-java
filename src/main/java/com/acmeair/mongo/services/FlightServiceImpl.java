/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
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

package com.acmeair.mongo.services;

import com.acmeair.AirportCodeMapping;
import com.acmeair.service.FlightService;
import com.acmeair.service.KeyGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import org.bson.Document;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
public class FlightServiceImpl extends FlightService {

  private static final Logger logger = Logger.getLogger(FlightServiceImpl.class.getName());
  private static final JsonReaderFactory factory = Json.createReaderFactory(null);

  private MongoCollection<Document> flight;
  private MongoCollection<Document> flightSegment;
  private MongoCollection<Document> airportCodeMapping;

  private Boolean isPopulated = false;

  @Inject
  KeyGenerator keyGenerator;

  @Inject
  MongoDatabase database;

  /**
   * Init mongo db.
   */
  @PostConstruct
  public void initialization() {
    flight = database.getCollection("flight");
    flightSegment = database.getCollection("flightSegment");
    airportCodeMapping = database.getCollection("airportCodeMapping");
  }

  @Override
  public Long countFlights() {
    /* REMOVED DB CALL
    return flight.countDocuments();
     */
    return 1L;
  }

  @Override
  public Long countFlightSegments() {
    /* REMOVED DB CALL
    return flightSegment.countDocuments();
     */
    return 1L;
  }

  @Override
  public Long countAirports() {
    /* REMOVED DB CALL
    return airportCodeMapping.countDocuments();
     */
    return 1L;
  }

  // function is never called, no need to remove DB call
  protected String getFlight(String flightId, String segmentId) {
    return flight.find(eq("_id", flightId)).first().toJson();
  }

  @Override
  protected  String getFlightSegment(String fromAirport, String toAirport) {
    try {
      /* REMOVED DB CALL
      return flightSegment.find(new BasicDBObject("originPort", fromAirport)
          .append("destPort", toAirport)).first().toJson();
       */
      // ADDED HARD-CODED SEGMENT
      Document flightSegmentDoc = new Document("_id", "AA0")
              .append("originPort", fromAirport)
              .append("destPort", toAirport)
              .append("miles", 4258);

      return flightSegmentDoc.toJson();

    } catch (java.lang.NullPointerException e) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("getFlghtSegment returned no flightSegment available");
      }
      return "";
    }
  }

  @Override
  protected  Long getRewardMilesFromSegment(String segmentId) {
    try {
      /* REMOVED DB CALL
      String segment = flightSegment.find(new BasicDBObject("_id", segmentId)).first().toJson();
       */

      // ADDED HARD-CODED SEGMENT
      Document flightSegmentDoc = new Document("_id", "AA0")
              .append("originPort", "AMS")
              .append("destPort", "BOM")
              .append("miles", 4258);

      JsonReader jsonReader = factory.createReader(new StringReader(flightSegmentDoc.toJson()));
      JsonObject segmentJson = jsonReader.readObject();
      jsonReader.close();

      return segmentJson.getJsonNumber("miles").longValue();

    } catch (java.lang.NullPointerException e) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("getFlghtSegment returned no flightSegment available");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected  List<String> getFlightBySegment(String segment, Date deptDate) {
    try {
      JSONObject segmentJson = (JSONObject) new JSONParser().parse(segment);
      MongoCursor<Document> cursor;
      List<String> flights =  new ArrayList<String>();

      if (deptDate != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("getFlghtBySegment Search String : "
              + new BasicDBObject("flightSegmentId", segmentJson.get("_id"))
              .append("scheduledDepartureTime", deptDate).toJson());
        }
        /* REMOVED DB CALL
        cursor = flight.find(new BasicDBObject("flightSegmentId", segmentJson.getString("_id"))
            .append("scheduledDepartureTime", deptDate)).iterator();
         */
        // ADDED HARD-CODED FLIGHT
        Document flightDoc = new Document("_id", "b7e3b028-7248-4763-b2a4-1b9c502664a1")
                .append("firstClassBaseCost", 500)
                .append("economyClassBaseCost", 200)
                .append("numFirstClassSeats", 10)
                .append("numEconomyClassSeats", 200)
                .append("airplaneTypeId", "B747")
                .append("flightSegmentId", "AA3")
                .append("scheduledDepartureTime", deptDate.toString())
                .append("scheduledArrivalTime", deptDate.toString())
                .append("flightSegment", segmentJson);

        flights.add(flightDoc.toJson());
        flights.add(flightDoc.toJson());

      } else {
        /* REMOVED DB CALL
        cursor = flight.find(eq("flightSegmentId", segmentJson.getString("_id"))).iterator();
         */

        // ADDED HARD-CODED FLIGHT
        Document flightDoc = new Document("_id", "b7e3b028-7248-4763-b2a4-1b9c502664a1")
                .append("firstClassBaseCost", 500)
                .append("economyClassBaseCost", 200)
                .append("numFirstClassSeats", 10)
                .append("numEconomyClassSeats", 200)
                .append("airplaneTypeId", "B747")
                .append("flightSegmentId", "AA3")
                .append("scheduledDepartureTime", "ISODate(\"2024-04-02T00:00:00.000Z\")")
                .append("scheduledArrivalTime", "ISODate(\"2024-04-02T14:15:00.000Z\")")
                .append("flightSegment", segmentJson);

        flights.add(flightDoc.toJson());
      }

      /* REMOVED DB CALL
      try {
        while (cursor.hasNext()) {
          Document tempDoc = cursor.next();

          if (logger.isLoggable(Level.FINE)) {
            logger.fine("getFlghtBySegment Before : " + tempDoc.toJson());
          }

          Date deptTime = (Date)tempDoc.get("scheduledDepartureTime");
          Date arvTime = (Date)tempDoc.get("scheduledArrivalTime");
          tempDoc.remove("scheduledDepartureTime");
          tempDoc.append("scheduledDepartureTime", deptTime.toString());
          tempDoc.remove("scheduledArrivalTime");
          tempDoc.append("scheduledArrivalTime", arvTime.toString());
          tempDoc.append("flightSegment", BasicDBObject.parse(segment));


          if (logger.isLoggable(Level.FINE)) {
            logger.fine("getFlghtBySegment after : " + tempDoc.toJson());
          }

          flights.add(tempDoc.toJson());

        }
      } finally {
        cursor.close();
      }
      */
      return flights;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }


  @Override
  public void storeAirportMapping(AirportCodeMapping mapping) {
    Document airportDoc = new Document("_id", mapping.getAirportCode())
        .append("airportName", mapping.getAirportName());
    airportCodeMapping.insertOne(airportDoc);
  }

  @Override
  public AirportCodeMapping createAirportCodeMapping(String airportCode, String airportName) {
    return new AirportCodeMapping(airportCode,airportName);
  }

  @Override
  public void createNewFlight(String flightSegmentId,
      Date scheduledDepartureTime, Date scheduledArrivalTime,
      int firstClassBaseCost, int economyClassBaseCost,
      int numFirstClassSeats, int numEconomyClassSeats,
      String airplaneTypeId) {
    String id = keyGenerator.generate().toString();
    Document flightDoc = new Document("_id", id)
        .append("firstClassBaseCost", firstClassBaseCost)
        .append("economyClassBaseCost", economyClassBaseCost)
        .append("numFirstClassSeats", numFirstClassSeats)
        .append("numEconomyClassSeats", numEconomyClassSeats)
        .append("airplaneTypeId", airplaneTypeId)
        .append("flightSegmentId", flightSegmentId)
        .append("scheduledDepartureTime", scheduledDepartureTime)
        .append("scheduledArrivalTime", scheduledArrivalTime);

    flight.insertOne(flightDoc);
  }

  @Override
  public void storeFlightSegment(String flightSeg) {
    try {
      JsonReader jsonReader = factory.createReader(new StringReader(flightSeg));
      JsonObject flightSegJson = jsonReader.readObject();
      jsonReader.close();
      storeFlightSegment(flightSegJson.getString("_id"),
          flightSegJson.getString("originPort"),
          flightSegJson.getString("destPort"),
          flightSegJson.getInt("miles"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void storeFlightSegment(String flightName, String origPort, String destPort, int miles) {
    Document flightSegmentDoc = new Document("_id", flightName)
        .append("originPort", origPort)
        .append("destPort", destPort)
        .append("miles", miles);

    flightSegment.insertOne(flightSegmentDoc);
  }

  @Override
  public void dropFlights() {
    airportCodeMapping.deleteMany(new Document());
    flightSegment.deleteMany(new Document());
    flight.deleteMany(new Document());
  }

  @Override
  public String getServiceType() {
    return "mongo";
  }

  @Override
  public boolean isPopulated() {
    /* REMOVED DB CALL
    if (isPopulated) {
      return true;
    }

    if (flight.countDocuments() > 0) {
      isPopulated = true;
      return true;
    } else {
      return false;
    }
     */
    return true;
  }

  @Override
  public boolean isConnected() {
    /* REMOVED DB CALL
    return (flight.countDocuments() >= 0);
     */
    return true;
  }

  //USER ADDED CODE

  /**
   * Method to find base economy cost of a flight in the service's database corresponding to the passed flight id.
   * @param flightId unique key of flight in db
   * @return base economy cost of the flight
   */
  @Override
  public Long getBaseCostById(String flightId) {
    /* REMOVED DB CALL
    String flightById = flight.find(eq("_id", flightId)).first().toJson();
     */

    // ADDED HARD-CODED FLIGHT
    Document flightDoc = new Document("_id", "cad686d7-a1d3-4666-a6bd-33612cdee146")
            .append("firstClassBaseCost", 300)
            .append("economyClassBaseCost", 200)
            .append("numFirstClassSeats", 20)
            .append("numEconomyClassSeats", 100)
            .append("airplaneTypeId", "B747")
            .append("flightSegmentId", "AA3")
            .append("scheduledDepartureTime", new Date().toString())
            .append("scheduledArrivalTime", new Date().toString());

    JsonReader jsonReader = factory.createReader(new StringReader(flightDoc.toJson()));
    JsonObject flightByIdJson = jsonReader.readObject();
    jsonReader.close();

    return flightByIdJson.getJsonNumber("economyClassBaseCost").longValue();
  }

  /**
   * Method to find base economy cost and bonus miles of a flight in the service's database
   * corresponding to the passed flight id.
   * @param flightId unique key of flight in db
   * @return base economy cost and bonus miles of the flight
   */
  @Override
  public List<Long> getCostAndMilesById(String flightId) {
    /* REMOVED DB CALL
    //search flight by ID
    String flightById = flight.find(eq("_id", flightId)).first().toJson();
     */

    // ADDED HARD-CODED FLIGHT
    Document flightDoc = new Document("_id", flightId)
            .append("firstClassBaseCost", 300)
            .append("economyClassBaseCost", 200)
            .append("numFirstClassSeats", 20)
            .append("numEconomyClassSeats", 100)
            .append("airplaneTypeId", "B747")
            .append("flightSegmentId", "AA3")
            .append("scheduledDepartureTime", new Date().toString())
            .append("scheduledArrivalTime", new Date().toString());

    //create Json Object from response
    JsonReader jsonReader = factory.createReader(new StringReader(flightDoc.toJson()));
    JsonObject flightByIdJson = jsonReader.readObject();
    jsonReader.close();

    List<Long> costAndMiles = new ArrayList<>();
    costAndMiles.add(flightByIdJson.getJsonNumber("economyClassBaseCost").longValue());
    costAndMiles.add(getRewardMiles(flightByIdJson.getJsonString("flightSegmentId").getString()));

    return costAndMiles;
  }
}
