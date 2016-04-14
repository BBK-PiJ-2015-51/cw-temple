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
import game.Tile;

import java.util.Collection;

public class Explorer {

    private Stack<Node> escapeStack = new Stack();
    ArrayList<Long> mazeMap = new ArrayList<Long>();


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
            if (!nodesNotVisited.isEmpty()) {
                for (NodeStatus n: nodesNotVisited) {
                    if (visitNext == null ) {
                        visitNext = n;
                    } else if (n.getDistanceToTarget() < visitNext.getDistanceToTarget()) {
                        visitNext = n;
                    }
                }
                state.moveTo(visitNext.getId());
                previousSquare.push(centre);
            } else {
                state.moveTo(previousSquare.pop());
            }

           /*



*/


        }


    }

    public int getWightedTimeRemaining(Stack<Node> n) {
        int result = 0;
        for(int i = 0; i< n.size()-1; i++) {
            result =  result + n.get(i).getEdge(n.get(i+1)).length;
            System.out.println(n.get(i).getEdge(n.get(i+1)).length);
        }
        return result;
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

        Stack<Node> exit = getExitPath(state.getCurrentNode(),state);
        int stepsToExit = 0;
        int timeRemaining = state.getTimeRemaining();



        System.out.println("exitsize:" + exit.size());
        System.out.println("weighted:" + stepsToExit);
        System.out.println(timeRemaining);

        Stack<Node> goToExit = new Stack<Node>();




        //travel around the map until just enough time to escape
     /*  while (timeRemaining>stepsToExit) {
            state.moveTo(visitNextNode(state));
            state.getCurrentNode().getTile().takeGold();
            stepsToExit = getExitPath(state.getCurrentNode(),state).size();
            timeRemaining = state.getTimeRemaining();
        }
*/




        //reverse the stack
        while(!exit.isEmpty()){
            goToExit.push(exit.pop());
        }

        System.out.println("current node " + state.getCurrentNode().getId());
        System.out.println("moving to "+ goToExit.peek().getId());
        goToExit.pop();
        System.out.println("moving to "+ goToExit.peek().getId());
        //follow path back to escape
        while(!goToExit.isEmpty()) {
            System.out.println("current node1 " + state.getCurrentNode().getId());
            state.moveTo(goToExit.pop());
            state.getCurrentNode().getTile().takeGold();
            System.out.println("current node3 " + state.getCurrentNode().getId());
        }
        state.moveTo(state.getExit());


    }


    public Node visitNextNode(EscapeState state) {

        Collection<Node> allNodes = state.getVertices();

        Node currentNode = state.getCurrentNode();
        mazeMap.add(currentNode.getId());
        Node visitNextNode;
        state.getCurrentNode().getTile().takeGold();

        //get all my neighbours
        ArrayList<Node> neighbours = new ArrayList();
        for (Node n : allNodes) {
            long neighbourDistance = calculateDistance(currentNode, n);
            if (neighbourDistance == 1) {
                neighbours.add(n);
            }
        }
        //then get neighbours not visited
        ArrayList<Node> nodesNotVisited = new ArrayList();
        //create a list of neighbours not been to
        for (Node n: neighbours) {
            if (!mazeMap.contains(n.getId())) {
                nodesNotVisited.add(n);
            }
        }
        //choose a square RANDOM to go to from those not yet been to
        if (!nodesNotVisited.isEmpty()) {
            Random r = new Random();
            int i = r.nextInt(nodesNotVisited.size());
            visitNextNode = nodesNotVisited.get(i);
            //state.moveTo(nextNode);
            escapeStack.push(currentNode);
        } else {
            //if there are no immediate squares to move to trace back through previously visited squares
            visitNextNode = escapeStack.pop();
        }

        return visitNextNode;


    }

    public Stack<Node> getExitPath(Node c, EscapeState state) {

        Collection<Node> allNodes = state.getVertices();
        Node exitNode = state.getExit();
        Tile exitTile = exitNode.getTile();
        int exitRow = exitTile.getRow();
        int exitColumn = exitTile.getColumn();


        long exitPathDistance = calculateDistance(state.getCurrentNode(),exitNode);
        Node exitPathNode = state.getCurrentNode();
        Stack<Node> exitStack = new Stack<Node>();
        ArrayList<Long> mazeMap = new ArrayList<Long>();

        //This calculates the number of steps to exit
        while (calculateDistance(exitPathNode,exitNode) != 0) {

            //get current location
            Node currentNode = exitPathNode;
            mazeMap.add(currentNode.getId());
            //calculate distance to exit using tile coordinates
            long distanceToExit = calculateDistance(currentNode, exitNode);
            //get neighbours
            ArrayList<Node> neighbours = new ArrayList();
            for (Node n : allNodes) {
                long neighbourDistance = calculateDistance(currentNode, n);
                if (neighbourDistance == 1) {
                    neighbours.add(n);
                }
            }
            //nodes visited

            //nodes not visited
            ArrayList<Node> nodesNotVisited = new ArrayList();
            //get neighbours not visited
            for (Node n : neighbours) {
                if (!mazeMap.contains(n.getId())) {
                    nodesNotVisited.add(n);
                }
            }
            //if blank square, choose shortest distance, if no blank square go back
            Node visitNext = null;
            if (!nodesNotVisited.isEmpty()) {
                for (Node n : nodesNotVisited) {
                    if (visitNext == null) {
                        visitNext = n;
                    } else if (calculateDistance(n, exitNode) < calculateDistance(visitNext, exitNode)) {
                        visitNext = n;
                    }
                }
                exitStack.push(currentNode);
                exitPathNode = visitNext;
            } else {
                exitPathNode = exitStack.pop();
            }
        }
        return exitStack;
    }

    public long calculateDistance(Node n1, Node n2) {
        Tile n1Tile = n1.getTile();
        Tile n2Tile = n2.getTile();
        int n1Row = n1Tile.getRow();
        int n1Column = n1Tile.getColumn();
        int n2Row = n2Tile.getRow();
        int n2Column = n2Tile.getColumn();
        long distanceRow = n1Row-n2Row;
        long distanceColumn = n1Column-n2Column;
        long distance = Math.abs(distanceRow) + Math.abs(distanceColumn);
        return distance;

    }



}
