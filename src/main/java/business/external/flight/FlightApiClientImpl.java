package business.external.flight;

import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@Stateless
public class FlightApiClientImpl implements FlightApiClient {
    private Client client = ClientBuilder.newClient();
    private static final String PATH = "http://localhost:9001/msa-flight/api/flights";

    @Override
    public List<FlightDTO> getFlightsByParams(FlightParamsDTO flightParamsDTO) {
        Response r = this.client.target(PATH + "/flight-info")
                .queryParam("countryOrigin", flightParamsDTO.getCountryOrigin())
                .queryParam("countryDestination", flightParamsDTO.getCountryDestination())
                .queryParam("cityOrigin", flightParamsDTO.getCityOrigin())
                .queryParam("cityDestination", flightParamsDTO.getCityDestination())
                .queryParam("dateOrigin", flightParamsDTO.getDateOrigin())
                .request()
                .get();
        if (r.getStatus() == 200) {
            return r.readEntity(new GenericType<List<FlightDTO>>() {});
        }

        return Collections.emptyList();
    }

}
