package com.airaria.extract;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
	
	/* FlightDetailsExtract is to extract the search data from AirAsia.com based on the given inputs. 
	 * This program is to search for AirAsia flights from source to destination both one way and Round Trip. 
	 * */
	public class FlightDetailsExtract {

		public static final String DELIMS = "[/]";
		public static final String ONE_WAY="OneWay";
	    private static final String USER_AGENT = "Mozilla/5.0 (jsoup)";
	    private static final int TIMEOUT = 5 * 1000;
	    private static final String BASE_URL="http://booking.airasia.com/Compact.aspx?";
	    private static final String LOW_FARE="Low fare";
	    
		public static void main(String args[])
		{
			/*
			 * Input data for flight search. All paramaters are mandatory. Any missing parameter will result in mal formed data.
			 * To fetch the oneway trip results, just want to replace isRoundTrip value by OneWay.
			 */
			final String isRoundTrip = "RoundTrip";//"OneWay"// Should be OneWay or RoundTrip.
			final String originStation="BLR"; // Station code of Origin Station (Get from http://www.airportcodes.org/).
			final String destinationStation="COK"; // Station code of Destination Station(Get from http://www.airportcodes.org/).
			final String leavingDate="12/24/2014"; //Date on which we need to fly from originStation to destinationStation
			final String arrivalDate="12/29/2014"; //Date on which we need to fly from destinationStation to originStation. In case of one way trip, this will be by default 2 days before the leaving date
			final String numberOfAdults="2"; //Number of Adults travelling
			final String numberOfChildren="1"; //Number of children travelling
			final String numberOfInfants="1"; //Number of Infants travelling
			final String currencyType="default";//INR by Default
			final String localLang="en-GB";
			final String outerSelector="div[id*=availabilityInputContent";
			final String spanSelector="span";
			final String divSelector="div";
        	
        	/*Generates the search url based on the input parameters */
			String searchUrl=generateSearchUrl( isRoundTrip, originStation, destinationStation, leavingDate, arrivalDate, numberOfAdults, numberOfChildren, numberOfInfants,  currencyType, localLang);
			
			/*To load the entire DOM*/
			Document flightSearchResultDoc=fetchTextDataFromUrl(searchUrl); 
			
			/*Generates the search url based on the input parameters */
			Map<String,List<List<String>>> flightHeaderList  = fetchHeaders(flightSearchResultDoc,outerSelector,spanSelector,divSelector,searchUrl);
			System.out.println(flightHeaderList);
			
			/*To fetch the flight details */
			Map<String,List<List<String>>> flightMainDataList=  parseDocuments(flightSearchResultDoc,outerSelector,spanSelector,divSelector);
			System.out.println(flightMainDataList);
			
			/*To fetch the lowest available fare details.*/
			Map<String,List<List<String>>> flightLowFareDataList=  getLowFareDetails(flightSearchResultDoc,outerSelector,spanSelector,divSelector);
			System.out.println(flightLowFareDataList);
			
			//To display the requirested data
			displaySearchData(flightLowFareDataList,flightMainDataList,flightHeaderList,isRoundTrip);	
			
			
		}
		
		public static void displaySearchData(Map<String,List<List<String>>> flightLowFareDataList,Map<String,List<List<String>>> flightMainDataList,Map<String,List<List<String>>> flightHeaderList ,String isRoundTrip)
		{
			
		}
		//Method to fetch the low fare flight details for the seach criteria.
		public static Map<String, List<List<String>>> getLowFareDetails(Document searchDoc, String outerSelector,String  spanSelector, String divSelector)
		{
			//Variables to select the dom objects
			final String tableSelector="table.rgMasterTable";
			final String thSelector="th.rgHeader";
			final String thLowFare="div[id*=iconLowfare";
			final String thHiFare="div[id*=iconHiflyer";
			final String descFare="div.fareDescWord2";
			final String mapKey="rowData_";
			final String trSelector="tr.rgRow";
			final String tdNextSelector="td.resultFareCell2";
			final String paxTypeDisplay="div.paxTypeDisplay";
			final String resultFare2="div.resultFare2";
			final String bold="div.bold";
			final String price="div.price";


			if(searchDoc !=null)
            {
				Map<String, List<List<String>>> flightDataListInner = new HashMap<String, List<List<String>>>();
	 	        Elements elementListOuterMost = searchDoc.select(tableSelector);
	 	        int counter=0;
	 	        for (Element tableOuterMost : elementListOuterMost )
		        {
		        	counter++;
		        	List<List<String>> seletedList = new ArrayList<List<String>>();
		        	/* Loop through the flight search results elements to fetch data */
		        	Elements elementListOuter = tableOuterMost.select(thSelector);
			        for (Element theader : elementListOuter )
			        {
			        	List<String> thSeletedList = new ArrayList<String>();
			            String thLowFareVal = theader.select(thLowFare+counter).text();
			            String thHiFareVal = theader.select(thHiFare+counter).text();
			            String descFareVal = theader.select(descFare).text();
			            thSeletedList.add(thLowFareVal);
			            thSeletedList.add(thHiFareVal);
			            thSeletedList.add(descFareVal);
			            seletedList.add(thSeletedList);

			        }
			        flightDataListInner.put(mapKey+counter, seletedList);
			        
			        List<List<String>> seletedTdList = new ArrayList<List<String>>();
		        	/* Loop through the flight search results elements to fetch data */
		        	Elements trElements = tableOuterMost.select(trSelector);
			        for (Element tRow : trElements)
			        {
			        	Elements tdNextElements = tRow.select(tdNextSelector);//selecting all  table row values
				        for (Element td : tdNextElements)
				        {				        	
				        	Elements tdElements = td.select(resultFare2);//selecting all  td values
				        	
				        	for (Element td1 : tdElements)//Looping through the columns
					        {
				        		List<String> tdSeletedList1 = new ArrayList<String>();
				        		Elements paxTypeDisp=td1.select(paxTypeDisplay); //Get all the traveller type
				        		for (Element td2 : paxTypeDisp)
						        {
				        			tdSeletedList1.add(td2.text());
						        }
				        		tdSeletedList1.add(td1.select(bold).text()); //Get infant type
				        		seletedTdList.add(tdSeletedList1);
				        		
				        		List<String> tdSeletedList2 = new ArrayList<String>();
				        		Elements priceVal=td1.select(price); //Get all he prices
				        		for (Element td2 : priceVal)
						        {
				        			tdSeletedList2.add(td2.text());
						        }
				        	
					        	seletedTdList.add(tdSeletedList2); //Forming a list of all the row data
					        }
				        	
				        	
				        	
				        }

				        flightDataListInner.put(mapKey+counter, seletedTdList);//Forming a map of all the table data
			        }
			        
		            
		        }
	 	        return flightDataListInner;
            }
			else
			{
				return null;
			}
			
			
		}
		
		//Method to parse the dom and add data to a map.
		public static Map<String, List<List<String>>> parseDocuments(Document searchDoc, String outerSelector,String  spanSelector, String divSelector)
		{
			//to select the dom objects
			final String availabilityInputContent=".availabilityInputContent";
			final String dayHeaderImage=".dayHeaderImage";
			final String selectTDate=".tDate";
			final String selectTodayDate=".todayDate";
			final String selectTFare=".tFare";
			final String selectedDay =".dayHeaderTodayImage";
			final String allInFrom =".allInFrom";
			final String mapKey="flightData_";
			final String selectedValKey="selectedVal";
			
	            if(searchDoc !=null)
	            {
			       //Parse the first display area
			       Map<String, List<List<String>>> flightDataListInner = new HashMap<String, List<List<String>>>();
			 	        Elements elementListOuterMost = searchDoc.select(divSelector+availabilityInputContent);
			 	        int counter=0;
			 	        /* To loop through the entire resulting DOM to fetch the required data */
				        for (Element divOuterMost : elementListOuterMost )
				        {
				        	counter++;
				        	/* Loop through the flight search results elements to fetch data */
				        	Elements elementListOuter = divOuterMost.select(outerSelector+counter);
					        for (Element divOuter : elementListOuter )
					        {
					            List<List<String>> spanSeletedList = new ArrayList<List<String>>();
					            Elements elementSelectedList = divOuter.select(divSelector+selectedDay);
					            
					            /* Loopng to fetch only the selected dates flght details. */
						        for (Element div : elementSelectedList )
						        {
						        	List<String> spanListInner = new ArrayList<String>();
						            String tDate = div.select(spanSelector+selectTDate).text();
						            String todayDate = div.select(spanSelector+selectTodayDate).text();
						            String selectedToday = div.select(divSelector+selectTodayDate).text();
						            String allInFromVal = div.select(divSelector+allInFrom).text();
						            String tFare = div.select(spanSelector+selectTFare).text();
						            spanListInner.add(tDate);
						            spanListInner.add(todayDate);
						            spanListInner.add(selectedToday);
						            spanListInner.add(allInFromVal);
						            spanListInner.add(tFare);	
						            spanSeletedList.add(spanListInner);
						        }
						        flightDataListInner.put(selectedValKey+counter, spanSeletedList);
					            
						        List<List<String>> spanList = new ArrayList<List<String>>();
					 	        Elements elementList = divOuter.select(divSelector+dayHeaderImage);
					 	        
					 	       /* Looping to fetch the flght details of near by dates apart from selected ones. */
						        for (Element div : elementList )
						        {
						        	List<String> spanListInner = new ArrayList<String>();
						            String tDate = div.select(spanSelector+selectTDate).text();
						            String todayDate = div.select(spanSelector+selectTodayDate).text();
						            String selectedToday = div.select(divSelector+selectTodayDate).text();
						            String allInFromVal = div.select(divSelector+allInFrom).text();
						            String tFare = div.select(spanSelector+selectTFare).text();
						            spanListInner.add(tDate);
						            spanListInner.add(todayDate);
						            spanListInner.add(selectedToday);
						            spanListInner.add(allInFromVal);
						            spanListInner.add(tFare);	
						            spanList.add(spanListInner);
						        }
						        flightDataListInner.put(mapKey+counter, spanList);

					        }
				        	
				        }
				        return flightDataListInner;
	            }
	            else
	            {
	            	 return null;
	            }
	        
	        
	       
		}
		public static  Map<String,List<List<String>>> fetchHeaders(Document searchDoc, String outerSelector,String  innerSelector,String innerMostSelector, String searchUrl)
		{
			final String divSelector="div.dateMarketHead";
			final String outerSpan="span.black5";
			final String innerSpan="span.red2";
			final String mapKey="Headers";
			
	        if(searchDoc!=null )
	        {
				//Parse the header values
		        Map<String,List<List<String>>> flightDataList = new HashMap<String, List<List<String>>>(); //Map with all the header details
		        List<List<String>> innerList = new ArrayList<List<String>>();//Outer List to keep header data
		        
		        Elements headerList = searchDoc.select(divSelector);
		       
		        for (Element tempHeader : headerList)
		        {
		            List<String> pList = new ArrayList<String>();//Inner list to keep the header data
		            String mainHeader = tempHeader.select(outerSpan).text(); //Fetch the mail header DEPART/ RETURN
		            String subHeader = tempHeader.select(innerSpan).text(); //Fetch the Place details
		            pList.add(mainHeader);
		            pList.add(subHeader);
		            innerList.add(pList);		            
		        }
		        flightDataList.put(mapKey, innerList);
		         
		        return flightDataList;
	        }
	        else
	        {
	        	return null;
	        }
		}
		public static Document fetchTextDataFromUrl(String searchUrl)
		{
			Document searchDoc =null;
			// fetch the specified URL and parse to a HTML DOM
	        try
	        {
	        	searchDoc = Jsoup.connect(searchUrl).userAgent(USER_AGENT).timeout(TIMEOUT).get();
	        }
	        catch(IOException ioe)
	        {
	        	System.out.println("Error in fetching data from search url. Please check the url and input values."+ioe.getMessage());
	        	return null;
	        }

	        return searchDoc;
	        
		}

		
		//In case of one way trip, this will be by default 2 days before the leaving date.
		public static String getArrivalDate(String leavingDate, String isRoundTrip,String arrivalDate)
		{
			
			if(isRoundTrip.equalsIgnoreCase(ONE_WAY))
			{
				String[] splitDate = splitDate(DELIMS,leavingDate);
				if(splitDate!=null && splitDate.length>0)
				{
					try
					{
						int dayOfLeaving=Integer.parseInt(splitDate[1])-2;
						arrivalDate=splitDate[0]+"/"+dayOfLeaving+"/"+splitDate[2];
					}
					catch(NullPointerException npe )
					{
						System.out.println("Exception occured. Please check your input Dates .. "+npe.getMessage());
						return null;
					}
					catch(IndexOutOfBoundsException oob )
					{
						System.out.println("Exception occured. Please check your input dates .. " + oob.getMessage());
						return null;
					}
				}
				
			}
			else
			{
				return arrivalDate;
			}
			return arrivalDate;
			
		}
		
		//Method to split the date and attach those in search url.
		public static String[] splitDate(String delims, String dateToSplit)
		{
			String[] splitDate = dateToSplit.split(delims);
			return splitDate;
			
		}
		
		//Method to generate the search url based on the user inputs.
		public static String generateSearchUrl(String isRoundTrip,String originStation,String destinationStation,String leavingDate,String arrivalDate,String numberOfAdults,String numberOfChildren,String numberOfInfants, String currencyType,String localLang)
		{
			
			String[] leavingDateSplit = splitDate(DELIMS,leavingDate);
			String[] arrivalDateSplit = splitDate(DELIMS,arrivalDate);
			String searchUrl=null;

				if(leavingDateSplit!=null && leavingDateSplit.length>0 && arrivalDateSplit!=null && arrivalDateSplit.length>0)
				{
					try
					{
					searchUrl=BASE_URL+
							"viewState=/wEPDwUBMGRktapVDbdzjtpmxtfJuRZPDMU9XYk=&"+
							"culture="+localLang+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$RadioButtonMarketStructure="+isRoundTrip+"&"+
							"ControlGroupCompactView_AvailabilitySearchInputCompactVieworiginStation1="+originStation+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$TextBoxMarketOrigin1="+originStation+"&"+
							"ControlGroupCompactView_AvailabilitySearchInputCompactViewdestinationStation1="+destinationStation+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$TextBoxMarketDestination1="+destinationStation+"&"+
							"date_picker="+leavingDate+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$DropDownListMarketDay1="+leavingDateSplit[1]+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$DropDownListMarketMonth1="+leavingDateSplit[2]+"-"+leavingDateSplit[0]+"&"+
							"date_picker="+arrivalDate+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$DropDownListMarketDay2="+arrivalDateSplit[1]+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$DropDownListMarketMonth2="+arrivalDateSplit[2]+"-"+arrivalDateSplit[0]+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$DropDownListPassengerType_ADT="+numberOfAdults+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$DropDownListPassengerType_CHD="+numberOfChildren+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$DropDownListPassengerType_INFANT="+numberOfInfants+"&"+
							"ControlGroupCompactView$MultiCurrencyConversionViewCompactSearchView$DropDownListCurrency="+currencyType+"&"+
							"ControlGroupCompactView$AvailabilitySearchInputCompactView$DropDownListSearchBy=columnView&"+
							"ControlGroupCompactView$ButtonSubmit=Search&"+
							"__VIEWSTATEGENERATOR=05F9A2B0&"+
							"__VIEWSTATE=/wEPDwUBMGRktapVDbdzjtpmxtfJuRZPDMU9XYk=";
						}
						catch(NullPointerException npe )
						{
							System.out.println("Exception occured. Please check your input values.. (Especially Dates ... )"+npe.getMessage());
							return searchUrl;
						}
						catch(IndexOutOfBoundsException oob )
						{
							System.out.println("Exception occured. Please check your input values.. (Especially Dates ... )" + oob.getMessage());
							return searchUrl;
						}
				}

			
			return searchUrl;
			
		}
	    /**
	     * Format an Element to plain-text
	     * @param element the root element to format
	     * @return formatted text
	     */
	    public String getPlainText(Element element)
	    {
	        FormattingVisitor formatter = new FormattingVisitor();
	        NodeTraversor traversor = new NodeTraversor(formatter);
	        traversor.traverse(element); // walk the DOM, and call .head() and .tail() for each node
	        return formatter.toString();
	    }

	    // The formatting rules, implemented in a breadth-first DOM traverse
	    private class FormattingVisitor implements NodeVisitor
	    {
		        private static final int maxWidth = 80;
		        private int width = 0;
		        private StringBuilder accum = new StringBuilder(); // holds the accumulated text
	
		        // hit when the node is first seen
		        public void head(Node node, int depth)
		        {
		            String name = node.nodeName();
		            if (node instanceof TextNode)
		                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
		            else if (name.equals("li"))
		                append("\n * ");
		            else if (name.equals("dt"))
		                append("  ");
		            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
		                append("\n");
		        }
	
		        // it when all of the node's children (if any) have been visited
		        public void tail(Node node, int depth) 
		        {
		            String name = node.nodeName();
		            if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
		                append("\n");
		            else if (name.equals("a"))
		                append(String.format(" <%s>", node.absUrl("href")));
		        }
	
		        // appends text to the string builder with a simple word wrap method
		        private void append(String text)
		        {
		            if (text.startsWith("\n"))
		                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
		            if (text.equals(" ") &&
		                    (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
		                return; // don't accumulate long runs of empty spaces
	
		            if (text.length() + width > maxWidth) 
		            { // won't fit, needs to wrap
		                String words[] = text.split("\\s+");
		                for (int i = 0; i < words.length; i++) 
		                {
		                    String word = words[i];
		                    boolean last = i == words.length - 1;
		                    if (!last) // insert a space if not the last word
		                        word = word + " ";
		                    if (word.length() + width > maxWidth) { // wrap and reset counter
		                        accum.append("\n").append(word);
		                        width = word.length();
		                    } else {
		                        accum.append(word);
		                        width += word.length();
		                    }
		                }
		            } else 
		            { // fits as is, without need to wrap text
		                accum.append(text);
		                width += text.length();
		            }
		        }
			    @Override
			    public String toString() 
			    {
			        return accum.toString();
			    }
	    }
	}