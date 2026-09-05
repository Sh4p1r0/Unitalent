package com.unitalent.dao;

import com.unitalent.db.PostgresConnection;
import com.unitalent.model.Empresa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpresaDAO {

    public void insertar(Connection con, int idUsuario, String nombreEmpresa, String rubro, String descripcion) throws SQLException {
        String sql = "INSERT INTO empresas (id_usuario, nombre_empresa, rubro, descripcion) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, nombreEmpresa);
            ps.setString(3, rubro);
            ps.setString(4, descripcion);
            ps.executeUpdate();
        }
    }

    public Empresa buscarPorIdUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT emp.*, u.nombre, u.correo FROM empresas emp " +
                "JOIN usuarios u ON u.id_usuario = emp.id_usuario WHERE emp.id_usuario = ?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Empresa buscarPorId(int idEmpresa) throws SQLException {
        String sql = "SELECT emp.*, u.nombre, u.correo FROM empresas emp " +
                "JOIN usuarios u ON u.id_usuario = emp.id_usuario WHERE emp.id_empresa = ?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void actualizarPerfil(Empresa e) throws SQLException {
        String sql = "UPDATE empresas SET nombre_empresa=?, rubro=?, descripcion=? WHERE id_usuario=?";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNombreEmpresa());
            ps.setString(2, e.getRubro());
            ps.setString(3, e.getDescripcion());
            ps.setInt(4, e.getIdUsuario());
            ps.executeUpdate();
        }
    }

    public List<Empresa> listarTodas() throws SQLException {
        List<Empresa> lista = new ArrayList<>();
        String sql = "SELECT emp.*, u.nombre, u.correo FROM empresas emp JOIN usuarios u ON u.id_usuario = emp.id_usuario ORDER BY emp.id_empresa DESC";
        try (Connection con = PostgresConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapRow(rs));
        }
        return lista;
    }

    private Empresa mapRow(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setIdEmpresa(rs.getInt("id_empresa"));
        e.setIdUsuario(rs.getInt("id_usuario"));
        e.setNombre(rs.getString("nombre"));
        e.setCorreo(rs.getString("correo"));
        e.setNombreEmpresa(rs.getString("nombre_empresa"));
        e.setRubro(rs.getString("rubro"));
        e.setDescripcion(rs.getString("descripcion"));
        return e;
    }
}
