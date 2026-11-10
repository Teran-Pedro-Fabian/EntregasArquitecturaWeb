function leer(nombre, campos) {
  return db.getCollection(nombre).find({}).toArray().map(function (d) {
    const fila = { id: d._id.toString() };
    for (const campo of campos) {
      const valor = d[campo];
      fila[campo] = (valor === undefined || valor === null) ? null :
                    ((campo === 'idLugar') ? valor.toString() : valor);
    }
    return fila;
  });
}
print(JSON.stringify({
  evento: leer('evento', ['nombre','descripcion','tipo','organizador','fechaHora','idLugar','estado','demanda']),
  participantes: leer('participantes', ['nombre']),
  participante_evento: leer('participante_evento', ['idEvento','idParticipante'])
}));
