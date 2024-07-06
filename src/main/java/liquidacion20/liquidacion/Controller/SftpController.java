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

    // Detalles del servidor
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

            // Cargar la clave privada desde los recursos
            InputStream inputStream = getClass().getResourceAsStream("/id_rsa.txt");
            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo de clave privada.");
            }

            // Crear un archivo temporal para la clave privada
            Path tempKeyPath = Files.createTempFile("id_rsa", ".txt");
            Files.copy(inputStream, tempKeyPath, StandardCopyOption.REPLACE_EXISTING);

            // Configurar SSHJ con la clave privada
            KeyProvider keyProvider = sshClient.loadKeys(tempKeyPath.toString());
            sshClient.authPublickey(username, keyProvider);
            System.out.println("Autenticación exitosa para el usuario: " + username);

            response.append("Conexión SSH establecida y autenticación realizada correctamente.\n");

            // Llamar al método para listar y leer archivos con el formato específico
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

            // Cargar la clave privada desde los recursos
            InputStream inputStream = getClass().getResourceAsStream("/id_rsa.txt");
            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo de clave privada.");
            }

            // Crear un archivo temporal para la clave privada
            Path tempKeyPath = Files.createTempFile("id_rsa", ".txt");
            Files.copy(inputStream, tempKeyPath, StandardCopyOption.REPLACE_EXISTING);

            // Configurar SSHJ con la clave privada
            KeyProvider keyProvider = sshClient.loadKeys(tempKeyPath.toString());
            sshClient.authPublickey(username, keyProvider);
            System.out.println("Autenticación exitosa para el usuario: " + username);

            response.append("Conexión SSH establecida y autenticación realizada correctamente.\n");

            // Llamar al método para listar y leer archivos con el formato específico
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

            // Crear variable para candelarizado por crear fecha
            Pattern pattern = Pattern.compile("\\d{8}\\.txt");

            // Obtener la fecha actual del sistema en el formato ddMMyyyy
            String fechaActual = new SimpleDateFormat("ddMMyyyy").format(new Date());

            for (RemoteResourceInfo file : files) {
                String fileName = file.getName();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    // Verificar si la fecha en el nombre del archivo coincide con la fecha actual
                    String fechaArchivo = fileName.substring(0, 8);
                    if (fechaArchivo.equals(fechaActual)) {
                        response.append("Archivo: ").append(fileName).append("\n");
                        String filePath = directorio + "/" + fileName;
                        String fileContent = readFileContent(sftpClient, filePath);
                        if (fileContent != null) {
                            response.append("transactions:[\n").append(fileContent).append("\n");
                            // Convertir el contenido del archivo a JSON
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

            // Crear variable para candelarizado por crear fecha
            Pattern pattern = Pattern.compile("\\d{8}\\.txt");

            // Obtener la fecha actual del sistema en el formato ddMMyyyy
            String fechaActual = new SimpleDateFormat("ddMMyyyy").format(new Date());

            for (RemoteResourceInfo file : files) {
                String fileName = file.getName();
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {
                    // Verificar si la fecha en el nombre del archivo coincide con la fecha actual
                    String fechaArchivo = fileName.substring(0, 8);
                    if (fechaArchivo.equals(fechaActual)) {
                        response.append("Archivo: ").append(fileName).append("\n");
                        String filePath = directorio + "/" + fileName;
                        String fileContent = readFileContent(sftpClient, filePath);
                        if (fileContent != null) {
                            response.append("Contenido del archivo:\n").append(fileContent).append("\n");
                            // Convertir el contenido del archivo a JSON
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

    //prueba
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
            System.out.println("Error al leer el contenido del archivo: " + filePath);
            return null;
        }
        return contentBuilder.toString();
    }

    private static String convertirAJsonCredito(String fileContent) {
        StringBuilder jsonBuilder = new StringBuilder("{ \"transactions\": [");

        // Define el patrón para cada línea del archivo
        Pattern pattern = Pattern.compile(
                "02(\\d{2})(\\d{16})(\\d{6})(\\d{12})(\\d{10})(\\d{2})(\\d{6})(\\d{4})(\\d{6})(\\d{8})");

        Pattern pattern2 = Pattern.compile("03(\\d{2})(\\d{12})");

        Matcher matcher = pattern.matcher(fileContent);

        String amountTotal = "";

        // Leer el archivo línea por línea
        try ( BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher2 = pattern2.matcher(line);
                if (matcher2.find()) {
                    amountTotal = matcher2.group(2); // Capturar el monto total del grupo 2
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (matcher.find()) {
            String pan = matcher.group(2);
            String authorizationId = matcher.group(3);
            String amount = matcher.group(4);
            String date = matcher.group(5);
            String sequenceNumber = matcher.group(6);
            String time = matcher.group(7);
            String expDate = matcher.group(8);
            String refNumber = matcher.group(9);
            String authorizationId1 = matcher.group(10);

            // Formatear el campo system_sequence_number agregando ceros a la izquierda
            String formattedSequenceNumber = String.format("%06d", Integer.parseInt(sequenceNumber));

            // Formatear el campo created_at a "yyyy-MM-dd HH:mm:ss"
            LocalDateTime dateTime = LocalDateTime.of(
                    LocalDate.parse(date.substring(0, 6), DateTimeFormatter.ofPattern("ddMMyy")),
                    LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmmss")));
            String formattedCreatedAt = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Construir el JSON con los campos requeridos
            String transaccionJson = "{"
                    + "\"pan\": \"" + pan + "\","
                    + "\"amount\": \"" + amountTotal + "\","
                    + "\"created_at\": \"" + formattedCreatedAt + "\","
                    + "\"system_sequence_number\": \"" + formattedSequenceNumber + "\","
                    + "\"authorization_id\": \"" + authorizationId + "\", "
                    + "\"traking_reference_number\": \"" + refNumber + "\""
                    + "}";

            jsonBuilder.append(transaccionJson).append(",\n");
        }

        // Eliminar la última coma y agregar el cierre del arreglo JSON
        if (jsonBuilder.length() > 0) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 2); // Eliminar la última coma y el \n
        }
        jsonBuilder.append("]}");

        return jsonBuilder.toString();
    }

    //http://www.lenguajes.somee.com/api/Autorizacion/actualizar
    private static List<String> convertirAJson(String fileContent) {
        List<String> jsonList = new ArrayList<>();

        // Define el patrón para cada línea del archivo
        Pattern pattern = Pattern.compile(
                "02(\\d{2})(\\d{16})(\\d{6})(\\d{12})(\\d{10})(\\d{2})(\\d{6})(\\d{4})(\\d{6})(\\d{8})");

        Matcher matcher = pattern.matcher(fileContent);

        while (matcher.find()) {
            String pan = matcher.group(2);
            String authorizationId = matcher.group(3);
            String amount = matcher.group(4);
            String date = matcher.group(5);
            String sequenceNumber = matcher.group(6);
            String time = matcher.group(7);
            String expDate = matcher.group(8);
            String refNumber = matcher.group(9);
            String authorizationId1 = matcher.group(10);

            // Formatear el campo system_sequence_number agregando ceros a la izquierda
            String formattedSequenceNumber = String.format("%06d", Integer.parseInt(sequenceNumber));

            // Formatear el campo created_at a "yyyy-MM-dd HH:mm:ss"
            LocalDateTime dateTime = LocalDateTime.of(
                    LocalDate.parse(date.substring(0, 6), DateTimeFormatter.ofPattern("ddMMyy")),
                    LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmmss")));
            String formattedCreatedAt = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Construir el JSON con los campos requeridos
            String transaccionJson = "{"
                    + "\"pan\": \"" + pan + "\","
                    + "\"amount\": \"" + amount + "\","
                    + "\"created_at\": \"" + formattedCreatedAt + "\","
                    + "\"system_sequence_number\": \"" + formattedSequenceNumber + "\","
                    + "\"authorization_id\": \"" + authorizationId + "\", "
                    + "\"traking_reference_number\": \"" + refNumber + "\""
                    + "}";

            jsonList.add(transaccionJson);
        }

        return jsonList;
    }
}
