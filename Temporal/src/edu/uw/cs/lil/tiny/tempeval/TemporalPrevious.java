package edu.uw.cs.lil.tiny.tempeval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;


public class TemporalPrevious extends TemporalPredicate{

	@Override
	public TemporalISO perform() {
		// TODO: This is a hack!! If the temporal phrase is just "last", it returns last year.
		if (second == null)
			return new TemporalDate("year", TemporalDate.getValueFromDate(first, "year"));
		testStoredDates();
		return findPrevious();
	}

	private TemporalDate quarterIsSet(){
		boolean thisYear = hasQuarterPassedThisYear();
		Map<String, Set<Integer>> tmpMap = first.getFullMapping();
		if (thisYear)
			tmpMap.put("year", second.getVal("year"));
		else 
			tmpMap.put("year", subtractOne(second.getVal("year")));
		return new TemporalDate(tmpMap);
	}
	
	private TemporalDate monthAndNotDay(){
		Map<String, Set<Integer>> tmpMap = first.getFullMapping();
		// if ref_time is past first.month, return this year. else return last year
		if  (TemporalDate.getValueFromDate(second, "month") >= TemporalDate.getValueFromDate(first, "month"))
			tmpMap.put("year", second.getVal("year"));
		else
			tmpMap.put("year", subtractOne(second.getVal("year")));
		return new TemporalDate(tmpMap);
	}
	
	private TemporalDate monthAndDay(){
		Map<String, Set<Integer>> tmpMap = first.getFullMapping();
		if  (TemporalDate.getValueFromDate(second, "month") > TemporalDate.getValueFromDate(first, "month"))
			tmpMap.put("year", second.getVal("year"));
		else if (TemporalDate.getValueFromDate(second, "month") < TemporalDate.getValueFromDate(first, "month"))
			tmpMap.put("year", subtractOne(second.getVal("year")));
		// Case when the month is equal for ref_time and first
		else{
			if (TemporalDate.getValueFromDate(second, "day") > TemporalDate.getValueFromDate(first, "day"))
				tmpMap.put("year", second.getVal("year"));
			else 
				tmpMap.put("year", subtractOne(second.getVal("year")));
		}	
		return new TemporalDate(tmpMap);
	}
	
	private TemporalDate weekdayAndNotMonthOrDay(){
		LocalDate date = TemporalJoda.convertISOToLocalDate(second);
		if (date.getDayOfWeek() == TemporalISO.getValueFromDate(this.first,
				"weekday")) {
			date = date.minusDays(1);
		}
		while (date.getDayOfWeek() != TemporalISO.getValueFromDate(
				this.first, "weekday")) {
			date = date.minusDays(1);
		}
		return TemporalJoda.convertLocalDateToISO(date);
	}
	
	private TemporalDate convexYear(){
		int tmpYear = TemporalDate.getValueFromDate(second, "year");
		return new TemporalDate("year", tmpYear - 1);
	}
	
	private TemporalDate convexQuarter(){
		int quarterNum = (TemporalDate.getValueFromDate(first, "quarter") + 4)/4;
		int year = TemporalDate.getValueFromDate(second,"year");
		if (quarterNum == 1){
			year = year - 1;
			quarterNum = 4;
		} else 
			quarterNum = quarterNum - 1;
		Map<String, Set<Integer>> tmpMap = new HashMap<String, Set<Integer>>();
		Set<Integer> tmpSet = new HashSet<Integer>();
		tmpSet.add(year);
		tmpMap.put("year", tmpSet);
		Set<Integer> tmpSet2 = new HashSet<Integer>();
		tmpSet2.add(quarterNum);
		tmpMap.put("quarter", tmpSet2);
		return new TemporalDate(tmpMap);
	}
	
	private TemporalDate convexMonth(){
		int tmpMonth = TemporalDate.getValueFromDate(second, "month");
		return new TemporalDate("month", tmpMonth - 1);
	}
	
	private TemporalISO findPrevious(){
		TemporalDate prevDate;
		if ((first.isSet("year") && !first.isConvexSet()) || first.isSet("present_ref"))
			return first;
		else{
			if (first.isSet("quarter") && !first.isConvexSet()){
				prevDate = quarterIsSet();
			} else if (first.isSet("month") && !first.isSet("day") && !first.isConvexSet()){
				prevDate = monthAndNotDay();
			} else if (first.isSet("month") && first.isSet("day")){
				prevDate = monthAndDay();
			} else if (first.isSet("weekday") && !first.isSet("month") && !first.isSet("day")){
				prevDate = weekdayAndNotMonthOrDay();
			} else if (first.isConvexSet()){
				if (first.isSet("year")){
					prevDate = convexYear();
				} else if (first.isSet("quarter")){
					prevDate = convexQuarter();
				} else if (first.isSet("month")){
					prevDate = convexMonth();
				} else if (first.isSet("week")){
					throw new IllegalArgumentException("Haven't implemented 'last week', as it's weird in the data.");
					//int tmpWeek = TemporalDate.getValueFromDate(second, "week");
					//TemporalDate tmpDate = new TemporalDate("week", tmpWeek - 1);
				} else 
					throw new IllegalArgumentException("Haven't implemented 'prevous' for convex set " + first);
			} else
				throw new IllegalArgumentException("Haven't implemented 'prevous' for " + first);
		}
		
		return prevDate;
	}
	
	
	
	// Only called when first is a quarter, and returns true if it is before the ref_time, false otherwise.
	// Note: Will always return false if quarter = 4.
	private boolean hasQuarterPassedThisYear(){
		if ((TemporalDate.getValueFromDate(first, "quarter") == 1 && TemporalDate.getValueFromDate(second, "month") > 3) ||
				(TemporalDate.getValueFromDate(first, "quarter") == 2 && TemporalDate.getValueFromDate(second, "month") > 6) ||
				(TemporalDate.getValueFromDate(first, "quarter") == 3 && TemporalDate.getValueFromDate(second, "month") > 9))
			return true;
		else 
			return false;
	}
	
	private Set<Integer> subtractOne(Set<Integer> oldIntSet){
		Set<Integer> tmpInt = new HashSet<Integer>();
		for (int i : this.second.getVal("year")) {
			tmpInt.add(Integer.valueOf(i - 1));
		}
		return tmpInt;
	}

	private void testStoredDates(){

		if (!(first instanceof TemporalDate) || !(second instanceof TemporalDate))
			throw new IllegalArgumentException("The two parameters to TemporalPrevious aren't TemporalDate objects, which they should be.");
	}
}