package com.nike.mm.service.impl

import com.nike.mm.service.IUtilitiesService
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


@Component
@Slf4j
class UtilitiesService implements IUtilitiesService {

    @Override
    String getPathFromUrl(url) {
        return url.replaceAll("http[s]*://[^/]+", "")
    }

    @Override
    String cleanFullBuildName(fullBuildName) {
        return fullBuildName.replaceAll(/\s#[0-9]+$/, "")
    }

    @Override
    String cleanEmail(author) {
        def result
        if (author) {
            result = author.replace("@", "_")
        }
        return result
    }

    @Override
    String cleanPersonName(name) {
        def result = "system_ghost"
        if(name) {
            name = name.replaceAll(~/\((ETW|etw)\)/, '').trim()
            if (name ==~ /[a-zA-Z0-9]+.[a-zA-Z0-9]+@[a-z]+.com/) { //this would be an email
                result = name.split('@')[0].replace('.', '_')
            } else if (name ==~ /[A-Za-z]+,\s[A-Za-z]+/) { //this would be last name, first name
                result = name.split(" ")[1] + "_" + name.split(" ")[0].replace(',', '')
            } else if (name ==~ /[A-Za-z]+\s[A-Za-z]+/) {
                result = name.split(" ")[0] + "_" + name.split(" ")[1]
            } else {
                result = name
            }
            result = result.replaceAll(~/[-\(\)\,\s]/, '_')
        }
        return result
    }

    @Override
    String makeNonTokenFriendly(userName) {
        def result
        if (userName) {
            result = userName.replace("_etw", "").replace("_whq", "").replace(" ", "_").replace("(", "").replace(")",
                    "").replace(",", "").replace("-", "")
        }
        return result
    }

    @Override
    Date cleanJiraDate(date) {
        def result
        if (date) {
            result = date.replace("T", "")
            result = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSSZ").parse(result)
        }
        return result
    }

    @Override
    Date cleanGithubDate(date) {
        def result
        if (date) {
            result = date.replace("T", "")
            result = new SimpleDateFormat("yyyy-MM-ddHH:mm:ssz").parse(result)
        }
        return result
    }

    @Override
    def estimateHealth(estimate, actualTime, maxEstimate, maxTime, estimationValues) {
        if (!estimationValues) {
            estimationValues = [1, 2, 3, 5, 8, 13]
        }
        def result
        def timeEstimateRatio = maxTime / maxEstimate
        def estimateTime = estimate * timeEstimateRatio
        def upperTimeBound = maxTime
        def lowerTimeBound = 0

        log.debug("estimate: $estimate | estimateTime: $estimateTime | actualTime: $actualTime")

        def currentEstimateIndex = estimationValues.findIndexOf { it == estimate }
        //calculate the lowerTimeBound
        if (currentEstimateIndex == 0) {
            lowerTimeBound = 0
        } else {
            lowerTimeBound = estimateTime - ((estimateTime - (estimationValues[estimationValues.findIndexOf {
                it == estimate
            } - 1] * timeEstimateRatio)) / 2)
        }
        //calculate the upperTimeBound
        if (currentEstimateIndex == estimationValues.size() - 1) {
            upperTimeBound = maxTime
        } else {
            upperTimeBound = estimateTime + (((estimationValues[estimationValues.findIndexOf {
                it == estimate
            } + 1] * timeEstimateRatio) - estimateTime) / 2)
        }

        //Calculate the result
        if (upperTimeBound < actualTime) {   //this took longer then it should have, it was underestimated
            def diff = actualTime - upperTimeBound
            result = 0 + diff
            //TODO normalize the number
        } else if (lowerTimeBound > actualTime) { //this took less time then estimated, it was overestimated
            def diff = lowerTimeBound - actualTime
            result = 0 - diff
            //TODO normalize the number
        } else {
            result = 0
        }
        return [raw: result, result: result.toInteger()]
    }

    @Override
    double getDifferenceBetweenDatesInHours(firstDate, secondDate) {
        def val

        if (!secondDate) { //if there is no second date, make it today
            secondDate = new Date()
        }
        def second = new Date(secondDate)
        def first = new Date(firstDate)
        def diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(second.getTime() - first.getTime())
        val = diffInMinutes / 60

        return val
    }

    @Override
    Date convertTimestampFromString(timestamp) {
        if (timestamp) {
            return new Date((long) timestamp)
        } else {
            return null
        }
    }
}
