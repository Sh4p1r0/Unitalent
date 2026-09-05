package com.unitalent.db;

import com.unitalent.dao.UsuarioDAO;
import com.unitalent.dao.EstudianteDAO;
import com.unitalent.model.Usuario;
import com.unitalent.model.Estudiante;
import com.unitalent.util.PasswordUtil;

public class TestLogin {
    public static void main(String[] args) {
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            EstudianteDAO estudianteDAO = new EstudianteDAO();
            
            System.out.println("Buscando usuario...");
            Usuario u = usuarioDAO.buscarPorCorreo("estudiante@unitalent.com");
            if (u == null) {
                System.out.println("Usuario no encontrado");
                return;
            }
            System.out.println("Usuario encontrado: " + u.getNombre() + " (ID: " + u.getIdUsuario() + ")");
            
            System.out.println("Verificando clave...");
            boolean ok = PasswordUtil.verify("123456", u.getContrasena());
            System.out.println("Clave correcta: " + ok);
            
            System.out.println("Buscando estudiante con idUsuario=" + u.getIdUsuario() + "...");
            Estudiante e = estudianteDAO.buscarPorIdUsuario(u.getIdUsuario());
            if (e == null) {
                System.out.println("Estudiante no encontrado en la tabla estudiantes.");
            } else {
                System.out.println("Estudiante encontrado: " + e.getCarrera());
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
