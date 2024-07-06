/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package liquidacion20.liquidacion.Controller;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author XPC
 */
@RestController
@RequestMapping("/api/liquidaciones")
public class SftpController {

    String hostname = "s-049c1379e80b490c8.server.transfer.us-east-2.amazonaws.com";
    String username = "usuarioAcceso";

    @PostMapping("/listar-archivos")
    public String listarArchivos() {
        SSHClient sshClient = new SSHClient();
        StringBuilder response = new StringBuilder();

        try {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(hostname);
            System.out.println("Conexión establecida con el servidor: " + hostname);

            InputStream inputStream = getClass().getResourceAsStream("/id_rsa.txt");
            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo de clave privada.");
            }

            Path tempKeyPath = Files.createTempFile("id_rsa", ".txt");
            Files.copy(inputStream, tempKeyPath, StandardCopyOption.REPLACE_EXISTING);

            KeyProvider keyProvider = sshClient.loadKeys(tempKeyPath.toString());
            sshClient.authPublickey(username, keyProvider);
            System.out.println("Autenticación exitosa para el usuario: " + username);

            response.append("Conexión SSH establecida y autenticación realizada correctamente.\n");

            listarArchivosConFormato(sshClient, "/archivossftp", response);

        } catch (IOException e) {
            e.printStackTrace();
            response.append("Error durante la conexión o autenticación con el servidor SSH: ").append(e.getMessage()).append("\n");
        } finally {
            try {
                sshClient.disconnect();
                System.out.println("Conexión cerrada.");
            } catch (IOException e) {
                e.printStackTrace();
                response.append("Error al cerrar la conexión SSH: ").append(e.getMessage()).append("\n");
            }
        }

        return response.toString();
    }

    @PostMapping("/listar-credito")
    public String listarArchivosCredito() {
        SSHClient sshClient = new SSHClient();
        StringBuilder response = new StringBuilder();

        try {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(hostname);
            System.out.println("Conexión establecida con el servidor: " + hostname);

            InputStream inputStream = getClass().getResourceAsStream("/id_rsa.txt");
            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo de clave privada.");
            }

            Path tempKeyPath = Files.createTempFile("id_rsa", ".txt");
            Files.copy(inputStream, tempKeyPath, StandardCopyOption.REPLACE_EXISTING);

            KeyProvider keyProvider = sshClient.loadKeys(tempKeyPath.toString());
            sshClient.authPublickey(username, keyProvider);
            System.out.println("Autenticación exitosa para el usuario: " + username);

            response.append("Conexión SSH establecida y autenticación realizada correctamente.\n");

            listarArchivosConFormatoCredito(sshClient, "/archivossftp", response);

        } catch (IOException e) {
            e.printStackTrace();
            response.append("Error durante la conexión o autenticación con el servidor SSH: ").append(e.getMessage()).append("\n");
        } finally {
            try {
                sshClient.disconnect();
                System.out.println("Conexión cerrada.");
            } catch (IOException e) {
                e.printStackTrace();
                response.append("Error al cerrar la conexión SSH: ").append(e.getMessage()).append("\n");
            }
        }

        return response.toString();
    }

    private void listarArchivosConFormato(SSHClient sshClient, String directorio, StringBuilder response) throws IOException {
        SFTPClient sftpClient = null;

        try {
            sftpClient = sshClient.newSFTPClient();
            List<RemoteResourceInfo> files = sftpClient.ls(directorio);
            response.append("Archivos en el directorio '").append(directorio).append("':\n");

            Pattern pattern = Pattern.compile("\\d{8}\\.txt");
            String fechaActual = new SimpleDateFormat("ddMMyyyy").format(new Date());

            for (RemoteResourceInfo file : files) {
                String fileName = file.getName();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    String fechaArchivo = fileName.substring(0, 8);
                    if (fechaArchivo.equals(fechaActual)) {
                        response.append("Archivo: ").append(fileName).append("\n");
                        String filePath = directorio + "/" + fileName;
                        String fileContent = readFileContent(sftpClient, filePath);
                        if (fileContent != null) {
                            response.append("Contenido del archivo:\n").append(fileContent).append("\n");

                            // Obtener los PANs de las transacciones
                            List<String> panJsonList = obtenerPANs(fileContent);
                            response.append("PANs generados:\n");
                            for (String panJson : panJsonList) {
                                response.append(panJson).append("\n");
                            }

                            List<String> jsonList = convertirAJson(fileContent);
                            response.append("JSONs generados:\n");
                            for (String json : jsonList) {
                                response.append(json).append("\n");
                            }
                        } else {
                            response.append("Error al leer el contenido del archivo: ").append(fileName).append("\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.append("Error al listar los archivos: ").append(e.getMessage()).append("\n");
        } finally {
            if (sftpClient != null) {
                try {
                    sftpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    response.append("Error al cerrar el cliente SFTP: ").append(e.getMessage()).append("\n");
                }
            }
        }
    }

    private void listarArchivosConFormatoCredito(SSHClient sshClient, String directorio, StringBuilder response) throws IOException {
        SFTPClient sftpClient = null;

        try {
            sftpClient = sshClient.newSFTPClient();
            List<RemoteResourceInfo> files = sftpClient.ls(directorio);
            response.append("Archivos en el directorio '").append(directorio).append("':\n");

            Pattern pattern = Pattern.compile("\\d{8}\\.txt");
            String fechaActual = new SimpleDateFormat("ddMMyyyy").format(new Date());

            for (RemoteResourceInfo file : files) {
                String fileName = file.getName();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    String fechaArchivo = fileName.substring(0, 8);
                    if (fechaArchivo.equals(fechaActual)) {
                        response.append("Archivo: ").append(fileName).append("\n");
                        String filePath = directorio + "/" + fileName;
                        String fileContent = readFileContent(sftpClient, filePath);
                        if (fileContent != null) {
                            response.append("Contenido del archivo:\n").append(fileContent).append("\n");

                            String json = convertirAJsonCredito(fileContent);
                            response.append("JSON generado:\n").append(json).append("\n");
                        } else {
                            response.append("Error al leer el contenido del archivo: ").append(fileName).append("\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.append("Error al listar los archivos: ").append(e.getMessage()).append("\n");
        } finally {
            if (sftpClient != null) {
                try {
                    sftpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    response.append("Error al cerrar el cliente SFTP: ").append(e.getMessage()).append("\n");
                }
            }
        }
    }

    public static String readFileContent(SFTPClient sftpClient, String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try ( RemoteFile remoteFile = sftpClient.open(filePath)) {
            InputStream inputStream = remoteFile.new RemoteFileInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return contentBuilder.toString();
    }

    private List<String> convertirAJson(String fileContent) throws IOException {
        List<String> jsonList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(fileContent));
        String line;
        while ((line = reader.readLine()) != null) {
            String pan = line.substring(0, 19).trim();
            String amount = line.substring(19, 31).trim();
            String createdAt = line.substring(31, 46).trim();
            String systemSeqNumber = line.substring(46, 56).trim();
            String authId = line.substring(56, 62).trim();
            String trackingRefNumber = line.substring(62, 78).trim();

            String json = String.format(
                    "{\"PAN\": \"%s\", \"Amount\": \"%s\", \"created_at\": \"%s\", \"system_sequence_number\": \"%s\", \"authorization_id\": \"%s\", \"tracking_reference_number\": \"%s\"}",
                    pan, amount, createdAt, systemSeqNumber, authId, trackingRefNumber
            );

            jsonList.add(json);
        }
        return jsonList;
    }

    private String convertirAJsonCredito(String fileContent) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(fileContent));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            String amount = line.substring(19, 31).trim();
            jsonBuilder.append(String.format("{\"amount\": \"%s\"}", amount)).append("\n");
        }
        return jsonBuilder.toString();
    }

    private List<String> obtenerPANs(String fileContent) throws IOException {
        List<String> panList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(fileContent));
        String line;
        while ((line = reader.readLine()) != null) {
            String pan = line.substring(0, 19).trim();
            String panJson = String.format("{\"PAN\": \"%s\"}", pan);
            panList.add(panJson);
        }
        return panList;
    }
}
