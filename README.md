
## Acme Air Flight Service - Java/Liberty

An implementation of the Acme Air Flight Service for Java/Liberty. The primary task of the flight service is to retrieve flight route data.

This implementation can support running on a variety of runtime platforms including standalone bare metal system, Virtual Machines, docker containers, IBM Bluemix, IBM Bluemix Container Service, and IBM Spectrum CFC.

## Build Instructions
* Instructions for [setting up and building the codebase](Build_Instructions.md)


## Docker Instructions

See Documentation for the [Main Service](https://github.com/blueperf/acmeair-mainservice-java)


## IBM Container Instructions

See Documentation for the [Main Service](https://github.com/blueperf/acmeair-mainservice-java)

## Istio Instructions 

See Documentation for the [Main Service](https://github.com/blueperf/acmeair-mainservice-java)

## User-added Docs
Added REST call to get base cost (economy) and the bonus miles of a flight.
```
# expected response: cost and miles as json:
FLIGHTID=<idOfFlightInDB>
curl http://localhost/flight/getcostandmiles/$FLIGHTID
```

