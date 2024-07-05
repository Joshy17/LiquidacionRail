/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Liquidacion.Liquidicacion;

import com.jcraft.jsch.*;

/**
 *
 * @author XPC
 */
public class Prueba {
    
    private static final String HOST = "s-049c1379e80b490c8.server.transfer.us-east-2.amazonaws.com"; // Reemplaza con tu servidor
    private static final String USERNAME = "usuarioAcceso";
    private static final String PRIVATE_KEY_PATH = "src\\main\\resources\\key.ppk"; // Ruta local de la clave privada

    public static void main(String[] args) throws JSchException, SftpException {
        JSch jsch = new JSch();
        jsch.addIdentity(PRIVATE_KEY_PATH);
        //jsch.addIdentityFile(PRIVATE_KEY_PATH);

        Session session = jsch.getSession(USERNAME, HOST);
        session.setConfig("StrictHostKeyChecking", "no"); // Desactiva la verificación estricta de clave de host
        session.connect();

        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();

        // Realiza las operaciones SFTP aquí (subir/descargar archivos, etc.)

        sftp.disconnect();
        session.disconnect();
    }
}