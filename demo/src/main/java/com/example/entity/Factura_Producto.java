package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Factura_Producto {

    private int idFactura;  /* FK-->Factura */
    private int idProducto;  /* FK-->Producto */
    private int cantidad;
}
