package marketsoft.model;

public class Usuario {
    private int idUsuario;
    private String usuario;
    private String contrasena;
    private String rol; // "Administrador" o "Cajero"

    public Usuario() {
    }

    public Usuario(int idUsuario, String usuario, String rol) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.rol = rol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
