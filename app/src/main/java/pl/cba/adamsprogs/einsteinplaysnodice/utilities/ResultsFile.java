package pl.cba.adamsprogs.einsteinplaysnodice.utilities;

import android.content.Context;
import android.util.Log;

import java.io.*;

public class ResultsFile {
    private String fileName = "EinsteinResults";
    private Context context;
    private int[] results;

    public ResultsFile(Context context) {
        this.context = context;
        results = new int[2];
        readFile();
    }

    private void readFile() {
        String inputString;
        String[] results;
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                    context.openFileInput(fileName)));
            if ((inputString = inputReader.readLine()) != null) {
                results = inputString.split(" ");
                for (int i = 0; i < 2; ++i) {
                    this.results[i] = Integer.parseInt(results[i]);
                }
            }
        } catch (Exception e) {
            Log.e("ResultFile", "Reading error "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void apply() {
        String res = results[0] + " " + results[1];
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(res.getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e("ResultFile", "Writing error "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void refresh() {
        readFile();
    }

    public String[] getResults() {
        String[] r = new String[2];
        r[0] = results[0] + "";
        r[1] = results[1] + "";
        return r;
    }

    public void setResults(int[] results) {
        this.results = results;
    }

    public void increment(int which) {
        ++results[which];
    }

    public void clear() {
        setResults(new int[]{0, 0});
    }

    public void delete() {
        File dir = context.getFilesDir();
        File file = new File(dir, fileName);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    @Override
    public String toString() {
        return results[0] + ", " + results[1];
    }
}
