package ml.adamsprogs.einstein.mobile.activities;

import android.app.Application;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Einstein extends Application {
    private Socket socket;
    private BufferedReader input;
    private PrintStream output;

    private String response = null;

    public String connectToGame(String address, String port, @Nullable String playersNumber) {
        Thread t = new Thread(() -> {
            try {
                socket = new Socket(String.valueOf(address),
                        Integer.parseInt(String.valueOf(port)));
                output = new PrintStream(socket.getOutputStream());

                if (playersNumber != null)
                    output.println("create " + playersNumber);
                else
                    output.println("join");

                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                response = input.readLine();
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
        return response;
    }
}
