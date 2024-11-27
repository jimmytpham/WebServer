/* Jimmy Pham
*  3711704
* COMP 489
*/


package proxy;
import java.io.*;
import java.net.*;

/*
 * ProxyServer.java
 * 
 * Description: This is a simple proxy server that listens for HTTP/FTP requests from clients and forwards them to appropriate servers.
 * 
 * Expected Inputs: Client sends a request to the proxy server, HTTP request (GET) or an FTP request (raw data).
 * 
 * Expected Outputs:
 * For HTTP requests, the proxy forwards the request to a web server and sends the server's response back to the client.
 * For FTP requests, the proxy sends a basic response to simulate an FTP server response.
 * 
 * Called by: Main method initiates the proxy server
 * 
 * Will Call: 
 * - handleClientRequest() to process individual client requests
 * - handleHttpRequest() to process HTTP requests and forward them to the web server
 * - handleFtpRequest() to process FTP requests and respond with a message
 * - sendResponse() to send a response back to the client
 * - forwardData() to forward data between the web server and client
 *
 */
public class ProxyServer {
    private static final int PROXY_PORT = 9090; 

    public static void main(String[] args) {
    	// Start proxy server and listen for connections
        try (ServerSocket serverSocket = new ServerSocket(PROXY_PORT)) {
            System.out.println("Proxy Server running on port " + PROXY_PORT);
            
            while (true) {
                // Accept incoming client connection
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClientRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * handleClientRequest()
     * 
     * Description: This function handles the incoming request from the client.
     * 
     * Expected Inputs: 
     * - Client sends a request line (ex. "GET http://example.com HTTP/1.1")
     * 
     * Expected Outputs/Results:
     * - If the URL is HTTP/HTTPS, it calls handleHttpRequest() to forward the request to the web server
     * - If the URL is FTP, it calls handleFtpRequest() to simulate an FTP response
     * - If the URL protocol is unsupported, it sends a response indicating the protocol is not supported
     * 
     * Called by: Main method calls this function after accepting client connection
     * 
     * Will call: handleHttpRequest(), handleFtpRequest(), sendResponse()
     */
    
    private static void handleClientRequest(Socket clientSocket) {
        try (BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream clientOut = clientSocket.getOutputStream()) {

            // Read the request from the client (first line)
            String requestLine = clientReader.readLine();
            if (requestLine == null) return;

            // Parse the request
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0]; // HHTP method (GET, POST, etc)
            String url = requestParts[1]; // URL requested

            // Handle HTTP or FTP based on the URL
            if (url.startsWith("http://") || url.startsWith("https://")) {
                handleHttpRequest(url, clientOut);
            } else if (url.startsWith("ftp://")) {
                handleFtpRequest(url, clientOut);
            } else {
                sendResponse(clientOut, "Unsupported URL protocol.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * handleHttpRequest()
     * 
     * Description: This function handles HTTP requests and forwards the request to the target web server
     * 
     * Expected Inputs:
     * - An HTTP URL (ex. "http://example.com/path/to/resource")
     * 
     * Expected Outputs/Results:
     * - Forwards the HTTP GET request to the web server and sends the web server's response back to the client
     * 
     * Called by: handleClientRequest() when an HTTP request is identified
     * 
     * Will call: forwardData(), sendResponse()
     */
    
    private static void handleHttpRequest(String url, OutputStream clientOut) {
        try {
            // Extract host and path 
            URL targetUrl = new URL(url);
            String host = targetUrl.getHost();
            int port = targetUrl.getPort() == -1 ? 80 : targetUrl.getPort(); // Default to port 80

            // Connect to the web server
            try (Socket webServerSocket = new Socket(host, port);
                 InputStream webServerIn = webServerSocket.getInputStream();
                 OutputStream webServerOut = webServerSocket.getOutputStream()) {

                // Send the request to the web server
                webServerOut.write(("GET " + targetUrl.getPath() + " HTTP/1.1\r\nHost: " + host + "\r\n\r\n").getBytes());
                webServerOut.flush();

                // Forward the response from the web server to the client
                forwardData(webServerIn, clientOut);
            }

        } catch (IOException e) {
            e.printStackTrace();
            sendResponse(clientOut, "Error processing HTTP request.");
        }
    }
    
    /**
     * handleFtpRequest()
     * 
     * Description: This function handles FTP requests and responds by sending a message indicating the requested FTP resource
     * 
     * Expected Inputs:
     * - An FTP URL (ex. "ftp://example.com/path/to/file")
     * 
     * Expected Outputs/Results:
     * - Sends a simulated message to the client indicating the FTP request details
     * 
     * Called by: handleClientRequest() when an FTP request is identified
     * 
     * Will call: sendResponse()
     */
    private static void handleFtpRequest(String url, OutputStream clientOut) {
        try {
            // Extract FTP URL details
            URL ftpUrl = new URL(url);
            String host = ftpUrl.getHost();
            String path = ftpUrl.getPath();

            // simple response to send message to client to show that it is working properly
            String message = "FTP request for: " + path + " on server: " + host;
            clientOut.write(message.getBytes());
            clientOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
            sendResponse(clientOut, "Error processing FTP request.");
        }
    }
    
    /**
     * sendResponse()
     * 
     * Description: This function sends a response back to the client
     * 
     * Expected Inputs:
     * - A string response to be sent to the client
     * 
     * Expected Outputs/Results:
     * - Sends the response string to the client over the output stream
     * 
     * Called by: handleClientRequest(), handleHttpRequest(), handleFtpRequest()
     * 
     * Will call: None
     */
    private static void sendResponse(OutputStream clientOut, String response) {
        try {
            clientOut.write(response.getBytes());
            clientOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * forwardData()
     * 
     * Description: This function forwards data from the input stream (from HTTP or FTP server)
     * to the output stream (client).
     * 
     * Expected Inputs:
     * - An InputStream (from a server) and an OutputStream (to the client)
     * 
     * Expected Outputs/Results:
     * - Transfers data from the input stream to the output stream
     * 
     * Called by: handleHttpRequest()
     * 
     * Will call: None
     */
    
    private static void forwardData(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
