# Re-Entrega TP2 - Arquitecturas Web

## Cambios realizados

**1. Relación N-N y clave primaria**

- Se mantiene `EstudianteCarrera` como entidad intermedia entre estudiante y carrera
- Se creó `EstudianteCarreraId` para representar la PK compuesta `(id_estudiante, d_carrera)`
- Se utilizan `@embeddable`, `@embeddedId` y `@mapsId` para mapear la relación
- Se eliminó la id independiente de la inscripción

**2. Repositorios**

- Se eliminó el uso de `JpaRepository` y Spring
- Los repositorios ahora utilizan `EntityManager`
- Persistencia JPA y consultas JPQL
- Configuración mediante `persistence.xml` sin Spring Boot

**3. Datos CSV**

- Los CSV se cargan automáticamente cuando la base de estudiantes está vacía

## Ejecución

1. Abrir `ReEntregaTP2` como proyecto Maven en IntelliJ
2. Levantar MySQL desde la carpeta del proyecto:

   ```bash
   docker compose up -d
   ```

3. Ejecutar `com.segundaentrega.Main`

La primera ejecución crea las tablas mediante Hibernate y carga los CSV, las otras siguientes ejecuciones reutilizan los datos existentes