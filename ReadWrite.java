import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import org.json.simple.JSONArray;

public class ReadWrite {
	// Initialize the Arraylist at first
	// Below is a self-call function
	static ArrayList<File> files = new ArrayList<>();
 
	public static ArrayList<File> read(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			// Delete the hidden .DS_Store file in Mac.
			if (file.getName().equals(".DS_Store")) {
				System.out.println(".DS_Store file detected in " + file.getAbsolutePath() + "\n\n");
				file.delete();
			}
			if (file.isFile() && (file.length() != 0)) {
				files.add(file);
			} 
			else if (file.isDirectory()) {
				read(file.getAbsolutePath());
			}
		}
		return files;
	}
	
	@SuppressWarnings("unchecked")
	public static void write(Map<String, ArrayList<String>> info) throws IOException { 		
		String path ="/Users/fangcaoxu/Desktop/SodaFinal/output.json";
		File output = new File(path);
		FileWriter writer = new FileWriter(output,true);
		JSONArray list = new JSONArray();
		list.add(info);			
		try{
			JSONArray.writeJSONString(list,writer);
		}
		catch (Exception e){
			writer.close();
		}
		finally{
			writer.close();
			}
		} 
	
	//Move finished files to the processed folder
	public static void move(File file, String path) {
		// TODO Auto-generated method stub
		File copy = new File(path + "/" + file.getName());
		try {
			Files.move(file.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
}
