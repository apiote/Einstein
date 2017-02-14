package ml.adamsprogs.einstein.mobile.activities;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import ml.adamsprogs.einstein.engine.utils.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Einstein extends Application {
    private Socket socket;
    private BufferedReader input;
    private PrintStream output;
    private String socketReturnValue;


    public String connectToGame(String address, String port, @Nullable String playersNumber) throws IllegalStateException {
        Thread t = new Thread(() -> {
            try {
                String response;
                socket = new Socket(String.valueOf(address),
                        Integer.parseInt(String.valueOf(port)));
                output = new PrintStream(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if (playersNumber != null) {
                    output.println("create " + playersNumber);
                    response = input.readLine();
                    if (response.split(" ")[0].equals("error"))
                        throw new IllegalStateException(response.split(" ")[2]);
                } else
                    output.println("join");

                response = input.readLine();
                if (response.split(" ")[0].equals("error"))
                    throw new IllegalStateException(response.split(" ")[2]);
                else
                    socketReturnValue = response.split(" ")[2];

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return socketReturnValue;
    }

    public void waitForGameBegin() {
        Thread t = new Thread(() -> {
            try {
                String response = input.readLine();
                if (!response.equals("success game started"))
                    throw new IllegalStateException(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String receiveBoard() {
        return null;
    }

    public int receiveRoll() {
        Thread t = new Thread(() -> {
            try {
                String response = input.readLine();
                if (response.split(" ")[0].equals("success")
                        && response.split(" ")[1].equals("rolled"))
                    socketReturnValue = response.split(" ")[2];
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(socketReturnValue);
    }

    public String receiveActive() {
        Thread t = new Thread(() -> {
            try {
                String response = input.readLine();
                if (response.split(" ")[0].equals("success")
                        && response.split(" ")[1].equals("active"))
                    socketReturnValue = response.split(" ")[2];
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return socketReturnValue;
    }

    public boolean receiveVoteStoneNeeded() {
        Thread t = new Thread(() -> {
            try {
                String response = input.readLine();
                if (response.split(" ")[0].equals("success")
                        && response.split(" ")[1].equals("vote")
                        && response.split(" ")[2].equals("stone"))
                    socketReturnValue = response.split(" ")[3];
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return socketReturnValue.equals("needed");
    }

    public void sendSelectVote(@NonNull Point touchedPoint) throws IllegalStateException {
        Thread t = new Thread(() -> output.println("vote stone " + touchedPoint.x + " " + touchedPoint.y));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receiveSelectVotes(BoardActivity context) {
        Thread t = new Thread(() -> {
            try {
                int a, b;
                boolean allVotes = false;
                while (!allVotes) {
                    String response = input.readLine();
                    if (response.split(" ")[0].equals("success")
                            && response.split(" ")[1].equals("vote")
                            && response.split(" ")[2].equals("stone")) {
                        a = Integer.parseInt(response.split(" ")[3]);
                        b = Integer.parseInt(response.split(" ")[4]);
                        context.acknowledgeNewSelectVote(a, b, false);
                    } else if (response.split(" ")[1].equals("stone")) {
                        if(response.split(" ")[4].equals("not"))
                            context.acknowledgeNewSelectVote(-1, -1, true);
                        else {
                            a = Integer.parseInt(response.split(" ")[2]);
                            b = Integer.parseInt(response.split(" ")[3]);
                            context.acknowledgeNewSelectVote(a, b, true);
                        }
                        allVotes = true;
                    } else if (response.split(" ")[0].equals("error"))
                        throw new IllegalStateException(response.split(" ")[3]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    public void sendMoveVote(Point target) {
        Thread t = new Thread(() -> output.println("vote move " + target.x + " " + target.y));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receiveMoveVotes(BoardActivity context) {
        Thread t = new Thread(() -> {
            try {
                int a, b;
                boolean allVotes = false;
                while (!allVotes) {
                    String response = input.readLine();
                    if (response.split(" ")[0].equals("success")
                            && response.split(" ")[1].equals("vote")
                            && response.split(" ")[2].equals("move")) {
                        a = Integer.parseInt(response.split(" ")[3]);
                        b = Integer.parseInt(response.split(" ")[4]);
                        context.acknowledgeNewMoveVote(a, b, false);
                    } else if (response.split(" ")[3].equals("moved")) {
                        a = Integer.parseInt(response.split(" ")[5]);
                        b = Integer.parseInt(response.split(" ")[6]);
                        context.acknowledgeNewMoveVote(a, b, true);
                        allVotes = true;
                    } else if (response.split(" ")[3].equals("not_moved")){
                        context.acknowledgeNewMoveVote(-1, -1, true);
                        allVotes = true;
                    } else if (response.split(" ")[0].equals("error"))
                        throw new IllegalStateException(response.split(" ")[3]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    public String receiveActiveOrWon() {
        return null;
    }
}
