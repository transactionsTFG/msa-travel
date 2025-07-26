package business.usecase.removebooking;

import java.util.ArrayList;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.commands.removereservation.RemoveBookingCommand;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.DeleteReservation;
import msa.commons.saga.SagaPhases;

@Stateless
public class RemoveBookingUseCase implements IRemoveBookingUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;
    
    @Override
    public boolean removeBooking(long bookingId) {
        TravelDTO travel = this.travelService.getTravelByIdsExternal(0L, bookingId);
        if (travel == null) 
            throw new RuntimeException("No travel found with booking ID: " + bookingId);
            
        if(!travel.isActive() || travel.getSagaPhases().equals(SagaPhases.STARTED))
            throw new RuntimeException("Cannot remove booking, travel is not completed: " + travel.getSagaPhases());
        
        RemoveBookingCommand command = new RemoveBookingCommand();
        command.setIdTravel(travel.getId());
        command.setIdBooking(travel.getHotelReservationID());
        final byte transactionActive = 1;
        final String sagaId = UUID.randomUUID().toString();
        EventData eventData = new EventData(
            sagaId,
            DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_BEGIN,
            new ArrayList<>(),
            command,
            transactionActive
        );
        travel.setTransactionActive(transactionActive);
        travel.setSagaId(sagaId);
        travel.setSagaPhases(SagaPhases.STARTED);
        this.travelService.initTransaction(travel);
        this.eventHandlerRegistry.getHandler(EventId.REMOVE_RESERVATION_TRAVEL).handleCommand(this.gson.toJson(eventData));
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
