package com.unitalent.model;

public class Postulacion {
    private int idPostulacion;
    private int idEstudiante;
    private int idOferta;
    private String fechaPostulacion;
    private String estado;
    private String tituloOferta;
    private String nombreEmpresa;
    private String nombreEstudiante;
    private String correoEstudiante;
    private String carreraEstudiante;
    private String cvEstudiante;

    public Postulacion() {}

    public int getIdPostulacion() { return idPostulacion; }
    public void setIdPostulacion(int idPostulacion) { this.idPostulacion = idPostulacion; }
    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }
    public int getIdOferta() { return idOferta; }
    public void setIdOferta(int idOferta) { this.idOferta = idOferta; }
    public String getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(String fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTituloOferta() { return tituloOferta; }
    public void setTituloOferta(String tituloOferta) { this.tituloOferta = tituloOferta; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }
    public String getCorreoEstudiante() { return correoEstudiante; }
    public void setCorreoEstudiante(String correoEstudiante) { this.correoEstudiante = correoEstudiante; }
    public String getCarreraEstudiante() { return carreraEstudiante; }
    public void setCarreraEstudiante(String carreraEstudiante) { this.carreraEstudiante = carreraEstudiante; }
    public String getCvEstudiante() { return cvEstudiante; }
    public void setCvEstudiante(String cvEstudiante) { this.cvEstudiante = cvEstudiante; }
}
