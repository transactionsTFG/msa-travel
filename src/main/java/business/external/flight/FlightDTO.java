package business.external.flight;

import java.time.LocalDate;


import lombok.Data;

@Data
public class FlightDTO {
    private long id;
	private LocalDate departureDate;
	private LocalDate arrivalDate;
	private String statusFlight;
	private int passengerCounter;
	private double price;
	private boolean active;
	private long idFlight;
	private long idAircraft;
	private String weekDay;
	private String cityDestination;
	private String countryOrigin;
	private String countryDestination;
}
