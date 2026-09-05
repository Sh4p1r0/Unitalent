package com.unitalent.db;

import com.unitalent.dao.HistorialDAO;
import com.unitalent.util.JsonUtil;
import org.bson.Document;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

public class TestPreferencias {
    public static void main(String[] args) {
        try {
            System.out.println("Test GET prefererencias...");
            HistorialDAO dao = new HistorialDAO();

            Document prefs = dao.obtenerPreferencias(1);
            List<Document> historial = dao.obtenerHistorial(1);

            Map<String, Object> out = Map.of(
                    "preferencias", prefs != null ? Document.parse(prefs.toJson()) : Map.of(),
                    "historial", historial);

            Gson gson = new Gson();
            String json = gson.toJson(out);
            System.out.println("JSON GENERADO: " + json);
            System.out.println("Exito!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
