package com.unitalent.servlet;

import com.unitalent.dao.EmpresaDAO;
import com.unitalent.dao.EstudianteDAO;
import com.unitalent.dao.UsuarioDAO;
import com.unitalent.model.Usuario;
import com.unitalent.util.JsonUtil;
import com.unitalent.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> body = JsonUtil.readBody(req);
        String correo = body.get("correo") == null ? null : body.get("correo").toString().trim();
        String contrasena = body.get("contrasena") == null ? null : body.get("contrasena").toString();

        if (correo == null || contrasena == null) {
            JsonUtil.sendError(resp, 400, "Correo y contraseña son obligatorios.");
            return;
        }

        try {
            Usuario u = usuarioDAO.buscarPorCorreo(correo);
            if (u == null || !PasswordUtil.verify(contrasena, u.getContrasena())) {
                JsonUtil.sendError(resp, 401, "Correo o contraseña incorrectos.");
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("idUsuario", u.getIdUsuario());
            session.setAttribute("nombre", u.getNombre());
            session.setAttribute("correo", u.getCorreo());
            session.setAttribute("rol", u.getRol());

            Map<String, Object> out = new HashMap<>();
            out.put("ok", true);
            out.put("idUsuario", u.getIdUsuario());
            out.put("nombre", u.getNombre());
            out.put("correo", u.getCorreo());
            out.put("rol", u.getRol());

            if (u.getRol().equals("ESTUDIANTE")) {
                int idEst = estudianteDAO.buscarPorIdUsuario(u.getIdUsuario()).getIdEstudiante();
                session.setAttribute("idEstudiante", idEst);
                out.put("idEstudiante", idEst);
            } else if (u.getRol().equals("EMPRESA")) {
                int idEmp = empresaDAO.buscarPorIdUsuario(u.getIdUsuario()).getIdEmpresa();
                session.setAttribute("idEmpresa", idEmp);
                out.put("idEmpresa", idEmp);
            }

            JsonUtil.sendJson(resp, 200, out);

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al iniciar sesión: " + e.getMessage());
        }
    }

    /** Permite comprobar si ya hay una sesión activa (usado por las páginas al cargar) */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Map<String, Object> out = new HashMap<>();
        if (session == null || session.getAttribute("idUsuario") == null) {
            out.put("ok", false);
            JsonUtil.sendJson(resp, 200, out);
            return;
        }
        out.put("ok", true);
        out.put("idUsuario", session.getAttribute("idUsuario"));
        out.put("nombre", session.getAttribute("nombre"));
        out.put("correo", session.getAttribute("correo"));
        out.put("rol", session.getAttribute("rol"));
        out.put("idEstudiante", session.getAttribute("idEstudiante"));
        out.put("idEmpresa", session.getAttribute("idEmpresa"));
        JsonUtil.sendJson(resp, 200, out);
    }
}
