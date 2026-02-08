# IY4113 Milestone 3

| Assessment Details | Please Complete All Details                                             |
| ------------------ | ----------------------------------------------------------------------- |
| Group              | B                                                                       |
| Module Title       | IY4113 Applied Software Engineering using Object-Orientated Programming |
| Assessment Type    | Milestone 3                                                             |
| Module Tutor Name  | Jonathan Shore                                                          |
| Student ID Number  | P501367                                                                 |
| Date of Submission | 08/02/2026                                                              |
| Word Count         | 1120                                                                    |

- [x] *I confirm that this assignment is my own work. Where I have referred to academic sources, I have provided in-text citations and included the sources in
  the final reference list.*
- [ ] *Where I have used AI, I have cited and referenced appropriately.

------------------------------------------------------------------------------------------------------------------------------

### Research (minimum of 2, at least 3)

---

Conduct research to support your coding process, including use of code examples, tutortials, documentation and AI tools (if used).
Use the structure below to capture your evidence:

------------------------------------------------------------------------------------------------------------------------------

Title of research: Input validation with range checking
Reference (link): https://ondrej-kvasnovsky.medium.com/java-101-lesson-14-input-validation-robustness-908331a56f3d
How does the research help with coding practise?: Input validation prevents the program to enter an error state. It checks and validates the user input and gives feedback to the user.  
Key coding ideas you could reuse in your program: 

```java
import java.util.Scanner;

void main() {
    Scanner scanner = new Scanner(System.in);

    int age = getValidInput(scanner, "Enter your age:", 1, 120);
    IO.println("Age: " + age);

    int score = getValidInput(scanner, "Enter test score:", 0, 100);
    IO.println("Score: " + score);

    int guess = getValidInput(scanner, "Guess a number:", 1, 100);
    IO.println("You guessed: " + guess);
}

int getValidInput(Scanner scanner, String prompt, int min, int max) {
    int value = min - 1;  // Start with invalid value
    while (!isInRange(value, min, max)) {
        IO.println(prompt);

        value = scanner.nextInt();
        if (!isInRange(value, min, max)) {
            IO.println("Invalid! Must be between " + min + " and " + max + ".\n");
        }
    }
    return value;
}

boolean isInRange(int value, int min, int max) {
    return value >= min && value <= max;
}
```

Screenshot of research:  

![Screenshot 1]https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/researchCode1.png

------------------------------------------------------------------------------------------------------------------------------

Title of research: Storing Objects in Array List
Reference (link): [How Objects Can an ArrayList Hold in Java?](https://www.tutorialspoint.com/how-objects-can-an-arraylist-hold-in-java)
How does the research help with coding practise?: It helps me to create classes and store instances of it in an "ArrayList". It makes it possible to store large amount of journeys.Key coding ideas you could reuse in your program:  

```java
import java.util.*;
public class Cart {
   String item;
   int price;
   Cart(String item, int price) {
      // this keyword shows these variables belong to constructor
      this.item = item;
      this.price = price;
   }
   // method for converting object into string
   public String toString() {
      return "Item: " + item + ", " + "Price: " + price;
   }
   public static void main(String[] args) {
      // Declaring collection arraylist
      ArrayList<Cart> obj = new ArrayList<>();
      // Adding object to the collection
      obj.add(new Cart("Rice", 59));
      obj.add(new Cart("Milk", 60));
      obj.add(new Cart("Bread", 45));
      obj.add(new Cart("Peanut", 230));
      obj.add(new Cart("Butter", 55));
      // to print list of objects
      System.out.println("The Objects in the List: ");
      for(Cart print : obj) {
         System.out.println(print);
      }
   }
}
```

Screenshot of research:

![Screenshot 2]https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/researchCode2.png

---

Title of research: Fare Capping
Reference (link): [GitHub - root0109/fare-calculator-engine: This is the solution to the problem statement to design the fare calculation engine called as TigerCard](https://github.com/root0109/fare-calculator-engine)
How does the research help with coding practise?:  It uses clipping to initialize daily fare capping.
Key coding ideas you could reuse in your program:

```java
public class DailyCappedFareStrategy implements FareStrategy {
    @Override
    public double apply(List<Journey> journeys, double currentTotalFare) {
        double dailyCap = getDailyCapForZones(journeys);
        double totalCalculated = currentTotalFare;
        
        for (Journey journey : journeys) {
            double individualFare = journey.getFare();
            if (totalCalculated + individualFare > dailyCap) {
                journey.setFare(dailyCap - totalCalculated);
                totalCalculated = dailyCap;
            } else {
                totalCalculated += individualFare;
            }
        }
        return totalCalculated;
    }
}
```

Screenshot of research:

![Screenshot 3]https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/researchCode3.png

---

### Program Code

---

Paste the current program code created so far. It does not have to be runnable code (document though if it does not work!)
------------------------------------------------------------------------------------------------------------------------------

*Program code goes here:*
------------------------------------------------------------------------------------------------------------------------------

```java
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Journey> journeys = new ArrayList<>();
    private static String passengerName = "Unknown";
    private static String passengerType = "Adult";

    public static void main(String[] args) {
        System.out.println("Welcome to CityRide Lite!");
        setupSession();
        
        boolean exit = false;
        while (!exit) {
            displayMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addJourney();
                    break;
                case "2":
                    listJourneys();
                    break;
                case "3":
                    filterJourneys();
                    break;
                case "4":
                    viewDailySummary();
                    break;
                case "5":
                    viewTotalsByPassengerType();
                    break;
                case "6":
                    removeJourney();
                    break;
                case "7":
                    resetDay();
                    break;
                case "8":
                    exit = true;
                    System.out.println("Exiting CityRide Lite. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    
    private static void setupSession() {
        System.out.print("Enter Passenger Name: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            passengerName = name;
        }

        System.out.print("Enter Passenger Type (Adult, Student, Child, Senior Citizen): ");
        String type = scanner.nextLine().trim();
        if (!type.isEmpty()) {
            passengerType = type;
        }
        System.out.println("Session started for " + passengerName + " as " + passengerType);
    }

    private static void displayMenu() {
        System.out.println("\nSession: " + passengerName + " (" + passengerType + ")");
        System.out.println("--- Main Menu ---");
        System.out.println("1. Add Journey");
        System.out.println("2. List Journeys");
        System.out.println("3. Filter Journeys");
        System.out.println("4. View Daily Summary");
        System.out.println("5. View Totals by Passenger Type");
        System.out.println("6. Undo/Remove Journey");
        System.out.println("7. Reset Day");
        System.out.println("8. Exit");
        System.out.print("Choose an option: ");
    }

    
    private static void addJourney() {
        System.out.println("\n--- Add Journey ---");
        
        System.out.print("Enter Date (DD/MM/YYYY): ");
        String date = scanner.nextLine().trim();
        
        System.out.print("Enter Starting Zone (1-5): ");
        String fromZone = scanner.nextLine().trim();
        
        System.out.print("Enter Destination Zone (1-5): ");
        String toZone = scanner.nextLine().trim();
        
        System.out.print("Enter Time Band (Peak/Off-peak): ");
        String timeBand = scanner.nextLine().trim();
        
        System.out.print("Enter Passenger Type: ");
        String type = scanner.nextLine().trim();

    }

    private static void listJourneys() {}

    private static void filterJourneys() {}

    private static void viewDailySummary() {}

    private static void viewTotalsByPassengerType() {}

    private static void removeJourney() {}

    private static void resetDay() {}
}


```



### Updated Gantt Chart

------------------------------------------------------------------------------------------------------------------------------

![](C:\Users\User\Downloads\Gantt%20(2).jpg)
------------------------------------------------------------------------------------------------------------------------------

### Diary Entries

------------------------------------------------------------------------------------------------------------------------------

### 02/02/2026 - Diary Entry 5 – Coding

Today I dived into my coding. Although I did some coding previously, it's progress was not good enough. So, I dedicated more of my time for my code than other tasks. The coding part was simple and straight forward. I did not face any problems while coding.

One note to myself is time management. For the first two milestones, I managed my time properly. However, it wasn't the case this time.  

### 6/02/2026 - Diary Entry 6 – Research on Coding

Today I did research that would be useful for my future coding. I was having a but of trouble finding research materials for my coding. After finding some resources, I tried using the code but it was incomplete or there was an error occuring. Which was super frustrating. However, I did more searching and in the end I'm happy with what I found.

------------------------------------------------------------------------------------------------------------------------------

