/*******************************************************************************
 * Copyright (c) 2020 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.acmeair.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;

// Payara and Helidon don't seem to support Date on a JAX-RS Param,
// so added this hack to convert to the Date.

public class DateParam {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy");
  private static int year = Calendar.getInstance().get(Calendar.YEAR);
  private static final int ZERO = 48;
  private static final Logger logger = Logger.getLogger(DateParam.class.getName());
  private Date date;
  
  public DateParam( String dateTime ) throws WebApplicationException {

      logger.warning("DATE TIME received: " + dateTime);
      /*
      String dateOnly;

      if (dateTime.charAt(11) == ZERO) {
          logger.warning("is in 11 if");
          // TODO: never goes into this if - fix it
          if (dateTime.charAt(24) == 'S') {
              logger.warning("is in 24 if");
              // Assume format is EEE MMM dd 00:00:00 CEST yyyy from jmeter. Chop off the time + timezone.
              dateOnly = dateTime.substring(0,10) + " " + dateTime.substring(24,29);
              logger.warning("substring: " + dateOnly);
          } else {
              logger.warning("is in 11 if ELSE");
              // Assume format is EEE MMM dd 00:00:00 z yyyy from jmeter. Chop off the time + timezone.
              dateOnly = dateTime.substring(0,10) + " " + dateTime.substring(24,28);
              logger.warning("substring: " + dateOnly);
          }
      }
      else {
        // Assume format is EEE MMM dd yyyy from the browser.
        dateOnly = dateTime.substring(0,15);
      }

      LocalDate localDate = LocalDate.parse(dateOnly, formatter);
      date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
      */

      // new solution:
      // A bit of hack to get the date in the correct format
      String dateOnly = dateTime.substring(0,10) + " " + year;

      LocalDate localDate = LocalDate.parse(dateOnly, formatter);
      date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());


  }

  public Date getDate() {
    return date;
  } 
}
