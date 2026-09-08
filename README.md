# Ejercicio Integrador 1 - Sistema de Facturación (JDBC)

---

## � Guía Rápida Paso a Paso (desde CERO)

> **Importante:** En **Windows PowerShell** (tanto 5.x como 7.x) se recomienda ejecutar **cada `cd` en su propia línea** (no encadenes con `;` entre comandos de cambio de directorio, a menos que uses `&&` en PowerShell 7+). Cada paso a continuación muestra el directorio exacto donde tenés que estar parado.

---

### Paso 0️⃣ — Verificar pre-requisitos instalados

Asegurarse de tener instalados y configurados en el `PATH`:

| Componente | Versión recomendada | Comando de VERIFICACIÓN (cualquier directorio) |
|---|---|---|
| **JDK 17** (LTS) | 17 | `java -version` → debe decir `openjdk 17...` o similar |
| **Maven** | 3.8+ | `mvn -version` → debe decir `Apache Maven 3.8...` **y** reconocer el JDK 17 |
| **Docker Desktop / Docker Engine** | cualquiera moderna | `docker --version` |
| **Docker Compose** | v2+ (integrado en Docker Desktop) | `docker compose version` |
| Git (opcional) | cualquiera | `git --version` |

> **Nota sobre Lombok (si usás IDE):** El proyecto usa Lombok para reducir boilerplate en las entidades.
> - **IntelliJ IDEA:** Instalar plugin "Lombok" y habilitar:
>   `File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors → ☑ Enable annotation processing`
> - **VSCode:** Instalar la extensión "Lombok Annotations Support for VS Code" (viene en Extension Pack for Java).
> - **Eclipse/Spring Tool Suite:** Ejecutar `java -jar lombok.jar` y apuntar al ejecutable del IDE.

---

### Paso 1️⃣ — Levantar la base de datos MySQL con Docker

⚠️ **Directorio correcto:** **`demo/DB`** (donde vive el `docker-compose.yml`, NO la raíz, NO `demo/`)

Archivo de configuración Docker: [docker-compose.yml](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/demo/DB/docker-compose.yml)

```bash
# 1. Entrar al directorio demo/DB (RUTA ABSOLUTA EJEMPLO para Windows — reemplazá por TU ruta si la distinta)
cd "C:\Users\asus\Desktop\Estudio\UNICEN\Arquis 2026\Entregables\EntregasArquitecturaWeb\demo\DB"

# 2. Levantar el contenedor (la primera vez descarga la imagen mysql:8.4 ~500MB, tarda)
docker compose up -d

# 3. Verificar que el contenedor está "Up"
docker ps
# Deberías ver: NAMES = mysql-facturacion, STATUS = Up ...
```

#### ¿Qué crea este contenedor?

| Parámetro | Valor |
|---|---|
| Imagen | `mysql:8.4` |
| Puerto expuesto en tu PC | `3306` (mapeado al interno `3306`) |
| Nombre de la base de datos | `facturacion` |
| Usuario de aplicación | `usuario` |
| Contraseña del usuario | `usuario123` |
| Usuario root | `root` |
| Contraseña root | `root` |
| Persistencia | Volumen nombrado `mysql_data` → los datos NO se pierden al apagar el contenedor |

#### Esperar a que MySQL esté LISTO (MUY IMPORTANTE):

```bash
# Seguí los logs hasta ver, en las últimas líneas, "ready for connections"
docker logs mysql-facturacion
```
*(Si en tu PC es la primera vez que levantas MySQL, tardá entre 5 y 20 segundos. No te apresures a correr el Main sin esperar este mensaje, vas a ver errores de "Communications link failure").*

#### 🚨 Troubleshooting rápido del Paso 1

- **Error:** `Bind for 0.0.0.0:3306 failed: port is already allocated`
  → Tenés otro MySQL local corriendo en el `3306`. Solución:
  1. Abrí [docker-compose.yml](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/demo/DB/docker-compose.yml) y cambiá el puerto expuesto, por ejemplo: `"3307:3306"`
  2. Abrí [MySQLConnectionManager.java](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/demo/src/main/java/com/example/repository/MySQL/MySQLConnectionManager.java#L25-L27) y actualizá la URL a:
     ```java
     private static final String URL = "jdbc:mysql://localhost:3307/facturacion";
     ```
  3. Volvé a ejecutar `docker compose up -d`

- **Error:** `Cannot connect to the Docker daemon`
  → Docker Desktop no está abierto. Abrilo y esperá que diga "Docker Desktop is running".

---

### Paso 2️⃣ — Resolver dependencias Maven (descargar drivers y libs)

⚠️ **Directorio correcto:** **`demo/`** (donde vive `pom.xml`. ❌ NO ejecutar esto en `demo/DB` — ahí no hay pom.xml y Maven falla con *"Goal requires a project to execute but there is no POM in this directory"*).

```bash
# Entrar al directorio demo/ (RUTA ABSOLUTA EJEMPLO Windows)
cd "C:\Users\asus\Desktop\Estudio\UNICEN\Arquis 2026\Entregables\EntregasArquitecturaWeb\demo"

# Descargar todas las dependencias (mysql-connector, lombok, commons-csv)
mvn dependency:resolve
```
*(La primera vez descarga varios MBs; posteriores ejecuciones son instantáneas porque usa cache local).*

---

### Paso 3️⃣ — Compilar el proyecto

⚠️ **Directorio correcto:** `demo/` (mismo que el paso anterior).

```bash
cd "C:\Users\asus\Desktop\Estudio\UNICEN\Arquis 2026\Entregables\EntregasArquitecturaWeb\demo"

mvn clean compile
```

#### Qué hace este comando:
1. `clean` → borra compilaciones previas en `demo/target/`
2. `compile` → compila `.java` → `.class` en `demo/target/classes/` y valida que no haya errores de sintaxis.

#### Resultado ESPERADO:
```
[INFO] BUILD SUCCESS
```
Si en vez de eso ves `BUILD FAILURE`:
- Leé el mensaje. La causa más común (arreglada a esta altura) era duplicados en `MySQLProductoDAO`.
- Si el error persiste: revisá el reporte actualizado en [reporte_errores_codigo.md](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/alan/reporte_errores_codigo.md) para la lista de issues pendientes.

---

### Paso 4️⃣ — Ejecutar la aplicación (Main)

⚠️ **Directorio correcto:** `demo/`

```bash
cd "C:\Users\asus\Desktop\Estudio\UNICEN\Arquis 2026\Entregables\EntregasArquitecturaWeb\demo"

mvn exec:java -Dexec.mainClass="com.example.Main"
```

#### Qué hace el Main ACTUALMENTE (etapa inicial del proyecto):
> El `Main.java` actual **solo crea el esquema de tablas** en la base (Punto 1 del integrador) al instanciar los 4 DAOs.
>
> Aún **no implementa**:
> - ❌ Carga masiva desde archivos CSV (`demo/DB/Datos/*.csv`)
> - ❌ Ejecución de los 3 reportes del enunciado
> - ❌ Impresión por consola de resultados
>
> Cuando agreguemos esas features al Main, esta sección se actualizará. Mientras tanto, el objetivo del Main es PROBAR que la conexión funciona y las tablas se crean bien.

Código actual de [Main.java](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/demo/src/main/java/com/example/Main.java):
```java
DAOFactory factory = DAOFactory.getInstance(DBType.MYSQL);
factory.createClienteDAO();
factory.createProductoDAO();
factory.createFacturaDAO();
factory.createFactura_ProductoDAO();
factory.shutdown();
```

#### Resultado ESPERADO en CONSOLA:
```
Conexión establecida correctamente con MySQL.
Hello world!
```
*(El `Hello world!` está intencionalmente — es parte del checklist de verificación del README, no borrarlo hasta que el Main imprima reportes reales).*

---

### Paso 5️⃣ — Verificar MANUALMENTE que las 4 tablas existen en MySQL

#### Opción A — Dentro del contenedor (sin instalar nada más):
```bash
# Cualquier directorio alcanza, no importa el cwd
docker exec -it mysql-facturacion mysql -u usuario -pfacturacion
```
Dentro del cliente MySQL que se abre, escribí:
```sql
USE facturacion;
SHOW TABLES;
SHOW CREATE TABLE Cliente;
SHOW CREATE TABLE Producto;
SHOW CREATE TABLE Factura;
SHOW CREATE TABLE Factura_Producto;
exit
```

#### Opción B — Cliente GUI (DBeaver, MySQL Workbench, DataGrip, VSCode + MySQL ext):
Conectate usando:
- **Host / Server:** `localhost`
- **Port:** `3306` (o `3307` si cambiaste el puerto en Paso 1)
- **Database / Schema:** `facturacion`
- **Usuario:** `usuario`
- **Contraseña:** `usuario123`

Luego ejecutá:
```sql
SHOW TABLES;
```

✅ **Resultado esperado (debes ver exactamente estas 4 filas):**
```
Cliente
Factura
Factura_Producto
Producto
```

---

### Paso 6️⃣ — Apagado SEGURO (cuando termines de probar)

```bash
# Entrar a demo/DB para los comandos docker compose
cd "C:\Users\asus\Desktop\Estudio\UNICEN\Arquis 2026\Entregables\EntregasArquitecturaWeb\demo\DB"

# Opción 1: Apagar y CONSERVAR los datos (proximas ejecuciones retoman donde quedaste)
docker compose down

# Opción 2: Apagar y BORRAR TODOS LOS DATOS (reset COMPLETO — usá solo si querés arrancar de cero)
docker compose down -v
```

---

## 🧪 Resumen en formato Checklist RÁPIDO

| # | Paso | ¿Listo? | Verificación |
|---|---|---|---|
| 0 | Pre-requisitos instalados | ☐ | `java -version`, `mvn -version`, `docker --version` OK |
| 1 | Docker levantado | ☐ | `docker ps` → `mysql-facturacion` con STATUS `Up` |
| 1b | MySQL listo para conexiones | ☐ | `docker logs mysql-facturacion` → `ready for connections` |
| 2 | Dependencias Maven resueltas | ☐ | `mvn dependency:resolve` en `demo/` → BUILD SUCCESS |
| 3 | Código compila | ☐ | `mvn clean compile` en `demo/` → BUILD SUCCESS |
| 4 | Main ejecuta sin error | ☐ | Consola muestra `Conexión establecida...` + `Hello world!` |
| 5 | Tablas creadas en BD | ☐ | `SHOW TABLES;` → `Cliente, Factura, Factura_Producto, Producto` |
| 6 | (futuro) Datos CSV cargados | ☐ | `SELECT COUNT(*) FROM Cliente;` > 0 |
| 7 | (futuro) Reportes OK | ☐ | Consola imprime los 3 reportes del enunciado |

---

## ⚙️ Credenciales y configuración técnica (referencia)

Si en un futuro modificás credenciales o puertos, acordate de actualizar AMBOS archivos (son espejo):

1. [docker-compose.yml](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/demo/DB/docker-compose.yml) → define lo que usa Docker
2. [MySQLConnectionManager.java](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/demo/src/main/java/com/example/repository/MySQL/MySQLConnectionManager.java#L25-L27) → define lo que usa el código Java

Valores por defecto actuales (coinciden en ambos archivos):
```java
URL      = "jdbc:mysql://localhost:3306/facturacion";
USER     = "usuario";
PASSWORD = "usuario123";
```
*Nota: las credenciales están hardcodeadas por decisión de diseño para un TP académico — en producción deberían estar en variables de entorno.*

### Dependencias Maven declaradas (en [pom.xml](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/demo/pom.xml)):
- `mysql-connector-j:26.7.0` — Driver JDBC oficial de MySQL
- `lombok:1.18.38` — Anotaciones `@Data`, `@NoArgsConstructor`, etc. en entidades
- `commons-csv:1.9.0` — Parser de CSV (para etapa de carga masiva de datos)

---

## 🗄️ Estructura de la Base de Datos (Punto 1 - Implementado)

### Tablas y relaciones

```
Cliente ──< Factura ──< Factura_Producto >── Producto
```

| Tabla | Descripción | PK | FKs |
|---|---|---|---|
| `Cliente` | Datos de clientes | `idCliente` | — |
| `Producto` | Catálogo de productos y precio unitario | `idProducto` | — |
| `Factura` | Encabezado de factura asociado a un cliente | `idFactura` | `idCliente → Cliente.idCliente` |
| `Factura_Producto` | Ítems / detalle de cada factura | **PK COMPUESTA `(idFactura, idProducto)`** | `idFactura → Factura.idFactura`, `idProducto → Producto.idProducto` |

### Orden de creación (automático al instanciar DAOs)

El `Main` instancia los DAOs (y por lo tanto corre `CREATE TABLE IF NOT EXISTS`) en este orden para respetar dependencias de FK:
1. `ClienteDAO`
2. `ProductoDAO`
3. `FacturaDAO`
4. `Factura_ProductoDAO`

---

## 📂 Datos CSV a Importar (Pendiente de implementar en Main)

Los archivos ya están disponibles en `demo/DB/Datos/`:

| Archivo | Columnas |
|---|---|
| `clientes.csv` | `idCliente, nombre, email` |
| `productos.csv` | `idProducto, nombre, valor` |
| `facturas.csv` | `idFactura, idCliente` |
| `facturas-productos.csv` | `idFactura, idProducto, cantidad` |
| `msql.yml` | Configuración de referencia |

La dependencia `commons-csv:1.9.0` ya está lista en el `pom.xml` para usarse cuando se implemente la carga.

### Reportes futuros a probar (cuando estén implementados en el Main):
1. Clientes ordenados por facturación total descendente
2. Producto con mayor recaudación
3. Facturas que contienen un producto determinado

---

## 🏛️ Arquitectura

### Patrones de diseño aplicados

| Patrón | ¿Dónde? | Propósito |
|---|---|---|
| **Abstract Factory** | `DAOFactory` (abstract) + `MySQLDAOFactory` (concreta) | Familia de DAOs por motor de BD |
| **Factory Method** | `createClienteDAO()`, `createProductoDAO()`, etc. en `DAOFactory` | Instanciación de cada DAO a cargo de la subclase concreta |
| **Singleton (DCL)** | `DAOFactory.getInstance()`, `MySQLConnectionManager.getInstance()` | Una sola instancia thread-safe con lazy initialization |
| **Template Method** | `DAOFactory.shutdown()` (final) que llama a `doShutdown()` (abstract) | Pasos fijos + pasos variables por motor |
| **DAO (Data Access Object)** | `com.example.dao.*` (interfaces) + `com.example.repository.MySQL.*` (impl) | Aislar la capa de persistencia de la lógica |
| **DTO / Java Records** | `com.example.dto.*` | Transporte de datos inmutables para reportes |

### Estructura de paquetes

```
demo/src/main/java/com/example/
├── Main.java                        # Punto de entrada
├── dao/                             # Contratos (interfaces) DAO
│   ├── ClienteDAO.java
│   ├── FacturaDAO.java
│   ├── Factura_ProductoDAO.java
│   └── ProductoDAO.java
├── dto/                             # Records de transferencia
│   ├── ClienteConFacturacion.java
│   ├── ListaDeClientes.java
│   └── ProductoMayorRecaudacion.java
├── entity/                          # Entidades de dominio (Lombok)
│   ├── Cliente.java
│   ├── Factura.java
│   ├── Factura_Producto.java
│   └── Producto.java
├── factory/                         # Abstracciones de la fábrica
│   ├── ConnectionManager.java       # Interface de conexión
│   ├── DAOFactory.java              # Abstract Factory + Singleton
│   └── DBType.java                  # Enum: MYSQL, DERBY, POSTGRES, MONGO
└── repository/
    └── MySQL/                       # Implementaciones concretas MySQL
        ├── MySQLClienteDAO.java
        ├── MySQLConnectionManager.java
        ├── MySQLDAOFactory.java
        ├── MySQLFacturaDAO.java
        ├── MySQLFactura_ProductoDAO.java
        └── MySQLProductoDAO.java
```

### Motores soportados (arquitectura preparada)

El enum `DBType` declara: `MYSQL`, `DERBY`, `POSTGRES`, `MONGO`. Actualmente solo está implementado **MySQL**. Para agregar otro motor se debe crear un nuevo sub-paquete en `repository/` con su propio `DAOFactory`, `ConnectionManager` y sus implementaciones DAO — sin tocar código existente (Open/Closed Principle).

---

## ❓ Troubleshooting Común

### 1. No me puedo conectar a la base de datos
- **Docker arrancó?** Corré `docker ps` y confirmá que `mysql-facturacion` esté `Up`.
- **Puerto ocupado?** Si tenés un MySQL local instalado, el `3306` ya está en uso. Solución: cambiar el puerto expuesto en el `docker-compose.yml` a `3307:3306` y actualizar la URL del `MySQLConnectionManager` a `jdbc:mysql://localhost:3307/facturacion`.
- **Credenciales distintas?** Asegurate de que `usuario / usuario123` coincidan entre el compose y el `MySQLConnectionManager`.
- **Esperá unos segundos:** MySQL tarda entre 5-20 segundos en estar listo la primera vez (probalo con `docker logs mysql-facturacion` hasta que aparezca `ready for connections`).

### 2. Error: "No suitable driver found for jdbc:mysql://..."
- Asegurarse de haber corrido `mvn clean compile` y que `mvn dependency:resolve` devuelva `BUILD SUCCESS`.
- El driver `mysql-connector-j` debe estar presente en el classpath al momento de ejecutar. Si se ejecuta con `mvn exec:java`, Maven se encarga automáticamente.

### 3. Lombok no funciona / IDE marca errores en getters/setters
- **IntelliJ IDEA:** Instalar el plugin oficial "Lombok" y habilitar:
  `File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors → ☑ Enable annotation processing`
- **Eclipse / Spring Tool Suite:** Ejecutar el installer de Lombok (`java -jar lombok.jar`) y apuntar a tu Eclipse.
- **VSCode:** Instalar extensión "Lombok Annotations Support" y asegurarse de tener Extension Pack for Java.

### 4. Errores de compilación en código fuente

- **Paso 1:** Asegurate de ejecutar `mvn clean compile` **dentro del directorio `demo/`** (no en la raíz, no en `demo/DB`).
- **Paso 2:** Leé el mensaje de error completo del `BUILD FAILURE`. Maven dice EXACTAMENTE en qué archivo y línea falló.
- **Paso 3:** Revisá el reporte actualizado de issues pendientes en:
  [reporte_errores_codigo.md](file:///c:/Users/asus/Desktop/Estudio/UNICEN/Arquis%202026/Entregables/EntregasArquitecturaWeb/alan/reporte_errores_codigo.md)
  (ahí se documentan todos los bugs conocidos, su impacto, y la solución paso a paso).
- Los bugs CRÍTICOS originales fueron resueltos, pero pueden quedar issues de categoría ALTO / MEDIO pendientes según etapa del desarrollo.

### 5. Error: "Table 'facturacion.X' doesn't exist" al consultar
- Asegurarse de haber corrido el `Main` al menos una vez (el esquema se crea "on demand" al instanciar los DAOs).
- Si se corrió `docker compose down -v` (volumen borrado), la base se reseteó y hay que volver a correr el Main.

### 6. Maven lento / no descarga dependencias
- Probar con `mvn dependency:resolve -U` (forzar actualización de snapshots/releases).
- Si se está en red UNICEN puede ser necesario configurar un mirror o proxy Maven en `settings.xml`.

### 7. Error: "The term 'mvn' is not recognized" (Windows)

- **Causa:** Maven no está en el `PATH` del sistema.
- **Verificar:** Abrir una consola NUEVA (no la que tenés abierta desde antes de instalar Maven) y correr:
  ```bash
  mvn -version
  ```
- **Solución Windows 10/11:**
  1. Instalar Maven (descargar zip, extraer en `C:\Program Files\Apache\maven` o similar).
  2. Agregar la variable de entorno **`MAVEN_HOME`** apuntando a esa carpeta.
  3. Editar el `PATH` y agregar `%MAVEN_HOME%\bin`.
  4. **Cerrar y reabrir TODAS las terminales** (PowerShell, CMD, VSCode) — los cambios en `PATH` no se aplican a consolas abiertas antes del cambio.
  5. Alternativa "de último momento" sin modificar PATH: usar ruta absoluta:
     ```bash
     & "C:\ruta\a\apache-maven-3.x.x\bin\mvn.cmd" clean compile
     ```

### 8. Error: "Goal requires a project to execute but there is no POM in this directory (...demo\DB)"

- **Causa:** Ejecutaste `mvn ...` estando parado en `demo/DB` (o cualquier directorio que no sea `demo/`). Solo el directorio `demo/` contiene el `pom.xml`.
- **Solución:**
  ```bash
  cd "C:\Users\asus\Desktop\Estudio\UNICEN\Arquis 2026\Entregables\EntregasArquitecturaWeb\demo"
  mvn dependency:resolve
  ```
  El **mismo `cd demo/` es necesario** para `mvn clean compile` y para `mvn exec:java ...`. Solo los comandos de **Docker** (`docker compose up/down/logs`) requieren estar en `demo/DB`.

---

## 📝 Consignas del Integrador 1

> Texto original de las consignas del enunciado del Ejercicio Integrador 1 (incluye consignas de otros dominios además de facturación, para referencia futura):

1) Considere el diseño de un registro de estudiantes, con la siguiente información: nombres,
apellido, edad, género, número de documento, ciudad de residencia, número de libreta
universitaria, carrera(s) en la que está inscripto, antigüedad en cada una de esas carreras, y
si se graduó o no. Diseñar el diagrama de objetos y el diagrama DER correspondiente.

2) Implementar consultas para:
- a) dar de alta un estudiante
- b) matricular un estudiante en una carrera
- c) recuperar todos los estudiantes, y especificar algún criterio de ordenamiento simple.
- d) recuperar un estudiante, en base a su número de libreta universitaria.
- e) recuperar todos los estudiantes, en base a su género.
- f) recuperar las carreras con estudiantes inscriptos, y ordenar por cantidad de inscriptos.
- g) recuperar los estudiantes de una determinada carrera, filtrado por ciudad de residencia.

3) Generar un reporte de las carreras, que para cada carrera incluya información de los
inscriptos y egresados por año. Se deben ordenar las carreras alfabéticamente, y presentar
los años de manera cronológica.

Nota: las consultas deben ser resueltas mayormente en JPQL, y no en código Java.
