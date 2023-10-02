package uk.gov.companieshouse.company_appointments.model.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uk.gov.companieshouse.company_appointments.exception.FailedToTransformException;

public interface Transformative<S, T> {
    /**
     * Creates a new instance of the output type required by the {@link #transform(Object, Object)} default method.
     * <p>
     * Java uses type erasure for generics. So you cannot create a new instance of a generic class
     * unless you have an instance already, or the class file.
     * This makes the transform default method impossible.
     * This method creates new instances of the output type for that method;
     * </p>
     * <p>
     * Usually the implementation of this method will simply be:
     * <pre>{@code
     * @Override
     * MyTargetClass factory() {
     *   return new MyTargetClass();
     * }
     * }</pre>
     * <p>
     * for class
     * <pre>{@code
     * class MyTransform implements Transformative<MySourceClass, MyTargetClass>
     * }
     * </pre>
     * </p>
     *
     * @return new instance of the output type T
     */
    T factory();

    default T transform(S source) throws FailedToTransformException {
        T target = transform(source, factory());
        if (target.equals(factory())) {
            target = null;
        }
        return target;
    }

    T transform(S source, T output) throws FailedToTransformException;

    default List<T> transform(Collection<S> sources) throws FailedToTransformException {
        List<T> list = new ArrayList<>();
        for (S source : sources) {
            T transform = transform(source);
            list.add(transform);
        }
        return list;
    }
}
