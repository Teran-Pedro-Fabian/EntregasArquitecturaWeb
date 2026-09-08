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

Archivo de configuración Docker: [docker-compose.yml](demo/DB/docker-compose.yml)

```bash
# 1. PARADO EN LA RAIZ DEL PROYECTO, entrar al directorio demo/DB
cd demo/DB

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
  1. Abrí [docker-compose.yml](demo/DB/docker-compose.yml) y cambiá el puerto expuesto, por ejemplo: `"3307:3306"`
  2. Abrí [MySQLConnectionManager.java](demo/src/main/java/com/example/repository/MySQL/MySQLConnectionManager.java#L25-L27) y actualizá la URL a:
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
# PARADO EN LA RAIZ DEL PROYECTO, entrar al directorio demo/
cd demo/

# Descargar todas las dependencias (mysql-connector, lombok, commons-csv)
mvn dependency:resolve
```
*(La primera vez descarga varios MBs; posteriores ejecuciones son instantáneas porque usa cache local).*

---

### Paso 3️⃣ — Compilar el proyecto

⚠️ **Directorio correcto:** `demo/` (mismo que el paso anterior).

```bash
# PARADO EN LA RAIZ DEL PROYECTO, entrar al directorio demo/
cd demo/

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

---

### Paso 4️⃣ — Ejecutar la aplicación (Main)

⚠️ **Directorio correcto:** `demo/`

```bash
# PARADO EN LA RAIZ DEL PROYECTO, entrar al directorio demo/
cd demo/

mvn exec:java "-Dexec.mainClass=com.example.Main"
```

#### Qué hace el Main ACTUALMENTE (consignas completadas):
> El `Main.java` actual ejecuta **todas las consignas del integrador** en este orden:
>
> 1️⃣ **Creación de tablas** → al instanciar los 4 DAOs (CREATE TABLE IF NOT EXISTS)
> 2️⃣ **Limpieza de datos** → `deleteAll()` en orden inverso de FK para evitar violaciones de integridad
> 3️⃣ **Carga masiva CSV** → 4 archivos de `demo/DB/Datos/` en orden correcto (respeta FK):
>    - `clientes.csv` → 100 registros
>    - `productos.csv` → 100 registros
>    - `facturas.csv` → 511 registros
>    - `facturas-productos.csv` → ~2590 registros
> 4️⃣ **Reporte 1** → Clientes ordenados por facturación total (DESC)
> 5️⃣ **Reporte 2** → Producto con mayor recaudación
> 6️⃣ **Reporte 3** → Facturas que contienen el producto `idProducto = 1`
> 7️⃣ **Cierre** → `factory.shutdown()` cierra la conexión de forma segura.

Código actual de [Main.java](demo/src/main/java/com/example/Main.java):
```java
// 1 - Instancia DAOs (crea tablas)
DAOFactory factory = DAOFactory.getInstance(DBType.MYSQL);
ClienteDAO clienteDAO = factory.createClienteDAO();
ProductoDAO productoDAO = factory.createProductoDAO();
FacturaDAO facturaDAO = factory.createFacturaDAO();
Factura_ProductoDAO fpDAO = factory.createFactura_ProductoDAO();

// 2 - Limpia datos previos
fpDAO.deleteAll();
facturaDAO.deleteAll();
productoDAO.deleteAll();
clienteDAO.deleteAll();

// 3 - Carga masiva CSV (Apache Commons CSV)
//   Clientes → Productos → Facturas → Factura_Producto

// 4 - Reporte 1: clientes por facturacion DESC
List<ClienteConFacturacion> r1 = clienteDAO.fintAllOrdenadoPorFacturacion();

// 5 - Reporte 2: producto con mayor recaudacion
ProductoMayorRecaudacion r2 = productoDAO.getProductoConMasRecaudacion();

// 6 - Reporte 3: facturas que tienen el producto id=1
List<Factura> r3 = facturaDAO.fintAllFacturasDeProducto(1);

factory.shutdown();
```

#### Nota importante sobre **PowerShell**:
> En PowerShell el flag `-D` se interpreta como parámetro propio de PS y rompe el comando Maven.
> ✅ **Usar el comando que ya figura arriba con comillas dobles** alrededor del `-D...`:
> ```powershell
> mvn exec:java "-Dexec.mainClass=com.example.Main"
> ```
> Si usás **CMD clásico** (no PowerShell), sí funciona el formato sin comillas.

#### Resultado ESPERADO en CONSOLA:
```
Conexión establecida correctamente con MySQL.
Tablas creadas correctamente.

Cargados 100 registros de Clientes
Cargados 100 registros de Productos
Cargados 511 registros de Facturas
Cargados 2590 registros de Factura_Producto

============================================================
REPORTE 1: Clientes ordenados por facturacion total (DESC)
============================================================
POS  | NOMBRE                              | EMAIL                                          | FACTURACION $
----------------------------------------------------------------------------------------------
1    | Xxxxxxx X. Xxxxxxx                  | xxxxx@xxxx.xxx                                 | $XXXX
...
(100 filas)

============================================================
REPORTE 2: Producto con mayor recaudacion
============================================================
Producto    : <nombre del producto>
Recaudacion : $XXXX

============================================================
REPORTE 3: Facturas que contienen el producto id=1
(Nombre del producto: "nisl sem,")
============================================================
NRO FACTURA     | ID CLIENTE
---------------------------------
XXX             | XX
...

Proceso completo - todas las consignas ejecutadas correctamente.
```

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
# PARADO EN LA RAIZ DEL PROYECTO, entrar a demo/DB para los comandos docker compose
cd demo/DB

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
| 4 | Main ejecuta sin error | ☐ | Consola muestra `Conexión establecida...` + contadores de carga + 3 reportes |
| 5 | Tablas creadas en BD | ☐ | `SHOW TABLES;` → `Cliente, Factura, Factura_Producto, Producto` |
| 6 | Datos CSV cargados correctamente | ☐ | `SELECT COUNT(*) FROM Cliente;` = 100, `Producto` = 100, `Factura` = 511 |
| 7 | Reportes del enunciado OK | ☐ | Consola imprime Reporte 1 (ranking clientes) + Reporte 2 (top producto) + Reporte 3 (facturas x producto) |

---

## ⚙️ Credenciales y configuración técnica (referencia)

Si en un futuro modificás credenciales o puertos, acordate de actualizar AMBOS archivos (son espejo):

1. [docker-compose.yml](demo/DB/docker-compose.yml) → define lo que usa Docker
2. [MySQLConnectionManager.java](demo/src/main/java/com/example/repository/MySQL/MySQLConnectionManager.java#L25-L27) → define lo que usa el código Java

Valores por defecto actuales (coinciden en ambos archivos):
```java
URL      = "jdbc:mysql://localhost:3306/facturacion";
USER     = "usuario";
PASSWORD = "usuario123";
```
*Nota: las credenciales están hardcodeadas por decisión de diseño para un TP académico — en producción deberían estar en variables de entorno.*

### Dependencias Maven declaradas (en [pom.xml](demo/pom.xml)):
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
  cd "\demo"
  mvn dependency:resolve
  ```
  El **mismo `cd demo/` es necesario** para `mvn clean compile` y para `mvn exec:java ...`. Solo los comandos de **Docker** (`docker compose up/down/logs`) requieren estar en `demo/DB`.

--- 
