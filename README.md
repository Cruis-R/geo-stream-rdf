# geo-stream-rdf
Microservice for reading a flux of geographic position from a SIM + GPS card and writing the data to SPARQL + LDP server

## Launch
From Scala REPL:
```scala
cruisR.geo_stream_rdf.TCPDump.main(Array() )
```

## Example of data received:

```
170524170838,+33689162952,GPRMC,160838.000,A,4850.2382,N,00220.0353,E,000.0,000.0,240517,,,A*68,L,, imei:863977030715952,06,76.8,F:4.14V,1,139,4305,208,01,0300,4679
```

The CSV headers are:
```
imei,date,latitudeDecimalDegree,longitudeDecimalDegree,batteryStatus,chargingStatus,speedNauticalMiles,angle,altitude,eventType
```

