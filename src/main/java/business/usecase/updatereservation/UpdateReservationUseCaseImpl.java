package business.usecase.updatereservation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.dto.FlightInstanceSeatsDTO;
import business.dto.UpdateReservationDTO;
import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.commands.modifyreservation.UpdateReservationCommand;
import msa.commons.commands.modifyreservation.model.IdUpdateFlightInstanceInfo;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.UpdateReservation;
import msa.commons.saga.SagaPhases;

@Stateless
public class UpdateReservationUseCaseImpl implements IUpdateReservationUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;

    @Override
    public boolean updateReservation(UpdateReservationDTO up) {
        TravelDTO travel = travelService.getTravelById(up.getIdTravel());
        if (travel == null || !travel.isActive() || travel.getSagaPhases().equals(SagaPhases.STARTED) || travel.getFlightReservationID() != up.getIdReservation() || travel.getHotelReservationID() != 0)
            throw new RuntimeException("Travel with ID " + up.getIdTravel() + " not found");

        List<IdUpdateFlightInstanceInfo> flightInstanceSeats = new ArrayList<>();
        for (FlightInstanceSeatsDTO seat : up.getFlightInstanceSeats()) {
            if (seat == null || seat.getIdFlightInstance() <= 0 || seat.getNumberSeats() < 0) 
                throw new RuntimeException("Invalid seat information provided");
            IdUpdateFlightInstanceInfo id = new IdUpdateFlightInstanceInfo();
            id.setIdFlightInstance(seat.getIdFlightInstance());
            id.setNumberSeats(seat.getNumberSeats());
            flightInstanceSeats.add(id);
        }
        final String sagaId = UUID.randomUUID().toString();
        final byte transActive = 1;
        UpdateReservationCommand command = new UpdateReservationCommand();
        command.setIdReservation(up.getIdReservation());
        command.setIdTravel(up.getIdTravel());
        command.setFlightInstanceInfo(flightInstanceSeats);
        command.setAllFlightUpdate(false);
        EventData eventData = new EventData(
            sagaId,
            UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_BEGIN,
            new ArrayList<>(),
            command,
            transActive
        );
        travel.setSagaId(sagaId);
        travel.setSagaPhases(SagaPhases.STARTED);
        travel.setTransactionActive(transActive);
        travelService.initTransaction(travel);
        this.eventHandlerRegistry.getHandler(EventId.UPDATE_RESERVATION_TRAVEL).handleCommand(this.gson.toJson(eventData));
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
