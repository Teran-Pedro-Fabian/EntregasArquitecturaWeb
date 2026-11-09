#  TicketYa - Plataforma de Venta de Entradas

TicketYa es una plataforma de ticketera virtual diseñada para permitir que organizadores
de cualquier escala publiquen sus eventos y que el público compre entradas 
de forma segura, simple y eficiente.

## Arquitectura del Sistema

El sistema ha sido implementado bajo una **Arquitectura de Microservicios**, garantizando la escalabilidad y la independencia de cada módulo.

### Microservicios implementados:
1. **MicroServicioCliente (Puerto 8081):** Gestión de perfiles y preferencias de usuarios. (Base de Datos: **MongoDB**)
2. **MicroServicioEvento (Puerto 8082):** Catálogo de eventos, gestión de estados (Borrador/Publicado) y búsqueda avanzada. (Base de Datos: **MongoDB**)
3. **MicroServicioLugar (Puerto 8083):** Gestión de recintos, sectores y control de inventario de asientos. (Base de Datos: **PostgreSQL** y **Redis**)
4. **MicroServicioTicket (Puerto 8084):** Emisión de tickets, generación de códigos QR y gestión de devoluciones. (Base de Datos: **PostgreSQL**)

---

## Tecnologías Utilizadas

- **Lenguaje:** Java 17+
- **Framework:** Spring Boot 3.2.2
- **Bases de Datos:**
    - MongoDB (NoSQL) para datos flexibles y catálogos.
    - PostgreSQL (SQL) para transacciones ACID y consistencia.
    - Redis (In-Memory) para gestión de reservas temporales.
- **Infraestructura:** Docker.
- **Herramientas:** Maven, Lombok, JPA/Hibernate.

---

## Guía de Ejecución

### Levantamiento de Infraestructura
Para iniciar las bases de datos, ejecutar el siguiente comando en la raíz del proyecto:

- **docker-compose up -d*:* esto va a levantar las 4 bases de datos necesarias para el funcionamiento del proyecto 
- **ejecutar los 4 microservicios:** cada Microservicio se va a ejecutrar en un puerto distinto (8081, 8082,8083,8084)

### importante:
- Para evitar conflictos de zona horaria con la base de datos PostgreSQL en Docker, se debe agregar el siguiente argumento de VM (VM Options) en la configuración de ejecución de los servicios que usan SQL (Lugar y Ticket): -Duser.timezone=UTC


---
 Adicionales
---
- los microservicion posee unos datainicialaizer que cargan algunos datos para hacer los test
- los constructors posee un path para vaciar las bases de datos por si se necesita hacer una ejecucion limpia