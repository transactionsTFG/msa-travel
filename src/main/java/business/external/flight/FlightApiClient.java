package business.external.flight;

import java.util.List;

public interface FlightApiClient {
    List<FlightDTO> getFlightsByParams(FlightParamsDTO flightParamsDTO);
}
