package domainevent.publisher.airline;

import java.util.Hashtable;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import domainevent.publisher.jmseventpublisher.BaseJMSEventPublisher;
import domainevent.publisher.jmseventpublisher.IEventPublisher;
import integration.producer.qualifiers.AirlineQueue;
import msa.commons.consts.JMSQueueNames;
import msa.commons.consts.PropertiesConsumer;
import msa.commons.event.Event;
import msa.commons.event.EventData;
import msa.commons.event.EventId;

@Stateless
@JMSAirlineReservationPublisherQualifier
@Local(IEventPublisher.class)
public class JMSAirlineReservationPublisher extends BaseJMSEventPublisher {

    private static final Logger LOGGER = LogManager.getLogger(JMSAirlineReservationPublisher.class);
    private static final String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";
    private static final String PROVIDER_URL = "t3://127.0.0.1:8001";
    private static final String CONNECTION_FACTORY_JNDI = "jms/airlineConnectionFactory";
    private static final String QUEUE_JNDI = "jms/reservationQueue";

    @Override
    @Inject
    public void setQueueInject(@AirlineQueue Queue queueInject) {
        this.queue = queueInject;
    }

    @Override
    public String getQueueName() {
        return JMSQueueNames.AIRLINE_RESERVATION_QUEUE;
    }

    @Override
    public void publish(EventId eventId, Object data) {
        try {
            // Configuración del contexto JNDI
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
            env.put(Context.PROVIDER_URL, PROVIDER_URL);
            env.put(Context.SECURITY_PRINCIPAL, "root");
            env.put(Context.SECURITY_CREDENTIALS, "password");

            Context ctx = new InitialContext(env);

            // Lookup de la fábrica y la cola
            ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(CONNECTION_FACTORY_JNDI);
            Queue queue = (Queue) ctx.lookup(QUEUE_JNDI);

            // Uso de JMSContext (JMS 2.0)
            try (JMSContext jmsContext = connectionFactory.createContext()) {
                Event sendMsg = new Event(eventId, (EventData) data);
                TextMessage txt = jmsContext.createTextMessage(this.gson.toJson(sendMsg));
                txt.setStringProperty(PropertiesConsumer.ORIGIN_QUEUE, queue.getQueueName());

                LOGGER.info("Publicando en Cola {}, Evento Id: {}, Mensaje: {}", queue.getQueueName(), eventId, data);
                jmsContext.createProducer().send(queue, txt);
            }

        } catch (NamingException | JMSException e) {
            LOGGER.error("Error al publicar el mensaje: {}", e.getMessage(), e);
        }

    }

}
