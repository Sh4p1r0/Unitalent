# UniTalent - Plataforma Inteligente de Vinculación Laboral Universitaria

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Tomcat](https://img.shields.io/badge/Apache_Tomcat-F8DC75?style=for-the-badge&logo=apache-tomcat&logoColor=black)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)

**UniTalent** es una solución tecnológica web diseñada para conectar de manera eficiente a estudiantes universitarios con empresas empleadoras, facilitando la inserción al mercado laboral preprofesional y profesional mediante una arquitectura de persistencia políglota (**SQL + NoSQL**).

---

## Características Principales

- **Arquitectura Híbrida de Persistencia (Políglota):**
  - **PostgreSQL (Relacional):** Gestión estructurada de usuarios, estudiantes, empresas, vacantes y postulaciones con integridad referencial, transacciones atómicas (ACID), procedimientos almacenados (`PL/pgSQL`) y triggers de validación activa.
  - **MongoDB (NoSQL):** Almacenamiento ágil y semiestructurado de historiales de búsqueda y preferencias laborales dinámicas (modalidades y áreas de interés).
- **Filtro Inteligente de Ofertas:** Las ofertas laborales se adaptan y priorizan automáticamente según las preferencias registradas en MongoDB de cada estudiante.
- **Múltiples Roles de Usuario:**
  - **Estudiantes:** Búsqueda avanzada de empleo, gestión de perfil, postulación a ofertas y seguimiento de estado en tiempo real.
  - **Empresas:** Publicación y edición de vacantes, revisión de postulantes y cambio de estado de solicitudes.
  - **Administradores:** Control integral del sistema y visualización de métricas generales.
- **Dashboards Interactivos con Chart.js:** Gráficos estadísticos dinámicos en tiempo real sobre postulaciones por estado y ofertas por modalidad.
- **Auto-actualización en Tiempo Real:** Las tablas y métricas se refrescan automáticamente cada 15 segundos sin necesidad de recargar la página (`F5`).
- **Seguridad Robusta:** Cifrado de contraseñas mediante hashing **SHA-256** con sal (*salt*) aleatoria de 16 bytes.
- **Interfaz Moderna y Responsiva:** Diseño contemporáneo con soporte nativo para Modo Oscuro / Modo Claro y adaptable a dispositivos móviles.

---

## Arquitectura del Software

El sistema sigue el patrón de diseño por capas estándar de **Java EE (Servlets)**:

```
UniTalent/
├── BASE DE DATOS - PostgreSQL - MongoDB/ # Scripts DDL, procedimientos, triggers y setup NoSQL
├── src/main/java/com/unitalent/
│   ├── dao/          # Capa de acceso a datos (JDBC PreparedStatement y MongoDB Driver)
│   ├── db/           # Conexiones gestionadas a PostgreSQL y MongoDB Atlas
│   ├── model/        # Entidades del dominio (POJOs)
│   ├── servlet/      # Controladores REST (/api/login, /api/ofertas, etc.)
│   └── util/         # Utilidades de seguridad (PasswordUtil) y JSON (JsonUtil)
└── src/main/webapp/  # Frontend desacoplado (HTML5, CSS3, JS puro y Chart.js)
```

---

## Puesta en Marcha Local

### Prerrequisitos
- **Java JDK** (versión 17 o superior)
- **Apache Maven** (incluido mediante Maven Wrapper `mvnw`)
- Instancia activa de **PostgreSQL** y **MongoDB** (locales o en la nube)

### Ejecución Rápida en Windows
1. Clona este repositorio:
   ```bash
   git clone https://github.com/Sh4p1r0/Unitalent.git
   ```
2. Ejecuta el iniciador automático:
   - Haz doble clic en el archivo `start_app.cmd` o ejecútalo desde terminal:
     ```cmd
     start_app.cmd
     ```
3. Abre tu navegador en:
   ```
   http://localhost:8080/UniTalent/
   ```

---

## Desarrolladores (Proyecto Académico)

Proyecto final desarrollado para la carrera de **Ingeniería de Sistemas e Informática** - Universidad Tecnológica del Perú (UTP).
