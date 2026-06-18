package com.fairshare.distributed_expense_splitter.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import org.openapitools.model.ExpenseSplitDTO;

public class ExpenseSplitter {

    public static List<Double> splitEvenlyWithCurrency(double totalAmount, int numPeople, String currencyCode) {
        if (numPeople <= 0) {
            throw new IllegalArgumentException("Number of people must be greater than zero.");
        }

        // currency's minor unit decimal places (e.g., USD=2, JPY=0, KWD=3)
        Currency currency = Currency.getInstance(currencyCode.toUpperCase());
        int fractionDigits = currency.getDefaultFractionDigits();
        long multiplier = (long) Math.pow(10, fractionDigits);

        long totalMinorUnits = Math.round(totalAmount * multiplier);
        long baseShare = totalMinorUnits / numPeople;
        long leftoverUnits = totalMinorUnits % numPeople;

        List<Long> individualShares = new ArrayList<>();

        for (int i = 0; i < numPeople; i++) {
            individualShares.add(baseShare);
            if (i < leftoverUnits) {
                individualShares.set(i, individualShares.get(i) + 1);
            }
        }

        Collections.shuffle(individualShares);

        // 6. Convert back to standard decimal doubles for database/DTOs
        List<Double> finalSplits = new ArrayList<>();
        for (long minorUnits : individualShares) {
            finalSplits.add(minorUnits / (double) multiplier);
        }

        return finalSplits;
    }

    public static List<Double> splitByPercentage(double totalAmount, List<Double> percentages, String currencyCode) {
        if (percentages == null || percentages.isEmpty()) {
            throw new IllegalArgumentException("Percentages list cannot be empty.");
        }

        // add up to exactly 100%?
        double sumOfPercentages = percentages.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sumOfPercentages - 100.0) > 0.001) {
            throw new IllegalArgumentException(
                    "Percentages must sum up to exactly 100%. Current sum: " + sumOfPercentages);
        }

        // currency multiplier
        Currency currency = Currency.getInstance(currencyCode.toUpperCase());
        int fractionDigits = currency.getDefaultFractionDigits();
        long multiplier = (long) Math.pow(10, fractionDigits);

        long totalMinorUnits = Math.round(totalAmount * multiplier);

        List<Long> individualSharesInCents = new ArrayList<>();
        double currentPercentageSum = 0.0;
        long assignedMinorUnits = 0;

        for (int i = 0; i < percentages.size(); i++) {
            currentPercentageSum += percentages.get(i);

            long totalTargetCentsForStep = Math.round((currentPercentageSum * totalMinorUnits) / 100.0);
            long individualShare = totalTargetCentsForStep - assignedMinorUnits;
            individualSharesInCents.add(individualShare);
            assignedMinorUnits += individualShare;
        }

        List<Double> finalSplits = new ArrayList<>();
        for (long minorUnits : individualSharesInCents) {
            finalSplits.add(minorUnits / (double) multiplier);
        }

        return finalSplits;
    }

    public static List<Double> splitByShare(double totalAmount, List<Integer> shares, String currencyCode) {
        int totalShares = shares.stream().mapToInt(Integer::intValue).sum();
        if (totalShares <= 0) {
            throw new IllegalArgumentException("Total shares must be greater than zero.");
        }

        Currency currency = Currency.getInstance(currencyCode.toUpperCase());
        int fractionDigits = currency.getDefaultFractionDigits();
        long multiplier = (long) Math.pow(10, fractionDigits);

        long totalMinorUnits = Math.round(totalAmount * multiplier);

        List<Long> individualSharesInCents = new ArrayList<>();
        int currentShareSum = 0;
        long assignedMinorUnits = 0;

        for (int i = 0; i < shares.size(); i++) {
            int userShares = shares.get(i);
            currentShareSum += userShares;

            long totalTargetUnitsForStep = Math.round(((double) currentShareSum * totalMinorUnits) / totalShares);
            long individualShare = totalTargetUnitsForStep - assignedMinorUnits;

            individualSharesInCents.add(individualShare);

            assignedMinorUnits += individualShare;
        }

        List<Double> finalSplits = new ArrayList<>();
        for (long minorUnits : individualSharesInCents) {
            finalSplits.add(minorUnits / (double) multiplier);
        }

        return finalSplits;
    }

}
