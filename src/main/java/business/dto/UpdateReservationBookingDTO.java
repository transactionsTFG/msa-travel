package business.dto;

import java.util.List;

import lombok.Data;

@Data
public class UpdateReservationBookingDTO {
    private long bookingId;
    private String customerDNI;
    private String startDate;
    private String endDate;
    private int numberOfNights;
    private Boolean withBreakfast;
    private int peopleNumber;
    private List<Long> roomsInfo;
    private long idReservation;
    private long idTravel;
    private List<FlightInstanceSeatsDTO> flightInstanceSeats;
}
