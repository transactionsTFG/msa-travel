package domainevent.command.reservation;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import business.qualifier.RemoveReservationTravelQualifier;
import business.qualifier.UpdateReservationTravelQualifier;
import business.travel.TravelDTO;
import business.travel.TravelHistoryDTO;
import domainevent.command.handler.BaseHandler;
import domainevent.command.handler.CommandHandler;
import msa.commons.commands.removereservation.RemoveBookingCommand;
import msa.commons.commands.removereservation.RemoveReservationCommand;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.CreateReservation;
import msa.commons.event.eventoperation.reservation.DeleteReservation;
import msa.commons.event.eventoperation.reservation.UpdateReservation;
import msa.commons.event.type.Type;
import msa.commons.saga.SagaPhases;

@Stateless
@UpdateReservationTravelQualifier
@Local(CommandHandler.class)
public class UpdateReservationTravel extends BaseHandler {

    @Override
    public void handleCommand(String json) {
        EventData e = this.gson.fromJson(json, EventData.class);
        if (UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_BEGIN.equals(e.getOperation()) || UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_BEGIN.equals(e.getOperation()))
            this.jmsEventPublisher.publish(EventId.UPDATE_RESERVATION_TRAVEL, e);

        if (UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_COMMIT.equals(e.getOperation()))
            this.handleUpdateReservationAirlineCommit(json);

        if (UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_COMMIT.equals(e.getOperation())) 
            this.handleUpdateReservationHotelCommit(json);

        if (UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_ROLLBACK.equals(e.getOperation()))
            this.handleUpdateReservationHotelRollback(json);

        if (UpdateReservation.UPDATE_RESERVATION_ONLY_AIRLINE_ROLLBACK.equals(e.getOperation()))
            this.handleUpdateReservationAirlineRollback(json);
    }

    private void handleUpdateReservationAirlineCommit(final String json) {
        EventData e = EventData.fromJson(json, RemoveReservationCommand.class);
        RemoveReservationCommand command = (RemoveReservationCommand) e.getData();
        // TODO: Implement the logic to handle the commit of airline reservation update
    }

    private void handleUpdateReservationHotelCommit(final String json) {
        EventData e = EventData.fromJson(json, RemoveReservationCommand.class);
        RemoveReservationCommand command = (RemoveReservationCommand) e.getData();
        // TODO: Implement the logic to handle the commit of hotel reservation update
    }

    private void handleUpdateReservationHotelRollback(final String json) {
        EventData e = EventData.fromJson(json, RemoveReservationCommand.class);
        RemoveReservationCommand command = (RemoveReservationCommand) e.getData();
        // TODO: Implement the logic to handle the rollback of hotel reservation update
    }

    private void handleUpdateReservationAirlineRollback(final String json) {
        EventData e = EventData.fromJson(json, RemoveReservationCommand.class);
        RemoveReservationCommand command = (RemoveReservationCommand) e.getData();
        // TODO: Implement the logic to handle the rollback of airline reservation update
    }
}

