package com.example.jigsaw.client;

import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ClientSocket {
    public int serverMov;

    public long serverMin;

    public long serverSec;
    ClientController clientController;

    private final Socket socket;

    private final BufferedReader bufferedReader;

    private final BufferedWriter bufferedWriter;

    public ClientSocket(ClientController clientController) {
        this.clientController = clientController;
        try {
            InetAddress host = InetAddress.getByName("localhost");
            socket = new Socket(host, 1234);
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String number) {
        try {
            bufferedWriter.write(number);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected()) {
                    try {
                        String name = bufferedReader.readLine();
                        if (name == null) {
                            break;
                        }
                        if (name.equals("Time")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    clientController.stop();
                                }
                            });
                            continue;
                        }
                        if (name.equals("Restart")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    clientController.restartGo();
                                }
                            });
                            continue;
                        }
                        if (name.length() > 2 && name.startsWith("DB")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    clientController.top(name.substring(2));
                                }
                            });
                            continue;
                        }
                        if (name.startsWith("NAME")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    clientController.getName(name);
                                }
                            });
                        } else {
                            if (name.equals("Equal")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        clientController.single();
                                    }
                                });
                                continue;
                            }
                            if (name.equals("CloseSocket")) {
                                break;
                            }
                            if (name.equals("Alone")) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        clientController.friendLeft();
                                    }
                                });
                                continue;
                            }
                            if (name.length() < 7) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        clientController.getShape(Integer.parseInt(name));
                                    }
                                });
                            } else {
                                String pref = name.substring(0, 7);
                                switch (pref) {
                                    case "OverMov" -> serverMov = Integer.parseInt(name.substring(7));
                                    case "OverMin" -> serverMin = Integer.parseInt(name.substring(7));
                                    case "OverSec" -> {
                                        serverSec = Integer.parseInt(name.substring(7));
                                        if (!clientController.rivalReady) {
                                            clientController.rivalReady = true;
                                        } else {
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    clientController.friendFinal();
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("catching client");
                        break;
                    }
                }
            }
        }).start();
    }

    public void closeEverything() {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
            System.out.println("Client closed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
