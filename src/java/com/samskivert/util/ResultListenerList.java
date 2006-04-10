//
// $Id$

package com.samskivert.util;

/**
 * Multiplexes ResultListener responses to multiple ResultListeners.
 */
public class ResultListenerList extends ObserverList<ResultListener>
    implements ResultListener
{
    /**
     * Create a ResultListenerList with the FAST_UNSAFE_NOTIFY policy.
     */
    public ResultListenerList ()
    {
        super(FAST_UNSAFE_NOTIFY);
    }

    /**
     * Create a ResultListenerList with your own notifyPolicy.
     */
    public ResultListenerList (int notifyPolicy)
    {
        super(notifyPolicy);
    }

    /**
     * Multiplex a requestCompleted response to all the ResultListeners in
     * this list.
     */
    public void requestCompleted (final Object result)
    {
        apply(new ObserverOp<ResultListener>() {
            public boolean apply (ResultListener observer) {
                observer.requestCompleted(result);
                return true;
            }
        });
    }

    /**
     * Multiplex a requestFailed response to all the ResultListeners in
     * this list.
     */
    public void requestFailed (final Exception cause)
    {
        apply(new ObserverOp<ResultListener>() {
            public boolean apply (ResultListener observer) {
                observer.requestFailed(cause);
                return true;
            }
        });
    }
}