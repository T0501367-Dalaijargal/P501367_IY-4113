# IY4113 Milestone 1 Part 2

| Assessment Details | Please Complete All Details                                      |
| ------------------ | ---------------------------------------------------------------- |
| Group              | B                                                                |
| Module Title       | Applied Software Engineering using Object-Orientated Programming |
| Assessment Type    | Part 2: Milestone 1                                              |
| Module Tutor Name  | Jonathan Shore                                                   |
| Student ID Number  | P501367                                                          |
| Date of Submission | 22/03/2026                                                       |
| Word Count         | 2410                                                             |
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

![Flowchart 1](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Part%202.jpg)

![Flowchart 2](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Part%202%20(1).jpg)

![Flowchart 3](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Part%202%20(2).jpg)

![Flowchart 4](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Part%202%20(3).jpg)

![Flowchart 5](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Part%202%20(4).jpg)

![Class Diagram]([https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Part%202%20(5).jpg](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Screenshot%202026-03-22%20235823.png))

------------------------------------------------------------------------------------------------------------------------------

### Research (minimum of 1 required, preferrebly 2)

---

Name of program: Oyster Calculator

Reference (link):  https://www.tubecalculator.co.uk/

What it does well (2-3 features that work effectively):

Multi-journey daily cap calculation: The user enters every journey they made that day. For each journey, they must select the time of departure. Peak times are between 06:30 and 09:30, as well as between 16:00 and 19:00 during weekdays. Off-peak times are all times outside of these windows. Based on the zones that the user travels through during the day, the system displays the daily cap for that individual. 

Passenger and card type selection: The users can select from a list of different passenger and card types.  Each of these types has a discount associated with it. For each selection that the user chooses, the application automatically adjusts the discount that will be applied to the overall fare for the individual. This feature is directly equivalent to the passenger type and discount system within CityRide Lite.

What it does poorly (at least 1 feature):
OysterCalculator is entirely session-based and does not offer any means of saving journeys between sessions. Any data that is entered is lost if the browser is refreshed. While this may be acceptable for individuals who wish to track their spending for just one day, it is a limitation compared to CityRide Lite which offers JSON profile persistence, CSV export, and text reports that can be saved to disk.

Key design ideas you could reuse (e.g., layout, navigation, input/output, program structure):

Each of the components of the fare should be displayed separately. CityRide Lite will display each of these components for each journey, as well as at the end-of-day summary.

Rather than making the user aware of the time bands, those time bands should be displayed within the prompt itself. CityRide Lite will display the peak time bands within the journey prompt itself.

The type of passenger selected by the user will be set once for each user, and will apply to all of their journeys. CityRide Lite will display the passenger type once for each user within their profile, and will not display it within each journey prompt.

Screenshot (showing the interface/output):

![Screenshot 1](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Oyster1.JPG)

![Screenshot 2](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Oyster2.JPG)

---

Name of program: City Mapper

Reference (link): [https://citymapper.com/](https://citymapper.com/)

What it does well (2-3 features that work effectively):

Real-time fare display across multiple journey options: When journeying with Citymapper, the app displays the estimated fare for each journey option. These fares can be sorted by cost to display the journey with the most competitive fare. The fares also account for the user's card type and any available discounts on the passenger's card – just as CityRide Lite applies discounts according to the type of passenger using the service.

Saved profile and persistent preferences: With Citymapper, users can save their preferences (such as saved locations and journey modes) which will be remembered when they use the app again. This is the mobile equivalent of CityRide Lite's RiderProfile which stores the rider's information in JSON format and retrieves the data upon each app launch.

Clear, step-by-step journey output: For each journey planned by the user, Citymapper displays a step-by-step journey. Each journey will feature each leg of the journey with the distance to travel, the mode of transportation, and the journey cost. 

What it does poorly (at least 1 feature):

While Citymapper does illustrate the fare for each journey displayed, it fails to provide a daily spending total, a display of the daily cap that can be enforced, or a spending summary for that day’s journeys. There is no way for users to see how close they are to their daily cap or how much they have saved through the implementation of that cap. Additionally, there is no way to export journey history in any way.

Key design ideas you could reuse (e.g., layout, navigation, input/output, program structure):

Although the current design elements for CityRide Lite do not include a means of displaying the cost comparison between what the user paid versus what they would have paid without the cap on fares, it is still conceivable that such a comparison could be displayed in a similar manner to that in which the current fares are displayed.

The way in which Citymapper displays each journey element within its journey record could be used as a model for the layout of each journey record that would be printed out by CityRide Lite. Each element of the journey record could be displayed in the same manner as the journey elements that currently exist for CityRide Lite.

Citymapper also saves the user’s default home and work locations within the app; it is conceivable, therefore, that CityRide Lite could employ a similar means of saving the user’s default passenger type and payment method within the user profile, requiring the user to re-enter these elements for each journey that is taken will be eliminated with such functionality.

Screenshot (showing the interface/output):

![Screenshot 3](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Screenshot%202026-03-23%20001927.png)

------------------------------------------------------------------------------------------------------------------------------

### Gantt Chart

------------------------------------------------------------------------------------------------------------------------------

![GANTT Chart](https://github.com/T0501367-Dalaijargal/P501367_IY-4113/blob/main/Part%202/Part%202%20(5).jpg)
------------------------------------------------------------------------------------------------------------------------------

### Diary Entries

------------------------------------------------------------------------------------------------------------------------------

Diary Entry 1 - 17/03/2026
------------------------------------------------------------------------------------------------------------------------------

After drafting the purpose of the program and a list of the program’s functionalities, I have begun to consider the constraints of the program (file formats, menu interactions, etc.). I have also reviewed some similar systems in the field to consider potential challenges with my own systems and to ensure their robustness. Although error handling was largely considered in the future of the program, such a determination from others in the field helps to validate such a consideration.

Completing the IPO tables for each function forces the developer to think critically about the needs of the function and how the system will interact with the function. Such a consideration directly informs the pseudocode and Python functions to be written for the system. Additionally, creating each of these tables helps to reconsider the steps to be performed by the function to improve its overall efficiency.

For determining the boundary between the ‘inputs’ and ‘processes’ for calculating the fare for a passenger, I determined that the daily running total should be an implicit ‘input’ from the journey list rather than an ‘input’ from the user. This decision helps to both create a more streamlined interaction between the user and the system, as well as to reduce the potential for the user to introduce errors into the system. Such a determination will help future maintainers of the system to understand the reasoning behind the structure and interaction of the program.

---

## Diary Entry 3 - 22/03/2026

Following the planning phase was the completion of the 10 IPO tables to illustrate each of the major functions of the software and the creation of the Gantt chart that illustrated the major tasks of the project from start to finish (including tasks that may occur in parallel). This Gantt chart would allow for an understanding of the workload for each phase of the project, as well as allow for an estimation of when tasks may be completed and when buffers may be applied to allow for potential delays in those tasks. This Gantt chart will aid in the mitigation of any risks that may exist for the project.

The Gantt chart will provide a realistic schedule for the completion of the project.  Because of the planning that was performed during the specification phase, it is evident that implementation should start as early as Week 4 of the project.  Such an understanding of the project schedule helps to indicate the impact that some of the design decisions will have upon other areas of the project.

Though I have no experience with developing systems of this complexity, I have created an estimation of the time that will be required to complete each task.  More time will be allotted for implementation than design for the consideration of the number of functions that will be implemented into the system, though testing will be performed at 10% of the total time for the project.  Furthermore, estimates for each task will be compared with the actual time required to complete each task in future projects in order to improve my time planning skills.  Thus, I have gained confidence with my abilities to complete future software development projects of any size.
