DataExtract
===========

This extracts search data from a specified url.
   
This project is to extract data from AirAsia.com to get flights information, specifically, the trip from [A] to [B] (roundtrip and one way) on specific date. 
   Necessary information to be extracted:
   
	1. Depart and arrive information (from/to, date, time, etc) of flights options.
	2. Fare value.
	
Requirements:
-------------
This project is dependant on jsoup-1.6.0.jar . So please keep this jar in the class path before running the class.


Input :
--------
Input values are hard coded in the FlightDetailsExtract. You can change the values as per the requirement.
Current input is to fetch round Trip data between Bangalore and Cochin.
 isRoundTrip --> Should be OneWay or RoundTrip.
 originStation--> Station code of Origin Station (Get from http:-->www.airportcodes.org/).
 destinationStation--> Station code of Destination Station(Get from http:-->www.airportcodes.org/).
 leavingDate -->Date on which we need to fly from originStation to destinationStation
 arrivalDate -->Date on which we need to fly from destinationStation to originStation. In case of one way trip, this will be by default 2 days before the leaving date
 numberOfAdults -->Number of Adults travelling
 numberOfChildren -->Number of children travelling
 numberOfInfants -->Number of Infants travelling
 
 Output : 
 ---------
 Out put is getting displayed in console now. 