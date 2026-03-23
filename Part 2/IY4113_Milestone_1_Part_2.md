# IY4113 Milestone 1 Part 2

| Assessment Details | Please Complete All Details                                      |
| ------------------ | ---------------------------------------------------------------- |
| Group              | B                                                                |
| Module Title       | Applied Software Engineering using Object-Orientated Programming |
| Assessment Type    | Part 2: Milestone 1                                              |
| Module Tutor Name  | Jonathan Shore                                                   |
| Student ID Number  | P501367                                                          |
| Date of Submission | 22/03/2026                                                       |
| Word Count         |                                                                  |
| GitHub Link        | https://github.com/T0501367-Dalaijargal/P501367_IY-4113          |

- [x] *I confirm that this assignment is my own work. Where I have referred to academic sources, I have provided in-text citations and included the sources in
  the final reference list.*
- [ ] *Where I have used AI, I have cited and referenced appropriately.

------------------------------------------------------------------------------------------------------------------------------

### Purpose of the Program

------------------------------------------------------------------------------------------------------------------------------

Lite Part 2 expands on the functionality of the Part 1’s program by introducing
features such as data storage, the ability to create user profiles, import and
export journeys from and to files, as well as an administrator interface that
allows the application to be configured without the need for coding knowledge.
The application aims to provide a reliable means of calculating the cost of
travel within CityRide’s public transport system, just as the Oyster card does
for travelers on the London Underground, or the Transport for Wales system.

The application is designed for two different types of users.  Riders are the main users of the application, and are able to create their own profile within the application. The rider is able to record their journeys, view the cost of each journey in real-time, and view the total cost of all journeys the rider has undertaken that day (which cannot exceed the daily costcap that is set up for that rider). At the end of the day, riders can export their journeys in either a human-readable format or a CSV file for record keeping. An administrator has a separate menu system that requires a password to
access that allows for the updating of parameters related to the cost of public
transportation within CityRide Lite Part 2.

One of the most important features of this updated version of CityRide Lite is the ability to store profiles and journey data in files. Profiles and configuration data are stored in JSON files, while journey data can be imported into the application or exported from the application in the form of a CSV file.  Thus, riders will not lose their travel data if they re-launch the program

### Input Process Output Table

------------------------------------------------------------------------------------------------------------------------------

| Input                                               | Process                                                                            | Output                                                           |
| --------------------------------------------------- | ---------------------------------------------------------------------------------- | ---------------------------------------------------------------- |
| Rider name                                          | Validate name is not empty                                                         | Name stored in profile object                                    |
| Passenger type (adult, child, senior, student)      | Validate type is in the allowed list                                               | Passenger type stored in profile object                          |
| Default payment method                              | Validate payment method is not empty                                               | Payment method stored in profile object                          |
| Profile filename                                    | Check file exists, open and parse JSON                                             | Profile object loaded into memory                                |
| Profile object + journey list                       | Serialise profile and journeys to JSON                                             | Rider JSON file written to disk                                  |
| fromZone (integer 1–6)                              | Validate zone is within range 1 to 6                                               | Validated fromZone stored for journey                            |
| toZone (integer 1–6)                                | Validate zone is within range 1 to 6                                               | Validated toZone stored for journey                              |
| Journey date and time                               | Validate format matches DD/MM/YYYY HH:MM                                           | Validated datetime stored for journey                            |
| fromZone and toZone                                 | Calculate zonesCrossed = absolute(toZone – fromZone) + 1                           | zonesCrossed value stored for journey                            |
| Journey time and peak window config                 | Compare journey time against configured peak windows                               | timeBand set to peak or off-peak                                 |
| zonesCrossed and timeBand                           | Look up base fare using (zonesCrossed, timeBand) as key                            | baseFare value retrieved                                         |
| passengerType and config discounts                  | Retrieve discount percentage for passenger type from config                        | discountPct value retrieved                                      |
| Completed journey record                            | Assign unique ID, append record to dayJourneys list and update runningTotal        | Journey stored, running total updated and confirmation displayed |
| Journey ID to edit                                  | Validate ID exists in journey list                                                 | Existing journey record retrieved for editing                    |
| Journey ID to delete                                | Validate ID exists; remove record from list                                        | Journey deleted, total recalculated and updated total displayed  |
| CSV file path                                       | Check file exists; open with DictReader; validate required headers                 | File opened and ready to import                                  |
| CSV row (fromZone, toZone, dateTime, passengerType) | Validate all four fields, calculate fare and build journey record                  | Valid row appended to journey list                               |
| dayJourneys list and export file path               | Open CSV file, write header row, write one row per journey and close file          | CSV file saved to disk                                           |
| Admin password                                      | Compare input against stored admin password                                        | Access granted to admin menu or access denied message displayed  |
| Config section choice (1–4)                         | Display current values for the chosen section                                      | Current config values shown to admin                             |
| New fare value (zones, peak flag, fare amount)      | Validate fare is a positive float greater than zero                                | If valid, base fare updated in config and saved to JSON          |
| New discount value (passenger type, percentage)     | Validate percentage is between 0 and 100 inclusive                                 | If valid, discount updated in config and saved to JSON           |
| New daily cap value (passenger type, cap amount)    | Validate cap is a positive float greater than zero                                 | If valid, daily cap updated in config and saved to JSON          |
| New peak window (start time HH:MM, end time HH:MM)  | Validate both times match HH:MM format and start is before end                     | If valid, peak window updated in config and saved to JSON        |
| Any invalid admin input                             | Validation fails and reason identified                                             | Error message displayed                                          |
| User exit choice (Y/N)                              | If Y, serialise current profile and journeys to JSON or if N, discard session data | Session saved to disk or discarded and program exits             |

### Algorithm Design

---

- *Add **images** for the design of your algorithm. Choose either Flowchart or JSP diagrams to demonstrate the functional elements of the algorithm. There should be multiple images for this part as you are decomposing the problem into smaller elements.*
- *Include a class diagram to demonstrate the class structure of the proposed program design.

------------------------------------------------------------------------------------------------------------------------------

### Research (minimum of 1 required, preferrebly 2)

---

*Research existing programs that solve a similar problem. The program does not have to be written in java or object orientated in nature - just solve a similar type of problem.*
*Use the strucutre below to capture your evidence:*
------------------------------------------------------------------------------------------------------------------------------Name of program:
Reference (link):
What it does well (2-3 features that work effectively):
What it does poorly (at least 1 feature):
Key design ideas you could reuse (e.g., layout, navigation, input/output, program structure):
Screenshot (showing the interface/output):

------------------------------------------------------------------------------------------------------------------------------

### Gantt Chart

------------------------------------------------------------------------------------------------------------------------------

*Add Gantt Chart (it maybe easier to create chart in Excel and paste as an image!)*
------------------------------------------------------------------------------------------------------------------------------

### Diary Entries

------------------------------------------------------------------------------------------------------------------------------

*Add diary entries here detailing what you have done, wny you have done it, and any problems encountered.*
------------------------------------------------------------------------------------------------------------------------------
