package edu.uw.cs.lil.tiny.tempeval;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joda.time.LocalDate;

public class TemporalNext extends TemporalPredicate {
	public TemporalISO perform() {
		testStoredDates();
		return findNext();
	}

	private TemporalISO findNext() {
		TemporalDate nextDate;
		if (this.first.getKeys().contains("year") || this.first.isSet("present_ref")) {
			nextDate = (TemporalDate) this.first;
		} else {
			if ((this.first.getKeys().contains("month"))
					&& (!this.first.getKeys().contains("day"))) {
				int ref_timeMonth = TemporalISO.getValueFromDate(this.second, "month");
				int dMonth = TemporalISO.getValueFromDate(this.first, "month");
				Map<String, Set<Integer>> tmpMap = this.first.getFullMapping();
				if (ref_timeMonth < dMonth) {
					tmpMap.put("year", this.second.getVal("year"));
				} else {
					Set<Integer> tmpInt = new HashSet<Integer>();
					for (int i : this.second.getVal("year")) {
						tmpInt.add(Integer.valueOf(i + 1));
					}
					tmpMap.put("year", tmpInt);
				}
				nextDate = new TemporalDate(tmpMap);
			// TODO: Change this to accurately give next if I say "next third quarter" in Jaunary. Should be same year.
			// 		 That's not possible with this. 
			} else if (first.isSet("quarter")) {
				int ref_timeYear = TemporalISO.getValueFromDate(second, "year") + 1;
				Map<String, Set<Integer>> tmpMap = this.first.getFullMapping();
				Set<Integer> tmpSet = new HashSet<Integer>();
				tmpSet.add(ref_timeYear);
				tmpMap.put("year", tmpSet);
				nextDate = new TemporalDate(tmpMap);
			} else {
				if ((this.first.getKeys().contains("month"))
						&& (this.first.getKeys().contains("day"))) {
					Map<String, Set<Integer>> tmpMap = this.first.getFullMapping();
					tmpMap.put("year", this.second.getVal("year"));
					TemporalDate firstTmpDate = new TemporalDate(tmpMap);
					Set<Integer> tmpInt = new HashSet<Integer>();
					for (int i : tmpMap.get("year")){
						tmpInt.add(Integer.valueOf(i + 1));
					}
					tmpMap.put("year", tmpInt);
					TemporalDate secondTmpDate = new TemporalDate(tmpMap);
					if (areTemporallyOrdered(firstTmpDate, secondTmpDate))
						nextDate = firstTmpDate;
					else
						nextDate = secondTmpDate;
				} else {
					if ((this.first.getKeys().contains("weekday"))
							&& (!this.first.getKeys().contains("month"))
							&& (!this.first.getKeys().contains("day"))) {
						LocalDate date = TemporalJoda.convertISOToLocalDate(this.second);
						if (date.getDayOfWeek() == TemporalISO.getValueFromDate(this.first,
								"weekday")) {
							date.plusDays(1);
						}
						while (date.getDayOfWeek() != TemporalISO.getValueFromDate(
								this.first, "weekday")) {
							date = date.plusDays(1);
						}
						nextDate = TemporalJoda.convertLocalDateToISO(date);
					} else {
						throw new IllegalArgumentException(
								"haven't implemented things other than" + " dates with months or days. Problem in TemporalNext's findNext()");
					}
				}
			}
		}
 		return nextDate;
	}

	// Takes two TemporalDates, turns them into JodaTime LocalDate objects, and
	// compares them.
	// Assumes this.second is ref_time
	// Assumes f and s are TemporalDates with the fields "year", "month", and
	// "day", or
	// TemporalJoda.convertISOToLocalDate throws an error.
	public boolean areTemporallyOrdered(TemporalDate f, TemporalDate s) {
		LocalDate d1 = TemporalJoda.convertISOToLocalDate(f);
		LocalDate d2 = TemporalJoda.convertISOToLocalDate(s);
		LocalDate ref_time = TemporalJoda.convertISOToLocalDate(this.second);
		if ((ref_time.isBefore(d1)) && (d1.isBefore(d2)))
			return true;
		if ((d1.isBefore(ref_time)) && (ref_time.isBefore(d2)))
			return false;
		if (d2.isBefore(ref_time))
			throw new IllegalArgumentException(
					"We have two dates that are before ref_time, when at least one should be after it.");
		if (d2.isBefore(d1)) {
			throw new IllegalArgumentException(
					"d2 is before d1, which shouldn't ever happen. In compareDates, TemporalNext");
		}
		throw new IllegalArgumentException(
				"Problem! Shouldn't be in this else clause. Could be because "
						+ f
						+ " and "
						+ s
						+ " are equal. Should test for this in predicate that calls this function.");
	}


	private void testStoredDates() {
		if ((!this.first.getClass().toString().endsWith("TemporalDate"))
				|| (!this.second.getClass().toString().endsWith("TemporalDate")))
			throw new IllegalArgumentException(
					"The two parameters to TemporalNext aren't TemporalDate objects, which they should be.");
	}
}