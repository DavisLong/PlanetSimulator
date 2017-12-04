package planetwars.strategies;

import planetwars.core.*;
import planetwars.publicapi.*;
import java.util.*;

public class Strat implements IStrategy{

    @Override
    public void takeTurn(List<IPlanet> planets, IPlanetOperations planetOperations, Queue<IEvent> eventsToExecute) {
        HashMap<Double, IEvent> moves = new HashMap<>();
        

    }

    @Override
    public String getName() {
        return "Strat";
    }

    @Override
    public boolean compete() {
        return false;
    }

    public double getNetPop(IVisiblePlanet planet, int numTurns) {
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
                        temp += turns;
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
                temp += turns;
                shuttle.remove(ind);

            }
            ret = pop;
        }
        return ret;
    }

    
}
