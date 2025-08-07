package business.external.flight;

import java.time.LocalTime;

import lombok.Data;

@Data
public class FlightDTO {
    private long id;
    private boolean active;
	private String codeFlight;
	private String weekDay;
	private LocalTime arrivalTime;
    private LocalTime departureTime;
    private Long idAircraft;
    private Long idOriginAirport;
    private Long idDestinationAirport;
    private String countryDestination;
}
