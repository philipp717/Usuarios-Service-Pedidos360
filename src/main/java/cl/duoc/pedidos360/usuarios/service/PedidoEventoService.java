package cl.duoc.pedidos360.usuarios.service;

import cl.duoc.pedidos360.usuarios.messaging.dto.PedidoCreadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Lógica de dominio asociada a eventos de pedidos creados, independiente de Kafka.
 */
@Service
public class PedidoEventoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventoService.class);

    public void registrarActividadDePedido(PedidoCreadoEvent evento) {
        // Punto de extensión: p. ej. actualizar la actividad reciente del cliente
        log.info("event=actividad_pedido_cliente pedidoId={} cliente={} producto={} cantidad={}",
                evento.pedidoId(), evento.cliente(), evento.producto(), evento.cantidad());
    }
}
