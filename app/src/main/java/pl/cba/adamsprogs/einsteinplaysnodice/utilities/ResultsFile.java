package pl.cba.adamsprogs.einsteinplaysnodice.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.*;

import pl.cba.adamsprogs.einsteinplaysnodice.R;

public class ResultsFile {
    private final String fileName = "EinsteinResults";
    private final Context context;
    private String[] results;

    public ResultsFile(Context context) {
        this.context = context;
        results = new String[2];
        readFile();
    }

    private void readFile() {
        try {
            readResults();
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void readResults() throws IOException {
        String inputString;
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                context.openFileInput(fileName)));
        if ((inputString = inputReader.readLine()) != null)
            results = inputString.split(" ");
    }

    private void handleException(IOException e) {
        Log.e("ResultFile", "Reading error " + e.getMessage());
        e.printStackTrace();
        Toast.makeText(context, context.getString(R.string.resultsReadingError), Toast.LENGTH_SHORT).show();
    }

    public void apply() {
        String res = results[0] + " " + results[1];
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(res.getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e("ResultFile", "Writing error " + e.getMessage());
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

    private void setResults(String[] results) {
        this.results = results;
    }

    public void increment(int which) {
        int result = Integer.parseInt(results[which]);
        ++result;
        results[which] = result + "";
    }

    public void clear() {
        setResults(new String[]{"0", "0"});
    }

    public void delete() throws IOException {
        File dir = context.getFilesDir();
        File file = new File(dir, fileName);

        if(!file.delete()){
            throw new IOException("Couldn't delete results file");
        }
    }

    @Override
    public String toString() {
        return results[0] + ", " + results[1];
    }
}
