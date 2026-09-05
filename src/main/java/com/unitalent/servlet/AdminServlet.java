package com.unitalent.servlet;

import com.unitalent.dao.EmpresaDAO;
import com.unitalent.dao.EstudianteDAO;
import com.unitalent.dao.OfertaDAO;
import com.unitalent.dao.PostulacionDAO;
import com.unitalent.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * GET /api/admin?tipo=stats       -> estadísticas generales
 * GET /api/admin?tipo=estudiantes -> lista de estudiantes
 * GET /api/admin?tipo=empresas    -> lista de empresas
 * GET /api/admin?tipo=ofertas     -> lista de todas las ofertas
 */
@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();
    private final OfertaDAO ofertaDAO = new OfertaDAO();
    private final PostulacionDAO postulacionDAO = new PostulacionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("rol"))) {
            JsonUtil.sendError(resp, 403, "Acceso solo para administradores.");
            return;
        }

        String tipo = req.getParameter("tipo");
        if (tipo == null) tipo = "stats";

        try {
            switch (tipo) {
                case "estudiantes":
                    JsonUtil.sendJson(resp, 200, estudianteDAO.listarTodos());
                    break;
                case "empresas":
                    JsonUtil.sendJson(resp, 200, empresaDAO.listarTodas());
                    break;
                case "ofertas":
                    JsonUtil.sendJson(resp, 200, ofertaDAO.listarTodas());
                    break;
                case "stats":
                default:
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("totalEstudiantes", estudianteDAO.listarTodos().size());
                    stats.put("totalEmpresas", empresaDAO.listarTodas().size());
                    stats.put("totalOfertas", ofertaDAO.listarTodas().size());
                    stats.put("totalPostulaciones", postulacionDAO.contarTotal());
                    stats.put("postulacionesEstado", postulacionDAO.contarPorEstado());
                    stats.put("ofertasModalidad", ofertaDAO.contarPorModalidad());
                    JsonUtil.sendJson(resp, 200, stats);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al obtener datos administrativos: " + e.getMessage());
        }
    }
}
