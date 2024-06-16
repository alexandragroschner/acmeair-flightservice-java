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
import jakarta.ws.rs.WebApplicationException;

// Payara and Helidon don't seem to support Date on a JAX-RS Param,
// so added this hack to convert to the Date.

public class DateParam {
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy");
  private static int year = Calendar.getInstance().get(Calendar.YEAR);
  private Date date;
  
  public DateParam( String dateTime ) throws WebApplicationException {

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
