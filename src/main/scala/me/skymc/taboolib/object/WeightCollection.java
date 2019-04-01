package me.skymc.taboolib.object;

import me.skymc.taboolib.other.NumberUtils;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author sky
 * @Since 2018-05-07 16:18
 */
@ThreadSafe
public class WeightCollection<A> {

    private final List<WeightObject> weightList = new CopyOnWriteArrayList<>();

    public int size() {
        return weightList.size();
    }

    public List<WeightObject> getWeightList() {
        return weightList;
    }

    public void add(int weightNumber, A weightObject) {
        weightList.add(new WeightObject(weightNumber, weightObject));
    }

    public void remove(WeightObject weightObject) {
        weightList.remove(weightObject);
    }

    @Nullable
    public WeightObject getWeight() {
        int weightSum = weightList.stream().mapToInt(WeightObject::getWeightNumber).sum();
        if (weightSum > 0) {
            Integer m = 0, n = NumberUtils.getRandom().nextInt(weightSum);
            for (WeightObject weightObject : weightList) {
                if (m <= n && n < m + weightObject.getWeightNumber()) {
                    return weightObject;
                }
                m += weightObject.getWeightNumber();
            }
        }
        return null;
    }

    public static class WeightObject<B> {

        private int weightNumber;
        private B weightObject;

        public WeightObject(int weightNumber, B weightObject) {
            this.weightNumber = weightNumber;
            this.weightObject = weightObject;
        }

        public int getWeightNumber() {
            return weightNumber;
        }

        public void setWeightNumber(int weightNumber) {
            this.weightNumber = weightNumber;
        }

        public B getWeightObject() {
            return weightObject;
        }

        public void setWeightObject(B weightObject) {
            this.weightObject = weightObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WeightObject)) {
                return false;
            }
            WeightObject that = (WeightObject) o;
            return getWeightNumber() == that.getWeightNumber() && Objects.equals(getWeightObject(), that.getWeightObject());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getWeightNumber(), getWeightObject());
        }

        @Override
        public String toString() {
            return "weightNumber=" + "WeightObject{" + weightNumber + ", weightObject=" + weightObject + '}';
        }
    }
}
