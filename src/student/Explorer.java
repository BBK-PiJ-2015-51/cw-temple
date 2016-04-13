package student;

import game.EscapeState;
import game.ExplorationState;
import game.Node;
import game.NodeStatus;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.Stack;

import java.util.Collection;

public class Explorer {

    private class explorerNode {

        long[] neighbours = null;

    }


    /**
     * Explore the cavern, trying to find the orb in as few steps as possible.
     * Once you find the orb, you must return from the function in order to pick
     * it up. If you continue to move after finding the orb rather
     * than returning, it will not count.
     * If you return from this function while not standing on top of the orb,
     * it will count as a failure.
     * <p>
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the orb in fewer steps.
     * <p>
     * At every step, you only know your current tile's ID and the ID of all
     * open neighbor tiles, as well as the distance to the orb at each of these tiles
     * (ignoring walls and obstacles).
     * <p>
     * To get information about the current state, use functions
     * getCurrentLocation(),
     * getNeighbours(), and
     * getDistanceToTarget()
     * in ExplorationState.
     * You know you are standing on the orb when getDistanceToTarget() is 0.
     * <p>
     * Use function moveTo(long id) in ExplorationState to move to a neighboring
     * tile by its ID. Doing this will change state to reflect your new position.
     * <p>
     * A suggested first implementation that will always find the orb, but likely won't
     * receive a large bonus multiplier, is a depth-first search.
     *
     * @param state the information available at the current state
     */
    public void explore(ExplorationState state) {

        Map<Long,Collection<NodeStatus>> mazeMap = new HashMap<>();

        Stack<Long> previousSquare = new Stack<Long>();
        while (state.getDistanceToTarget() > 0) {
            //create mazeMap
            Long centre = state.getCurrentLocation();
            Collection<NodeStatus> neighbours = state.getNeighbours();
            mazeMap.put(centre,neighbours);

            ArrayList<Long> squaresNotBeenTo = new ArrayList();
            ArrayList<NodeStatus> nodesNotVisited = new ArrayList();
            NodeStatus visitNext = null;

            //create a list of neighbours not been to
            for (NodeStatus n: neighbours) {
                if (!mazeMap.containsKey(n.getId())) {
                    squaresNotBeenTo.add(n.getId());
                    nodesNotVisited.add(n);
                }
            }

            //choose to go to the square that has the shortest distance first
            System.out.println("centre" + centre);
            if (!nodesNotVisited.isEmpty()) {
                for (NodeStatus n: nodesNotVisited) {

                    System.out.println("Id" + n.getId());
                    System.out.println("distance" + n.getDistanceToTarget());

                    if (visitNext == null ) {
                        visitNext = n;
                    } else if (n.getDistanceToTarget() < visitNext.getDistanceToTarget()) {
                        visitNext = n;
                    }
                }

                System.out.println("moving to" + visitNext.getId());


                state.moveTo(visitNext.getId());
                previousSquare.push(centre);
            } else {
                state.moveTo(previousSquare.pop());
            }

           /*

            //choose a square RANDOM to go to from those not yet been to
            if (!squaresNotBeenTo.isEmpty()) {
                Random r = new Random();
                int i = r.nextInt(squaresNotBeenTo.size());
                long nextSquare = squaresNotBeenTo.get(i);
                state.moveTo(nextSquare);
                previousSquare.push(centre);
            } else {
                //if there are no immediate squares to move to trace back through previously visited squares
                state.moveTo(previousSquare.pop());
            }

*/


        }


    }


    public void exploreRandom(ExplorationState state) {
        System.out.println("this is my start location: " + state.getCurrentLocation());
        while (state.getDistanceToTarget() > 0) {
            Collection<NodeStatus> neighbours = state.getNeighbours();
            System.out.println("this is my current location: " + state.getCurrentLocation());
            long[] temp = new long[neighbours.size()];
            int i = 0;
            for (NodeStatus n: neighbours) {
                temp[i] = n.getId();
                System.out.println(n.getId());
                i++;
            }
            Random r = new Random();
            int n = r.nextInt(temp.length);
            state.moveTo(temp[n]);
        }
    }

    /**
     * Escape from the cavern before the ceiling collapses, trying to collect as much
     * gold as possible along the way. Your solution must ALWAYS escape before time runs
     * out, and this should be prioritized above collecting gold.
     * <p>
     * You now have access to the entire underlying graph, which can be accessed through EscapeState.
     * getCurrentNode() and getExit() will return you Node objects of interest, and getVertices()
     * will return a collection of all nodes on the graph.
     * <p>
     * Note that time is measured entirely in the number of steps taken, and for each step
     * the time remaining is decremented by the weight of the edge taken. You can use
     * getTimeRemaining() to get the time still remaining, pickUpGold() to pick up any gold
     * on your current tile (this will fail if no such gold exists), and moveTo() to move
     * to a destination node adjacent to your current node.
     * <p>
     * You must return from this function while standing at the exit. Failing to do so before time
     * runs out or returning from the wrong location will be considered a failed run.
     * <p>
     * You will always have enough time to escape using the shortest path from the starting
     * position to the exit, although this will not collect much gold.
     *
     * @param state the information available at the current state
     */
    public void escape(EscapeState state) {



        //apply explore move and create a stack which contains an "exit stack" i.e. the
        //shortest time back to exit
        //if count of exit stack e.g. 10 and time remaining is 11 then "exit"
        //in the meantime, use blank square and random move rule (later version can try to target large sums?)
        // this should cover most of the space and then get back to exit in time

        //get current location
        Node currentNode = state.getCurrentNode();

        //calculate distance to exit using tile coordinates
        Node exitNode = state.getExit();
        long distanceToExitRow = currentNode.getTile().getRow()-exitNode.getTile().getRow();
        long distanceToExitColumn = currentNode.getTile().getColumn()-exitNode.getTile().getColumn();
        long distanceToExit = Math.abs(distanceToExitRow) + Math.abs(distanceToExitColumn);
        System.out.println(distanceToExit);
        System.out.println("current row" + state.getCurrentNode().getTile().getRow());
        System.out.println("exit row" + state.getExit().getTile().getRow());
        System.out.println("current column" + state.getCurrentNode().getTile().getColumn());
        System.out.println("exit column" + state.getExit().getTile().getColumn());
        System.out.println("time remaining" + state.getTimeRemaining());

    }




}
