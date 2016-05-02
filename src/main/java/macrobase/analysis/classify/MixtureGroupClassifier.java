package macrobase.analysis.classify;

import macrobase.analysis.pipeline.operator.MBStream;
import macrobase.analysis.result.OutlierClassificationResult;
import macrobase.analysis.stats.mixture.BatchMixtureModel;
import macrobase.conf.ConfigurationException;
import macrobase.conf.MacroBaseConf;
import macrobase.datamodel.Datum;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Tags points of the group which has it's center closest to TARGET_GROUP location as outliers.
 * Assumes that the input data already has probabilities corresponding to each of the groups as their metrics.
 */
public class MixtureGroupClassifier implements OutlierClassifier {
    private static final Logger log = LoggerFactory.getLogger(MixtureGroupClassifier.class);
    private final RealVector targetLocation;
    private final BatchMixtureModel mixtureModel;

    MBStream<OutlierClassificationResult> results = new MBStream<>();

    public MixtureGroupClassifier(MacroBaseConf conf, BatchMixtureModel mixtureModel) throws ConfigurationException {
        List<Double> list = conf.getDoubleList(MacroBaseConf.TARGET_GROUP);
        Double[] array = new Double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        this.targetLocation = new ArrayRealVector(array);
        this.mixtureModel = mixtureModel;
    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void consume(List<Datum> records) throws Exception {
        log.debug("got {} records", records.size());
        List<RealVector> clusters = mixtureModel.getClusterCenters();
        int K = clusters.size();
        log.debug("cluster center are: {}", clusters);
        log.debug("target cluster is: {}", targetLocation);
        int targetClusterIndex = -1;
        double distanceToClosestCluster = Double.MAX_VALUE;
        for (int i = 0; i < K; i++) {
            double dist = clusters.get(i).getDistance(targetLocation);
            if (dist < distanceToClosestCluster) {
                distanceToClosestCluster = dist;
                targetClusterIndex = i;
            }
        }
        log.debug("target cluster index is {}", targetClusterIndex);
        for (Datum d : records) {
            boolean isOutlier = true;
            for (int i =0 ; i< K; i++) {
                if (d.getMetrics().getEntry(i) > d.getMetrics().getEntry(targetClusterIndex)) {
                    isOutlier = false;
                    break;
                }
            }
            results.add(new OutlierClassificationResult(d, isOutlier));
        }
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public MBStream<OutlierClassificationResult> getStream() throws Exception {
        return results;
    }
}
