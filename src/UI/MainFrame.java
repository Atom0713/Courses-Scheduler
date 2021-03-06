package UI;

import IO.OutputEXCEL;
import root.*;
import root.Class;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MainFrame {
    public static String path=null;//will hold path to the EXCEL input file
    public  static void mainFrame(){
        //UI..start
        //create Jframe object
        JFrame frame = new JFrame("Set up");
        //create labels and buttons objects
        JLabel chooseFileLabel,loadingLable;
        JButton runGA, run1,run2, chooseFileButton;
        //set sizes and initialize labels and buttons objects
        chooseFileButton = new JButton("Open");
        chooseFileLabel = new JLabel("Choose File");
        chooseFileButton.setBounds(80,10,80,30);
        chooseFileLabel.setBounds(10, 10, 90,30);

        chooseFileButton.addActionListener( (event) -> {//event listener implementation using lambda expression

                JFileChooser chooseFile = new JFileChooser();
                //invoke the showSaveDialog function to show the save dialog
                int r =  chooseFile.showSaveDialog(null);

                //if the user selects a file
                if(r == JFileChooser.APPROVE_OPTION)
                {
                    //set the path to the path of the selected file
                    path = chooseFile.getSelectedFile().getAbsolutePath();
                }
        });


        runGA = new JButton("Run using Genetic Algorithm");
        run1 = new JButton("...");
        run2 = new JButton("...");
        loadingLable = new JLabel("file was not choosen ");
        runGA.setBounds(100,200,200,30);
        run1.setBounds(100,250,200,30);
        run2.setBounds(100,300,200,30);
        runGA.addActionListener( (event) -> {
                try{
                    //main processes
                    //System.out.println("Initialization of root.Timetable started...");
                    Timetable timetable = MainGA.initializeTimetable(path);
                    //System.out.println("Initialization of root.Timetable Finished\n");

                    //System.out.println("Initialization of Genetic Algorithm started...");
                    GA ga = new GA(200, 0.02, 0.9, 2, 5);
                    //System.out.println("Initialization of Genetic Algorithm finished...\n");

                    //System.out.println("Initialization of root.Population started...");
                    Population population = ga.initialize_population(timetable);
                    //System.out.println("Initialization of root.Population finished.\n");

                    //System.out.println("First population is being evaluated...");
                    ga.evaluate_population(population, timetable);
                    //System.out.println("First population is evaluated.\n");
                    // Keep track of current generation
                    int generation = 1;
                    // Start evolution loop
                    int nonProgressingGenerationsCount = 0;
                    double previousFitness = population.get_fittest(0).get_fitness();
                    while (!ga.is_termination_condition_met(generation, 50000) && !ga.is_termination_condition_met(population,nonProgressingGenerationsCount)) {
                        if(population.get_fittest(0).get_fitness()>previousFitness){
                            previousFitness=population.get_fittest(0).get_fitness();
                            nonProgressingGenerationsCount=0;
                        }
                        //System.out.println("Generation number: "+ generation);
                        // Print fitness
                        //System.out.println("G" + generation + " Best fitness: " + population.get_fittest(0).get_fitness());

                        // Apply crossover
                        population = ga.crossover(population);

                        // Apply mutation
                        population = ga.apply_mutation(population, timetable);

                        // Evaluate population
                        ga.evaluate_population(population, timetable);
                        // Increment the current generation
                        if(previousFitness>=1.0) {
                            nonProgressingGenerationsCount++;
                        }
                        generation++;
                    }
                    // Print fitness
                    timetable.createClasses(population.get_fittest(0));

                    Class[] classes = timetable.getClasses();
                    int classIndex = 1;
                    for (Class bestClass : classes) {
                        Course course = timetable.getCourse(bestClass.getCourseId());
                        ClassRoom classRoom = timetable.getClassRoom(bestClass.getClassRoomId());
                        Professor professor = timetable.getProfessor(bestClass.getProfessorId());
                        TimePeriod timePeriod = timetable.getTimePeriod(bestClass.getTimePeriodId());

                        //Filling timetables of Professors and ClassRooms
                        int[][] timePeriodIndexes = timePeriod.getTimePeriodAsIndexes();
                        for (int[] timePeriodIndex : timePeriodIndexes) {
                            professor.addToProfessorsTimetable(timePeriodIndex[0], timePeriodIndex[1], classRoom.getClassRoomName(), course.getCourseCode());
                            classRoom.addToClassRoomsTimetable(timePeriodIndex[0], timePeriodIndex[1], course.getCourseCode());
                        }
                        classIndex++;
                    }


                    /*
                     * @allProfTimetables содержит в себе расписания каждого профессора
                     * @allProfNames содержит имена всех преподов в том же порядке что и @allProfTimetables
                     */

                    ArrayList<String[][]> allProfTimetables = new ArrayList<>();
                    ArrayList<String> allProfNames = new ArrayList<>();
                    Professor[] professors = timetable.getProfessorsAsArray();
                    for (Professor professor : professors) {
                        allProfTimetables.add(professor.getTimetable());
                        allProfNames.add(professor.getProfessorName());
                    }


                    /** allClassRoomsTimetables содержит в себе расписания каждого кабинета
                     * @allClassRoomsNames содержит названия всех кабинетов в том же порядке что и @allProfTimetables
                     */

                    ArrayList<String[][]> allClassRoomsTimetables = new ArrayList<>();
                    ArrayList<String> allClassRoomsNames = new ArrayList<>();
                    ClassRoom[] classRooms = timetable.getClassRoomsAsArray();
                    for (ClassRoom classRoom : classRooms) {
                        allClassRoomsTimetables.add(classRoom.getTimetable());
                        allClassRoomsNames.add(classRoom.getClassRoomName());
                    }


                    DataManipulations dm = new DataManipulations(timetable);

                    //OutputPDF.writeAllToPdf(dm.getClassesAsCodeTimeRoom());

                    //OutputFrame constructor gets all the timetable and other information necessary as arguments to produce output frame.
                    OutputFrame outputFrame = new OutputFrame(generation, population.get_fittest(0).get_fitness(), timetable.calculate_clashes(), allProfNames,allProfTimetables, allClassRoomsNames,allClassRoomsTimetables,dm);
                    OutputEXCEL outputExcel = new OutputEXCEL(timetable);
                    //main output frame
                    OutputFrame.outputFrame();
                }catch (Exception ex){
                    loadingLable.setBounds(150,50,250,30);
                    //System.out.println(ex);
                }
        });
        //adding labels, and buttons to the frame. Setting its size and layout and default close operation.
        frame.add(loadingLable);
        frame.add(chooseFileLabel);
        frame.add(chooseFileButton);
        frame.add(runGA);
        frame.add(run1);
        frame.add(run2);
        frame.setSize(400,400);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //UI...end
    }
}
