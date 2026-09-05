package com.unitalent.servlet;

import com.unitalent.dao.PostulacionDAO;
import com.unitalent.model.Postulacion;
import com.unitalent.util.JsonUtil;

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
 * GET  /api/postulaciones                 -> lista postulaciones del usuario logueado
 *      /api/postulaciones?idOferta=X      -> postulantes de una oferta (empresa, dueña de la oferta)
 * POST /api/postulaciones                 -> el estudiante postula (body: idOferta)
 * PUT  /api/postulaciones                 -> la empresa cambia el estado (body: idPostulacion, estado)
 */
@WebServlet("/api/postulaciones")
public class PostulacionServlet extends HttpServlet {

    private final PostulacionDAO postulacionDAO = new PostulacionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            JsonUtil.sendError(resp, 401, "Debes iniciar sesión.");
            return;
        }
        String rol = (String) session.getAttribute("rol");

        try {
            String idOfertaParam = req.getParameter("idOferta");

            if ("ESTUDIANTE".equals(rol)) {
                int idEstudiante = (int) session.getAttribute("idEstudiante");
                List<Postulacion> lista = postulacionDAO.listarPorEstudiante(idEstudiante);
                JsonUtil.sendJson(resp, 200, lista);

            } else if ("EMPRESA".equals(rol)) {
                int idEmpresa = (int) session.getAttribute("idEmpresa");
                if (idOfertaParam != null) {
                    List<Postulacion> lista = postulacionDAO.listarPorOferta(Integer.parseInt(idOfertaParam));
                    JsonUtil.sendJson(resp, 200, lista);
                } else {
                    List<Postulacion> lista = postulacionDAO.listarPorEmpresa(idEmpresa);
                    JsonUtil.sendJson(resp, 200, lista);
                }
            } else {
                JsonUtil.sendError(resp, 403, "Rol no autorizado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al obtener postulaciones: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"ESTUDIANTE".equals(session.getAttribute("rol"))) {
            JsonUtil.sendError(resp, 403, "Solo los estudiantes pueden postular a ofertas.");
            return;
        }
        Map<String, Object> body = JsonUtil.readBody(req);
        try {
            int idOferta = (int) Double.parseDouble(body.get("idOferta").toString());
            int idEstudiante = (int) session.getAttribute("idEstudiante");

            int id = postulacionDAO.postular(idEstudiante, idOferta);
            if (id == -1) {
                JsonUtil.sendError(resp, 409, "Ya postulaste a esta oferta anteriormente.");
                return;
            }
            JsonUtil.sendJson(resp, 201, Map.of("ok", true, "idPostulacion", id, "message", "Postulación enviada correctamente."));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al postular: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"EMPRESA".equals(session.getAttribute("rol"))) {
            JsonUtil.sendError(resp, 403, "Solo las empresas pueden actualizar el estado de una postulación.");
            return;
        }
        Map<String, Object> body = JsonUtil.readBody(req);
        try {
            int idPostulacion = (int) Double.parseDouble(body.get("idPostulacion").toString());
            String estado = body.get("estado").toString(); // Enviada, En Proceso, Aceptada, Rechazada

            postulacionDAO.actualizarEstado(idPostulacion, estado);
            JsonUtil.sendJson(resp, 200, Map.of("ok", true, "message", "Estado actualizado a: " + estado));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al actualizar el estado: " + e.getMessage());
        }
    }
}
