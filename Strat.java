package planetwars.strategies;

import planetwars.core.*;
import planetwars.publicapi.*;
import java.util.*;

public class Strat implements IStrategy{

    private HashMap<Integer,HashMap<Integer[],Double>> moveRanks;
    private List<IVisiblePlanet> ownedPlanets;
    private int count = 0;

    @Override
    public void takeTurn(List<IPlanet> planets, IPlanetOperations planetOperations, Queue<IEvent> eventsToExecute) {
        System.out.println(count);
        count++;
        updateOwnedPlanets(planets);
        updatePossibleMoves(planets);
        rankMoves();
        Iterator<IEvent> events = getIdealEvents(planetOperations);
        while(events.hasNext()){
            eventsToExecute.add(events.next());
        }
    }

    @Override
    public String getName() {
        return "Strat";
    }

    @Override
    public boolean compete() {
        return false;
    }


    private Iterator<IEvent> getIdealEvents(IPlanetOperations planetOperations){

        List<IEvent> events = new LinkedList<>();
        int curPlanetID;
        for(HashMap.Entry<Integer,HashMap<Integer[],Double>> planet :moveRanks.entrySet()){

            curPlanetID = planet.getKey();
            double max = 1.0;
            //Stores move with highest rank
            for(HashMap.Entry<Integer[],Double> move :moveRanks.get(curPlanetID).entrySet()) {
                if (move.getValue() > max) {
                    max = move.getValue();
                }
            }
            //Iterates again to create event for every event that has max rank
            for(HashMap.Entry<Integer[],Double> move :moveRanks.get(curPlanetID).entrySet()) {
                // Also checks for no ideal moves
                if (move.getValue() == max && max <= 1) {


                    long numPeople = (move.getKey()[1] / 4 * getOwnedPlanet(curPlanetID).getPopulation());
                    IPlanet to = getOwnedPlanet(move.getKey()[0]);
                    IPlanet from = getOwnedPlanet(curPlanetID);
                    IEvent e = planetOperations.transferPeople(from, to, numPeople);
                    //Adds event to list so it can be added to queue during turn
                    events.add(e);
                }
            }
        }
        return events.iterator();
    }


    private void changeRank(int ID, Integer[] move,double factor){

        HashMap<Integer[],Double> planetsMoves = moveRanks.get(ID);
        double updatedFitness = planetsMoves.get(move) * factor;
        planetsMoves.replace(move,updatedFitness);
        moveRanks.replace(ID,planetsMoves);

    }


    private void rankMoves(){


    }


    private void updatePossibleMoves(List<IPlanet> planets){

        moveRanks = new HashMap<>(ownedPlanets.size());

        for(IVisiblePlanet planet: ownedPlanets){
            HashMap<Integer[],Double> curPlanet = new HashMap<>();
            for(IEdge edge: planet.getEdges()){
                for(int i=1; i<4;i++){
                    Integer[] a = {edge.getDestinationPlanetId(),i};
                    curPlanet.put(a,1.0);
                }
            }
            moveRanks.put(planet.getId(),curPlanet);
        }
    }


    public void updateOwnedPlanets(List<IPlanet> planets) {

        ownedPlanets = new ArrayList<>();

        for(IPlanet planet : planets) {
            if (planet instanceof IVisiblePlanet && ((IVisiblePlanet) planet).getOwner() == Owner.SELF) {
                ownedPlanets.add((IVisiblePlanet) planet);
            }
        }
    }

    private IVisiblePlanet getOwnedPlanet(int ID){
        for(IVisiblePlanet p: ownedPlanets){
            if(p.getId() == ID){
                return p;
            }
        }
        System.out.println("Error");
        return null;
    }

    public boolean nextAbove(IVisiblePlanet planet) {
        double pop = planet.getPopulation();
        double h = planet.getHabitability();
        double m = planet.getSize();

        pop = pop * (1 + (h / 100));
        return pop >= m;
    }


    public double aboveMax(IVisiblePlanet planet) {
        double pop = planet.getPopulation();
        double h = planet.getHabitability();
        double m = planet.getSize();

        pop = pop * (1 + (h / 100));
        double ret = pop - m;
        return ret;
    }


    private double getNetPop(IVisiblePlanet planet, int numTurns) {
        List<IShuttle> shuttle = planet.getIncomingShuttles();
        int turns;
        int temp = 0;
        double pop;
        double ret = 0;
        int ind=0;

        if(planet.getOwner() == Owner.SELF) {
            turns = 100;
            pop = planet.getPopulation();


            if(shuttle.size() != 0) {
                while (shuttle.size() > 0) {
                    for (int i = 0; i < shuttle.size(); i++) {
                        if (shuttle.get(i).getTurnsToArrival() < turns) {
                            turns = shuttle.get(i).getTurnsToArrival();
                            ind = i;
                        }
                    }

                    // We now have the minimum number of turns to the next arrival, need to see who owns it to proceed
                    if (shuttle.get(ind).getOwner() == Owner.SELF && shuttle.get(ind).getTurnsToArrival() <= numTurns) {
                        int h = planet.getHabitability();
                        int max = (int) planet.getSize();
                        long incoming = shuttle.get(ind).getNumberPeople();
                        for (int i = 0; i < turns - temp; i++) { // temp is a counter of how many steps have already been taken
                            pop = pop * (1 + (h / 100));
                            if (pop >= max) {
                                pop = pop - ((pop - max) * 0.1);
                            }
                        }
                        pop += incoming;
                        // subtract number of turns
                        temp += turns; // updates the counter

                    } else if(shuttle.get(ind).getTurnsToArrival() <= numTurns){
                        int h = planet.getHabitability();
                        int max = (int) planet.getSize();
                        long incoming = shuttle.get(ind).getNumberPeople();
                        for (int i = 0; i < turns - temp; i++) { // temp is a counter of how many steps have already been taken
                            pop = pop * (1 + (h / 100));
                            if (pop >= max) {
                                pop = pop - ((pop - max) * 0.1);
                            }
                        }
                        pop -= incoming;
                        temp += (turns-temp);
                    }
                    shuttle.remove(ind);
                    turns = 100;
                }
            }

            ret = pop;

        } else if (planet.getOwner() == Owner.OPPONENT) {
            turns = 100;
            pop = -planet.getPopulation();
            while(shuttle.size() > 0) {

                for (int i = 0; i < shuttle.size(); i++) {
                    if (shuttle.get(i).getTurnsToArrival() < turns) {
                        turns = shuttle.get(i).getTurnsToArrival();
                        ind = i;
                    }
                } // grabs index of minimum turns
                    
                int h = planet.getHabitability();
                int max = (int) planet.getSize();
                long incoming = shuttle.get(ind).getNumberPeople();

                for (int i = 0; i < turns - temp; i++) { // temp is a counter of how many steps have already been taken
                    pop = pop * (1 + (h / 100));
                    if (pop >= max) {
                        pop = pop - ((pop - max) * 0.1);
                    }
                } // updated values of the population on the planet with growth/decay

                if (shuttle.get(ind).getOwner() == Owner.OPPONENT && shuttle.get(ind).getTurnsToArrival() <= numTurns) {
                    pop -= shuttle.get(ind).getNumberPeople();
                } else if (shuttle.get(ind).getTurnsToArrival() <= numTurns) { // Owner is we
                    pop += incoming;
                }
                temp += (turns - temp);
                shuttle.remove(ind);

            }
            ret = pop;
        }
        return ret;
    }

    public static void main(String[] args){

    }
    
}
