package ozeret;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.ini4j.Ini;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class InitialPane extends GridPane {

	private Stage stage;
	private Scene nextScene, myScene;
	private BufferedReader infoReader;
	private String infoText;
	private Ini settings;
	
	public InitialPane(Stage s, Scene ns, Scene ms, Ini set) {
		super();
		stage = s;
		nextScene = ns;
		myScene = ms;
		settings = set;
		
		infoText = "";
		getFileContents();
		
		setup();
		
		myScene.setRoot(this); // set this pane on its scene
	}
	
	// reads information from file to display if the info button is clicked
	private void getFileContents() {
		
		// get location of the info file for this pane
		String infoPath = settings.get("initialPaneSettings", "infoPath", String.class);
		String infoFileParent = infoPath.substring(0, infoPath.lastIndexOf("/")); // get the parent name
		String infoFileName = infoPath.split("/")[infoPath.split("/").length - 1]; // get the file name
		
		// set up reader to read from file
		try {
			infoReader = new BufferedReader(new FileReader(infoPath));
		} catch (FileNotFoundException e) {		
			Alert fileNotAccessible = new Alert(AlertType.ERROR, "Unable to access \""+ infoFileName+ "\" file.\n"
					+ "Please create this file in the " + infoFileParent + " directory.");
			fileNotAccessible.setTitle("Info File Not Accessible");
			fileNotAccessible.getDialogPane().getStylesheets().add(OzeretMain.class.getResource(settings.get("stageSettings", "cssFile", String.class)).toExternalForm());
			fileNotAccessible.showAndWait();
			
			Platform.exit();
		}
		
		
		// read from file into infoText
		try {
			String temp = "";
			
			while((temp = infoReader.readLine()) != null) {
				infoText += temp + "\n";
			}
			
		} catch (IOException e) {
			infoText = "Something went wrong while reading this text.\nCheck the \""+ infoFileName+ "\" file to see if there are errors in it.";
		}
	}
	
	// sets up layout and functionality of InitalPane
	private void setup() {
		
		// set up grid layout and sizing
		this.setAlignment(Pos.CENTER);
		this.setHgap(15);
		this.setVgap(20);
		this.setPadding(new Insets(30));
		
		// header
		Label title = new Label("Sign-in Setup");
		title.setId("header");
		
		this.add(title, 0, 0, 3, 1);
		
		// location for person on ozeret to input their name
		Label ozName = new Label("Ozeret Name:");
		this.add(ozName, 0, 1);
		
		TextField ozNameEntry = new TextField();
		this.add(ozNameEntry, 1, 1, 2, 1);
		
		
		// location for person on ozeret to input time of curfew
		Label curfewTime = new Label("Curfew (HH:MM):");
		this.add(curfewTime, 0, 2);
		
		TextField curfewEntry = new TextField();
		this.add(curfewEntry, 1, 2);
		
		// create and add am/pm selector buttons
		ToggleGroup timeGroup = new ToggleGroup();
		RadioButton am = new RadioButton("AM");
		RadioButton pm = new RadioButton("PM");
		am.getStyleClass().add("radiobutton");
		pm.getStyleClass().add("radiobutton");
		am.setToggleGroup(timeGroup);
		pm.setToggleGroup(timeGroup);
		am.setSelected(true);
		
		VBox timeBox = new VBox(this.getVgap());
		timeBox.getChildren().addAll(am, pm);
		this.add(timeBox, 2, 2);
		
		// add continue/exit buttons
		Button exit = new Button("Exit");
		exit.setCancelButton(true); // exit button is triggered on ESC keypress
		
		Button advance = new Button("Choose File");
		advance.setDefaultButton(true); // advance button is triggered on ENTER keypress
		
		// add information button (will pop up credits and instructions)
		Button info = new Button("i");
		info.getStyleClass().add("info");
		
		// make buttons grow to fit entire width of row
		HBox statusBox = new HBox(this.getHgap());
		HBox.setHgrow(exit, Priority.ALWAYS);
		HBox.setHgrow(advance, Priority.ALWAYS);
		exit.setMaxWidth(Double.MAX_VALUE);
		advance.setMaxWidth(Double.MAX_VALUE);
		statusBox.getChildren().addAll(exit, advance, info);
		this.add(statusBox, 0, 3, 3, 1);
		
		// set info button behavior (show credits, brief explanation of what to do)
		info.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				Alert infoDialog = new Alert(AlertType.NONE, infoText, ButtonType.CLOSE);
				infoDialog.setTitle("Credits and Instructions � Set-up");
				infoDialog.getDialogPane().getStylesheets().add(getClass().getResource(settings.get("stageSettings", "cssFile", String.class)).toExternalForm());
				infoDialog.initOwner(info.getScene().getWindow());
				infoDialog.initModality(Modality.NONE);
				infoDialog.setResizable(true);
				infoDialog.getDialogPane().setPrefWidth(stage.getWidth());
				infoDialog.show();
			}
			
		});
			
		// if the exit ('X') button of the window is pressed, act as if the in-window "Exit" button was pressed
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				event.consume();
				exit.fire();
			}
		});
		
		// set exit button behavior
		exit.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				// pop up exit confirmation alert
				Alert exitConfirmation = new Alert(AlertType.CONFIRMATION, "Are you sure you want to exit?\nData entered will be lost.");
				exitConfirmation.setTitle("Exit Confirmation");
				exitConfirmation.getDialogPane().getStylesheets().add(getClass().getResource(settings.get("stageSettings", "cssFile", String.class)).toExternalForm());
				exitConfirmation.initOwner(exit.getScene().getWindow());
				exitConfirmation.showAndWait();
				
				// exit if user confirms exit
				if (exitConfirmation.getResult() == ButtonType.OK)
					Platform.exit();
			}
		});
		
		// set advance button behavior
		advance.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				// check to make sure that both fields are properly filled out
				//  (name field is not empty, curfew field matches an hh:mm time regex)
				if(!ozNameEntry.getText().equals("") && curfewEntry.getText().matches("^(0?[1-9]|1[0-2]):[0-5][0-9]")) {
					
					// open FileChooser for person on ozeret to select which file has the attendance information
					FileChooser fileChooser = new FileChooser();
					fileChooser.setInitialDirectory(new File("."));
					fileChooser.setTitle("Select Attendance File");
					fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel Files", "*.xlsx"));
					File attendanceFile = fileChooser.showOpenDialog(stage);
					
					if (attendanceFile != null) {
						
						// update next scene's SignInPane to have correct curfew, ozeret name, and attendance file
						((SignInPane) nextScene.getRoot()).setPrevVars(ozNameEntry.getText(), curfewTime(), attendanceFile, myScene);
						
						// clear text in fields, in case user returns to this scene
						ozNameEntry.clear();
						curfewEntry.clear();
						
						// change the scene to the next one (will be a scene with SignInPane in it)
						stage.setScene(nextScene);
						stage.centerOnScreen();
					}
					
				} else {
				
					// if fields aren't properly filled out, pop up an alert saying so, and don't advance
					Alert notDone = new Alert(AlertType.WARNING, "You must fill out both fields properly to proceed");
					notDone.setHeaderText("Improper Ozeret Name and/or Curfew Time");
					notDone.getDialogPane().getStylesheets().add(getClass().getResource(settings.get("stageSettings", "cssFile", String.class)).toExternalForm());
					notDone.showAndWait();
				}
			}
			
			// takes the string entered as curfew time and converts it to the date and time of curfew
			private LocalDateTime curfewTime() {
				
				int hour, minute;
				hour = Integer.parseInt(curfewEntry.getText().split(":")[0]);
				minute = Integer.parseInt(curfewEntry.getText().split(":")[1]);
				
				// to convert to 24-hr time properly, change 12 to 0
				if (hour == 12)
					hour = 0;
				
				// to convert to 24-hr time, add 12 to the hour if time is PM.
				if (pm.isSelected())
					hour += 12;
				
				LocalTime curfew = LocalTime.of(hour, minute);
				
				// if curfew is after the current time, curfew is today
				if (curfew.isAfter(LocalTime.now()))
					return LocalDateTime.of(LocalDate.now(), curfew); // return LocalDateTime object with today's date and entered time
				else // otherwise, curfew is tomorrow (read: after midnight)
					return LocalDateTime.of(LocalDate.now().plusDays(1), curfew); // return LocalDateTime object with tomorrow's date and entered time
			}
		});
	}
	
}
