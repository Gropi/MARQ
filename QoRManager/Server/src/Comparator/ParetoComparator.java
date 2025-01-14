package Comparator;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * This class allows you to have different comparator you want to take care of. You can add them by using
 * this comparator as a list. Each will be checked. They will be checked in a "Pareto"-way.
 * This means a solution is only better, if no other target is worse.
 * @param <T>
 */
public class ParetoComparator<T> extends
        LinkedList<Comparator<T>> implements Comparator<T> {

    /**
     * Compares each object with all comparators that are added to this list of comparators.
     * This method will return a 0 if at least on comparator returns a value that is worse than the current.
     * It returns 1 if
     * @param objectToCompare1
     * @param objectToCompare2
     * @return
     */
    public int compare(T objectToCompare1, T objectToCompare2) {
        double reference = 0;
        for (var comparator : this) {
            // Signum makes a high value to 1 or a high negative value to -1
            var signum = (double) Math.signum(comparator.compare(objectToCompare1, objectToCompare2));
            if (reference == 0d) {
                reference = signum;
            } else {
                if (signum * reference < 0) {
                    // one better, another worst : cannot decide
                    return 0;
                }
            }
        }
        return reference < 0d ? -1 : reference > 0d ? 1 : 0;
    }
}
