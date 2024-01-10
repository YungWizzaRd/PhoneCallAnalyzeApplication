package com.example.analyzer;



import java.util.Comparator;
import java.util.List;

public class Filter {

    // Sort by Count from Low to High
    public static void sortByCountLowToHigh(List<ContactStatistics> data) {
        data.sort(Comparator.comparingInt(ContactStatistics::getCallCount));
    }

    // Sort by Count from High to Low
    public static void sortByCountHighToLow(List<ContactStatistics> data) {
        data.sort((c1, c2) -> Integer.compare(c2.getCallCount(), c1.getCallCount()));
    }

    // Sort by Total Duration from Low to High
    public static void sortByTotalDurationLowToHigh(List<ContactStatistics> data) {
        data.sort(Comparator.comparingLong(ContactStatistics::getTotalDuration));
    }

    // Sort by Total Duration from High to Low
    public static void sortByTotalDurationHighToLow(List<ContactStatistics> data) {
        data.sort((c1, c2) -> Long.compare(c2.getTotalDuration(), c1.getTotalDuration()));
    }
}
