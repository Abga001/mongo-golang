package student;

import game.EscapeState;
import game.ExplorationState;
import game.Node;
import game.NodeStatus;

import java.util.*;

public class Explorer {

    // Explore method to find the orb
    public void explore(ExplorationState state) {
        Set<Long> visited = new HashSet<>();
        Stack<Long> path = new Stack<>();

        visited.add(state.getCurrentLocation());
        path.push(state.getCurrentLocation());

        while (state.getDistanceToTarget() != 0) {
            List<NodeStatus> neighbors = new ArrayList<>(state.getNeighbours());
            neighbors.sort(Comparator.comparingInt(NodeStatus::distanceToTarget));

            NodeStatus next = null;
            for (NodeStatus ns : neighbors) {
                if (!visited.contains(ns.nodeID())) {
                    next = ns;
                    break;
                }
            }

            if (next != null) {
                state.moveTo(next.nodeID());
                visited.add(next.nodeID());
                path.push(next.nodeID());
            } else if (!path.isEmpty()) {
                path.pop();
                if (!path.isEmpty()) {
                    state.moveTo(path.peek());
                }
            }
        }
    }

    // Escape method to collect as much gold as possible and exit safely
    public void escape(EscapeState state) {
        Node current = state.getCurrentNode();
        Node exit = state.getExit();
        int timeRemaining = state.getTimeRemaining();

        // Immediately pick up gold if standing on it
        if (current.getTile().getGold() > 0) {
            state.pickUpGold();
        }

        Set<Node> visited = new HashSet<>();

        while (true) {
            Node bestGoldNode = null;
            List<Node> bestPath = null;
            int bestValue = 0;

            // Search for the best gold node to go to next
            for (Node node : state.getVertices()) {
                if (visited.contains(node)) continue;
                if (node.getTile().getGold() == 0) continue;

                List<Node> pathToGold = getShortestPath(current, node);
                List<Node> pathToExit = getShortestPath(node, exit);

                int totalCost = getPathCost(pathToGold) + getPathCost(pathToExit);

                // Only consider nodes that can be reached and still escape
                if (totalCost <= timeRemaining) {
                    int value = node.getTile().getGold();
                    if (value > bestValue) {
                        bestValue = value;
                        bestGoldNode = node;
                        bestPath = pathToGold;
                    }
                }
            }

            // Break if no more reachable gold nodes
            if (bestGoldNode == null) {
                break;
            }

            // Follow the path to the selected gold node
            for (Node step : bestPath) {
                if (!state.getCurrentNode().equals(step)) {
                    state.moveTo(step);
                    if (step.getTile().getGold() > 0) {
                        state.pickUpGold();
                    }
                }
            }

            visited.add(bestGoldNode);
            current = state.getCurrentNode();
            timeRemaining = state.getTimeRemaining();
        }

        // Pick up gold again in case the last node had some and we didn't move
        if (state.getCurrentNode().getTile().getGold() > 0) {
            state.pickUpGold();
        }

        // Finally, move to the exit
        List<Node> exitPath = getShortestPath(state.getCurrentNode(), exit);
        for (Node step : exitPath) {
            if (!state.getCurrentNode().equals(step)) {
                state.moveTo(step);
                if (step.getTile().getGold() > 0) {
                    state.pickUpGold();
                }
            }
        }
    }

    // Dijkstra's algorithm to compute shortest path between two nodes
    private List<Node> getShortestPath(Node start, Node goal) {
        Map<Node, Node> prev = new HashMap<>();
        Map<Node, Integer> dist = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        dist.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current.equals(goal)) break;

            for (Node neighbor : current.getNeighbours()) {
                int alt = dist.get(current) + current.getEdge(neighbor).length();
                if (!dist.containsKey(neighbor) || alt < dist.get(neighbor)) {
                    dist.put(neighbor, alt);
                    prev.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        // Reconstruct the path from start to goal
        LinkedList<Node> path = new LinkedList<>();
        for (Node at = goal; at != null; at = prev.get(at)) {
            path.addFirst(at);
        }

        return path;
    }

    // Calculates the total travel cost of a given path
    private int getPathCost(List<Node> path) {
        int cost = 0;
        for (int i = 1; i < path.size(); i++) {
            cost += path.get(i - 1).getEdge(path.get(i)).length();
        }
        return cost;
    }
}
