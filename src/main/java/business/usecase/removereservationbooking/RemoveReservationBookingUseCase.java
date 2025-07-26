package business.usecase.removereservationbooking;

import java.util.ArrayList;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.commands.removereservation.RemoveBookingCommand;
import msa.commons.commands.removereservation.RemoveReservationCommand;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.DeleteReservation;
import msa.commons.saga.SagaPhases;

@Stateless
public class RemoveReservationBookingUseCase implements IRemoveReservationBookingUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;


    @Override
    public boolean removeReservationBooking(long reservationId, long bookingId) {
        TravelDTO travel = this.travelService.getTravelByIdsExternal(reservationId, bookingId);
        if (travel == null) 
            throw new RuntimeException("No travel found with reservation ID: " + reservationId + " and booking ID: " + bookingId);
        
        if(!travel.isActive() || travel.getSagaPhases().equals(SagaPhases.STARTED)) 
            throw new RuntimeException("Cannot remove reservation, travel is not completed: " + travel.getSagaPhases());


        final byte transactionActive = 2;
        final String sagaId = UUID.randomUUID().toString();
    
        RemoveBookingCommand command = new RemoveBookingCommand();
        command.setIdTravel(travel.getId());
        command.setIdBooking(travel.getHotelReservationID());
        EventData eventDataHotel = new EventData(
            sagaId,
            DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_BEGIN,
            new ArrayList<>(),
            command,
            transactionActive
        );


        RemoveReservationCommand commandAirline = new RemoveReservationCommand();
        commandAirline.setIdReservation(travel.getFlightReservationID());
        commandAirline.setIdTravel(travel.getId());
        commandAirline.setAllRemove(false);
        commandAirline.setListIdFlightInstance(new ArrayList<>());
        EventData eventDataAirline = new EventData(
            sagaId,
            DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_BEGIN,
            new ArrayList<>(),
            commandAirline,
            transactionActive
        );

        travel.setTransactionActive(transactionActive);
        travel.setSagaId(sagaId);
        travel.setSagaPhases(SagaPhases.STARTED);
        this.travelService.updateTransaction(travel);
        this.eventHandlerRegistry.getHandler(EventId.REMOVE_RESERVATION_TRAVEL).handleCommand(this.gson.toJson(eventDataHotel));
        this.eventHandlerRegistry.getHandler(EventId.REMOVE_RESERVATION_TRAVEL).handleCommand(this.gson.toJson(eventDataAirline));
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
