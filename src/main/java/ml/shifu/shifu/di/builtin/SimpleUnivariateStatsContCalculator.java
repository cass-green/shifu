package ml.shifu.shifu.di.builtin;

import com.sun.xml.internal.rngom.digested.DDataPattern;
import ml.shifu.shifu.container.NumericalValueObject;
import ml.shifu.shifu.container.obj.ColumnBinningResult;
import ml.shifu.shifu.core.QuantileCalculator;
import ml.shifu.shifu.util.CommonUtils;
import ml.shifu.shifu.util.PMMLUtils;
import ml.shifu.shifu.util.Params;
import org.dmg.pmml.ContStats;
import org.dmg.pmml.Interval;
import org.dmg.pmml.NumericInfo;
import org.dmg.pmml.UnivariateStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimpleUnivariateStatsContCalculator {
    private Logger log = LoggerFactory.getLogger(SimpleUnivariateStatsContCalculator.class);

    private NumericInfo numericInfo = new NumericInfo();
    private ContStats contStats = new ContStats();


    public void calculate(UnivariateStats univariateStats, List<? extends Object> values, Params params) {


        calculateBasicStats(values);


        univariateStats.setNumericInfo(numericInfo);
        univariateStats.setContStats(contStats);

        return;
    }

    private void calculateBasicStats(List<? extends Object> values) {

        Double sum;
        Double squaredSum;
        Double min = Double.MAX_VALUE;
        Double max = -Double.MAX_VALUE;
        Double mean = Double.NaN;
        Double stdDev = Double.NaN;
        Double median = Double.NaN;

        Double EPS = 1e-6;

        sum = 0.0;
        squaredSum = 0.0;


        List<Double> validValues = new ArrayList<Double>();

        for (Object valueObject : values) {

            if (CommonUtils.isValidNumber(valueObject)) {

                Double value = Double.valueOf(valueObject.toString());

                max = Math.max(max, value);
                min = Math.min(min, value);

                sum += value;
                squaredSum += value * value;

                validValues.add(value);
            }
        }


        numericInfo.setMaximum(max);
        numericInfo.setMinimum(min);


        int validSize = validValues.size();
        // mean and stdDev defaults to NaN
        if (validSize == 0 || sum.isInfinite() || squaredSum.isInfinite()) {
            return;
        }
        Collections.sort(validValues);

        //it's ok while the voList is sorted;
        median = validValues.get(validSize/ 2);

        mean = sum / validSize;
        stdDev = Math.sqrt((squaredSum - (sum * sum) / validSize + EPS)
                / (validSize - 1));

        Double interQuartileRange = validValues.get((int)Math.floor(validSize * 0.75)) - validValues.get((int)Math.floor(validSize * 0.25));

        numericInfo.setMean(mean);
        numericInfo.setStandardDeviation(stdDev);
        numericInfo.setMedian(median);
        numericInfo.setInterQuartileRange(interQuartileRange);

        QuantileCalculator quantileCalculator = new QuantileCalculator();





        numericInfo.withQuantiles(quantileCalculator.getEvenlySpacedQuantiles(validValues, 11));

        contStats.setTotalValuesSum(sum);
        contStats.setTotalSquaresSum(squaredSum);

        return;

    }



}
