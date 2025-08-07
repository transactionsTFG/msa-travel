package business.dto;

import java.time.LocalDate;

import javax.ws.rs.QueryParam;

import lombok.Data;

@Data
public class TravelInfo {
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

    @QueryParam("hotelName")
    private String hotelName;
}
