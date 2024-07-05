/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Liquidacion.Liquidicacion;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.sftp.SFTPFileTransfer;
import net.schmizz.sshj.sftp.RemoteFile;


/**
 *
 * @author XPC
 */
public class SSHJprueba {
    
        public static void main(String[] args) {
        // Detalles del servidor
        String hostname = "s-049c1379e80b490c8.server.transfer.us-east-2.amazonaws.com";
        String username = "usuarioAcceso";
        String privateKeyPath = "src\\main\\resources\\id_rsa.txt"; // Reemplaza con la ruta a tu clave privada
        
        SSHClient sshClient = new SSHClient();
        try {
            // Configurar verificación de host (opción insegura para pruebas, usar adecuadamente en producción)
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());

            // Conectar al servidor
            sshClient.connect(hostname);
            System.out.println("Conexión establecida con el servidor: " + hostname);

            // Cargar la clave privadas
            KeyProvider keyProvider = sshClient.loadKeys(privateKeyPath);

            // Autenticar usando la clave privada
            sshClient.authPublickey(username, keyProvider);
            System.out.println("Autenticación exitosa para el usuario: " + username);

            // Si la conexión y autenticación son exitosas, se imprime el mensaje
            System.out.println("La conexión SFTP se ha establecido correctamente usando clave privada.");
            
            listFiles(sshClient, "/archivossftp"); // Especifica el directorio que deseas listar

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error durante la conexión o autenticación con el servidor.");
        } finally {
            try {
                // Cerrar la conexión SSH
                sshClient.disconnect();
                System.out.println("Conexión cerrada.");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error al cerrar la conexión.");
            }
        }
    }
        
    public static void listFiles(SSHClient sshClient, String directoryPath) {
        SFTPClient sftpClient = null;
        try {
            sftpClient = sshClient.newSFTPClient();
            List<RemoteResourceInfo> files = sftpClient.ls(directoryPath);
            System.out.println("Archivos en el directorio '" + directoryPath + "':");

            // Definir el patrón para el formato ddMMyyyy.txt
            Pattern pattern = Pattern.compile("\\d{8}\\.txt");

            for (RemoteResourceInfo file : files) {
                String fileName = file.getName();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    System.out.println("Archivo: " + fileName);
                    // Leer y mostrar el contenido del archivo
                    String filePath = directoryPath + "/" + fileName;
                    String fileContent = readFileContent(sftpClient, filePath);
                    System.out.println("Contenido del archivo:\n" + fileContent);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al listar los archivos.");
        } finally {
            if (sftpClient != null) {
                try {
                    sftpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error al cerrar el cliente SFTP.");
                }
            }
        }
    }

    public static String readFileContent(SFTPClient sftpClient, String filePath) {
        try (RemoteFile remoteFile = sftpClient.open(filePath);
             InputStream inputStream = remoteFile.new RemoteFileInputStream()) {
            return IOUtils.readFully(inputStream).toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al leer el contenido del archivo: " + filePath);
            return null;
        }
    }
}


