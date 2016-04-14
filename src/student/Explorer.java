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
import java.util.Set;
import java.lang.Long;

import java.util.Collection;

public class Explorer {

    private Stack<Node> escapeStack = new Stack();
    ArrayList<Long> mazeMap = new ArrayList<Long>();
    Stack<Node> exitStack = new Stack();
    Stack<Node> tempStack = new Stack();
    int totalEscapeTimeAllowed = 0;
    boolean nextMoveOutOfTime;

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
        System.out.println("This is total amount left" + state.getTimeRemaining());
        totalEscapeTimeAllowed = state.getTimeRemaining();
        Node currentNode = state.getCurrentNode();
        findBestExitPath(currentNode,state) ;
        followExitPath(exitStack,state);
    }

    public ArrayList<Node> getNextMoveOptions(Node curr, Node prev) {
        ArrayList<Node> result = new ArrayList();
        Set<Node> neighbours = curr.getNeighbours();
        Long longPrev = prev.getId();
        for(Node m:neighbours) {
            Long longCurr = m.getId();
            int sameNode = longCurr.compareTo(longPrev);
            if( sameNode != 0 ) {
                result.add(m);
            }
        }
        return result;
    }

    public Node getPreviousNode(Node n) {
        if (tempStack.isEmpty()) {
            return n;
        } else {
            return tempStack.get(tempStack.size()-1);
        }
    }

    public void findBestExitPath(Node n, EscapeState state) {
        System.out.println(n.getId());
        System.out.println("SizeOfStack" + tempStack.size());
        System.out.println("Size of exitStack" + exitStack.size());
        System.out.println("time of stack" + getWeightedTimeFromStack(tempStack));
        System.out.println("pushing on stack"+tempStack.push(n).getId());
        ArrayList<Node> routeOptions = getNextMoveOptions(n, getPreviousNode(n));

        System.out.println("no of route options" +routeOptions.size());

        //if run out of time move back
        if (routeOptions.isEmpty() || !enoughTimeToMove(state,tempStack)) {
            System.out.println("popping off stack" + tempStack.pop().getId());
            return;
        }
        //if on exit tile replace exitStack if more money
        if (n.getId() == state.getExit().getId()) {
            if(getGoldFromStack(tempStack)> getGoldFromStack(exitStack)) {
                exitStack = tempStack;
            }
        }
        if (routeOptions.size() == 1) {
            findBestExitPath(routeOptions.get(0),state);
        }
        if (routeOptions.size() <= 2) {
            findBestExitPath(routeOptions.get(1),state);
        }
        if (routeOptions.size() <= 3) {
            findBestExitPath(routeOptions.get(2),state);
        }
        if (!tempStack.isEmpty()) {
            tempStack.pop();
        }
    }

    public boolean enoughTimeToMove(EscapeState state, Stack<Node> s) {
        boolean result = false;
        int currentStackTime = getWeightedTimeFromStack(s);
      //  int nextMoveTime = getWeightedTimeFromNodes(src, dest);
        if(totalEscapeTimeAllowed > (currentStackTime )) {
            result = true;
        }
        return result;
    }

    public int getWeightedTimeFromStack(Stack<Node> n) {
        int result = 0;
        for(int i = 0; i< n.size()-1; i++) {
            result =  result + n.get(i).getEdge(n.get(i+1)).length;
        }
        return result;
    }

    public int getWeightedTimeFromNodes(Node src, Node dest) {
        int result = 0;
        result =  src.getEdge(dest).length;
        return result;
    }

    public int getGoldFromStack(Stack<Node> s) {
        int result = 0;
        for(Node n: s) {
            result = result + n.getTile().getGold();
        }
        return result;
    }

    public void followExitPath(Stack<Node> exitStack, EscapeState state) {
        System.out.println("proceeding to exit");
        for (int i = 1; i < exitStack.size(); i++) {
            state.moveTo(exitStack.get(i));
            if (state.getCurrentNode().getTile().getGold() > 0) {
                state.pickUpGold();
            }
        }

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

    public Stack<Node> getShortestPath(Node c, Node e, EscapeState state) {

        Collection<Node> allNodes = state.getVertices();
        Node exitNode = e;
        Tile exitTile = exitNode.getTile();
        int exitRow = exitTile.getRow();
        int exitColumn = exitTile.getColumn();


        long exitPathDistance = calculateDistance(c,exitNode);
        Node exitPathNode = c;
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
    exitStack.push(e);
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
