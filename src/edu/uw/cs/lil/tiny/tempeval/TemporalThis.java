package edu.uw.cs.lil.tiny.tempeval;

import java.util.*;

import org.joda.time.LocalDate;

public class TemporalThis extends TemporalPredicate {
	public TemporalISO perform() {
		testStoredDates();
		return findThis();
	}

	private TemporalISO findThis() {
		if (first.isFullySpecified())
			return first;
		Map<String, Set<Integer>> tmpMap = first.getFullMapping();
		if (!(first instanceof TemporalDuration)) {
			if (tmpMap.containsKey("weekday") && 
					(TemporalJoda.convertISOToLocalDate(second).dayOfWeek().get() == TemporalISO.getValueFromDate(first, "weekday"))){
				return second;
			} else if (!tmpMap.containsKey("year")) {
				Set<Integer> tmpSet = new HashSet<Integer>();
				tmpSet.add(TemporalISO.getValueFromDate(second, "year"));
				tmpMap.put("year", tmpSet);
			// case for things like (this:<s,<r,s>> 1901:r ref_time:r)
			} else if (tmpMap.containsKey("year") && 
					!(first.isSet("quarter") || first.isSet("month")
					|| first.isSet("week") || first.isSet("weekday")
					|| first.isSet("season")))
				return first;
			else if (!(first.isSet("quarter") || first.isSet("month")
					|| first.isSet("week") || first.isSet("weekday")
					|| first.isSet("season"))) {
				Set<Integer> tmpSet = new HashSet<Integer>();
				tmpSet.add(TemporalISO.getValueFromDate(second, "month"));
				tmpMap.put("month", tmpSet);
			}
		// Case where first is a duration. Will implement 'this year', 'this month' and 'this week'
		} else {
			if (first.isSet("year"))
				tmpMap.put("year", second.getVal("year"));
			else if (first.isSet("month")){
				tmpMap.put("year", second.getVal("year"));
				tmpMap.put("month", second.getVal("month"));
			} else if (first.isSet("week")){
				LocalDate tmpLocalDate = TemporalJoda.convertISOToLocalDate(second);
				int weekNum = tmpLocalDate.getWeekOfWeekyear();
				Set<Integer> weekNums = new HashSet<Integer>();
				weekNums.add(weekNum);
				tmpMap.put("week", weekNums);
				tmpMap.put("year", second.getVal("year"));
			}
		}

		return new TemporalDate(tmpMap);

	}

	private void testStoredDates() {
		if (!(first instanceof TemporalDate || first instanceof TemporalDuration)
				|| !(second instanceof TemporalDate)){			
			throw new IllegalArgumentException(
					"The two parameters to TemporalThis aren't TemporalDate objects, which they should be.");
		}
	}
}

// TODO: Find a case where this is useful. I don't think it is, but look anyway. 
/*
if (!(first.isSet("quarter") || first.isSet("week") || first.isSet("day") || first.isSet("weekday"))){
	Set<Integer> tmpSet = new HashSet<Integer>();
	tmpSet.add(TemporalISO.getValueFromDate(second, "day"));
	tmpMap.put("day", tmpSet);
}
*/