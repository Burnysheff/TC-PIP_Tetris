package com.example.jigsaw;

import com.example.jigsaw.client.ClientController;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.*;
import java.sql.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server {
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    private final String db = "PLAYS";

    private final String URL = "jdbc:derby:" + db + ";create=true";

    private final Connection connection;

    private boolean times = false;

    private final int maxSeconds;

    private final int maxMinutes;

    private int seconds;

    private int minutes;

    private boolean otherRestart = false;

    private final List<Integer> firstData = new ArrayList<>();
    private final List<Integer> secondData = new ArrayList<>();

    private String nameFirst;

    private String nameSecond;

    private final Random random = new Random();

    List<Integer> figures = new ArrayList<>();

    public int numberPlayers;

    private final List<Socket> listSocket = new ArrayList<>();

    private final List<BufferedReader> readers = new ArrayList<>();

    private final List<BufferedWriter> writers = new ArrayList<>();

    public Server(int number, ServerStart serverStart, int seconds, int minutes) {
        this.maxSeconds = seconds;
        this.maxMinutes = minutes;
        try {
            connection = DriverManager.getConnection(URL);
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, "PLAYS", null);
            if (!tables.next()) {
                Statement statement = connection.createStatement();
                String request = "CREATE TABLE PLAYS (" +
                        "Login VARCHAR(255), " +
                        "Dat DATE, " +
                        "Tim TIME, " +
                        "Moves INT, " +
                        "Times VARCHAR(255))";
                statement.execute(request);
                statement.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        serverStart.started();
                    }
                });
                numberPlayers = number;
                int counter = 0;
                while (counter != numberPlayers) {
                    try {
                        ServerSocket serverSocket = new ServerSocket(1234);
                        Socket socket = serverSocket.accept();
                        ++counter;
                        serverSocket.close();
                        listSocket.add(socket);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        readers.add(reader);
                        writers.add(writer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                receiveMessage();
                for (int i = 0; i < 90; ++i) {
                    figures.add(random.nextInt(0, 31) + 1);
                }
            }
        }).start();
    }

    public void sendMessage(int destination, String number) {
        try {
            writers.get(destination).write(number);
            writers.get(destination).newLine();
            writers.get(destination).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (listSocket.get(0).isConnected()) {
                    try {
                        String name = readers.get(0).readLine();
                        if (name.equals("Restart")) {
                            if (numberPlayers == 1) {
                                sendMessage(0, "Restart");
                            } else {
                                if (otherRestart) {
                                    otherRestart = false;
                                    sendMessage(0, "Restart");
                                    sendMessage(1, "Restart");
                                    seconds = 0;
                                    minutes = 0;
                                    if (!times) {
                                        times = true;
                                        new Thread(task).start();
                                    }
                                } else {
                                    otherRestart = true;
                                }
                            }
                            continue;
                        }
                        if (name.equals("TOP")) {
                            sendTop(0);
                            continue;
                        }
                        if (name.equals("Close")) {
                            --numberPlayers;
                            if (numberPlayers == 0) {
                                closeEverything();
                            } else {
                                sendMessage(0, "CloseSocket");
                                sendMessage(1, "Alone");
                            }
                            break;
                        }
                        if (name.length() > 3 && name.substring(0, 4).equals("NAME")) {
                            if (numberPlayers == 1) {
                                nameFirst = name.substring(5);
                                sendMessage(0, "Equal");
                                if (!times) {
                                    times = true;
                                    new Thread(task).start();
                                }
                            } else {
                                nameFirst = name.substring(4);
                                if (nameSecond != null) {
                                    sendMessage(0, "NAME" + nameSecond);
                                    sendMessage(1, "NAME" + nameFirst);
                                    seconds = 0;
                                    minutes = 0;
                                    if (!times) {
                                        times = true;
                                        new Thread(task).start();
                                    }
                                }
                            }
                        } else {
                            if (name.equals("Shake")) {
                                figures.clear();
                                for (int i = 0; i < 90; ++i) {
                                    figures.add(random.nextInt(0, 31) + 1);
                                }
                            } else {
                                if (name.length() >= 7) {
                                    String pref = name.substring(0, 7);
                                    if (pref.equals("OverMov")  || pref.equals("OverMin") || pref.equals("OverSec")) {
                                        firstData.add(Integer.parseInt(name.substring(7)));
                                    }
                                    if (secondData.size() == 3 && firstData.size() == 3) {
                                        sendData();
                                    }
                                    if (firstData.size() == 3) {
                                        toDb(firstData);
                                    }
                                    if (secondData.size() == 3) {
                                        toDb(secondData);
                                    }
                                } else {
                                    sendMessage(0, String.valueOf(figures.get(Integer.parseInt(name))));
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
        if (numberPlayers == 2) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (listSocket.get(1).isConnected()) {
                        try {
                            String name = readers.get(1).readLine();
                            if (name.equals("Restart")) {
                                if (numberPlayers == 1) {
                                    sendMessage(0, "Restart");
                                } else {
                                    if (otherRestart) {
                                        otherRestart = false;
                                        sendMessage(0, "Restart");
                                        sendMessage(1, "Restart");
                                        seconds = 0;
                                        minutes = 0;
                                        if (!times) {
                                            times = true;
                                            new Thread(task).start();
                                        }
                                    } else {
                                        otherRestart = true;
                                    }
                                }
                                continue;
                            }
                            if (name.equals("Close")) {
                                --numberPlayers;
                                if (numberPlayers == 0) {
                                    closeEverything();
                                } else {
                                    sendMessage(0, "Alone");
                                    sendMessage(1, "CloseSocket");
                                }
                                break;
                            }
                            if (name.equals("TOP")) {
                                sendTop(1);
                                continue;
                            }
                            if (name.equals("Shake")) {
                                figures.clear();
                                for (int i = 0; i < 90; ++i) {
                                    figures.add(random.nextInt(0, 31) + 1);
                                }
                                continue;
                            }
                            if (name.length() > 3 && name.substring(0, 4).equals("NAME")) {
                                if (numberPlayers == 1) {
                                    sendMessage(0, "Equal");
                                } else {
                                    nameSecond = name.substring(4);
                                    if (nameFirst != null) {
                                        sendMessage(0, "NAME" + nameSecond);
                                        sendMessage(1, "NAME" + nameFirst);
                                        seconds = 0;
                                        minutes = 0;
                                        if (!times) {
                                            times = true;
                                            new Thread(task).start();
                                        }
                                    }
                                }
                            } else {
                                if (name.length() >= 7) {
                                    String pref = name.substring(0, 7);
                                    if (pref.equals("OverMov")  || pref.equals("OverMin") || pref.equals("OverSec")) {
                                        secondData.add(Integer.parseInt(name.substring(7)));
                                    }
                                    if (secondData.size() == 3 && firstData.size() == 3) {
                                        sendData();
                                    }
                                    if (firstData.size() == 3) {
                                        toDb(firstData);
                                    }
                                    if (secondData.size() == 3) {
                                        toDb(secondData);
                                    }
                                } else {
                                    sendMessage(1, String.valueOf(figures.get(Integer.parseInt(name))));
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();
        }
    }

    private void sendTop(int dest) {
        try {
            Statement statement = connection.createStatement();
            String request = "SELECT Login, Dat, Tim, Moves, Times " +
                    "From PLAYS " +
                    "ORDER BY Moves DESC, Times DESC, Dat DESC, Tim DESC " +
                    "FETCH FIRST 10 ROWS ONLY";
            ResultSet set = statement.executeQuery(request);
            StringBuilder result = new StringBuilder("DB");
            result.append("login |         ").append("data          |      ").append("time     | ").append("moves | ").append("times").append("\n");
            result.append("DB");
            for (int i = 0; i < 55; ++i) {
                result.append('-');
            }
            result.append("\n");
            while (set.next()) {
                String login = set.getString("Login") + " | ";
                String data = set.getString("Dat") + " | ";
                String time = set.getString("Tim") + "  |     ";
                String moves = set.getString("Moves") + "     | ";
                String times = set.getString("Times");
                result.append("DB").append(login).append(data).append(time).append(moves).append(times).append("\n");
            }
            result = new StringBuilder(result.substring(0, result.length() - 1));
            sendMessage(dest, String.valueOf(result));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void toDb(List<Integer> list) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO PLAYS(Login, Dat, Tim, Moves, Times) " +
                            "VALUES (?, ?, ?, ?, ?)"
            );
            preparedStatement.setString(1, nameFirst);
            LocalDate date = LocalDate.now();
            preparedStatement.setDate(2, Date.valueOf(date));
            LocalTime time = LocalTime.now();
            preparedStatement.setTime(3, Time.valueOf(time));
            preparedStatement.setInt(4, list.get(0));
            preparedStatement.setString(5, list.get(1) + " min; " + list.get(2) + " sec");

            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendData() {

        sendMessage(0, "OverMov" + secondData.get(0));
        sendMessage(0, "OverMin" + secondData.get(1));
        sendMessage(0, "OverSec" + secondData.get(2));

        sendMessage(1, "OverMov" + firstData.get(0));
        sendMessage(1, "OverMin" + firstData.get(1));
        sendMessage(1, "OverSec" + firstData.get(2));

        secondData.clear();
        firstData.clear();
    }

    public void closeEverything() {
        try {
            for (Socket socket : listSocket) {
                socket.close();
            }
            for (BufferedReader reader : readers) {
                reader.close();
            }
            for (BufferedWriter writer : writers) {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Task<Void> task = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (true) {
                ++seconds;
                if (seconds == 60) {
                    ++minutes;
                    seconds = 0;
                }
                if (seconds == maxSeconds && maxMinutes == minutes) {
                    sendMessage(0, "Time");
                    sendMessage(1, "Time");
                }
                Thread.sleep(1000);
            }
        }
    };
}
