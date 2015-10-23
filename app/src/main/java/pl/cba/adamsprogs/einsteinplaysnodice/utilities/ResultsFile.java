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
        readFile();
        results = new int[2];
    }

    private void readFile() {
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                    context.openFileInput(fileName)));
            String inputString;
            String[] results;
            if ((inputString = inputReader.readLine()) != null) {
                Log.i("ResultsFile", "readFile: " + inputString);
                results = inputString.split(" ");
                for (int i = 0; i < 2; ++i)
                    this.results[i] = Integer.parseInt(results[i]);
            }else Log.i("ResultsFile", "readFile: null");
        } catch (Exception ignored) {
        }
    }

    public void apply() {
        String res = results[0] + " " + results[1];
        Log.i("ResultsFile", "apply: " + toString());
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(res.getBytes());
            fos.close();
            Log.i("ResultsFile", "apply: written");
        } catch (Exception e) {
            e.printStackTrace();
            Log.wtf("ResultsFile", "apply: not written" + e.getMessage());
        }
    }

    public void refresh() {
        readFile();
    }

    public int[] getResults() {
        return results;
    }

    public void setResults(int[] results) {
        this.results = results;
    }

    public void increment(int which) {
        Log.i("ResultsFile", "pre: " + toString());
        ++results[which];
        Log.i("ResultsFile", "post: " + toString());
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
