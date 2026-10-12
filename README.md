# Tercera Entrega - Servicios REST con Spring Boot

Ejercicio integrador del TP3 de Arquitecturas Web (TUDAI - UNICEN). Es un registro de estudiantes, carreras e inscripciones que expone sus consultas como **servicios REST con Spring Boot**, testeados con **Postman**. El código está en [`TercerEntrega/`](TercerEntrega/).

## Tecnologías

- Java 21
- Spring Boot 3.3.5 (Web + Data JPA)
- MySQL 8.4 (Docker)
- Lombok, Apache Commons CSV
- Maven
- Postman (testing)

## Requisitos

- JDK 21
- Maven
- Docker y Docker Compose
- Postman (o IntelliJ HTTP Client como alternativa)

## Ejecución

```bash
cd TercerEntrega
docker compose up -d        # levanta MySQL en localhost:3306 (root / root)
mvn spring-boot:run         # levanta la API en http://localhost:8080
```

La conexión está configurada en [`application.properties`](TercerEntrega/src/main/resources/application.properties). Al iniciar, si la base está vacía, se cargan automáticamente los CSV de [`TercerEntrega/DB/`](TercerEntrega/DB/) (`carreras.csv`, `estudiantes.csv`, `estudianteCarrera.csv`).

Para volver al estado inicial de los datos:

```bash
docker compose down -v && docker compose up -d
```

## Endpoints

Base URL: `http://localhost:8080`

### Consignas del integrador

| Punto | Método | URL | Descripción |
|---|---|---|---|
| 2a | `POST` | `/Estudiante` | Dar de alta un estudiante |
| 2b | `POST` | `/EstudianteCarrera` | Matricular un estudiante en una carrera |
| 2c | `GET` | `/Estudiante` | Todos los estudiantes, ordenados por DNI |
| 2d | `GET` | `/Estudiante/libreta/{lu}` | Estudiante por número de libreta universitaria |
| 2e | `GET` | `/Estudiante/genero/{genero}` | Estudiantes por género |
| 2f | `GET` | `/Carrera/inscriptos` | Carreras con inscriptos, ordenadas por cantidad (mayor a menor) |
| 2g | `GET` | `/EstudianteCarrera/FiltradoPorCarreraYCiudad/{idCarrera}/{ciudad}` | Estudiantes de una carrera filtrados por ciudad |
| 2h | `GET` | `/EstudianteCarrera/ReporteAnual` | Inscriptos y egresados por carrera y año (carreras alfabéticamente, años cronológicamente) |

### Otros endpoints

| Método | URL | Descripción |
|---|---|---|
| `GET` | `/Carrera` | Todas las carreras |
| `GET` | `/Carrera/{id}` | Carrera por ID |
| `GET` | `/EstudianteCarrera` | Todas las matrículas |
| `GET` | `/EstudianteCarrera/{id}` | Matrícula por ID |
| `PUT` | `/EstudianteCarrera/{id}` | Actualizar inscripción, graduación y antigüedad |
| `DELETE` | `/EstudianteCarrera/{id}` | Eliminar matrícula |
| `GET` | `/EstudianteCarrera/idCarrera/{id}` | Matrículas de una carrera |
| `GET` | `/EstudianteCarrera/idEstudiante/{dni}` | Matrículas de un estudiante |
| `GET` | `/EstudianteCarrera/OrdenadoPorEstudiante` | Matrículas ordenadas por DNI |
| `GET` | `/EstudianteCarrera/ReporteDeInscriptos` | Inscriptos por carrera y año |
| `GET` | `/EstudianteCarrera/ContarEgresados` | Egresados por carrera y año |

### Ejemplos de body

Alta de estudiante (2a):

```json
{
  "dni": 40123456,
  "nombre": "Prueba",
  "apellido": "Integrador",
  "edad": 22,
  "genero": "Masculino",
  "ciudad": "Tandil",
  "lu": 999999
}
```

Matricular estudiante (2b), con `graduacion = 0` si todavía no egresó:

```json
{
  "id": 900005,
  "estudiante": { "dni": 40123456 },
  "carrera": { "id": 1 },
  "inscripcion": 2026,
  "graduacion": 0,
  "antiguedad": 0
}
```

### Códigos de respuesta

| Código | Caso |
|---|---|
| `200 OK` | Consulta o actualización exitosa |
| `201 Created` | Estudiante o matrícula creada |
| `204 No Content` | Matrícula eliminada |
| `400 Bad Request` | Faltan estudiante/carrera, ID inválido o años inválidos (inscripción ≤ 0, graduación anterior a la inscripción, antigüedad negativa) |
| `404 Not Found` | Estudiante, carrera o matrícula inexistente |
| `409 Conflict` | DNI o LU duplicados, ID de matrícula duplicado, o estudiante ya inscripto en esa carrera |

## Testing con Postman

La colección está en [`TercerEntrega/postman/TP3-ArqWeb.postman_collection.json`](TercerEntrega/postman/TP3-ArqWeb.postman_collection.json). Tiene 32 requests organizadas en una carpeta por consigna (2a a 2h) y una carpeta para el CRUD de matrículas. Cada request incluye un test que verifica el código de respuesta esperado.

1. Levantar la base y la API (ver [Ejecución](#ejecución)).
2. En Postman: **Import** → seleccionar el archivo de la colección.
3. La variable de colección `baseUrl` ya vale `http://localhost:8080`. Se puede cambiar en la pestaña **Variables** de la colección.
4. Ejecutar request por request o toda la colección con **Run collection** (Collection Runner).

> La colección está pensada para correrse **en orden y sobre una base recién cargada**: primero crea el estudiante `40123456` (2a), después lo matricula (2b), consulta, actualiza la matrícula `900005` y al final la elimina. Si se corre dos veces sin reiniciar la base, el alta del estudiante devuelve `409` porque ya existe. Para reiniciar: `docker compose down -v`.

| Carpeta | Requests | Resultado esperado |
|---|---|---|
| 2a - Alta de estudiante | Alta / DNI duplicado / LU duplicada | 201 / 409 / 409 |
| 2b - Matricular | Matricular / duplicados / estudiante o carrera inexistente / datos inválidos | 201 / 409 / 404 / 400 |
| 2c - Estudiantes ordenados | Todos por DNI | 200 |
| 2d - Estudiante por LU | LU existente / inexistente | 200 / 404 |
| 2e - Por género | Male / Female | 200 |
| 2f - Carreras por inscriptos | Ranking / todas / por ID / inexistente | 200 / 200 / 200 / 404 |
| 2g - Carrera y ciudad | TUDAI en Rauch (4 estudiantes) / ciudad sin estudiantes (lista vacía) | 200 |
| 2h - Reporte anual | Reporte | 200 |
| CRUD EstudianteCarrera | Listar, buscar, actualizar, eliminar (válidos e inválidos) | 200 / 400 / 404 / 204 |

### Alternativa: IntelliJ HTTP Client

En [`TercerEntrega/http-client-plus/collections/TPArquiWeb3Entrega/`](TercerEntrega/http-client-plus/collections/TPArquiWeb3Entrega/) están los mismos casos en archivos `.http`, con el resultado esperado comentado en cada request. Se ejecutan desde IntelliJ o VS Code (extensión REST Client).

## Estructura

```
TercerEntrega/
├── DB/                         # CSV de carga inicial
├── postman/                    # Colección Postman
├── http-client-plus/           # Tests .http
├── docker-compose.yml          # MySQL
└── src/main/java/com/
    ├── Controllers/            # Endpoints REST
    ├── Services/               # Lógica de negocio y validaciones
    ├── Repositorys/            # Repositorios JPA y consultas JPQL
    ├── Entitys/                # Entidades: Estudiante, Carrera, EstudianteCarrera
    ├── dtos/                   # DTOs de reportes
    ├── utils/                  # Carga de CSV
    └── SpringApp.java          # Punto de entrada
```

