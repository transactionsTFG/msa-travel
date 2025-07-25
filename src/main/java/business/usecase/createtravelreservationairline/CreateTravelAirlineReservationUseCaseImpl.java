package business.usecase.createtravelreservationairline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.commands.createreservation.CreateReservationCommand;
import msa.commons.commands.createreservation.model.CustomerInfo;
import msa.commons.commands.createreservation.model.IdFlightInstanceInfo;
import msa.commons.controller.agency.reservationairline.ReservationAirlineRequestDTO;
import msa.commons.controller.airline.reservation.create.FlightInstanceSeatsDTO;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.user.UserValidate;
import msa.commons.saga.SagaPhases;


@Stateless
public class CreateTravelAirlineReservationUseCaseImpl implements CreateTravelAirlineReservationUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;
    @Override
    public boolean createTravelAirlineReservation(ReservationAirlineRequestDTO request) {
        if (request == null) 
            return false;

        Map<Long, Integer> flightInstanceSeats = new HashMap<>();
        for (FlightInstanceSeatsDTO flightInstanceSeatsDTO : request.getListIdFlightInstanceSeats()) {
            if (flightInstanceSeatsDTO == null || flightInstanceSeatsDTO.getIdFlightInstance() <= 0 || flightInstanceSeatsDTO.getNumberSeats() <= 0) 
                return false;
            
            if (flightInstanceSeats.containsKey(flightInstanceSeatsDTO.getIdFlightInstance())) 
                flightInstanceSeats.put(flightInstanceSeatsDTO.getIdFlightInstance(), flightInstanceSeats.get(flightInstanceSeatsDTO.getIdFlightInstance()) + flightInstanceSeatsDTO.getNumberSeats());
            else 
                flightInstanceSeats.put(flightInstanceSeatsDTO.getIdFlightInstance(), flightInstanceSeatsDTO.getNumberSeats());
        
        }
        final String sagaId = UUID.randomUUID().toString();
        CreateReservationCommand createReservationCommand = new CreateReservationCommand();
        List<IdFlightInstanceInfo> listFlights = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : flightInstanceSeats.entrySet()) {
            Long idFlightInstance = entry.getKey();
            Integer numberSeats = entry.getValue();
            IdFlightInstanceInfo flightInstanceInfo = new IdFlightInstanceInfo();
            flightInstanceInfo.setIdFlightInstance(idFlightInstance);
            flightInstanceInfo.setNumberSeats(numberSeats);
            listFlights.add(flightInstanceInfo);
        }
        TravelDTO travelDTO = new TravelDTO();
            travelDTO.setActive(false);
            travelDTO.setId(-1);
            travelDTO.setSagaId(sagaId);
            travelDTO.setSagaPhases(SagaPhases.STARTED);
        long travelId = this.travelService.createTravel(travelDTO);          
        CustomerInfo c = new CustomerInfo();
        c.setDni(request.getDni());
        createReservationCommand.setIdTravelAgency(travelId);
        createReservationCommand.setAllFlightBuy(false);
        createReservationCommand.setFlightInstanceInfo(listFlights);
        createReservationCommand.setCustomerInfo(c);
        createReservationCommand.setIdReservation(-1);
        createReservationCommand.setIdUser(request.getIdUser());
        EventData eventData = new EventData(sagaId, UserValidate.CREATE_RESERVATION_AIRLINE, new ArrayList<>(), createReservationCommand, 1);
        this.eventHandlerRegistry.getHandler(EventId.VALIDATE_USER).handleCommand(this.gson.toJson(eventData));
        return true;
    }
    
    @Inject
    public void setTravelService(TravelService travelService) {
        this.travelService = travelService;
    }

    @Inject
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Inject
    public void setEventHandlerRegistry(EventHandlerRegistry eventHandlerRegistry) {
        this.eventHandlerRegistry = eventHandlerRegistry;
    }
}
