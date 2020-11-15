import java.util.*;
import java.io.*;

public class HighScore {
    //Array List of all of the names and scores in the file,
    //both ArrayList are related list
    private ArrayList<String> names = new ArrayList<String>();
    private ArrayList<Integer> scores = new ArrayList<Integer>();
    //When initialize it opens the file and adds all of the content to the names and scores lists
    public HighScore() {
        try {
            Scanner inFile = new Scanner(new BufferedReader(new FileReader("Scores.txt")));
            while(inFile.hasNext()) {
                String n = inFile.next(); names.add(n);
                int s = inFile.nextInt(); scores.add(s);
            }
        }
        catch(FileNotFoundException ex) {
            System.out.println("Add a file called Score.txt at the root");
            System.out.println(ex);
        }
    }

    //Adds the score and name to the names and scores list sorted from greatest to least
    public void addScore(String name, int score) {
        //A buffer for if there is no value at all or the new score is lower than last lowest score
        scores.add(Integer.MIN_VALUE);
        int size = scores.size();
        for (int i = 0; i < size; i++) {
            if (scores.get(i) <= score) {
                scores.add(i, score);
                names.add(i, name);
                break;
            }
        }
        //Remove the buffer
        scores.remove(size);
    }

    //Writes the current ArrayList of scores along with the name to Score.txt file
    public void saveScore() {
        try {
            //Output I use a PrintWriter
            PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter("Scores.txt")));

            for (int i = 0; i < names.size(); i++) {
                outFile.println(names.get(i) + " " + scores.get(i));
            }
            outFile.close();
        }
        catch(IOException ex) {
            System.out.println("Create new file please");
            System.out.println(ex);
        }
    }

    //Getters for scores and names
    public ArrayList<Integer> getScores() {return scores;}
    public ArrayList<String> getNames() {return names;}
}