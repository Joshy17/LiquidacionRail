/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Liquidacion.Liquidicacion.Client;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
/**
 *
 * @author XPC
 */
public class SftpClient {
    
    private String host;
    private int port;
    private String user;
    private String privateKeyPath;

    public SftpClient(String host, int port, String user, String privateKeyPath) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.privateKeyPath = privateKeyPath;
    }

    public String downloadFile(String remoteFilePath) throws JSchException, SftpException, IOException {
        JSch jsch = new JSch();

        // Cargar el archivo id_rsa desde resources
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(privateKeyPath);
        if (resource == null) {
            throw new IllegalArgumentException("File not found: " + privateKeyPath);
        }

        try (InputStream inputStream = resource.openStream()) {
            byte[] privateKeyBytes = inputStream.readAllBytes();
            jsch.addIdentity("id_rsa", privateKeyBytes, null, null);
        }

        Session session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = channelSftp.get(remoteFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        channelSftp.disconnect();
        session.disconnect();

        return new String(outputStream.toByteArray());
    }
}
