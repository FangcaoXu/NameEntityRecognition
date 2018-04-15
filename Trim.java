import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.geonames.*;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.UnknownErrorException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.AddressType;
import com.google.maps.model.ComponentFilter;
import com.google.maps.model.GeocodingResult;

public class Trim {

	public static String[] docSegments(File file) throws IOException {
		// TODO Auto-generated method stub
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String title = br.readLine();
		String Date = br.readLine();
		StringBuilder content = new StringBuilder();
		for (String l; (l = br.readLine()) != null;){
			if (l.isEmpty()) {
				continue;
			} else {
				content.append(l);
			    content.append(" ");
			}
		}
		br.close();
		List<SimpleDateFormat> formatDates= new ArrayList<SimpleDateFormat>();
		formatDates.add(new SimpleDateFormat("dd.MM.yyyy"));
		formatDates.add(new SimpleDateFormat("yyyy-MM-dd"));
		formatDates.add(new SimpleDateFormat("MMMM dd, yyyy"));	
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String[] docParse = new String[3];
		for (SimpleDateFormat formatDate : formatDates) {
			try {
				Date startDate= formatDate.parse(Date);
				String newDate = sdf.format(startDate);
				docParse[0] = newDate;
				} 
			catch (ParseException e) {
				// Move those files without dates into a specific folder
				}
			}
		if(docParse[0]==null) {				
			ReadWrite.move(file,"/Users/fangcaoxu/Desktop/SodaFinal/NoDate");
			System.out.println("No Date detected in: " + file.getName());
			docParse[0]="NoDateDetected";
		}
		docParse[1]=title;
		docParse[2]=content.toString();
		return docParse;
	}
	
	public static ArrayList<String> detectAddressByGeoNames (ArrayList<String> addressList) throws Exception {
		// queries geonames for given location name
		// http://www.geonames.org/
		WebService.setUserName("fleurxu");
		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		searchCriteria.setFuzzy(0);
		// Copy the addressList to an iterator to avoid the ConcurrentModificationException
		ListIterator<String> iter = addressList.listIterator();
		while (iter.hasNext()) {
			String address = iter.next();
			searchCriteria.setQ(address);
			ToponymSearchResult searchResult = WebService.search(searchCriteria);
			if (searchResult.getTotalResultsCount() > 0) {
				Toponym toponym = searchResult.getToponyms().get(0);
				String coordinates = String.valueOf(toponym.getLatitude()) +", "+ String.valueOf(toponym.getLongitude());
				String featureCode = toponym.getFeatureCode();
				System.out.println(toponym.getName() + ", "+ coordinates + ", " + featureCode);				
				address = toponym.getName() + ", "+ coordinates + ", " + featureCode;
				iter.set(address);
				}
			else {
				iter.remove();
			}
		}
		return addressList;		
	}	

	public static ArrayList<String> detectAddressByGoogle (ArrayList<String> addressList) throws Exception {
		GeoApiContext context = new GeoApiContext.Builder()
				.apiKey("AIzaSyD7d90AxdVJYMuTwEljJsipOHnwtKP4Trc")
				.queryRateLimit(8)
				.retryTimeout(10,TimeUnit.SECONDS)
				.build()	;
		ComponentFilter filters = ComponentFilter.country("US");
		ListIterator<String> iter = addressList.listIterator();
		while (iter.hasNext()) {	
			String address = iter.next();
			try{
				GeocodingResult[] results =  GeocodingApi.geocode(context, address).components(filters).await();
				if (results.length > 0) {
					address = results[0].formattedAddress + ", " + results[0].geometry.location.toString();
					// Address Components 
					String coms = detectAddressComponent(results[0]);
					AddressType[] addressTypes =  results[0].types;				
					String listString = "";
					for (AddressType addressType : addressTypes){
						listString =  listString+ addressType.name() + " ";
					}
					if (listString.contains("STREET_ADDRESS")){
						address = "1 "+"Street Address: " + address + "; " + coms;					
						iter.set(address);
					}
					else if (listString.contains("LOCALITY")){
						address = "2 "+ "City: " + address + "; " + coms;
						iter.set(address);						
					}
					else if (listString.contains("ADMINISTRATIVE_AREA_LEVEL_1")){
						address = "3 "+ "State: " + address+ "; " + coms;
						iter.set(address);
					}
					else if (listString.contains("COUNTRY")){
						address = "4 "+ "COUNTRY: " + address+ "; " + coms;
						iter.set(address);
					}
					else {
						iter.remove();
					}
				}
				else {
					iter.remove();
					}
			}catch(UnknownErrorException e){
				iter.remove();
				continue;
			    }	
		}
		return detectHierarchy(addressList);
	}
	
	public static ArrayList<String> detectHierarchy (ArrayList<String> addressList){
		Collections.sort(addressList);
		Collections.reverse(addressList);
		addressList.forEach(value->System.out.println(value));
		System.out.println("---");
		for (int i = 0; i < addressList.size(); i++) {
			String[] coms = addressList.get(i).split("; ")[1].split(", ");
			String com = "";
			for (String string:coms) {
				if(!string.isEmpty()) {
					com=string;
					break;
				}
			}
			for (int j = i + 1; j < addressList.size(); j++) {
				if (addressList.get(j).split("; ")[1].contains(com)) {
					addressList.remove(i);
					i--;
					break; //break the inner for-loop
				}
			}
		}
		addressList.forEach(value->System.out.println(value));
		addressList.forEach(value-> addressList.set(addressList.indexOf(value),value.split(": ")[1].split(";")[0]));
		System.out.println("---");
		return addressList;
	}
	// Used for detecting the hierarchy of Place Names
	public static String detectAddressComponent (GeocodingResult result){
		AddressComponent[] components =  result.addressComponents;
		String city="";
		String state="";
		String country="";
		for (AddressComponent component: components){ 			
			String listString = "";
			AddressComponentType[] types = component.types;		
			for (AddressComponentType type : types){
				listString =  listString + type.name() + " ";	
			}
			//System.out.println(component.longName + ", "+ listString);
			if (listString.contains("LOCALITY")){
				city = component.longName;
				}
			else if (listString.contains(("ADMINISTRATIVE_AREA_LEVEL_1"))){
				state = component.longName;
				}
			else if (listString.contains(("COUNTRY"))){
				country = component.longName;
				}
			}
		String coms = city + ", " + state + ", " + country;
		return coms;
	}
	
	public static ArrayList<String> removeIncomplete(ArrayList<String> array) {
		// Sort names by their lengths
		array.sort((a, b) -> Integer.compare(a.length(), b.length()));
		// remove incomplete strings, such as Fangcao if Fangcao Xu exist
		for (int i = 0; i < array.size(); i++) {
			for (int j = i + 1; j < array.size(); j++) {
				if (array.get(j).contains(array.get(i))) {
					array.remove(i);
					i--;
					break; //break the inner for-loop
				}
			}
		}
		return array;
	}
	
	public static ArrayList<String> removeDuplicate(ArrayList<String> array) {
		Set<String> hs = new HashSet<>();
		hs.addAll(array);
		array.clear();
		array.addAll(hs);
		return array;
	}

	public static String capitalize(String line) {
		String[] words = line.split(" ");
		for (int i = 0; i < words.length; ++i) {
			String word = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
			words[i] = word;
		}
		return String.join(" ", words);
	}

}
