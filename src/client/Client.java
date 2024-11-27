/* Jimmy Pham
*  3711704
* COMP 489
*/

package client;
import java.io.*;
import java.net.*;

/**
 * Client.java
 * 
 * Description: simple client that connects to a proxy server. It sends an HTTP GET request to the proxy server, which forwards the request to a web server. Client displays the response from proxy server
 * 
 * Expected Inputs:
 * - User inputs a URL or "exit" to terminate the program
 * 
 * Expected Outputs/Results:
 * - The client sends the URL as an HTTP GET request to the proxy server
 * - The client prints the response received from the proxy server
 * 
 * Called by: Main method initiates the connection to the proxy server and handles input/output operations
 * 
 * Will call: 
 * - handleResponse() to process the response from the proxy server
 */
public class Client {
	// Define proxy server host and port
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 9090;

    public static void main(String[] args) {
    	// establish connection to proxy server
        try (Socket socket = new Socket(PROXY_HOST, PROXY_PORT);
        		
        	// Read user inputs from console with a BufferedReader
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        		
        	// Send data to proxy server with a PrintWriter
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        		
        	// Receive data from proxy server with an InputStream
             InputStream in = socket.getInputStream()) {

            System.out.println("Connected to Proxy Server.");

            // Ask the user for a URL to fetch
            System.out.print("Enter URL (or 'exit' to quit): ");
            String url = consoleInput.readLine();

            if ("exit".equalsIgnoreCase(url)) {
                System.out.println("Exiting...");
                return;  // Exit the program
            }

            // Send the URL as an HTTP GET request to the proxy server
            out.println("GET " + url + " HTTP/1.1");
            out.println("Host: " + PROXY_HOST);
            out.println();
            out.flush();

            // Display the response from the proxy server
            handleResponse(in);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * handleResponse()
     * 
     * Description: handles the response data received from the proxy server. 
     * 
     * Expected Inputs:
     * - InputStream from the proxy server (data sent back from the server)
     * 
     * Expected Outputs/Results:
     * - Prints each line of the response from the proxy server
     * 
     * Called by: main method calls this function to process the response from the proxy server
     *
     * Will call: None
     */
    private static void handleResponse(InputStream in) throws IOException {
    	// Read the response data from the proxy
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line); // Print response from the proxy
        }
    }
}
