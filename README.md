# Ejercicio Integrador 1

## Punto 1 

### Cree un programa utilizando JDBC que cree el esquema de la base de datos.

### Tablas creadas:

- Cliente
- Producto
- Factura
- Factura_Producto

### Relaciones:
Cliente -> Factura -> Factura_Producto <- Producto

- Cliente.idCliente -> PK
- Producto.idProducto -> PK
- Factura.idFactura -> PK
- Factura.idCliente -> FK a Cliente
- Factura_Producto -> PK compuesta por idFactura + idProducto
- Factura_Producto.idFactura -> FK a Factura
- Factura_Producto.idProducto -> FK a Producto

### Creacion de tablas
Los DAO se crean en este orden: Cliente -> Producto -> Factura -> Factura_Producto
El orden se usa porque:
Factura depende de Cliente
Factura_Producto depende de Factura y Producto
Cada DAO ejecuta su CREATE TABLE IF NOT EXISTS al ser creado

## Verificacion
Desde MySQL:
```sql
SHOW TABLES;
SHOW CREATE TABLE Cliente;
SHOW CREATE TABLE Producto;
SHOW CREATE TABLE Factura;
SHOW CREATE TABLE Factura_Producto;
```

Resultado: 
Cliente
Factura
Factura_Producto
Producto