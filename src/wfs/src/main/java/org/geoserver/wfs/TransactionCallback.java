/* (c) 2017 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.util.List;

public interface TransactionCallback {

    default TransactionContext beforeHandlerExecution(TransactionContext context) {
        // by default nothing is done
        return context;
    }

    default TransactionContext beforeInsertFeatures(TransactionContext context) {
        // by default nothing is done
        return context;
    }

    default TransactionContext beforeUpdateFeatures(TransactionContext context) {
        // by default nothing is done
        return context;
    }

    default TransactionContext beforeDeleteFeatures(TransactionContext context) {
        // by default nothing is done
        return context;
    }

    default TransactionContext beforeReplaceFeatures(TransactionContext context) {
        // by default nothing is done
        return context;
    }

    @FunctionalInterface
    interface Executor {
        TransactionContext apply(TransactionCallback callback, TransactionContext context);
    }

    static TransactionContext executeCallbacks(TransactionContext context,
                                               List<TransactionCallback> callbacks,
                                               Executor executor) {
        for (TransactionCallback callback : callbacks) {
            context = executor.apply(callback, context);
        }
        return context;
    }
}
