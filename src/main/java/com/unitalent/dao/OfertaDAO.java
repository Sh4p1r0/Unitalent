package com.unitalent.dao;

import com.unitalent.db.PostgresConnection;
import com.unitalent.model.Oferta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OfertaDAO {

    private static final String BASE_SELECT =
            "SELECT o.*, emp.nombre_empresa, " +
            "(SELECT COUNT(*) FROM postulaciones p WHERE p.id_oferta = o.id_oferta) AS total_postulantes " +
            "FROM ofertas o JOIN empresas emp ON emp.id_empresa = o.id_empresa ";

    public int insertar(Oferta o) throws SQLException {
        String sql = "INSERT INTO ofertas (id_empresa, titulo, descripcion, carrera, modalidad, estado) " +
                "VALUES (?,?,?,?,?, 'Activa') RETURNING id_oferta";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, o.getIdEmpresa());
            ps.setString(2, o.getTitulo());
            ps.setString(3, o.getDescripcion());
            ps.setString(4, o.getCarrera());
            ps.setString(5, o.getModalidad());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Oferta o) throws SQLException {
        String sql = "UPDATE ofertas SET titulo=?, descripcion=?, carrera=?, modalidad=?, estado=? " +
                "WHERE id_oferta=? AND id_empresa=?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, o.getTitulo());
            ps.setString(2, o.getDescripcion());
            ps.setString(3, o.getCarrera());
            ps.setString(4, o.getModalidad());
            ps.setString(5, o.getEstado());
            ps.setInt(6, o.getIdOferta());
            ps.setInt(7, o.getIdEmpresa());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idOferta, int idEmpresa) throws SQLException {
        String sql = "DELETE FROM ofertas WHERE id_oferta=? AND id_empresa=?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            ps.setInt(2, idEmpresa);
            ps.executeUpdate();
        }
    }

    public Oferta buscarPorId(int idOferta) throws SQLException {
        String sql = BASE_SELECT + " WHERE o.id_oferta = ?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Oferta> listarPorEmpresa(int idEmpresa) throws SQLException {
        List<Oferta> lista = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE o.id_empresa = ? ORDER BY o.id_oferta DESC";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    /** Búsqueda / filtro de ofertas activas para estudiantes */
    public List<Oferta> buscar(String texto, String carrera, String modalidad) throws SQLException {
        List<Oferta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE o.estado = 'Activa' ");
        List<String> params = new ArrayList<>();

        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (o.titulo ILIKE ? OR o.descripcion ILIKE ?) ");
            params.add("%" + texto.trim() + "%");
            params.add("%" + texto.trim() + "%");
        }
        if (carrera != null && !carrera.trim().isEmpty()) {
            sql.append(" AND o.carrera ILIKE ? ");
            params.add("%" + carrera.trim() + "%");
        }
        if (modalidad != null && !modalidad.trim().isEmpty()) {
            sql.append(" AND o.modalidad = ? ");
            params.add(modalidad.trim());
        }
        sql.append(" ORDER BY o.fecha_publicacion DESC");

        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public List<Oferta> listarTodas() throws SQLException {
        List<Oferta> lista = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY o.id_oferta DESC";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapRow(rs));
        }
        return lista;
    }

    public java.util.Map<String, Integer> contarPorModalidad() throws SQLException {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        String sql = "SELECT modalidad, COUNT(*) as cantidad FROM ofertas GROUP BY modalidad";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String mod = rs.getString("modalidad");
                if (mod == null) mod = "No especificado";
                map.put(mod, rs.getInt("cantidad"));
            }
        }
        return map;
    }

    private Oferta mapRow(ResultSet rs) throws SQLException {
        Oferta o = new Oferta();
        o.setIdOferta(rs.getInt("id_oferta"));
        o.setIdEmpresa(rs.getInt("id_empresa"));
        o.setNombreEmpresa(rs.getString("nombre_empresa"));
        o.setTitulo(rs.getString("titulo"));
        o.setDescripcion(rs.getString("descripcion"));
        o.setCarrera(rs.getString("carrera"));
        o.setModalidad(rs.getString("modalidad"));
        o.setEstado(rs.getString("estado"));
        Date fecha = rs.getDate("fecha_publicacion");
        o.setFechaPublicacion(fecha != null ? fecha.toString() : null);
        o.setTotalPostulantes(rs.getInt("total_postulantes"));
        return o;
    }
}
