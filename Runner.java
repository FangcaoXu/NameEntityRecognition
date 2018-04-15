import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Runner {

	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		ArrayList<File> files = new ArrayList<>();
		files = ReadWrite.read("/Users/fangcaoxu/Desktop/SodaFinal/GOP_press_releases");
		//files = ReadWrite.read("/Users/fangcaoxu/Desktop/Tutorial/SpecialCase");
		System.out.println("There are total " + files.size() + " presses collected");
		for (File file : files) {
			// Filter news with dates
			System.out.println(file.getName());
			String[] segments = Trim.docSegments(file);
			if (segments[0] != "NoDateDetected") {
				//System.out.println(file.getName());
				// Name Entity Recognition
				Map<String, ArrayList<String>> info = NameEntityRecog.parse(file);
				info.put("ID", new ArrayList<String>(Arrays.asList(file.getName())));
				info.put("DATE", new ArrayList<String>(Arrays.asList(segments[0])));
				info.put("TITLE", new ArrayList<String>(Arrays.asList(segments[1])));
				//info.put("CONTENT", new ArrayList<String>(Arrays.asList(segments[2])));
				info.forEach((key, values) -> System.out.println(key + ": " + values));
				System.out.println("---");
				// Write the info to file
				ReadWrite.write(info);
				ReadWrite.move(file,"/Users/fangcaoxu/Desktop/SodaFinal/Processed");
			}
		}
		long endTime = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println("Time elapsed: " + (double) totalTime / 1000000000.0 + "seconds\n");
	}
}
