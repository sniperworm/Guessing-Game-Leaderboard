import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
public class Sorts {
    // This is the file where the full character list is saved.
    // It is different from character-id-counter.txt:
    // - character-id-counter.txt remembers the next ID number.
    // - characters.txt remembers the actual characters in the ArrayList.
    private static final Path CHARACTER_LIST_FILE = Path.of("characters.txt");
    ArrayList<Character> list = new ArrayList<Character>();
    Scanner scan = new Scanner(System.in);

public Sorts(){
     // When the program starts, loadCharacters() tries to rebuild the ArrayList
     // from characters.txt. This is what lets the list survive after you stop
     // the program and press Run again.
     this.list = loadCharacters();

     // After the saved list is loaded, askUser() lets the user add more names.
     askUser();
}

public void insertionSort(){
    for(int i = 1; i < list.size(); i++){
        Character temp = list.get(i);
        int key = temp.getId();
        int j = i - 1;

        while(j >= 0 && list.get(j).getId() > key){
            list.set(j+1, list.get(j));
            j--;
        }
        list.set(j+1, temp);
    }

}
public void askUser(){
    boolean updateTruthStatements = true;
    while(updateTruthStatements) {
        System.out.println("Would you like to add a Character (Y/N)");
        String quit =  scan.nextLine();
        try{
            if(quit.equalsIgnoreCase("N") || quit.equalsIgnoreCase("No")) {
                updateTruthStatements = false;
            }
            else if(quit.equalsIgnoreCase("Y") || quit.equalsIgnoreCase("Yes")){

                System.out.print("Enter a name:");
                String name = scan.nextLine();

                // Check the current ArrayList to see if that name already exists.
                // If it does, we do not create a brand-new Character with another ID.
                boolean check = findDupes(list, name);
                if(check){
                    System.out.println("That is a dupe");
                    removeDupes();

                    // After changing the list, save it again so characters.txt matches
                    // what is currently inside the ArrayList.
                    saveCharacters();
                }
                else {
                    // This calls our helper method instead of asking the user for an ID.
                    // The Character class handles the ID automatically when new Character(name)
                    // runs inside createCharacter.
                    Character temp = createCharacter(name);
                    list.add(temp);

                    // Make a new GuessingGame for this one Character.
                    // This matters because numGuesses and totalRounds should start at 0
                    // for each new player instead of carrying over from the last player.
                    GuessingGame testRunner = new GuessingGame(scan);
                    String storage = testRunner.userResult();
                    System.out.println(storage);
                    temp.setScore(testRunner.average());

                    // Save the whole list after adding a new Character.
                    // This makes the list available again the next time the program runs.
                    saveCharacters();
                    updateTruthStatements = false;
                }

            }
            else{
                System.out.println("Not a valid input");
            }
        }catch(InputMismatchException e){
            System.out.println("Invalid Input");
        }
    }
}
    public void printList() {
        sortAverages();
        for(int i = 0; i < list.size(); i++) {
            // i is the ArrayList index, which means the position in the list.
            // temp.getId() is the Character's permanent ID number.
            // These are not always the same thing.
            // Example: Index 0 could hold a Character whose ID is 5.
            Character temp = list.get(i);
            System.out.println("Index: " + i + " " + temp.toString() + " Score: " + temp.getScore()) ;
        }
    }
    public Character findCharacter(int id){
    insertionSort();
        for (Character temp : list) {
            if (temp.getId() == id) {
                return temp;
            }
        }
    return null;
    }
    public void addCharacter(Character temp){
    list.add(temp);
    insertionSort();

    // Any time code adds a Character through this method, save the updated list
    // so the new Character appears again on the next run.
    saveCharacters();
    }

    public Character createCharacter(String name){
    // This method is a simple "factory" method: its job is to create and return
    // a new Character object. Since we only pass in the name, Java chooses the
    // Character(String name) constructor, which automatically assigns a unique ID.
    return new Character(name);
    }

    public boolean findDupes(ArrayList<Character> list, String name) {
        for(Character temp: list){
            if(temp.getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }
    public void removeDupes(){
    Character temp;
    Character temp2;
    for(int i = 0; i < list.size(); i++){
        for(int j = i + 1; j < list.size(); j++) {
            temp = list.get(i);
            temp2 = list.get(j);
            if (temp.getName().equalsIgnoreCase(temp2.getName())){
                list.remove(temp2);
                j--;
            }
        }
    }
    }

    public ArrayList<Character> loadCharacters(){
    // Make a new empty ArrayList. If there is no saved file yet, this empty list
    // will be returned and the program will start with no saved characters.
    ArrayList<Character> savedCharacters = new ArrayList<Character>();

    // If characters.txt does not exist, there is nothing to load.
    // This usually happens the first time the program runs or after you delete the file.
    if(!Files.exists(CHARACTER_LIST_FILE)){
        return savedCharacters;
    }

    try{
        // Read every line from characters.txt.
        // Each line represents one Character.
        ArrayList<String> lines = new ArrayList<String>(Files.readAllLines(CHARACTER_LIST_FILE));
        for(String line : lines){
            // Each saved line is written as:
            // ID, then a tab character, then score, then a tab character, then name.
            // Example line: 3    4.5    Alice
            // split("\t", 3) separates that line into three parts:
            // parts[0] is the ID, parts[1] is the score, and parts[2] is the name.
            String[] parts = line.split("\t", 3);

            if(parts.length == 3){
                // The ID was saved as text in the file, so Integer.parseInt()
                // converts it back into an int.
                int id = Integer.parseInt(parts[0]);
                double score = Double.parseDouble(parts[1]);
                String name = parts[2];

                // This constructor rebuilds the old Character using its saved name
                // and saved ID. It does not create a brand-new automatic ID.
                Character temp = new Character(name, id);
                temp.setScore(score);
                savedCharacters.add(temp);
            }
            else if(parts.length == 2){
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                savedCharacters.add(new Character(name, id));
            }
        }
    }
    catch(IOException | NumberFormatException e){
        // IOException means there was a problem reading the file.
        // NumberFormatException means an ID in the file was not a valid number.
        System.out.println("Could not load the saved character list.");
    }

    return savedCharacters;
    }

    public void saveCharacters(){
    // Files.write() needs a list of Strings, so this ArrayList will hold the text
    // version of each Character before writing it to characters.txt.
    ArrayList<String> lines = new ArrayList<String>();

    for(Character temp : list){
        // Save each Character on one line using this format:
        // ID + tab + score + tab + name
        // The tab makes it easier to split the line apart later in loadCharacters().
        lines.add(temp.getId() + "\t" + temp.getScore() + "\t" + temp.getName());
    }

    try{
        // Write every line to characters.txt.
        // This replaces the old file contents with the current ArrayList contents.
        Files.write(CHARACTER_LIST_FILE, lines);
    }
    catch(IOException e){
        // If the list cannot be saved, the program keeps running, but the file
        // may not match the latest ArrayList.
        System.out.println("Could not save the character list.");
    }
    }
    //Precondition
        //All scores are positive doubles
    public void sortAverages(){
        for(int i = 0; i < list.size(); i++){
            Character temp = list.get(i);
            double key = temp.getScore();
            int j = i - 1;

            while(j >= 0 && list.get(j).getScore() > key){
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, temp);
        }
    }

}
