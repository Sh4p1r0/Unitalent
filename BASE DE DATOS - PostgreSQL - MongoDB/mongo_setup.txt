
use unitalent;


db.createCollection("historial_busquedas");


db.createCollection("preferencias");


db.historial_busquedas.createIndex({ id_estudiante: 1 });
db.preferencias.createIndex({ id_estudiante: 1 }, { unique: true });

print("Base de datos 'unitalent' y colecciones creadas correctamente.");
