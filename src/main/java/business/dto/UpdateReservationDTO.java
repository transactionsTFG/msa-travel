package business.dto;

import java.util.List;

import lombok.Data;

@Data
public class UpdateReservationDTO {
    private long idReservation;
    private long idTravel;
    private List<FlightInstanceSeatsDTO> flightInstanceSeats;
}
