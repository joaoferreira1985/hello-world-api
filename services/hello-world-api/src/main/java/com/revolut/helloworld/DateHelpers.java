package com.revolut.helloworld;

import io.vertx.ext.web.RoutingContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateHelpers {

    public static int calculateDaysToBirthday(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        LocalDate birthday = LocalDate.of(birthDate.getYear(), birthDate.getMonth(), birthDate.getDayOfMonth());

        if (today.getMonth() == birthday.getMonth() && today.getDayOfMonth()== birthday.getDayOfMonth()){
            return 0; // Today Happy birthday!
        }
        LocalDate nextBDay = birthday.withYear(today.getYear());

        //If your birthday has occurred this year already, add 1 to the year.
        if (nextBDay.isBefore(today) || nextBDay.isEqual(today)) {
            nextBDay = nextBDay.plusYears(1);
        }

        Period p = Period.between(today, nextBDay);
        long p2 = ChronoUnit.DAYS.between(today, nextBDay);
        System.out.println("There are " + p.getMonths() + " months, and " + p.getDays() + " days until your next birthday. (" + p2 + " total)");
        return  (int)p2;
    }
    public static boolean isDateBeforeTheToday(RoutingContext rc)  {
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd");
        try {
            return new Date().before(format.parse(rc.getBodyAsJson().getString("dateOfBirth")));
        } catch (ParseException e) {
            return false;
        }
    }
}
