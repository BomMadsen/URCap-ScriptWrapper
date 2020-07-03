package com.ur.urcap.sample.scriptwrapper.impl;

// Generic Java stuff imported
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Arrays;
import javax.imageio.ImageIO;

// API stuff imported
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Img;
import com.ur.urcap.api.ui.annotation.Select;
import com.ur.urcap.api.ui.component.ImgComponent;
import com.ur.urcap.api.ui.component.SelectDropDownList;
import com.ur.urcap.api.ui.component.SelectEvent;

public class ScriptWrapperProgramNodeContribution implements ProgramNodeContribution {

	private final URCapAPI urCapAPI;
	private final DataModel dataModel;
	
	private static final String SelectScript = "selScript";
	private static final String ProgramsPath = "progPath";
	private static final String SelectImage = "selImage";

	public ScriptWrapperProgramNodeContribution(URCapAPI urCapAPI, DataModel dataModel) {
		this.urCapAPI = urCapAPI;
		this.dataModel = dataModel;
	}
	
	/*****************************
	 * Methods for reading the script
	 *****************************/
	
	/*
	 * This methods reads the specified file at the absolute file path
	 * The file is read line-by-line (for processor load concerns)
	 * The complete content is returned with \n for each line break
	 */
	private String readScriptFile(String filepath){
		try {
			File file = new File(filepath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null){
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}
			fileReader.close();
			
			System.out.println("Read the file: "+filepath);
			System.out.println("The content was:");
			System.out.println(stringBuffer.toString());
			
			return stringBuffer.toString();
		} catch (IOException e){
			e.printStackTrace();
		}
		
		
		return "# No data read";
	}
	
	private boolean doesScriptStillExist(){
		File file = new File(getProgramsFolderPath()+getSelectedScript());
		return file.exists();
	}
	
	/******************************
	 * Methods to select the script
	 ******************************/
	
	/*
	 * This method check if the robot is a simulator or a real robot, 
	 * to find the correct file path for the "programs" folder
	 */
	
	private String getFilePath(){
		String serial = this.urCapAPI.getRobotModel().getSerialNumber();
		System.out.println("Serial number is: "+serial);
		
		if (serial.endsWith("9999")){
			// Then this is most likely a simulator
			return "/home/ur/ursim-current/programs/";
		}
		else{
			// Then this is a real robot
			return "/programs/";
		}
	}
	
	/*
	 * This method finds all script files in the programs folder. 
	 * Argument needed is the path for the directory to search in. 
	 * It returns a File[] (file array) of all available files.
	 */
	
	private File[] findScriptFiles(String directory){
		File dir = new File(directory);
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".script");
		    }
		});
		Arrays.sort(files);
		
		// Just for debugging
		System.out.println("Number of script files found: "+files.length);
		for(int i = 0; i < files.length; i++){
			String filename = files[i].getName();
			System.out.println("File "+i+" is "+filename);
		}
		
		return files;
	}
	
	private String NoSelected = "<select>";
	
	@Select(id = "selScript")
	private SelectDropDownList selScript;

	@Select(id = "selScript")
	public void onScriptChange(SelectEvent event){
		if (event.getEvent() == SelectEvent.EventType.ON_SELECT){
			System.out.println("Selected: "+selScript.getSelectedItem().toString());

			// Save selected script in DataModel
			setProgramsFolderPath(getFilePath());
			setSelectedScript(selScript.getSelectedItem().toString());		
		}
	}
	
	/*
	 * Method to push script files into drop down
	 */
	
	private void setupScriptFileDropdown(File[] files){
		selScript.removeAllItems();
		if(getSelectedScript().equals(NoSelected) || !doesScriptStillExist()){
			// No script selected, populate with the latest found files.
			selScript.addItem(NoSelected);
			for(int i = 0; i < files.length; i++){
				selScript.addItem(files[i].getName());
			}
		}
		else{
			// Buffer selected script, as adding new will fire select event
			String PreSelected = getSelectedScript();
			String PrePath = getProgramsFolderPath();
			System.out.println("Looking for this file: "+PrePath+PreSelected);
			// Script already is chosen and still exists
			int SelectedFileIndex = -1;
			for(int i = 0; i < files.length; i++){
				selScript.addItem(files[i].getName());
				System.out.println("This file is: "+files[i].toString());
				// Check the index if previously selected script
				if((files[i].toString()).equals(PrePath+PreSelected)){
					// File is found
					SelectedFileIndex = i;
				}
			}
			if(SelectedFileIndex != -1){
				System.out.println("Selected file index: "+SelectedFileIndex);
				selScript.selectItemAtIndex(SelectedFileIndex);;
			}
		}
	}
	
	private void setSelectedScript(String name){
		dataModel.set(SelectScript, name);
	}
	private String getSelectedScript(){
		return dataModel.get(SelectScript, NoSelected);
	}
	
	private void setProgramsFolderPath(String path){
		dataModel.set(ProgramsPath, path);
	}
	private String getProgramsFolderPath(){
		return dataModel.get(ProgramsPath, "/programs/");
	}
	
	/******************************
	 * Methods to select image
	 ******************************/
	
	@Select(id = "selImage")
	private SelectDropDownList selImage;

	@Select(id = "selImage")
	public void onImageChange(SelectEvent event){
		if (event.getEvent() == SelectEvent.EventType.ON_SELECT){
			System.out.println("Selected this image: "+selImage.getSelectedItem().toString());

			// Save selected image in DataModel
			setSelectedImage(selImage.getSelectedItem().toString());	
			showImage();
		}
	}
	
	private File[] findImageFiles(String directory){
		File dir = new File(directory);
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return  name.toLowerCase().endsWith(".jpeg")	||
		        		name.toLowerCase().endsWith(".jpg")  	||
		        		name.toLowerCase().endsWith(".png")  	||
		        		name.toLowerCase().endsWith(".bmp")  	;
		    }
		});
		Arrays.sort(files);
		
		// Just for debugging
		System.out.println("Number of image files found: "+files.length);
		for(int i = 0; i < files.length; i++){
			String filename = files[i].getName();
			System.out.println("Image "+i+" is "+filename);
		}
		
		return files;
	}
	
	private boolean doesImageStillExist(){
		File file = new File(getProgramsFolderPath()+getSelectedImage());
		return file.exists();
	}
	
	private void setupImageDropdown(File[] files){
		selImage.removeAllItems();
		if(getSelectedScript().equals(NoSelected) || !doesImageStillExist()){
			// No image selected, populate with the latest found files.
			selImage.addItem(NoSelected);
			for(int i = 0; i < files.length; i++){
				selImage.addItem(files[i].getName());
			}
		}
		else{
			// Buffer selected image, as adding new will fire select event
			String PreSelected = getSelectedImage();
			String PrePath = getProgramsFolderPath();
			// Image already is chosen and still exists
			int SelectedFileIndex = -1;
			for(int i = 0; i < files.length; i++){
				selImage.addItem(files[i].getName());
				// Check the index if previously selected image
				if((files[i].toString()).equals(PrePath+PreSelected)){
					// File is found
					SelectedFileIndex = i;
				}
			}
			if(SelectedFileIndex != -1){
				System.out.println("Selected image index: "+SelectedFileIndex);
				selImage.selectItemAtIndex(SelectedFileIndex);;
			}
		}
	}
	
	private void setSelectedImage(String name){
		dataModel.set(SelectImage, name);
	}
	private String getSelectedImage(){
		return dataModel.get(SelectImage, NoSelected);
	}

	/***************************
	 *  METHOD TO SHOW IMAGE
	 ***************************/
	
	@Img(id="illustrationImg")
	private ImgComponent illustrationImg;
	
	private void showImage(){
		try{
			if(getSelectedImage().equals(NoSelected) || !doesImageStillExist()){
				// No image chosen or it is gone, load a generic 
				illustrationImg.setImage(ImageIO.read(getClass().getResource("/com/ur/urcap/sample/scriptwrapper/impl/noimg.png")));
			}
			else{
				// Load the image chosen
				BufferedImage image = ImageIO.read(new File(getProgramsFolderPath()+getSelectedImage()));
				// As the image might be crazy shape, we change the dimensions
				int org_width = image.getWidth();
				int org_height = image.getHeight();
				System.out.println("Image: Original width "+org_width+" height "+org_height);
				int new_height = 250; // pixels, which is the available height left
				int new_width = Math.round((float)org_width/org_height*new_height);
				System.out.println("Image: New width "+new_width+" height "+new_height);
				// Create new image element
				BufferedImage resized = new BufferedImage(new_width, new_height, 2);
				Graphics2D g = resized.createGraphics();
				g.drawImage(image,0,0,new_width,new_height,null);
				g.dispose();
				// Now we show the new image
				illustrationImg.setImage(resized);
			}
		}catch (java.io.IOException e){
			e.printStackTrace();
		}
	}
	
	/************************
	 * OVERWRITTEN METHODS
	 ************************/

	@Override
	public void openView() {
		setupScriptFileDropdown(findScriptFiles(getFilePath()));
		setupImageDropdown(findImageFiles(getFilePath()));
		showImage();
	}

	@Override
	public void closeView() {
	}

	@Override
	public String getTitle() {
		if(isDefined()){
			return "Wrapped: "+getSelectedScript();
		}
		else{
			return "Wrapped Script";
		}
	}

	@Override
	public boolean isDefined() {
		return doesScriptStillExist();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		if(isDefined()){
			// Use appendRaw to simply paste the contents of the script file
			writer.appendRaw(readScriptFile(getProgramsFolderPath()+getSelectedScript()));
		}
	}

}
