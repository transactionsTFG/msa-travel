package business.usecase.createtravelreservationairline;

import java.util.ArrayList;
import java.util.List;

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
import msa.commons.event.EventId;
import msa.commons.saga.SagaPhases;

@Stateless
public class CreateTravelAirlineReservationUseCaseImpl implements CreateTravelAirlineReservationUseCase{
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;
    @Override
    public boolean createTravelAirlineReservation(ReservationAirlineRequestDTO request) {
        if (request == null) 
            return false;
        
        TravelDTO travelDTO = new TravelDTO();
        travelDTO.setActive(false);
        travelDTO.setId(-1);
        travelDTO.setSagaPhases(SagaPhases.STARTED);
        long travelId = this.travelService.createTravel(travelDTO);
        if (travelId <= 0) 
            return false;
        
        CreateReservationCommand createReservationCommand = new CreateReservationCommand();
        List<IdFlightInstanceInfo> flightInstanceSeatsDTOs = new ArrayList<>();
        for (FlightInstanceSeatsDTO flightInstanceSeatsDTO : request.getListIdFlightInstanceSeats()) {
            IdFlightInstanceInfo f = new IdFlightInstanceInfo();
            f.setIdFlightInstance(flightInstanceSeatsDTO.getIdFlightInstance());
            f.setNumberSeats(flightInstanceSeatsDTO.getNumberSeats());
            flightInstanceSeatsDTOs.add(f);
        }
        CustomerInfo c = new CustomerInfo();
        c.setDni(request.getDni());
        createReservationCommand.setAllFlightBuy(false);
        createReservationCommand.setFlightInstanceInfo(flightInstanceSeatsDTOs);
        createReservationCommand.setCustomerInfo(c);
        createReservationCommand.setIdReservation(-1);
        createReservationCommand.setIdReservationTravel(travelId);
        this.eventHandlerRegistry.getHandler(EventId.USER_AGENCY_VALIDATE_USER_BEGIN).handleCommand(this.gson.toJson(createReservationCommand));
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
