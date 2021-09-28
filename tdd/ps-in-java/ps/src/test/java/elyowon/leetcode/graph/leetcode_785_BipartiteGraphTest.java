package elyowon.leetcode.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class leetcode_785_BipartiteGraphTest {

    @Test
    public void test() throws Exception{
        //given
        leetcode_785_BipartiteGraph v = new leetcode_785_BipartiteGraph();
        int[][] graph = new int[][]{{1,3},{0,2},{1,3},{0,2}};
        //when
//        boolean bipartite = v.isBipartite(graph);
        boolean bipartiteBfs = v.isBipartite_bfs(graph);

        System.out.println("bipartite = " + bipartiteBfs);

        //then
    }

}