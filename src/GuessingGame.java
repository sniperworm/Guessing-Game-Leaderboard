import java.util.*;
public class GuessingGame {
    Scanner scan = new Scanner(System.in);
    private boolean quit;
    private int computerNum;
    private double numGuesses = 0.0;
    private int totalRounds = 0;

    public GuessingGame(){
        quit = false;
        computerNum = 0;
    }

    public GuessingGame(Scanner scan){
        this.scan = scan;
        quit = false;
        computerNum = 0;
    }

    public int randomNum(){
        computerNum =  (int)(Math.random()*1000)+1;
        return computerNum;
    }


    public void guessRound(){
        while(totalRounds < 3){
            int store = randomNum();
            boolean roundIsOver = false;

            while(!roundIsOver){
                System.out.println("Guess a number between 1 and 1000");

                if(!scan.hasNextLine()){
                    quit = true;
                }

                try {
                    int guess = Integer.parseInt(scan.nextLine().trim());
                    numGuesses++;

                    if(guess > store){
                        System.out.println("Your guess is too high");
                    }
                    else if(guess < store){
                        System.out.println("Your guess is too low");
                    }
                    else{
                        System.out.println("Congratulations, you guessed it!");
                        totalRounds++;
                        roundIsOver = true;
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Invalid input");
                }
            }
        }
    }
    public double average(){
       return numGuesses/totalRounds;
    }

    public String userResult(){
        guessRound();
        double avg = average();
        return "Your guessing average = " + avg;
    }
}
