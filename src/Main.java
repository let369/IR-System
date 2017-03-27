import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
	private static final String FILENAME = "cran/cranall.txt";

	public static void main(String[] args) {
		try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
			int count =1;
			String sCurrentLine;
			String NewDoc = "";
			sCurrentLine = br.readLine();
			while (sCurrentLine != null) {
				if(sCurrentLine.contains(".I")){
					NewDoc = NewDoc + sCurrentLine+"\n";
					sCurrentLine = br.readLine();
					while(sCurrentLine != null && sCurrentLine.contains(".I")==false){
						NewDoc = NewDoc + sCurrentLine+"\n";
						sCurrentLine = br.readLine();
					}
					try{
					    PrintWriter writer = new PrintWriter("docs/"+count+".txt", "UTF-8");
					    writer.println(NewDoc);
					    writer.close();
					}
					catch (IOException e) {
					}
				}
				count++;
				NewDoc = "";
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
