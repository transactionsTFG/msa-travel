package domainevent.publisher.hotel;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Queue;

import domainevent.publisher.jmseventpublisher.BaseJMSEventPublisher;
import domainevent.publisher.jmseventpublisher.IEventPublisher;
import integration.producer.qualifiers.HotelQueue;
import msa.commons.consts.JMSQueueNames;

@Stateless
@JMSHotelBookingPublisherQualifier
@Local(IEventPublisher.class)
public class JMSHotelBookingPublisher extends BaseJMSEventPublisher {

    @Override
    @Inject
    public void setQueueInject(@HotelQueue Queue queueInject) {
        this.queue = queueInject;
    }

    @Override
    public String getQueueName() {
        return JMSQueueNames.HOTEL_BOOKING_QUEUE;
    }
    
}
