package template;

import logist.topology.Topology.City;

public class State {
    private City currentCity;
    private City futureCity;

    public State(City from) {
        this.currentCity = from;
        this.futureCity = from;
    }

    public State(City from, City to) {
        this.currentCity = from;
        this.futureCity = to;
    }

    public City getCurrentCity() { return this.currentCity; }
    public City getFutureCity() { return this.futureCity; }
    public boolean notask() { return (this.currentCity == this.futureCity); }
}
