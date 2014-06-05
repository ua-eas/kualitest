/*
 * Copyright 2014 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.test.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

public class ApplicationInstanceManager {
    private static final Logger LOG = Logger.getLogger(ApplicationInstanceManager.class);
    
    private static ApplicationInstanceListener subListener;

    /**
     * Randomly chosen, but static, high socket number
     */
    public static final int SINGLE_INSTANCE_NETWORK_SOCKET1 = 44331;
    public static final int SINGLE_INSTANCE_NETWORK_SOCKET2 = 44332;

    /**
     * Must end with newline
     */
    public static final String SINGLE_INSTANCE_SHARED_KEY = "$$NewInstance$$\n";

    /**
     * Registers this instance of the application.
     *
     * @return true if first instance, false if not.
     */
    public static boolean registerInstance(int networkSocket) {
        // returnValueOnError should be true if lenient (allows app to run on network error) or false if strict.
        boolean returnValueOnError = true;
        // try to open network socket
        // if success, listen to socket for new instance message, return true
        // if unable to open, connect to existing and send new instance message, return false
        try {
            final ServerSocket socket = new ServerSocket(networkSocket, 10, InetAddress.getLocalHost());
            LOG.debug("Listening for application instances on socket " + networkSocket);
            Thread instanceListenerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean socketClosed = false;
                    while (!socketClosed) {
                        if (socket.isClosed()) {
                            socketClosed = true;
                        } else {
                            try {
                                Socket client = socket.accept();
                                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                String message = in.readLine();
                                if (SINGLE_INSTANCE_SHARED_KEY.trim().equals(message.trim())) {
                                    LOG.debug("Shared key matched - new application instance found");
                                    fireNewInstance();
                                }
                                in.close();
                                client.close();
                            } catch (IOException e) {
                                socketClosed = true;
                            }
                        }
                    }
                }
            });
            instanceListenerThread.start();
            // listen
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
            return returnValueOnError;
        } catch (IOException e) {
            LOG.debug("Port is already taken.  Notifying first instance.");
            try {
                Socket clientSocket = new Socket(InetAddress.getLocalHost(), networkSocket);
                OutputStream out = clientSocket.getOutputStream();
                out.write(SINGLE_INSTANCE_SHARED_KEY.getBytes());
                out.close();
                clientSocket.close();
                LOG.debug("Successfully notified first instance.");
                return false;
            } catch (UnknownHostException e1) {
                LOG.error(e.getMessage(), e);
                return returnValueOnError;
            } catch (IOException e1) {
                LOG.error("Error connecting to local port for single instance notification");
                LOG.error(e1.getMessage(), e1);
                return returnValueOnError;
            }

        }
        return true;
    }

    public static void setApplicationInstanceListener(ApplicationInstanceListener listener) {
        subListener = listener;
    }

    private static void fireNewInstance() {
        if (subListener != null) {
            subListener.newInstanceCreated();
        }
    }
}
