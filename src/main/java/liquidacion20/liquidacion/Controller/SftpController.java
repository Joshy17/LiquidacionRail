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
                            // Generar JSON con solo el campo pan y agregarlo a la respuesta
                            List<String> pans = extractAllPansFromJson(json);
                            List<String> jsonPans = convertirAJsonSoloPans(pans);
                            response.append("JSON Pan generados:\n");
                            for (String jsonPan : jsonPans) {
                                response.append(jsonPan).append("\n");
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

    private static String readFileContent(SFTPClient sftpClient, String filePath) {
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

    private List<String> convertirAJson(String fileContent) {
        List<String> jsonList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(fileContent));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                // Procesa cada línea del contenido del archivo y conviértela a JSON
                String json = convertirLineaAJson(line);
                jsonList.add(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonList;
    }

    private String convertirLineaAJson(String line) {
        // Ejemplo básico de conversión, ajusta según tu formato de línea y requisitos de JSON
        return "{\"line\":\"" + line + "\"}";
    }

    private String convertirAJsonCredito(String fileContent) {
        // Ejemplo básico de conversión, ajusta según tu formato de contenido de archivo y requisitos de JSON
        return "{\"fileContent\":\"" + fileContent.replace("\n", "\\n") + "\"}";
    }

    private List<String> extractAllPansFromJson(String json) {
        List<String> pans = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"fileContent\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String fileContent = matcher.group(1);  // Obtén el contenido del archivo
            BufferedReader reader = new BufferedReader(new StringReader(fileContent));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    // Supongamos que el PAN está en la primera posición de la línea, ajusta según sea necesario
                    String[] parts = line.split(" ");
                    if (parts.length > 0) {
                        pans.add(parts[0]);  // Agrega el PAN a la lista
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pans;
    }

    private List<String> convertirAJsonSoloPans(List<String> pans) {
        List<String> jsonList = new ArrayList<>();
        for (String pan : pans) {
            jsonList.add("{\"pan\":\"" + pan + "\"}");
        }
        return jsonList;

    }
}
