package com.unitalent.dao;

import com.unitalent.db.PostgresConnection;
import com.unitalent.model.Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public void insertar(Connection con, int idUsuario, String carrera, Integer ciclo) throws SQLException {
        String sql = "INSERT INTO estudiantes (id_usuario, carrera, ciclo) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, carrera);
            if (ciclo != null) ps.setInt(3, ciclo); else ps.setNull(3, Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public Estudiante buscarPorIdUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT e.*, u.nombre, u.correo FROM estudiantes e " +
                "JOIN usuarios u ON u.id_usuario = e.id_usuario WHERE e.id_usuario = ?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Estudiante buscarPorId(int idEstudiante) throws SQLException {
        String sql = "SELECT e.*, u.nombre, u.correo FROM estudiantes e " +
                "JOIN usuarios u ON u.id_usuario = e.id_usuario WHERE e.id_estudiante = ?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void actualizarPerfil(Estudiante e) throws SQLException {
        String sql = "UPDATE estudiantes SET carrera=?, ciclo=?, cv=?, telefono=? WHERE id_usuario=?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getCarrera());
            if (e.getCiclo() != null) ps.setInt(2, e.getCiclo()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, e.getCv());
            ps.setString(4, e.getTelefono());
            ps.setInt(5, e.getIdUsuario());
            ps.executeUpdate();
        }
    }

    public List<Estudiante> listarTodos() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        String sql = "SELECT e.*, u.nombre, u.correo FROM estudiantes e JOIN usuarios u ON u.id_usuario = e.id_usuario ORDER BY e.id_estudiante DESC";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapRow(rs));
        }
        return lista;
    }

    private Estudiante mapRow(ResultSet rs) throws SQLException {
        Estudiante e = new Estudiante();
        e.setIdEstudiante(rs.getInt("id_estudiante"));
        e.setIdUsuario(rs.getInt("id_usuario"));
        e.setNombre(rs.getString("nombre"));
        e.setCorreo(rs.getString("correo"));
        e.setCarrera(rs.getString("carrera"));
        int ciclo = rs.getInt("ciclo");
        e.setCiclo(rs.wasNull() ? null : ciclo);
        e.setCv(rs.getString("cv"));
        e.setTelefono(rs.getString("telefono"));
        return e;
    }
}
