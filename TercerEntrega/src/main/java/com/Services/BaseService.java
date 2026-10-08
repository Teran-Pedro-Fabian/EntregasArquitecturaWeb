package com.Services;

import com.Entitys.EstudianteCarrera;

import java.util.List;

public interface BaseService <P>{

    /**
     * Servicio encargado de retornar un listado completo de una entidad.
     *
     * @return Listado con entidades.
     * @throws Exception e
     */
    public List<P> findAll()throws Exception;

    /**
     * Servicio encargado de buscar y retornar una entidad coincidente con el id ingresado por parámetro.
     *
     * @param id Identificador únido de la entidad.
     * @return Entidad coincidente con id.
     * @throws Exception e
     */
    public P findById(int id)throws Exception;

    /**
     * Servicio encargado de persistir una entidad ingresada por parámetro.
     * @param entity entidad a persistir
     * @return Entidad persistida con id asignado.
     * @throws Exception e
     */
    public P save(P entity)throws  Exception;

    /**
     * Servicio encargado de actualizar una entidad.
     * @param id Identificador único de la entidad a actualizar.
     * @param entity Entidad con los datos a a actualizar.
     * @return Retorna la entidad actualizada.
     * @throws Exception e
     */
    public P update(Long id, P entity)throws Exception;

    /**
     * Servicio encargado de eliminar una entidad correspondiente al id ingresado por parámetro.
     * @param id Identificador único de la entidad a eliminar.
     * @return True en caso de eliminación exitosa, caso contrario false.
     * @throws Exception e
     */
    public boolean delete(Long id)throws Exception;


}
