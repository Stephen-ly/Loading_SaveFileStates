package ca.utoronto.utm.paint;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Parse a file in Version 1.0 PaintSaveFile format. An instance of this class
 * understands the paint save file format, storing information about
 * its effort to parse a file. After a successful parse, an instance
 * will have an ArrayList of PaintCommand suitable for rendering.
 * If there is an error in the parse, the instance stores information
 * about the error. For more on the format of Version 1.0 of the paint 
 * save file format, see the associated documentation.
 * 
 * @author 
 *
 */
public class PaintSaveFileParser {
	private int lineNumber = 0; // the current line being parsed
	private String errorMessage =""; // error encountered during parse
	private ArrayList<PaintCommand> commands; // created as a result of the parse
	private String shape = null;
	private static final String CIRCLE = "Circle";
	private static final String RECTANGLE = "Rectangle";
	private static final String SQUIGGLE = "Squiggle";
	/**
	 * Below are Patterns used in parsing 
	 */
	private Pattern pFileStart=Pattern.compile("^PaintSaveFileVersion1.0$");
	private Pattern pFileEnd=Pattern.compile("^EndPaintSaveFile$");

	//Start and End Shape patterns
	private Pattern pCircleStart=Pattern.compile("^Circle$");
	private Pattern pCircleEnd=Pattern.compile("^EndCircle$");

	private Pattern pRectangleStart= Pattern.compile("^Rectangle$");
	private Pattern pRectangleEnd= Pattern.compile("^EndRectangle$");

	private Pattern pSquiggleStart=Pattern.compile("^Squiggle$");
	private Pattern pSquiggleEnd=Pattern.compile("^EndSquiggle$");

	//The regular expression to match each "0-255" value in the string.
	private Pattern pColor= Pattern.compile("^color:([0-9]{1,2}|1[0-9]{1,2}|2[0-4][0-9]|25[0-5]),"
			+ "([0-9]{1,2}|1[0-9]{1,2}|2[0-4][0-9]|25[0-5]),"
			+ "([0-9]{1,2}|1[0-9]{1,2}|2[0-4][0-9]|25[0-5])$");

	private Pattern pCenter= Pattern.compile("^center:\\(\\d+,\\d+\\)$");
	private Pattern pRadius= Pattern.compile("^radius:\\d+$");
	
	private Pattern pPointStart= Pattern.compile("^points$");
	private Pattern pPoint= Pattern.compile("^point:\\(\\d+,\\d+\\)$");
	private Pattern pPointEnd= Pattern.compile("^endpoints$");
	/*//Unfortunately this is needed as the order of RECTANGLE p1, and RECTANGLE p2 may be reversed, which is something 
	 * that needs to be detected. Although they are very similar, they must use different patterns
	 * because of this.
	 */
	private Pattern pRectanglePoint1= Pattern.compile("^p1:\\(\\d+,\\d+\\)$");
	private Pattern pRectanglePoint2= Pattern.compile("^p2:\\(\\d+,\\d+\\)$");

	private Pattern pIsFilled = Pattern.compile("^filled:(true|false)$");

	// ADD MORE!!

	/**
	 * Store an appropriate error message in this, including 
	 * lineNumber where the error occurred.
	 * @param mesg
	 */
	private void error(String mesg){
		this.errorMessage = "Error in line "+lineNumber+" "+mesg;
	}
	/**
	 * 
	 * @return the PaintCommands resulting from the parse
	 */
	public ArrayList<PaintCommand> getCommands(){
		return this.commands;
	}
	/**
	 * 
	 * @return the error message resulting from an unsuccessful parse
	 */
	public String getErrorMessage(){
		return this.errorMessage;
	}

	/**
	 * Parse the inputStream as a Paint Save File Format file.
	 * The result of the parse is stored as an ArrayList of Paint command.
	 * If the parse was not successful, this.errorMessage is appropriately
	 * set, with a useful error message.
	 * 
	 * @param inputStream the open file to parse
	 * @return whether the complete file was successfully parsed
	 */
	public boolean parse(BufferedReader inputStream) {
		this.commands = new ArrayList<PaintCommand>();
		this.errorMessage="";

		// During the parse, we will be building one of the 
		// following shapes. As we parse the file, we modify 
		// the appropriate shape.

		Circle circle = null; 
		Rectangle rectangle = null;
		Squiggle squiggle = null;

		try {	
			int state=0; Matcher m; String l;

			this.lineNumber=0;
			while ((l = inputStream.readLine()) != null) {
				l = l.replaceAll("\\s","");
				this.lineNumber++;
				//Changed this line so that the line number and the state is less cryptic and easier to work with
				System.out.println("The line number is: " + lineNumber +". The string being read is: " + l +". The state is : " + state);
				switch(state){
				case 0:
					m=pFileStart.matcher(l);
					if(m.matches()){
						state=1;
						break;
					}
					else{
						//error("") changed into println for development purposes.
						error("Expected Start of Paint Save File");
						return false;
					}
				case 1: // Looking for the start of a new object or end of the save file
					m=pCircleStart.matcher(l);
					if(m.matches()){
						state=2; 
						circle = new Circle();
						this.shape = CIRCLE;
						break;
					}else{
						m=pRectangleStart.matcher(l);
						if (m.matches()){
							state=2;
							rectangle = new Rectangle();
							this.shape = RECTANGLE;
							break;
						}else{
							m=pSquiggleStart.matcher(l);
							if (m.matches()){
								state=2;
								squiggle = new Squiggle();
								this.shape = SQUIGGLE;
								break;

							}else{
								m=pFileEnd.matcher(l);
								if (m.matches()){
									break;
								}

								else{
									error("Expected Shape Start or End of file");
									return false;
								}
							}
						}
					}
				case 2:
					m=pColor.matcher(l);			
					if(m.matches()){
						String[] removingColorWord = l.split(":");
						String[] splitIntoValues = removingColorWord[1].split(",");
						state=3;

						if (this.shape == CIRCLE){
							circle.setColor(new Color(Integer.parseInt(splitIntoValues[0]),Integer.parseInt(splitIntoValues[1]),
									Integer.parseInt(splitIntoValues[2])));	
							break;
						}else if(this.shape == RECTANGLE){
							rectangle.setColor(new Color(Integer.parseInt(splitIntoValues[0]),Integer.parseInt(splitIntoValues[1]),
									Integer.parseInt(splitIntoValues[2])));
							break;
						}else if(this.shape == SQUIGGLE){
							squiggle.setColor(new Color(Integer.parseInt(splitIntoValues[0]),Integer.parseInt(splitIntoValues[1]),
									Integer.parseInt(splitIntoValues[2])));
							break;
						}
					}else{
						error("The Color String is expected");
						return false;
					}

				case 3:
					m = pIsFilled.matcher(l);
					if(m.matches()){
						String[] removingFilledWord = l.split(":");
						state=4;
						if (this.shape == CIRCLE){
							circle.setFill(Boolean.valueOf(removingFilledWord[1]));
							break;
						}else if (this.shape == RECTANGLE){
							rectangle.setFill(Boolean.valueOf(removingFilledWord[1]));
							break;
						}else if (this.shape == SQUIGGLE){
							squiggle.setFill(Boolean.valueOf(removingFilledWord[1]));
							break;
						}
					}else{
						error("The is fill string is expected");
						return false;
					}

				case 4:
					if (this.shape == CIRCLE){
						m = pCenter.matcher(l);
						if(m.matches()){
							String[] removingCenterWord = l.split(":");
							String removeBrackets = removingCenterWord[1].replaceAll("[\\()]","");
							String [] removeComma = removeBrackets.split(",");
							state=5;
							circle.setCentre(new Point(Integer.parseInt(removeComma[0]),Integer.parseInt(removeComma[1])));
							break;
						}
						else{
							error("The centre string is expected");
							return false;
						}

					} else if (this.shape == RECTANGLE){
						m = pRectanglePoint1.matcher(l);
						if(m.matches()){
							String[] removingP1 = l.split(":");
							String removeBrackets = removingP1[1].replaceAll("[\\()]","");
							String [] removeComma = removeBrackets.split(",");
							rectangle.setP1(new Point(Integer.parseInt(removeComma[0]),Integer.parseInt(removeComma[1])));
							state=5;
							break;
						}
						else{
							error("The point1 string is expected");
							return false;
						}

					}else if (this.shape == SQUIGGLE){
						m = pPointStart.matcher(l);
						if(m.matches()){
							state=5;
							break;
						}
						else{
							error("The point start string is expected");
							return false;
						}
					}
					case 5:
						if (this.shape == CIRCLE){
							m = pRadius.matcher(l);
							if(m.matches()){
								String[] removingRadiusWord = l.split(":");
								circle.setRadius(Integer.parseInt(removingRadiusWord[1]));
								state=6;
								break;
							}
							else{
								error("The radius string is expected");
								return false;
							}
						} else if(this.shape == RECTANGLE){ //remember to fix this cause you might have errors with the p1/p2 order
							m = pRectanglePoint2.matcher(l);
							if(m.matches()){
								String[] removingP2 = l.split(":");
								String removeBrackets = removingP2[1].replaceAll("[\\()]","");
								String [] removeComma = removeBrackets.split(",");
								rectangle.setP2(new Point(Integer.parseInt(removeComma[0]),Integer.parseInt(removeComma[1])));
								state=6;
								break;
							}
							else{
								error("The point2 string is expected");
								return false;
							}
							
						} else if(this.shape == SQUIGGLE){
							m = pPoint.matcher(l);
							if(m.matches()){
								String[] removingPoint = l.split(":");
								String removeBrackets = removingPoint[1].replaceAll("[\\()]","");
								String [] removeComma = removeBrackets.split(",");
								squiggle.add(new Point(Integer.parseInt(removeComma[0]),Integer.parseInt(removeComma[1])));
								state=5;
								break;
							}else{
								m = pPointEnd.matcher(l);
								if(m.matches()){
									state=6;
									break;
								}
								else{
									error("The point string is expected");
									return false;
								}
							}
						}
					case 6:
						if (this.shape == CIRCLE){
							m = pCircleEnd.matcher(l);
							if(m.matches()){
								state=1;
								commands.add(new CircleCommand(circle));
								this.shape = null;
								circle = null;// this may cause some issues because of pointers with the circle command but I'm not sure at this point
								break;
							}
							else{
								//error("") changed into println for development purposes.
								error("Expected Circle End");
								return false;
							}
						}
						else if (this.shape == RECTANGLE){
							m = pRectangleEnd.matcher(l);
							if(m.matches()){
								state=1;
								commands.add(new RectangleCommand(rectangle));
								this.shape = null;
								rectangle = null;// this may cause some issues because of pointers with the circle command but I'm not sure at this point
								break;
							}
							else{
								//error("") changed into println for development purposes.
								error("Expected Rectangle End");;
								return false;
							}

						}
						else if (this.shape == SQUIGGLE){
							m =  pSquiggleEnd.matcher(l);
							if(m.matches()){
								state=1;
								commands.add(new SquiggleCommand(squiggle));
								this.shape = null;
								squiggle = null;
								break;
							}

							else{
								error("The Squiggle end is expected");
								return false;
							}
						}
					}

				}
			}  catch (Exception e){

			} 
			return true;
		}
	}
