/*******************************************************************************
 * Copyright (c) 2013 IBM Corp.
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
package com.acmeair.web;

import com.acmeair.service.FlightService;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;

@Path("/")
@ApplicationScoped
public class FlightServiceRest {

  @Inject
  FlightService flightService;

  @Inject
  @ConfigProperty(name = "TARGET_SUCCESS_RATIO_FOR_AUDIT", defaultValue = "34")
  Integer TARGET_SUCCESS_RATIO_FOR_AUDIT;

  @Inject
  @ConfigProperty(name = "TOLERANCE_FOR_AUDIT", defaultValue = "1")
  Integer TOLERANCE_FOR_AUDIT;
 
  private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);
  private static final JsonBuilderFactory jsonObjectFactory  = Json.createBuilderFactory(null);

  private static final AtomicLong SEARCHES = new AtomicLong(0);
  private static final AtomicLong SUCCESS = new AtomicLong(0);

  /**
   * Get flights.
   */

  @POST
  @Path("/queryflights")
  @Consumes({"application/x-www-form-urlencoded"})
  @Produces("application/json")
  @Timed(name = "com.acmeair.web.FlightServiceRest.getTripFlights", tags = "app=acmeair-flightservice-java")
  public JsonObject getTripFlights(
      @FormParam("fromAirport") String fromAirport,
      @FormParam("toAirport") String toAirport,
      @FormParam("fromDate") DateParam fromDate,
      @FormParam("returnDate") DateParam returnDate,
      @FormParam("oneWay") boolean oneWay
      ) throws ParseException {

    if (!flightService.isPopulated()) {
      throw new RuntimeException("Flight DB has not been populated");
    }
        
    return getFlightOptions(fromAirport,toAirport,fromDate.getDate(),returnDate.getDate(),oneWay);
  }

  /**
   * Get reward miles for flight segment.
   */
  @POST
  @Path("/getrewardmiles")
  @Consumes({"application/x-www-form-urlencoded"})
  @Produces("application/json")
  @Timed(name = "com.acmeair.web.FlightServiceRest.getRewardsMiles", tags = "app=acmeair-flightservice-java")
  public MilesResponse getRewardMiles(
      @FormParam("flightSegment") String segmentId
      ) {
    Long miles = flightService.getRewardMiles(segmentId); 

    return new MilesResponse(miles);
  }

  @GET
  @Path("/status")
  public Response status() {
    return Response.ok("OK").build();
  } 

  // Audit the number of searches and successes. Differences between time zones of the client 
  // and server can sometimes lead to different success ratios.
  // This is a way to check.

  @GET
  @Path("/searches")
  public Response searches() {
    return Response.ok(SEARCHES.get()).build();
  }

  @GET
  @Path("/successes")
  public Response successes() {
    return Response.ok(SUCCESS.get()).build();
  }

  // Returns percentage of searches that are successful
  @GET
  @Path("/successratio")
  public Response successratio() {
    if (SEARCHES.get() == 0) {
      return Response.ok(0).build();
    }

    double ratio = (double) (SUCCESS.get() * 100) / SEARCHES.get();
    return Response.ok(new BigDecimal(ratio).setScale(1, RoundingMode.HALF_UP).doubleValue()).build();
  }

  @GET
  @Path("/audit")
  public Response audit() {
    if (SEARCHES.get() == 0) {
      return Response.ok("fail").build();
    }

    double ratio = (double) (SUCCESS.get() * 100) / SEARCHES.get();
    if (ratio > TARGET_SUCCESS_RATIO_FOR_AUDIT - TOLERANCE_FOR_AUDIT && ratio < TARGET_SUCCESS_RATIO_FOR_AUDIT + TOLERANCE_FOR_AUDIT) {
      return Response.ok("pass").build();
    }

    return Response.ok("fail").build();
  }

  private JsonObject getFlightOptions(String fromAirport, String toAirport, Date fromDate, 
      Date returnDate, boolean oneWay) {

    // Get list of toflights as Json Array
    List<String> toFlights = flightService.getFlightByAirportsAndDepartureDate(fromAirport, 
        toAirport, fromDate);
    JsonArray toFlightsJsonArray = convertFlightListToJsonArray(toFlights);

    // audit toFlights
    SEARCHES.incrementAndGet();
    if (toFlights.size() > 0) {
      SUCCESS.incrementAndGet();
    }

    JsonObject options;

    if (oneWay) {
      options = jsonObjectFactory.createObjectBuilder()
          .add("tripFlights", jsonObjectFactory.createArrayBuilder()
              .add(jsonObjectFactory.createObjectBuilder()
                  .add("numPages", 1)
                  .add("flightsOptions", toFlightsJsonArray)
                  .add("currentPage", 0)
                  .add("hasMoreOptions", false)
                  .add("pageSize", 10)))
          .add("tripLegs", 1)
          .build();
    } else { 
      // Get list of returnflights as Json Array
      List<String> retFlights = flightService.getFlightByAirportsAndDepartureDate(toAirport, 
          fromAirport, returnDate);
      JsonArray retFlightsJsonArray = convertFlightListToJsonArray(retFlights);

      // audit retFlights
      SEARCHES.incrementAndGet();
      if (retFlights.size() > 0) {
        SUCCESS.incrementAndGet();
      }

      options = jsonObjectFactory.createObjectBuilder()
          .add("tripFlights", jsonObjectFactory.createArrayBuilder()
              .add(jsonObjectFactory.createObjectBuilder()
                  .add("numPages", 1)
                  .add("flightsOptions", toFlightsJsonArray)
                  .add("currentPage", 0)
                  .add("hasMoreOptions", false)
                  .add("pageSize", 10))
              .add(jsonObjectFactory.createObjectBuilder()
                  .add("numPages", 1)
                  .add("flightsOptions", retFlightsJsonArray)
                  .add("currentPage", 0)
                  .add("hasMoreOptions", false)
                  .add("pageSize", 10)))
          .add("tripLegs", 2)
          .build();
    }
    
    return options;
  }

  private JsonArray convertFlightListToJsonArray(List<String> flights) {

    if (flights == null) {
      // empty array
      return jsonObjectFactory.createArrayBuilder().build();
    }

    JsonReader jsonReader = jsonReaderFactory.createReader(new StringReader(flights.toString()));
    JsonArray  flightsJsonArray = jsonReader.readArray();
    jsonReader.close();

    return flightsJsonArray;
  }

  //USER ADDED CODE
  @GET
  @Path("getcostandmiles/{flightId}")
  @Produces("application/json")
  public CostAndMilesResponse getCostAndMiles(@PathParam("flightId") String flightId) {
    List<Long> costAndMiles = flightService.getCostAndMilesById(flightId);

    return new CostAndMilesResponse(costAndMiles.get(0), costAndMiles.get(1));
  }
}
