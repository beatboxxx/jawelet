package ru.ifmo.diplom.kirilchuk.jawelet.dwt.transforms;

import ru.ifmo.diplom.kirilchuk.jawelet.dwt.DecompositionResult;
import ru.ifmo.diplom.kirilchuk.jawelet.dwt.filters.AbstractFiltersFactory;
import ru.ifmo.diplom.kirilchuk.jawelet.dwt.transforms.strategies.impl.StrategiesFactory;
import ru.ifmo.diplom.kirilchuk.jawelet.util.Assert;
import ru.ifmo.diplom.kirilchuk.jawelet.util.MathUtils;
import ru.ifmo.diplom.kirilchuk.jawelet.util.extensioner.Extensioner;
import ru.ifmo.diplom.kirilchuk.jawelet.util.extensioner.actions.ZeroPaddingTo2Power;

/**
 * Abstract class that corresponds for providing basic implementation
 * of decomposition and reconstruction of discrete wavelet transform.
 *
 * @author Kirilchuk V.E.
 */
public abstract class DiscreteWaveletTransform {
    protected final AbstractFiltersFactory filtersFactory;
    private DWTransformStrategy strategy;

    /**
     * Constructs discrete wavelet transform with specified filters factory.
     *
     * @param filtersFactory factory of filters.
     */
    public DiscreteWaveletTransform(AbstractFiltersFactory filtersFactory) {
        Assert.argNotNull(filtersFactory);

        this.filtersFactory = filtersFactory;
        this.strategy = StrategiesFactory.getByName("default");
    }

    public void setTransformStrategy(DWTransformStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Performs dwt decomposition to maximal level. It means that
     * data length would be extended(if need) to closest 2^N
     * and decomposition result will have approximation vector with
     * size=1 and list with N detail vectors.
     *
     * @param data input vector to decompose.
     * @return result of decomposition.
     */
    public DecompositionResult decompose(double[] data) {
        Assert.argNotNull(data);
        Assert.argCondition(data.length >= 2, "Data length must be >= 2.");

        //finding closest 2nd power value
        int value = MathUtils.getClosest2PowerValue(data.length);
        int level = MathUtils.getExact2Power(value);

        //extending if need
        if(data.length - value < 0) {
            data = new Extensioner(data)
                    .schedule(new ZeroPaddingTo2Power(level))
                    .execute();
        }

        return decompose(data, level);
    }

    /**
     * Performs dwt decomposition to specified level. If level is
     * more than maximal level availiable for the data, then
     * the maximal level of decomposition will be returned.
     *
     * @param data input vector to decompose.
     * @param level level to which decompose.
     * @return result of decomposition.
     */
    public DecompositionResult decompose(double[] data, int level) {//TODO check can we have approximation of size 3?
        Assert.argNotNull(data);
        Assert.argCondition(level >= 1, "Level argument must be >= 1.");
        Assert.argCondition(data.length >= 2, "Data length must be >= 2.");

        DecompositionResult result = new DecompositionResult();
        double[] approximation;
        double[] details;
        for (int i = 1; i <= level; ++i) {
            approximation = decomposeLow(data);
            details = decomposeHigh(data);
            result.setApproximation(approximation);
            result.addDetails(details);
            result.setLevel(i);
            if(approximation.length == 1) {
                break; //we can`t decompose more...
            }
            data = approximation; //approximation is data for next decomposition
        }

        return result;
    }
    
    public double[] reconstruct(DecompositionResult decomposition) {
        return reconstruct(decomposition, 0);
    }

    public double[] reconstruct(DecompositionResult decomposition, int level) {
        if(level >= decomposition.getLevel()) {
            throw new IllegalArgumentException("Level must be less than decomposition level.");
        }

        double[] reconstructed = decomposition.getApproximation();
        for(int i = decomposition.getLevel(); i > level; --i) {
            reconstructed = reconstruct(reconstructed, decomposition.getDetailsList().get(i-1));
        }

        return reconstructed;
    }

    private double[] decomposeLow(double[] data) {
        return strategy.decomposeLow(data, filtersFactory.getLowDecompositionFilter());
    }

    private double[] decomposeHigh(double[] data) {
        return strategy.decomposeHigh(data, filtersFactory.getHighDecompositionFilter());
    }

    private double[] reconstruct(double[] approximation, double[] details) {
        return strategy.reconstruct(approximation, details,
                                    filtersFactory.getLowReconstructionFilter(),
                                    filtersFactory.getHighReconstructionFilter());
    }

    public AbstractFiltersFactory getFiltersFactory() {
        return filtersFactory;
    }
}
