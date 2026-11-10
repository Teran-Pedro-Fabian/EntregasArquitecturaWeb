function leer(nombre, campos) {
  return db.getCollection(nombre).find({}).toArray().map(function (d) {
    const fila = { id: d._id.toString() };
    for (const campo of campos) {
      const valor = d[campo];
      fila[campo] = (valor === undefined || valor === null) ? null : valor.toString();
    }
    return fila;
  });
}
print(JSON.stringify({
  Cliente: leer('Cliente', ['nombre']),
  preferencias: leer('preferencias', ['idCliente','idEvento','idParticipante','idLugar'])
}));
