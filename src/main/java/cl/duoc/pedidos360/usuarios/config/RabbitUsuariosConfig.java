
package cl.duoc.pedidos360.usuarios.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.AcknowledgeMode;

@Configuration
public class RabbitUsuariosConfig {

    @Bean
    public TopicExchange usuariosPedidosExchange(
            @Value("${pedidos360.rabbitmq.exchange}") String nombre) {
        return new TopicExchange(nombre, true, false);
    }

    @Bean
    public Queue usuariosPedidoCreadoQueue(
            @Value("${pedidos360.rabbitmq.usuarios.queue}") String nombre,
            @Value("${pedidos360.rabbitmq.usuarios.dead-letter-exchange}") String dlx,
            @Value("${pedidos360.rabbitmq.usuarios.dead-letter-routing-key}") String dlqRoutingKey) {

        return QueueBuilder.durable(nombre)
                .deadLetterExchange(dlx)
                .deadLetterRoutingKey(dlqRoutingKey)
                .build();
    }

    @Bean
    public Binding usuariosPedidoCreadoBinding(
            Queue usuariosPedidoCreadoQueue,
            TopicExchange usuariosPedidosExchange,
            @Value("${pedidos360.rabbitmq.usuarios.routing-key}") String routingKey) {

        return BindingBuilder.bind(usuariosPedidoCreadoQueue)
                .to(usuariosPedidosExchange)
                .with(routingKey);
    }

    @Bean
    public DirectExchange usuariosDeadLetterExchange(
            @Value("${pedidos360.rabbitmq.usuarios.dead-letter-exchange}") String nombre) {

        return new DirectExchange(nombre, true, false);
    }

    @Bean
    public Queue usuariosDeadLetterQueue(
            @Value("${pedidos360.rabbitmq.usuarios.dead-letter-queue}") String nombre) {

        return QueueBuilder.durable(nombre).build();
    }

    @Bean
    public Binding usuariosDeadLetterBinding(
            Queue usuariosDeadLetterQueue,
            DirectExchange usuariosDeadLetterExchange,
            @Value("${pedidos360.rabbitmq.usuarios.dead-letter-routing-key}") String routingKey) {

        return BindingBuilder.bind(usuariosDeadLetterQueue)
                .to(usuariosDeadLetterExchange)
                .with(routingKey);
    }
    @Bean
public JacksonJsonMessageConverter rabbitMessageConverter() {
    return new JacksonJsonMessageConverter();
}

@Bean
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory) {

    SimpleRabbitListenerContainerFactory factory =
            new SimpleRabbitListenerContainerFactory();

    factory.setConnectionFactory(connectionFactory);
    factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
    factory.setMessageConverter(rabbitMessageConverter());

    return factory;
}
}
