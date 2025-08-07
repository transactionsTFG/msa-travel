package business.external.flight;

import java.time.LocalDate;

import javax.ws.rs.QueryParam;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlightParamsDTO {
    @QueryParam("countryOrigin")
    private String countryOrigin;
    
    @QueryParam("countryDestination")
    private String countryDestination;

    @QueryParam("cityOrigin")
    private String cityOrigin;
    
    @QueryParam("cityDestination")
    private String cityDestination;

    @QueryParam("dateOrigin")
    private LocalDate dateOrigin;
}
