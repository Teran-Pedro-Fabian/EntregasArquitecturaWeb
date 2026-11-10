# Segunda Entrega

Aplicación Java con Spring Boot para consultar información de estudiantes, carreras e inscripciones. Importa datos desde archivos CSV y genera reportes por carrera y año.

## Funcionalidades

- Búsqueda y ordenamiento de estudiantes por distintos criterios.
- Consultas de inscripciones asociadas a estudiantes y carreras.
- Filtros de estudiantes por carrera y ciudad.
- Conteos anuales de inscriptos y egresados por carrera.

## Tecnologías

- Java
- Spring Boot
- Maven
- Base de datos configurada en `src/main/resources/application.properties`

## Requisitos

- JDK compatible con la versión definida en `pom.xml`.
- Maven instalado.
- Acceso a la base de datos configurada para el proyecto.

## Configuración y ejecución

1. Configurá la conexión a la base de datos en `src/main/resources/application.properties`.
2. Asegurate de que la base de datos esté disponible. El repositorio también incluye `docker-compose.yml` para los servicios definidos allí.
3. Desde la carpeta raíz del proyecto, compilá y ejecutá la aplicación:

   ```bash
   mvn clean package
   mvn spring-boot:run

## Datos de entrada
Los archivos CSV se encuentran en DB:

estudiantes.csv
carreras.csv
estudianteCarrera.csv


## Estructura principal
src/main/java/com/segundaentrega/
├── Entitys/       # Entidades del dominio
├── Repository/    # Consultas y operaciones de persistencia
├── dto/           # Objetos para reportes
├── utils/         # Carga de datos CSV
└── Main.java      # Punto de entrada y ejecución de consultas
