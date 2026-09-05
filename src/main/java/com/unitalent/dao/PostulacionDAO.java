package com.unitalent.dao;

import com.unitalent.db.PostgresConnection;
import com.unitalent.model.Postulacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostulacionDAO {

    /** Devuelve el id de la nueva postulación, o -1 si ya existía (evita duplicados) */
    public int postular(int idEstudiante, int idOferta) throws SQLException {
        String checkSql = "SELECT id_postulacion FROM postulaciones WHERE id_estudiante=? AND id_oferta=?";
        String insertSql = "INSERT INTO postulaciones (id_estudiante, id_oferta, estado) VALUES (?,?,'Enviada') RETURNING id_postulacion";

        try (Connection con = PostgresConnection.getConnection()) {
            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setInt(1, idEstudiante);
                check.setInt(2, idOferta);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) return -1; // ya postuló
                }
            }
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, idEstudiante);
                ps.setInt(2, idOferta);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }
    }

    public void actualizarEstado(int idPostulacion, String estado) throws SQLException {
        String sql = "UPDATE postulaciones SET estado=? WHERE id_postulacion=?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idPostulacion);
            ps.executeUpdate();
        }
    }

    /** Postulaciones realizadas por un estudiante (para su panel "Mis postulaciones") */
    public List<Postulacion> listarPorEstudiante(int idEstudiante) throws SQLException {
        List<Postulacion> lista = new ArrayList<>();
        String sql = "SELECT p.*, o.titulo AS titulo_oferta, emp.nombre_empresa " +
                "FROM postulaciones p " +
                "JOIN ofertas o ON o.id_oferta = p.id_oferta " +
                "JOIN empresas emp ON emp.id_empresa = o.id_empresa " +
                "WHERE p.id_estudiante = ? ORDER BY p.fecha_postulacion DESC";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Postulacion p = mapRowBasic(rs);
                    p.setTituloOferta(rs.getString("titulo_oferta"));
                    p.setNombreEmpresa(rs.getString("nombre_empresa"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    /** Postulantes que aplicaron a una oferta específica (para el panel de la empresa) */
    public List<Postulacion> listarPorOferta(int idOferta) throws SQLException {
        List<Postulacion> lista = new ArrayList<>();
        String sql = "SELECT p.*, u.nombre AS nombre_estudiante, u.correo AS correo_estudiante, " +
                "est.carrera AS carrera_estudiante, est.cv AS cv_estudiante " +
                "FROM postulaciones p " +
                "JOIN estudiantes est ON est.id_estudiante = p.id_estudiante " +
                "JOIN usuarios u ON u.id_usuario = est.id_usuario " +
                "WHERE p.id_oferta = ? ORDER BY p.fecha_postulacion DESC";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Postulacion p = mapRowBasic(rs);
                    p.setNombreEstudiante(rs.getString("nombre_estudiante"));
                    p.setCorreoEstudiante(rs.getString("correo_estudiante"));
                    p.setCarreraEstudiante(rs.getString("carrera_estudiante"));
                    p.setCvEstudiante(rs.getString("cv_estudiante"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    /** Todas las postulaciones de todas las ofertas publicadas por una empresa */
    public List<Postulacion> listarPorEmpresa(int idEmpresa) throws SQLException {
        List<Postulacion> lista = new ArrayList<>();
        String sql = "SELECT p.*, o.titulo AS titulo_oferta, u.nombre AS nombre_estudiante, " +
                "u.correo AS correo_estudiante, est.carrera AS carrera_estudiante, est.cv AS cv_estudiante " +
                "FROM postulaciones p " +
                "JOIN ofertas o ON o.id_oferta = p.id_oferta " +
                "JOIN estudiantes est ON est.id_estudiante = p.id_estudiante " +
                "JOIN usuarios u ON u.id_usuario = est.id_usuario " +
                "WHERE o.id_empresa = ? ORDER BY p.fecha_postulacion DESC";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Postulacion p = mapRowBasic(rs);
                    p.setTituloOferta(rs.getString("titulo_oferta"));
                    p.setNombreEstudiante(rs.getString("nombre_estudiante"));
                    p.setCorreoEstudiante(rs.getString("correo_estudiante"));
                    p.setCarreraEstudiante(rs.getString("carrera_estudiante"));
                    p.setCvEstudiante(rs.getString("cv_estudiante"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM postulaciones";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public java.util.Map<String, Integer> contarPorEstado() throws SQLException {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        String sql = "SELECT estado, COUNT(*) as cantidad FROM postulaciones GROUP BY estado";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("estado"), rs.getInt("cantidad"));
            }
        }
        return map;
    }

    private Postulacion mapRowBasic(ResultSet rs) throws SQLException {
        Postulacion p = new Postulacion();
        p.setIdPostulacion(rs.getInt("id_postulacion"));
        p.setIdEstudiante(rs.getInt("id_estudiante"));
        p.setIdOferta(rs.getInt("id_oferta"));
        Date fecha = rs.getDate("fecha_postulacion");
        p.setFechaPostulacion(fecha != null ? fecha.toString() : null);
        p.setEstado(rs.getString("estado"));
        return p;
    }
}
