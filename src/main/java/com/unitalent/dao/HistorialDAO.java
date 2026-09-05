package com.unitalent.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.unitalent.db.MongoConnection;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para las colecciones NoSQL (MongoDB): historial_busquedas y preferencias.
 */
public class HistorialDAO {

    private MongoCollection<Document> historial() {
        MongoDatabase db = MongoConnection.getDatabase();
        return db.getCollection("historial_busquedas");
    }

    private MongoCollection<Document> preferencias() {
        MongoDatabase db = MongoConnection.getDatabase();
        return db.getCollection("preferencias");
    }

    /** Registra una búsqueda realizada por un estudiante */
    public void registrarBusqueda(int idEstudiante, String texto, String carrera, String modalidad) {
        Document doc = new Document()
                .append("id_estudiante", idEstudiante)
                .append("texto", texto)
                .append("carrera", carrera)
                .append("modalidad", modalidad)
                .append("fecha", LocalDateTime.now().toString());
        historial().insertOne(doc);
    }

    public List<Document> obtenerHistorial(int idEstudiante) {
        List<Document> lista = new ArrayList<>();
        historial().find(Filters.eq("id_estudiante", idEstudiante))
                .sort(new Document("fecha", -1))
                .limit(20)
                .into(lista);
        return lista;
    }

    /** Guarda o actualiza las preferencias laborales del estudiante */
    public void guardarPreferencias(int idEstudiante, List<String> areas, String modalidad) {
        Document doc = new Document()
                .append("id_estudiante", idEstudiante)
                .append("areas", areas)
                .append("modalidad", modalidad);
        preferencias().replaceOne(
                Filters.eq("id_estudiante", idEstudiante),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public Document obtenerPreferencias(int idEstudiante) {
        return preferencias().find(Filters.eq("id_estudiante", idEstudiante)).first();
    }
}
