import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.util.Triple;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NameEntityRecog {

	public static Map<String, ArrayList<String>> parse(File file) throws Exception {
		String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);

		// Returns all the text in the given file
		String fileContents = IOUtils.slurpFile(file);
		List<List<CoreLabel>> out = classifier.classify(fileContents);
		for (List<CoreLabel> sentence : out) {
			for (CoreLabel word : sentence) {
				System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
			}
			System.out.println();
		}
		System.out.println("---");

		
/*		 System.out.println("Ten best entity labelings");
		 DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.makePlainTextReaderAndWriter();
		 classifier.classifyAndWriteAnswersKBest(file.getAbsolutePath(), 10, readerAndWriter); 
		 System.out.println("---");
		 System.out.println("Per-token marginalized probabilities");
		 classifier.printProbs(file.getAbsolutePath(), readerAndWriter);*/
		 

		// Triple<String, Integer, Integer>:Person/Organization/Location,BeginIndex,EndIndex
		List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);
		ArrayList<String> Person = new ArrayList<String>();
		ArrayList<String> Organization = new ArrayList<String>();
		ArrayList<String> Location = new ArrayList<String>();

		for (Triple<String, Integer, Integer> item : list) {
			String key = item.first();
			String content = fileContents.substring(item.second(), item.third()).replaceAll("\\s+", " ");
			System.out.println(key + ": " + content);
			if (key.equals("PERSON")) {
				String trim = Trim.capitalize(content);
				Person.add(trim);
			}
			if (key.equals("ORGANIZATION")) {
				Organization.add(content);
			}
			if (key.equals("LOCATION")) {
				String trim = Trim.capitalize(content);
				Location.add(trim);
			}
		}

		System.out.println("---");
		Map<String, ArrayList<String>> info = new HashMap<String, ArrayList<String>>();
		info.put("PERSON", Person);
		info.put("ORGANIZATION", Organization);
		info.put("LOCATION", Location);
		info.forEach((key,values) -> info.put(key, Trim.removeIncomplete(Trim.removeDuplicate(values))));
		info.put("LOCATION", Trim.detectAddressByGoogle(info.get("LOCATION")));
		//info.put("LOCATION", Trim.detectAddressByGeoNames(info.get("LOCATION")));	
		return info;
	}

}
