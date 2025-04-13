package test;

import game.EscapeState;
import game.ExplorationState;
import game.Node;
import game.NodeStatus;
import game.Tile;
import game.Edge;
import student.Explorer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class ExplorerTest {

    private Explorer explorer;

    @BeforeEach
    public void setup() {
        explorer = new Explorer();
    }

    @Test
    public void testEscapeGoesToExitWhenNoGold() {
        // Mock the game state and required objects
        EscapeState mockState = mock(EscapeState.class);
        Node start = mock(Node.class);
        Node exit = mock(Node.class);
        Edge edge = mock(Edge.class);
        Tile startTile = mock(Tile.class);
        Tile exitTile = mock(Tile.class);

        // Setup graph connections
        when(mockState.getCurrentNode()).thenReturn(start);
        when(mockState.getExit()).thenReturn(exit);
        when(start.getNeighbours()).thenReturn(Set.of(exit));
        when(start.getEdge(exit)).thenReturn(edge);
        when(edge.length()).thenReturn(1);

        // Setup gold info
        when(start.getTile()).thenReturn(startTile);
        when(exit.getTile()).thenReturn(exitTile);
        when(startTile.getGold()).thenReturn(0);
        when(exitTile.getGold()).thenReturn(0);

        // Game conditions
        when(mockState.getTimeRemaining()).thenReturn(2);
        when(mockState.getVertices()).thenReturn(List.of(start, exit));

        // Call the method under test
        explorer.escape(mockState);

        // Verify that we went to the exit
        verify(mockState, atLeastOnce()).moveTo(exit);
    }

    @Test
    public void testEscapeCollectsGoldIfTimeAllows() {
        // Mock the game state and required objects
        EscapeState mockState = mock(EscapeState.class);
        Node start = mock(Node.class);
        Node goldNode = mock(Node.class);
        Node exit = mock(Node.class);

        Tile startTile = mock(Tile.class);
        Tile goldTile = mock(Tile.class);
        Tile exitTile = mock(Tile.class);

        Edge toGold = mock(Edge.class);
        Edge toExit = mock(Edge.class);

        // Setup start state
        when(mockState.getCurrentNode()).thenReturn(start);
        when(mockState.getExit()).thenReturn(exit);
        when(mockState.getVertices()).thenReturn(List.of(start, goldNode, exit));
        when(mockState.getTimeRemaining()).thenReturn(10);

        // Setup tile gold values
        when(start.getTile()).thenReturn(startTile);
        when(goldNode.getTile()).thenReturn(goldTile);
        when(exit.getTile()).thenReturn(exitTile);

        when(startTile.getGold()).thenReturn(0);
        when(goldTile.getGold()).thenReturn(100);  // gold available!
        when(exitTile.getGold()).thenReturn(0);

        // Graph: start -> gold -> exit
        when(start.getNeighbours()).thenReturn(Set.of(goldNode));
        when(start.getEdge(goldNode)).thenReturn(toGold);
        when(toGold.length()).thenReturn(3);

        when(goldNode.getNeighbours()).thenReturn(Set.of(exit));
        when(goldNode.getEdge(exit)).thenReturn(toExit);
        when(toExit.length()).thenReturn(3);

        // Call the method under test
        explorer.escape(mockState);

        // Validate we visited the gold tile and picked it up, then went to exit
        verify(mockState, atLeastOnce()).moveTo(goldNode);
        verify(mockState).pickUpGold();
        verify(mockState).moveTo(exit);
    }
}