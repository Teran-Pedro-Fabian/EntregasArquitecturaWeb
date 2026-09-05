package com.example;

import com.example.factory.DAOFactory;
import com.example.factory.DBType;

public class Main {
    public static void main(String[] args) {


        DAOFactory factory = DAOFactory.getInstance(DBType.MYSQL);

        factory.createClienteDAO();
        factory.createProductoDAO();
        factory.createFacturaDAO();
        factory.createFactura_ProductoDAO();

        factory.shutdown();

        System.out.println("Hello world!");
    }
}