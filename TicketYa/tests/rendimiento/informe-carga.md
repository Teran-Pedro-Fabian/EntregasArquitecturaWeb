# Informe prueba de carga

## Objetivo

Comprobar el comportamiento del flujo principal de compra cuando varios clientes realizan operaciones al mismo tiempo

## Escenario

Apache JMeter contra el sistema completo

Se simularon 5 usuarios concurrentes

Cada usuario realizó el siguiente flujo

`Login -> fila virtual -> reserva -> compra`

El escenario se encuentra en

`Login Cliente.jmx`

Los datos de clientes utilizados se encuentran en

`clientes.csv`

## Resultados

| Operación | Tiempo promedio |
|---|---:|
| Login | 85 ms |
| Fila virtual | 46 ms |
| Reserva | 66 ms |
| Compra | 203 ms |

Errores obtenidos `0%`

Los resultados completos se encuentran en

`resultados-5usuarios.csv`

## Conclusion

La prueba pudo completarse con los 5 usuarios concurrentes sin errores

La operación mas lenta fue la compra con un promedio de 203 ms

Esto es esperable porque durante una compra Ticket necesita validar datos con Evento y Lugar confirmar la reserva y guardar la compra y sus tickets

Para la carga probada el sistema mantuvo tiempos de respuesta bajos y no se detectaron errores