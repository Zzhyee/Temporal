package edu.uw.cs.lil.tiny.tempeval.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.util.Filters;
import edu.uw.cs.lil.tiny.tempeval.structures.NewTemporalSentence;
import edu.uw.cs.lil.tiny.tempeval.structures.TemporalDataset;

public class DataReader extends DefaultHandler {
	final private static String SERIALIZED_DIR = "data/new_serialized_data/";
	private String currentText;
	private String currentTimex;
	private String currentDocID;
	private int lastTimexID;
	private boolean isReadingText;
	private boolean isReadingTimex;
	private boolean isReadingDocID;
	private TemporalDocument currentDocument;
	private StanfordCoreNLP pipeline;
	private GrammaticalStructureFactory gsf;
	private SAXParser sp;

	public DataReader() throws ParserConfigurationException, SAXException{
		// Initialize annotation pipeline for preprocessing
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, parse");
		pipeline = new StanfordCoreNLP(props);
		gsf = new PennTreebankLanguagePack().grammaticalStructureFactory(Filters.<String>acceptFilter(), new SemanticHeadFinder(false));

		// Initialize xml parser
		SAXParserFactory spfac = SAXParserFactory.newInstance();
		sp = spfac.newSAXParser();
	}

	public TemporalDataset getDataset(String xmlDirName, boolean forceSerialize) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException {
		File xmlDir = new File(xmlDirName);
		String datasetName = xmlDir.getName();
		File serializedFile = new File(SERIALIZED_DIR + datasetName + ".ser");
		if(forceSerialize || !serializedFile.exists()) {
			if (forceSerialize)			
				System.out.println("Forcing serialization.");
			else
				System.out.println("Serialized data unavailable.");
			System.out.println("Reading and dependency parsing data...");

			long startTime = System.nanoTime();

			TemporalDataset dataset = new TemporalDataset();
			File[] xmlFiles = xmlDir.listFiles();
			System.out.printf("Reading %d files\n", xmlFiles.length);
			for(File f: xmlFiles) {
				currentDocument = new TemporalDocument();
				sp.parse(xmlDirName + f.getName(), this);
				currentDocument.doPreprocessing(pipeline, gsf);
				dataset.addSentences(currentDocument.getSentences());
			}
			dataset.serialize(serializedFile.getPath());

			long endTime = System.nanoTime();
			System.out.printf("%d sentences parsed and serialized (%.2f seconds)\n", dataset.size(), (endTime - startTime)*1.0e-9);
			return dataset;
		}
		else {
			System.out.println("Serialized data found. Deserializing data...");
			return TemporalDataset.deserialize(serializedFile.getPath());
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("TIMEX3")) {
			int id = Integer.parseInt(attributes.getValue("tid").substring(1));
			String type = attributes.getValue("type");
			String value = attributes.getValue("value");
			int anchor;
			if (attributes.getIndex("anchorTimeID") == -1)
				anchor = -1;
			else
				anchor = Integer.parseInt(attributes.getValue("anchorTimeID").substring(1));
			int offset;
			if (isReadingText)
				offset = currentText.length();
			else
				offset = -1;
			lastTimexID = id;
			currentDocument.insertTimex(id, type, value, anchor, offset);
			isReadingTimex = true;
			currentTimex = "";
		}
		else if (qName.equals("TEXT")) {
			isReadingText = true;
			currentText = "";
		}
		else if (qName.equals("DOCID")){
			isReadingDocID = true;
			currentDocID = "";
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("TIMEX3")) {
			currentDocument.setTimexText(lastTimexID, currentTimex);
			isReadingTimex = false;
		}
		else if (qName.equals("TEXT")) {
			currentDocument.setText(currentText);
			isReadingText = false;
		}
		else if (qName.equals("DOCID")){
			currentDocument.setDocID(currentDocID);
			isReadingDocID = false;
		}
	}

	public void characters(char[] buffer, int start, int length) {
		if (isReadingText)
			currentText += new String(buffer, start, length);
		if (isReadingTimex)
			currentTimex += new String(buffer, start, length);
		if (isReadingDocID)
			currentDocID += new String(buffer, start, length);
	}

	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException {
		TemporalDataset dataset = new DataReader().getDataset("data/TempEval3/TBAQ-cleaned/TimeBank/", true);
		for (NewTemporalSentence s : dataset) {
			if (s.getNumMentions() < 0)
				System.out.println(s);
		}
	}
}