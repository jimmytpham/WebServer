/* Jimmy Pham
*  3711704
* COMP 489
*/

package webserver;
import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * WebServer.java
 * 
 * Description: basic web server that listens on port 80, handles incoming HTTP, GET requests, and serves files from a base directory
 * 
 * Expected Inputs:
 * - Client sends an HTTP GET request for a file
 * 
 * Expected Outputs/Results:
 * - The web server sends the file's content in the HTTP response
 * - If the file is not found, the server sends an  error response
 * 
 * Called by: Main method initializes the server and listens for client connections
 * 
 * Will call: 
 * - handleClient() to process client requests
 * - sendResponse() to send custom error or success responses
 * - sendFileResponse() to send the content of requested files
 */

public class WebServer {
	// Define the port for the web server and base directory
    private static final int PORT = 80;
    private static final String BASE_DIRECTORY = "C:\\Users\\jimmy\\Documents\\j2ee\\Assignment1\\webroot"; // Adjust to the directory that the testfiles are saved in.

    public static void main(String[] args) {
    	// Start the server and listen on specific port
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Web Server running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * handleClient()
     * 
     * Description: handles an individual client's HTTP request. It processes the GET request, checks the validity, and sends the appropriate response 
     * 
     * Expected Inputs:
     * - Client request: A GET request with a file path
     * 
     * Expected Outputs/Results:
     * - Returns the requested file content in the response if the file exists
     * - Returns an HTTP error message if there's an issue with the request or the file
     * 
     * Called by: Main method calls this function for each client connection
     * 
     * Will call:
     * - sendResponse() to send error/general messages
     * - sendFileResponse() to send content as an HTTP response
     */
    
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

        	// Read the client HTTP request line 
            String requestLine = in.readLine();
            if (requestLine == null) return;
            
            // Print request for debugging
            System.out.println("Received Request: " + requestLine);

            // Split request line into parts
            String[] parts = requestLine.split(" ");
            if (parts.length < 2 || !"GET".equals(parts[0])) {
                sendResponse(out, "HTTP/1.1 400 Bad Request\r\n\r\nBad Request.");
                return;
            }

            String filePath = parts[1].substring(1); // Remove leading "/"
            if (filePath.isEmpty()) filePath = "index.html";

            // Ensure the file path is under the BASE_DIRECTORY
            File file = new File(BASE_DIRECTORY, filePath).getCanonicalFile();
            if (!file.getPath().startsWith(new File(BASE_DIRECTORY).getCanonicalPath())) {
                sendResponse(out, "HTTP/1.1 403 Forbidden\r\n\r\nAccess denied.");
            } else if (!file.exists()) {
                sendResponse(out, "HTTP/1.1 404 Not Found\r\n\r\nFile not found.");
            } else {
                sendFileResponse(out, file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * sendResponse()
     * 
     * Description: sends a simple HTTP response message
     * 
     * Expected Inputs:
     * - A string containing the HTTP response 
     * 
     * Expected Outputs/Results:
     * - Sends the response as HTTP headers and content to the client
     * 
     * Called by: handleClient() when an error or message needs to be sent
     * 
     * Will call: None
     */
    private static void sendResponse(OutputStream out, String response) throws IOException {
        out.write(response.getBytes());
        out.flush();
    }
    
    /**
     * sendFileResponse()
     * 
     * Description: This method sends the requested file content as part of the HTTP response and content type (MIME type)
     * 
     * Expected Inputs:
     * - Requested file
     * 
     * Expected Outputs/Results:
     * - Sends the file content to the client
     * 
     * Called by: handleClient() valid file is found and sent to the client
     * 
     * Will call: None
     */
    private static void sendFileResponse(OutputStream out, File file) throws IOException {
        String contentType = Files.probeContentType(file.toPath()); // Determine MIME type of file
        if (contentType == null) {
            contentType = "application/octet-stream"; // Default to binary data
        }

        String header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "\r\n\r\n";
        out.write(header.getBytes());
        Files.copy(file.toPath(), out);  // Send the file content as binary
        out.flush();
    }

}
