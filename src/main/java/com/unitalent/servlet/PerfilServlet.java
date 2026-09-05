package com.unitalent.servlet;

import com.unitalent.dao.EmpresaDAO;
import com.unitalent.dao.EstudianteDAO;
import com.unitalent.model.Empresa;
import com.unitalent.model.Estudiante;
import com.unitalent.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * GET  /api/perfil  -> devuelve el perfil del usuario logueado (estudiante o empresa)
 * PUT  /api/perfil  -> actualiza el perfil del usuario logueado
 */
@WebServlet("/api/perfil")
public class PerfilServlet extends HttpServlet {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            JsonUtil.sendError(resp, 401, "Debes iniciar sesión.");
            return;
        }
        String rol = (String) session.getAttribute("rol");
        int idUsuario = (int) session.getAttribute("idUsuario");

        try {
            if ("ESTUDIANTE".equals(rol)) {
                Estudiante e = estudianteDAO.buscarPorIdUsuario(idUsuario);
                JsonUtil.sendJson(resp, 200, e);
            } else if ("EMPRESA".equals(rol)) {
                Empresa e = empresaDAO.buscarPorIdUsuario(idUsuario);
                JsonUtil.sendJson(resp, 200, e);
            } else {
                JsonUtil.sendError(resp, 400, "Los administradores no tienen perfil editable.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al obtener el perfil: " + ex.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            JsonUtil.sendError(resp, 401, "Debes iniciar sesión.");
            return;
        }
        String rol = (String) session.getAttribute("rol");
        int idUsuario = (int) session.getAttribute("idUsuario");
        Map<String, Object> body = JsonUtil.readBody(req);

        try {
            if ("ESTUDIANTE".equals(rol)) {
                Estudiante e = new Estudiante();
                e.setIdUsuario(idUsuario);
                e.setCarrera(strOf(body.get("carrera")));
                Object ciclo = body.get("ciclo");
                e.setCiclo(ciclo != null && !ciclo.toString().isEmpty() ? (int) Double.parseDouble(ciclo.toString()) : null);
                e.setCv(strOf(body.get("cv")));
                e.setTelefono(strOf(body.get("telefono")));
                estudianteDAO.actualizarPerfil(e);
            } else if ("EMPRESA".equals(rol)) {
                Empresa e = new Empresa();
                e.setIdUsuario(idUsuario);
                e.setNombreEmpresa(strOf(body.get("nombreEmpresa")));
                e.setRubro(strOf(body.get("rubro")));
                e.setDescripcion(strOf(body.get("descripcion")));
                empresaDAO.actualizarPerfil(e);
            } else {
                JsonUtil.sendError(resp, 400, "Los administradores no tienen perfil editable.");
                return;
            }
            JsonUtil.sendJson(resp, 200, Map.of("ok", true, "message", "Perfil actualizado correctamente."));
        } catch (Exception ex) {
            ex.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al actualizar el perfil: " + ex.getMessage());
        }
    }

    private String strOf(Object o) {
        return o == null ? null : o.toString();
    }
}
