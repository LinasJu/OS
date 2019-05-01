package lt.mif.vu.UI.Observer;
/**
 * This interface is used in combination with JKVObservable
 */
public interface JKVObserver {

    String KVO_WILL_CHANGE = "will.change";
    String KVO_DID_CHANGE = "did.change";

    /**
     * This method is triggered if an observed value will/did change
     *
     * @param value  The observed value
     * @param tag    The observing identifier
     * @param change can either be KVO_WILL_CHANGE or KVO_DID_CHANGE
     */
    void observeValue(Object value, String tag, String change);
}
