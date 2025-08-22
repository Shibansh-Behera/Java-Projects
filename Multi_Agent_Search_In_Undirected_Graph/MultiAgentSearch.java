import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


class AgentMan{
    AtomicInteger[] vis;
    AtomicBoolean found;
    int[][] graph;
    int numNodes;


    AgentMan(int n,int[][]graph){
        vis=new AtomicInteger[n];
        found = new AtomicBoolean(false);
        this.graph=graph;
        numNodes=n;
        for (int i = 0; i < n; i++) {
            vis[i] = new AtomicInteger(0);
        }

    }

    void search(int st,int target){
        int[] parent = new int[numNodes];
        Arrays.fill(parent, -1);
        Deque<Integer> queue= new ArrayDeque<>();
        queue.addLast(st);
        vis[st].set(1);
        while(!queue.isEmpty() && !found.get()){
            int curr =queue.removeFirst();
            vis[curr].set(2);

            if(curr ==target){
                found.set(true);
                getPath(st,target,parent);
                return;
            }
            for(int i=0;i<numNodes;i++){
                if(vis[i].get()==0 && graph[curr][i]==1){
                    queue.addLast(i);
                    vis[i].set(1);
                    parent[i] = curr;
                }
            }
        }
    }

    void getPath(int start,int target,int[] parent){

        if (!found.get()) {
            System.out.println("No path found from " + start + " to " + target);
            return;
        }
        ArrayList<Integer> path = new ArrayList<>();
        for (int at = target; at != -1; at = parent[at]) {
            path.add(at);
        }
        Collections.reverse(path);

        System.out.println("Path from " + start + " to " + target + ": " + path);
        System.out.println(Thread.currentThread().getName());
    }

}
public class MultiAgentSearch {
    static int[][] genGraph(int n){
        int[][] g=new int[n][n];
        Random rand= new Random();
        boolean connected;
        for(int i=0;i<n;i++){
            connected=false;
            for(int j=0;j<n;j++){
                if(i!=j){
                    if(g[i][j]==1){
                        connected=true;
                    }
                    else {
                        g[i][j] = rand.nextInt(2);
                        if(g[i][j]==1){
                            g[j][i]=1;
                            connected=true;
                        }
                    }
                }
            }
            if(!connected){
                int k= rand.nextInt(n);
                while(i==k){
                    k=rand.nextInt(n);
                }
                g[i][k]=1;
                g[k][i]=1;
            }
        }
        return g;
    }
    
    public static void main(String[] args) {
        Random rand=new Random();
        int numNodes=rand.nextInt(1000)+1;
        int target=numNodes/2;
        int totalStPoints;
        int [] stNodes;



        if(numNodes<=10){
            totalStPoints=1;
            stNodes=new int[1];
            int node=rand.nextInt(numNodes);
            while(node==target){
                node=rand.nextInt(numNodes);
            }
            stNodes[0]=node;
        } else if (numNodes<=20) {
            totalStPoints=2;
            stNodes=new int[2];
            for(int i=0;i<2;i++) {
                int node = rand.nextInt(numNodes);
                while (node == target) {
                    node = rand.nextInt(numNodes);
                }
                stNodes[i] = node;
            }
        }
        else{
            totalStPoints=4;
            stNodes=new int[4];
            for(int i=0;i<4;i++) {
                int node = rand.nextInt(numNodes);
                while (node == target) {
                    node = rand.nextInt(numNodes);
                }
                stNodes[i] = node;
            }
        }


        int[][] graph=genGraph(numNodes);
        AgentMan detective = new AgentMan(numNodes,graph);


        ExecutorService executor= Executors.newFixedThreadPool(totalStPoints);
        for(int i=0;i<totalStPoints;i++){
            final int k=i;
            executor.submit(()->{
                detective.vis[k].set(1);
                detective.search(stNodes[k],target);
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(100, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        System.out.println("done");
    }

}
