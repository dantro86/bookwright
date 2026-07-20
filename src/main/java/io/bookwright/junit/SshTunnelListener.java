package io.bookwright.junit;

import io.bookwright.db.DbPool;
import io.bookwright.db.SshTunnel;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Run-level lifecycle for the DB stack. The tunnel and pool open lazily on
 * first DB access (API/UI-only runs never touch SSH); this listener guarantees
 * they are shut down when the test plan finishes.
 */
public class SshTunnelListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        DbPool.shutdown();
        SshTunnel.close();
    }
}
