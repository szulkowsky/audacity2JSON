import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import netscape.javascript.JSObject;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        // -w - wav filename
        // -wd - wav file duration
        // -a - audacity filepath
        // -d - dembski json name
        // -i - index

        String wavFilePath = "";
        String audacityFilePath = "";
        String dembskiFileName = "";
        Integer index = 0;
        Double duration = 0.0;


        if(args.length < 8){
            System.out.println("Not enough arguments");
            System.out.println("Proper parameters are:");
            System.out.println("-w - wav filepath\n" +
                    "-a - audacity filepath\n" +
                    "-d - dembski json name\n" +
                    "-i - index");
            return;
        }

        for(int i = 0; i<args.length; i++){
            if(args[i].equals("-w")){
                if(args[i+1] != null)
                    wavFilePath = args[i+1];
                else{
                    System.out.println("Error with .wav file, consider correct parameters");
                    return;
                }
            }
            else if(args[i].equals("-a")){
                if(args[i+1] != null)
                    audacityFilePath = args[i+1];
                else{
                    System.out.println("Error opening audacity labels file, consider correct parameters");
                    return;
                }
            }
            else if(args[i].equals("-d")){
                if(args[i+1] != null)
                    dembskiFileName = args[i+1];
                else{
                    System.out.println("Error creating dembski.fis file, consider correct parameters");
                    return;
                }
            }
            else if(args[i].equals("-i")){
                if(args[i+1] != null)
                    index = Integer.parseInt(args[i+1]);
                else{
                    System.out.println("Error getting your index, consider correct parameters");
                    return;
                }
            }
            else
                System.out.println(args[i]);
        }

        File wavFile = new File(wavFilePath);
        AudioInputStream audioInputStream = null;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(wavFile);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            return;
        }

        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        duration = (frames+0.0) / format.getFrameRate();

        JsonObject allData = new JsonObject();
        JsonObject job = new JsonObject();
        job.addProperty("lang", "pl");
        job.addProperty("user_id", index);
        job.addProperty("name", wavFile.getName());
        job.addProperty("duration", duration);

        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        Date date = new Date();

        job.addProperty("created_at", dateFormat.format(date));
        job.addProperty("id", new Random().nextInt());

        allData.add("job", job);

        JsonArray speakersArray = new JsonArray();
        JsonObject speaker = new JsonObject();
        speaker.addProperty("duration", String.valueOf(duration));
        speaker.addProperty("confidence", String.valueOf(0));
        speaker.addProperty("name", String.valueOf(index));
        speaker.addProperty("time", "0.00");
        speakersArray.add(speaker);
        allData.add("speakers", speakersArray);


        JsonArray wordsArray = new JsonArray();

        //opening Audacity File and parsing into json
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(audacityFilePath));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] parts = line.split("\t");
                JsonObject word = new JsonObject();
                float wordDuration = Float.parseFloat(parts[1]) - Float.parseFloat(parts[0]);
                word.addProperty("duration", String.valueOf(wordDuration));
                word.addProperty("confidence", String.valueOf(1.0));
                word.addProperty("name", parts[2]);
                word.addProperty("time", parts[0]);
                wordsArray.add(word);
            }
            reader.close();
            allData.add("words", wordsArray);
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", audacityFilePath);
            e.printStackTrace();
            return;
        }
        allData.addProperty("format", "1.0");
        allData.toString();

        try (Writer writer = new FileWriter(dembskiFileName)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(allData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
