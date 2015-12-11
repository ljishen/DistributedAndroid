package org.droidphy.core.network.raft.state;

import org.robotninjas.barge.state.AbstractListenersModule;
import org.robotninjas.barge.state.RaftProtocolListener;
import org.robotninjas.barge.state.StateTransitionListener;

import java.util.List;

public class EventListenersModule extends AbstractListenersModule {

    private final List<StateTransitionListener> transitionListeners;
    private final List<RaftProtocolListener> protocolListeners;

    public EventListenersModule(List<StateTransitionListener> transitionListeners,
                                List<RaftProtocolListener> protocolListeners) {
        this.transitionListeners = transitionListeners;
        this.protocolListeners = protocolListeners;
    }

    @Override
    protected void configureListeners() {

        for (StateTransitionListener listener : transitionListeners) {
            bindTransitionListener().toInstance(listener);
        }

        for (RaftProtocolListener protocolListener : protocolListeners) {
            bindProtocolListener().toInstance(protocolListener);
        }

    }
}
