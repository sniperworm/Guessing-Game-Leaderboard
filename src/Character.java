import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Character {
    private String name;
    private int id;
    private double score;
    // This is the file where the program remembers the next available ID number.
    // A normal static variable resets to 0 every time the program starts over.
    // By saving the number in a file, the program can close, run again later,
    // and continue counting from where it left off.
    private static final Path ID_COUNTER_FILE = Path.of("character-id-counter.txt");

    // "static" means there is only one nextId value shared by the whole Character class.
    // Every Character object uses this same counter instead of each object having its own.
    // loadNextId() runs when the class is first loaded so the counter starts with the
    // number saved in the file, or 0 if the file does not exist yet.
    private static int nextId = loadNextId();

    public Character(String name){
        this.name = name;

        // The ID is not typed in by the user anymore.
        // getNextId() gives this Character the current ID number, then prepares the
        // next number for the next Character that gets created.
        this.id = getNextId();
    }

    public Character(String name, int id) {
        this.name = name;
        this.id = id;

        // This constructor still lets you create a Character with a specific ID.
        // If that ID is bigger than the saved counter, we move the counter forward
        // so future automatically-created IDs do not accidentally repeat it.
        updateNextIdAfterManualId(id);
    }

    public Character() {
        this.name = "Unnamed Character";

        // The default constructor also uses the automatic ID system.
        // That way every Character gets a unique ID no matter which constructor is used.
        this.id = getNextId();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;

        // If someone changes a Character's ID manually, the counter may need to move up.
        // Example: if nextId is 5 and someone sets an ID to 20, the next automatic ID
        // should become 21, not 5.
        updateNextIdAfterManualId(id);
    }
    public void setScore(double score){
        this.score = score;
    }
    public double getScore(){
        return score;
    }
    private static int getNextId() {
        // Save the current counter value into a local variable.
        // This is the ID we are about to give to the new Character.
        int id = nextId;

        // Increase nextId so the next Character gets a different number.
        nextId++;

        // Save the new nextId value to the file right away.
        // This is what allows the counter to keep increasing after pressing Run again.
        saveNextId();

        // Return the original value, not the increased one.
        // Example: if nextId started as 7, this Character gets 7,
        // then the saved nextId becomes 8 for the next Character.
        return id;
    }

    private static int loadNextId() {
        // If the counter file has not been created yet, this is probably the first run.
        // Starting at 0 means the first Character will have ID 0.
        if (!Files.exists(ID_COUNTER_FILE)) {
            return 0;
        }

        try {
            // Read the text from the file, remove extra spaces/new lines with trim(),
            // then convert that text into an integer.
            String value = Files.readString(ID_COUNTER_FILE).trim();
            return Integer.parseInt(value);
        } catch (IOException | NumberFormatException e) {
            // IOException means Java had trouble reading the file.
            // NumberFormatException means the file did not contain a valid number.
            // In either case, the program falls back to 0 instead of crashing.
            return 0;
        }
    }

    private static void saveNextId() {
        try {
            // Files can only store text or bytes, not an int directly.
            // String.valueOf(nextId) converts the number into text before writing it.
            Files.writeString(ID_COUNTER_FILE, String.valueOf(nextId));
        } catch (IOException e) {
            // If the file cannot be saved, the program still keeps running.
            // The warning lets you know the ID counter may not be remembered next time.
            System.out.println("Could not save the next character ID.");
        }
    }

    private static void updateNextIdAfterManualId(int id) {
        // Only move the counter forward if the manual ID is greater than or equal to
        // the next automatic ID. Smaller manual IDs do not affect the future counter.
        if (id >= nextId) {
            // Add 1 because the next automatic ID should come after the manual ID.
            nextId = id + 1;
            saveNextId();
        }
    }

     @Override
    public String toString() {
        return "Name: " + getName() + " " + "ID Number: " +  String.valueOf(getId());
    }
}
