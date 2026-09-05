package com.unitalent.servlet;

import com.unitalent.dao.EmpresaDAO;
import com.unitalent.dao.EstudianteDAO;
import com.unitalent.dao.UsuarioDAO;
import com.unitalent.db.PostgresConnection;
import com.unitalent.model.Usuario;
import com.unitalent.util.JsonUtil;
import com.unitalent.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, Object> body = JsonUtil.readBody(req);

        String nombre = str(body.get("nombre"));
        String correo = str(body.get("correo"));
        String contrasena = str(body.get("contrasena"));
        String rol = str(body.get("rol")); // ESTUDIANTE o EMPRESA

        if (nombre == null || correo == null || contrasena == null || rol == null) {
            JsonUtil.sendError(resp, 400, "Todos los campos son obligatorios.");
            return;
        }
        rol = rol.toUpperCase();
        if (!rol.equals("ESTUDIANTE") && !rol.equals("EMPRESA") && !rol.equals("ADMIN")) {
            JsonUtil.sendError(resp, 400, "Rol inválido.");
            return;
        }

        try {
            if (usuarioDAO.buscarPorCorreo(correo) != null) {
                JsonUtil.sendError(resp, 409, "Ya existe una cuenta registrada con ese correo.");
                return;
            }

            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setCorreo(correo);
            u.setContrasena(PasswordUtil.hash(contrasena));
            u.setRol(rol);

            try (Connection con = PostgresConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    int idUsuario = usuarioDAO.insertar(con, u);

                    if (rol.equals("ESTUDIANTE")) {
                        String carrera = str(body.get("carrera"));
                        Integer ciclo = null;
                        Object cicloObj = body.get("ciclo");
                        if (cicloObj != null && !cicloObj.toString().isEmpty()) {
                            ciclo = (int) Double.parseDouble(cicloObj.toString());
                        }
                        estudianteDAO.insertar(con, idUsuario, carrera, ciclo);
                    } else if (rol.equals("EMPRESA")) {
                        String nombreEmpresa = str(body.get("nombreEmpresa"));
                        String rubro = str(body.get("rubro"));
                        String descripcion = str(body.get("descripcion"));
                        empresaDAO.insertar(con, idUsuario, nombreEmpresa, rubro, descripcion);
                    }

                    con.commit();
                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            }

            Map<String, Object> out = new HashMap<>();
            out.put("ok", true);
            out.put("message", "Cuenta creada correctamente. Ya puedes iniciar sesión.");
            JsonUtil.sendJson(resp, 201, out);

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al registrar: " + e.getMessage());
        }
    }

    private String str(Object o) {
        return o == null ? null : o.toString().trim();
    }
}
