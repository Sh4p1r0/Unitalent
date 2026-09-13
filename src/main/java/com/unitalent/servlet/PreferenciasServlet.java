package com.unitalent.servlet;

import com.unitalent.dao.HistorialDAO;
import com.unitalent.util.JsonUtil;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Demuestra el uso de MongoDB (NoSQL) desde la app:
 * GET /api/preferencias -> preferencias guardadas + historial de búsquedas del
 * estudiante
 * POST /api/preferencias -> guarda/actualiza preferencias (body: areas[],
 * modalidad)
 */
@WebServlet("/api/preferencias")
public class PreferenciasServlet extends HttpServlet {

    private final HistorialDAO historialDAO = new HistorialDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            HttpSession session = req.getSession(false);
            if (session == null || !"ESTUDIANTE".equals(session.getAttribute("rol"))) {
                JsonUtil.sendError(resp, 403, "Solo disponible para estudiantes.");
                return;
            }
            int idEstudiante = (int) session.getAttribute("idEstudiante");

            Document prefs = historialDAO.obtenerPreferencias(idEstudiante);
            List<Document> historial = historialDAO.obtenerHistorial(idEstudiante);

            Map<String, Object> out = Map.of(
                    "preferencias", prefs != null ? prefs : Map.of(),
                    "historial", historial);
            JsonUtil.sendJson(resp, 200, out);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al obtener preferencias: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            HttpSession session = req.getSession(false);
            if (session == null || !"ESTUDIANTE".equals(session.getAttribute("rol"))) {
                JsonUtil.sendError(resp, 403, "Solo disponible para estudiantes.");
                return;
            }
            int idEstudiante = (int) session.getAttribute("idEstudiante");
            Map<String, Object> body = JsonUtil.readBody(req);

            List<String> areas = (List<String>) body.getOrDefault("areas", List.of());
            String modalidad = body.get("modalidad") == null ? "" : body.get("modalidad").toString();

            historialDAO.guardarPreferencias(idEstudiante, areas, modalidad);
            JsonUtil.sendJson(resp, 200, Map.of("ok", true, "message", "Preferencias guardadas."));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al guardar preferencias: " + e.getMessage());
        }
    }
}
