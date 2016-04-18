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

/**
 * @author stevenjenkins SJENKI05
 *
 * this class navigates and moves the explorer through the game
 *
 * In the exploration phase, the explorer chooses to move to a node not visited before and
 * which is closer to the exit. Random number chooses when equivalent choices.
 * If the explorer has been to all node choices relating to a particular node the explorer
 * moves back until a new not visited node is found
 *
 * In the escape phase, the explorer journey is divided into 2 paths. A wander path, where
 *the explorer wanders around picking up gold, and then when there is just enough time to exit
 * the explorer follows the exit path. The full escape path (wander + exit) is calculated multiple times
 * and the path that picks up the most gold is subsequently moved
 *
 */
public class Explorer {

    private ArrayList<Node> nodesVisited;
    private ArrayList<Long> nodeStatusVisited;
    private Stack<Node> exitStack;
    private Stack<Node> wanderStack;
    private Stack<Node> bestEscapePathStack;
    private Stack<Node> moveAroundStack;
    private int totalEscapeTimeAllowed;
    private boolean outOfTime;

    public Explorer() {
        nodesVisited = new ArrayList<Node>();
        nodeStatusVisited = new ArrayList<Long>();
        exitStack = new Stack();
        wanderStack = new Stack();
        bestEscapePathStack = new Stack();
        moveAroundStack = new Stack();
        totalEscapeTimeAllowed = 0;
        outOfTime = false;
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
        Stack<Long> explorePath = new Stack<Long>();
        while (state.getDistanceToTarget() > 0) {
            Long currentNodeStatus = state.getCurrentLocation();
            nodeStatusVisited.add(currentNodeStatus);
            visitNextNodeStatus(getNeighboursNotBeenTo(state),state,explorePath,currentNodeStatus);
        }
    }

    /**
     *returns a list of the current neighbours
     * @param state the information available at the current state
     * @return a list of neighbouring nodes not been to
     */
    public ArrayList<NodeStatus> getNeighboursNotBeenTo(ExplorationState state) {
        Collection<NodeStatus> neighbours = state.getNeighbours();
        ArrayList<NodeStatus> options = new ArrayList<NodeStatus>();
        for (NodeStatus n: neighbours) {
            if (!nodeStatusVisited.contains(n.getId())) {
                options.add(n);
            }
        }
        return options;
    }

    /**
     *calculates the next node to visit
     * @param options the node's neighbours
     *@param state the information available at the current state
     * @param explorePath path the current path
     * @param current the current node
     */
    public void visitNextNodeStatus(ArrayList<NodeStatus> options, ExplorationState state,Stack<Long> explorePath,Long current) {
        NodeStatus visitNext = null;
        if (!options.isEmpty()) {
            for (NodeStatus n: options) {
                if (visitNext == null ) {
                    visitNext = n;
                } else if (n.getDistanceToTarget() < visitNext.getDistanceToTarget()) {
                    visitNext = n;
                }
            }
            state.moveTo(visitNext.getId());
            explorePath.push(current);
        } else {
            state.moveTo(explorePath.pop());
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
        //find the path with the most gold
        findBestEscapePath(state);
        //move through this path
        followStackPath(bestEscapePathStack,state);
    }

    /**
     *runs multiple escape paths and then keeps one with highest amount of gold
     * @param state the information available at the current state
     */
    public void findBestEscapePath(EscapeState state) {
        totalEscapeTimeAllowed = state.getTimeRemaining();
        System.out.println(totalEscapeTimeAllowed);
        Node startNode = state.getCurrentNode();
        //find 100 paths that exit in time and take the path with the most gold
        for (int i = 0; i < 100; i++) {
            findEscapePath(startNode,state);
            checkAndReplaceBestEscapeStack();
            clearAll();
        }
    }

    /**
     *compares best and newly generated escape paths
     */
    public void checkAndReplaceBestEscapeStack() {
        Stack<Node> newEscapePathStack = createNewEscapePathStack();
        //choose the path with the most gold
        if (bestEscapePathStack.isEmpty()) {
            bestEscapePathStack = newEscapePathStack;
        } else {
            int bestEscapePathGold = getGoldFromStack(bestEscapePathStack);
            int newEscapePathGold = getGoldFromStack(newEscapePathStack);

            if (newEscapePathGold > bestEscapePathGold) {
                bestEscapePathStack = newEscapePathStack;
            }
        }
    }

    /**
     *create new escape path stack from wander and exit stacks
     * @return returns a full escape path stack
     */
    public Stack<Node> createNewEscapePathStack() {
        Stack<Node> result = new Stack<Node>();
        for (int j = 0; j < wanderStack.size(); j++) {
            result.push(wanderStack.get(j));
        }
        for (int j = 1; j < exitStack.size(); j++) {
            result.push(exitStack.get(j));
        }
        return result;
    }

    /**
     *clears the global stacks for each run
     */
    public void clearAll() {
        wanderStack.removeAllElements();
        exitStack.removeAllElements();
        moveAroundStack.removeAllElements();
        nodesVisited.clear();
        outOfTime = false;
    }

    /**
     *finds an escape path that ends within the time limit
     * @param n current node
     * @param state the information available at the current state
     */
    public void findEscapePath(Node n, EscapeState state) {
        Node currentNode = n;
        ArrayList<Node> moveOptions;
        wanderStack.push(n);
        moveAroundStack.push(n);
        Stack<Node> tempExitStack = new Stack<Node>();
        while(!outOfTime) {
            moveOptions = getNextMoveOptions(currentNode, getPreviousNode(currentNode));
            exitStack = tempExitStack;
            currentNode = getNextNodeMove(moveOptions,currentNode);
            tempExitStack = getShortestExitPath(currentNode,state.getExit(),state);;
            outOfTime = checkIfOutOfTime(wanderStack, tempExitStack);
        }
        //last move led to out of time so step back one
        wanderStack.pop();
    }

    /**
     *calculates best next move
     * @param moveOptions the possible next move options
     * @param current the current node
     * @return returns a node to move to next
     */
    public Node getNextNodeMove(ArrayList<Node> moveOptions, Node current) {
        if (moveOptions.isEmpty()) {
            moveAroundStack.pop();
            wanderStack.push(moveAroundStack.peek());
            current = moveAroundStack.peek();

        } else {
            Node nextMove = chooseNextMove(moveOptions);
            moveAroundStack.push(nextMove);
            wanderStack.push(nextMove);
            nodesVisited.add(current);
            current = nextMove;
        }
        return current;
    }

    /**
     *gets the previously visited node
     * @param current the current node
     * @return the previous node
     */
    public Node getPreviousNode(Node current) {
        Node previousNode;
        if (moveAroundStack.size()>=2) {
            previousNode = moveAroundStack.get(moveAroundStack.size()-2);
        } else {
            previousNode = current;
        }
        return previousNode;
    }

    /**
     *where multiple paths have same priority take one at random
     * @param options the available move options
     * @return chosen node to move to
     */
    public Node chooseNextMove(ArrayList<Node> options) {
        Random r = new Random();
        int rand = r.nextInt(options.size());
        Node result = options.get(rand);
        return result;
    }

    public Stack<Node> setExitStack(Node n, EscapeState state) {
        totalEscapeTimeAllowed = state.getTimeRemaining();
        boolean exitPathWithinTime = false;
        Stack<Node> result = new Stack<Node>();
        //always return an exit path that is within the time limit
        while(!exitPathWithinTime) {
            result = getShortestExitPath(n,state.getExit(),state);
            int newExitTime = getWeightedTimeFromStack(result);
            System.out.println("total time allowed" + totalEscapeTimeAllowed);
            System.out.println("new exit time" + newExitTime);
            if (totalEscapeTimeAllowed > newExitTime) {
                exitPathWithinTime = true;
            }
        }
        return result;
    }

    /**
     *returns the possible next move options
     * @param curr the current node
     * @param prev the previosly visited node
     * @return list of possible next move options
     */
    public ArrayList<Node> getNextMoveOptions(Node curr, Node prev) {
        ArrayList<Node> result = new ArrayList();
        Set<Node> neighbours = curr.getNeighbours();
        Long longPrev = prev.getId();
        for(Node n:neighbours) {
            Long longCurr = n.getId();
            int sameNode = longCurr.compareTo(longPrev);
            if( sameNode != 0 && !nodesVisited.contains(n.getId())) {
                result.add(n);
            }
        }
        return result;
    }

    /**
     *adds the wander and exit stack time to determine if path is within time limit
     * @param wander the stack that holds the wander path
     * @param exit the stack that holds the exit path
     * @return boolean of whether the next move is out of time
     */
    public boolean checkIfOutOfTime( Stack<Node> wander, Stack<Node> exit ) {
        boolean result = false;
        int wanderStackTime = getWeightedTimeFromStack(wander);
        int exitStackTime = getWeightedTimeFromStack(exit);
        int wanderToExitTime = getWeightedTimeFromNodes(wander.peek(), exit.get(0));
        //  int nextMoveTime = getWeightedTimeFromNodes(src, dest);
        if(totalEscapeTimeAllowed < (wanderStackTime + exitStackTime + wanderToExitTime)) {
            result = true;
        }
        return result;
    }

    /**
     *counts the weighted time for a give stack
     * @param n any node stack
     * @return an integer for weighted time of stack
     */
    public int getWeightedTimeFromStack(Stack<Node> n) {
        //System.out.println("size of stack" + tempStack.size());
        int result = 0;
        if (n.size() > 1) {
            for(int i = 0; i< n.size()-1; i++) {
                result =  result + n.get(i).getEdge(n.get(i+1)).length();
            }
        }
        return result;
    }

    /**
     *counts the weighted time between 2 nodes
     * @param src the source node
     * @param dest the desination node
     * @return returns an integer for weighted time between 2 nodes
     */
    public int getWeightedTimeFromNodes(Node src, Node dest) {
        int result = 0;
        Long srcLong = src.getId();
        Long destLong = dest.getId();
        if(srcLong.compareTo(destLong) == 0) {
            return result;
        } else {
            result =  src.getEdge(dest).length;
            return result;
        }

    }

    /**
     *returns the amount of gold from a given stack
     * @param s any given stack
     * @return returns an integer with total gold from stack
     */
    public int getGoldFromStack(Stack<Node> s) {
        int result = 0;
        ArrayList<Long> visited = new ArrayList<Long>();
        for(Node n: s) {
            if (!visited.contains(n.getId())) {
                result = result + n.getTile().getGold();
                visited.add(n.getId());
            }
        }
        return result;
    }

    /**
     *executes state move for a given stack
     * @param s any given stack
     * @param state current game state
     */
    public void followStackPath(Stack<Node> s, EscapeState state) {
        for (int i = 1; i < s.size(); i++) {
            state.moveTo(s.get(i));
            if (state.getCurrentNode().getTile().getGold() > 0) {
                state.pickUpGold();
            }
        }
    }

    /**
     *calculates the shortest path between 2 nodes
     * @param c the current node
     * @param e the destination node
     * @param state the game state
     * @return returns a stack of the shortest path between 2 nodes
     */
    public Stack<Node> getShortestExitPath(Node c, Node e, EscapeState state) {
        Collection<Node> allNodes = state.getVertices();
        Node exitNode = e;
        Tile exitTile = exitNode.getTile();
        Node exitPathNode = c;
        Stack<Node> exitStack = new Stack<Node>();
        ArrayList<Long> mazeMap2 = new ArrayList<Long>();
        while (calculateDistance(exitPathNode,exitNode) != 0) {
            Node currentNode = exitPathNode;
            mazeMap2.add(currentNode.getId());
            ArrayList<Node> neighbours = new ArrayList();
            for (Node n : allNodes) {
                long neighbourDistance = calculateDistance(currentNode, n);
                if (neighbourDistance == 1) {
                    neighbours.add(n);
                }
            }
            ArrayList<Node> nodesNotVisited = new ArrayList();
            for (Node n : neighbours) {
                if (!mazeMap2.contains(n.getId())) {
                    nodesNotVisited.add(n);
                }
            }
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

    /**
     *calculates the distance between 2 nodes using coordinates
     * @param n1 source node
     * @param n2 destination node
     * @returns a long distance between 2 nodes
     */
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