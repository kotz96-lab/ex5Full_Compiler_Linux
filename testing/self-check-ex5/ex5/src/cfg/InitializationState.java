package cfg;

import java.util.*;

public class InitializationState {
    private Set<String> initialized;

    public InitializationState() {
        this.initialized = new HashSet<>();
    }

    public InitializationState(InitializationState other) {
        this.initialized = new HashSet<>(other.initialized);
    }

    public boolean isInitialized(String variable) {
        return initialized.contains(variable);
    }

    public void markInitialized(String variable) {
        initialized.add(variable);
    }

    public Set<String> getInitialized() {
        return new HashSet<>(initialized);
    }

    public InitializationState meet(InitializationState other) {
        InitializationState result = new InitializationState();
        result.initialized.addAll(this.initialized);
        result.initialized.retainAll(other.initialized);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InitializationState that = (InitializationState) obj;
        return Objects.equals(initialized, that.initialized);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialized);
    }

    @Override
    public String toString() {
        return "Init{" + initialized + "}";
    }
}
