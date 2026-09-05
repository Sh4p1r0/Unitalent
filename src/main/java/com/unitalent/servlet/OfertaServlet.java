package com.unitalent.servlet;

import com.unitalent.dao.HistorialDAO;
import com.unitalent.dao.OfertaDAO;
import com.unitalent.model.Oferta;
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
 * GET    /api/ofertas                -> lista/busca ofertas activas (estudiante) o propias (empresa)
 * GET    /api/ofertas?id=X           -> detalle de una oferta
 * POST   /api/ofertas                -> crea una nueva oferta (empresa)
 * PUT    /api/ofertas                -> edita una oferta propia (empresa), body debe traer idOferta
 * DELETE /api/ofertas?id=X           -> elimina una oferta propia (empresa)
 */
@WebServlet("/api/ofertas")
public class OfertaServlet extends HttpServlet {

    private final OfertaDAO ofertaDAO = new OfertaDAO();
    private final HistorialDAO historialDAO = new HistorialDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String idParam = req.getParameter("id");

        try {
            if (idParam != null) {
                Oferta o = ofertaDAO.buscarPorId(Integer.parseInt(idParam));
                JsonUtil.sendJson(resp, 200, o);
                return;
            }

            String rol = session != null ? (String) session.getAttribute("rol") : null;

            if ("EMPRESA".equals(rol)) {
                int idEmpresa = (int) session.getAttribute("idEmpresa");
                List<Oferta> lista = ofertaDAO.listarPorEmpresa(idEmpresa);
                JsonUtil.sendJson(resp, 200, lista);
                return;
            }

            // Estudiante o visitante: búsqueda/filtro de ofertas activas
            String texto = req.getParameter("q");
            String carrera = req.getParameter("carrera");
            String modalidad = req.getParameter("modalidad");
            
            boolean hayFiltros = (texto != null && !texto.isEmpty()) || (carrera != null && !carrera.isEmpty()) || (modalidad != null && !modalidad.isEmpty());

            if (!hayFiltros && session != null && "ESTUDIANTE".equals(rol)) {
                // Aplicar preferencias de MongoDB automáticamente si no hay filtros explícitos
                int idEstudiante = (int) session.getAttribute("idEstudiante");
                org.bson.Document prefs = historialDAO.obtenerPreferencias(idEstudiante);
                if (prefs != null) {
                    modalidad = prefs.getString("modalidad");
                    List<String> areas = prefs.getList("areas", String.class);
                    if (areas != null && !areas.isEmpty()) {
                        // Tomamos el primer área como carrera para el filtro simple
                        carrera = areas.get(0);
                    }
                }
            }

            List<Oferta> lista = ofertaDAO.buscar(texto, carrera, modalidad);
            JsonUtil.sendJson(resp, 200, lista);

            // Registrar búsqueda en MongoDB si el estudiante está logueado y mandó algún filtro explícito
            if (session != null && "ESTUDIANTE".equals(rol) && hayFiltros) {
                int idEstudiante = (int) session.getAttribute("idEstudiante");
                historialDAO.registrarBusqueda(idEstudiante, texto, carrera, modalidad);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al obtener ofertas: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!esEmpresa(session)) {
            JsonUtil.sendError(resp, 403, "Solo las empresas pueden publicar ofertas.");
            return;
        }
        Map<String, Object> body = JsonUtil.readBody(req);
        try {
            Oferta o = new Oferta();
            o.setIdEmpresa((int) session.getAttribute("idEmpresa"));
            o.setTitulo(strOf(body.get("titulo")));
            o.setDescripcion(strOf(body.get("descripcion")));
            o.setCarrera(strOf(body.get("carrera")));
            o.setModalidad(strOf(body.get("modalidad")));

            if (o.getTitulo() == null || o.getTitulo().isEmpty()) {
                JsonUtil.sendError(resp, 400, "El título de la oferta es obligatorio.");
                return;
            }

            int id = ofertaDAO.insertar(o);
            JsonUtil.sendJson(resp, 201, Map.of("ok", true, "idOferta", id, "message", "Oferta publicada correctamente."));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al publicar la oferta: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!esEmpresa(session)) {
            JsonUtil.sendError(resp, 403, "Solo las empresas pueden editar ofertas.");
            return;
        }
        Map<String, Object> body = JsonUtil.readBody(req);
        try {
            Oferta o = new Oferta();
            o.setIdOferta((int) Double.parseDouble(body.get("idOferta").toString()));
            o.setIdEmpresa((int) session.getAttribute("idEmpresa"));
            o.setTitulo(strOf(body.get("titulo")));
            o.setDescripcion(strOf(body.get("descripcion")));
            o.setCarrera(strOf(body.get("carrera")));
            o.setModalidad(strOf(body.get("modalidad")));
            o.setEstado(strOf(body.get("estado")));

            ofertaDAO.actualizar(o);
            JsonUtil.sendJson(resp, 200, Map.of("ok", true, "message", "Oferta actualizada correctamente."));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al actualizar la oferta: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!esEmpresa(session)) {
            JsonUtil.sendError(resp, 403, "Solo las empresas pueden eliminar ofertas.");
            return;
        }
        try {
            int idOferta = Integer.parseInt(req.getParameter("id"));
            int idEmpresa = (int) session.getAttribute("idEmpresa");
            ofertaDAO.eliminar(idOferta, idEmpresa);
            JsonUtil.sendJson(resp, 200, Map.of("ok", true, "message", "Oferta eliminada."));
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, 500, "Error al eliminar la oferta: " + e.getMessage());
        }
    }

    private boolean esEmpresa(HttpSession session) {
        return session != null && "EMPRESA".equals(session.getAttribute("rol")) && session.getAttribute("idEmpresa") != null;
    }

    private String strOf(Object o) {
        return o == null ? null : o.toString();
    }
}
