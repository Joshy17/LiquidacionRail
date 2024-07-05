/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Liquidacion.Liquidicacion;
import Liquidacion.Liquidicacion.Client.SftpClient;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import com.jcraft.jsch.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import com.jcraft.jsch.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Vector;

import java.net.*;

/**
 *
 * @author XPC
 */
public class SftpTest {
    
    public static void main(String[] args) throws IOException, SftpException {
        String host = "s-049c1379e80b490c8.server.transfer.us-east-2.amazonaws.com";
        int port = 22;
        String user = "usuarioAcceso";
        String privateKeyPath = "src/main/resources/id_rsa.txt";  // Ruta específica dentro del proyecto
        String remoteFilePath = "/archivossftp/archivo.txt"; // Ruta del archivo remoto

        try {
            File privateKeyFile = new File(privateKeyPath);
            if (!privateKeyFile.exists()) {
                throw new FileNotFoundException("No se pudo encontrar el archivo de clave privada en " + privateKeyPath);
            }

            // Leer el contenido del archivo de clave privada como una cadena
            BufferedReader reader = new BufferedReader(new FileReader(privateKeyFile));
            StringBuilder privateKeyBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                privateKeyBuilder.append(line).append("\n");
            }
            reader.close();

            // Crear URL para la conexión SFTP
            URL url = new URL("sftp://" + user + "@" + host + ":" + port + remoteFilePath);

            // Abrir conexión SFTP
            URLConnection urlConnection = url.openConnection();

            // Configurar las propiedades de la conexión
            urlConnection.setDoOutput(true); // Permitir escritura
            urlConnection.setDoInput(true); // Permitir lectura
            urlConnection.setRequestProperty("Content-Type", "application/octet-stream"); // Tipo de contenido

            // Configurar autenticación usando la clave privada
            urlConnection.setRequestProperty("Authorization", "Bearer " + privateKeyBuilder.toString());

            // Leer respuesta del servidor
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String lineResponse;
            while ((lineResponse = bufferedReader.readLine()) != null) {
                System.out.println(lineResponse);
            }

            // Cerrar recursos
            bufferedReader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
