package weightedloadexperiment.pairstrategies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javatuples.* ;

import common.RandomGenerator;
import custom.fattree.Address;
import custom.fattree.FatTreeGraph;
import custom.fattree.FatTreeRoutingAlgorithm;

public class InterPodIncoming extends OverSubscription {

 
    private int [][] adjMx;
  
    public InterPodIncoming(FatTreeRoutingAlgorithm routing, FatTreeGraph G)
    {
    	super();
    	this.routing = routing;
        this.G = G;
    }
	
    @Override
    public void setAllHosts(Integer[] allHosts)
    {
    	super.setAllHosts(allHosts);
    	this.k =  (int)Math.cbrt(4*allHosts.length);
        
        int numOfHosts = allHosts.length;
        
        adjMx = new int[numOfHosts][numOfHosts];
        for(int i = 0; i < numOfHosts; i++)
        {
            for(int j = 0; j < numOfHosts; j++)
            {
                if(i / (k*k/4) != j / (k*k/4)) {
                    int src = allHosts[i];
                    int dst = allHosts[j];
                    int core = getCoreSwitch(src, dst);
                    adjMx[i][j] = core;
                }
                else{
                    adjMx[i][j] = 0;
                }
            }
        }
    	
    }
    @Override
    public void pairHosts() {
        List<Integer> sources = getSources();
        List<Integer> destinations = getDestinations();

        Integer[] allHosts = this.getAllHosts();
        int delta = RandomGenerator.nextInt(0, k*k/4);
        int numOfHosts = allHosts.length;
        int sizeOfPod = k*k/4;
        int currPod = 0, prePod = 0;
        for(int i = 0; i < numOfHosts; i++) {
            int dst = allHosts[i];
            prePod = currPod;
            if(!destinations.contains(dst))
            {   int index = (i + sizeOfPod + delta) % numOfHosts;                      
            	index = CalIndex(index, sizeOfPod, prePod, numOfHosts, dst);
                int count = 0;
                int expectedSrc = allHosts[index];
                boolean found = false;
                this.addSrcAndDst(allHosts, destinations, sources, i, dst, currPod, sizeOfPod, index, numOfHosts);
            }            
            else {
                currPod = i / sizeOfPod;
            }
        }
        this.setSources(sources);
        this.setDestinations(destinations);
    }
    /**
     * this method is for pairing source and destination hosts
     * @param allHosts list of all the host
     * @param destinations list od destination hall
     * @param sources list of source host
     * @param i
     * @param dst is destionation host
     * @param currPod is current host
     * @param sizeOfPod is size of a pod
     * @param index
     * @param numOfHosts is the host number
     */
    private void addSrcAndDst( Integer[] allHosts ,List<Integer> destinations, List<Integer> sources,int i,int dst,int currPod, int sizeOfPod, int index,int numOfHosts) {
		
        int count = 0;
        int expectedSrc = allHosts[index];
        boolean found = false;
        while(!found && count < k)
    {
        if(sources.contains(expectedSrc))
        {
           this.add(allHosts, destinations, sources, sizeOfPod, index, expectedSrc, dst, found, index, currPod);
        }
        else{
            if(expectedSrc / sizeOfPod != dst / sizeOfPod)
            {
                found = true;
                sources.add(expectedSrc);
                destinations.add(dst);

                sources.add(dst);
                destinations.add(expectedSrc);

                if((i + 1) % sizeOfPod == 0)
                {
                    currPod = (i + 1) / sizeOfPod;
                }
                else {
                    currPod = index / sizeOfPod;
                }
                break;
            }
        }

        if(!found)
        {
            count++;
            index = (index + sizeOfPod) % numOfHosts;
        }
    }   
}   
    /**
     * this method is calculating index variable
     * @param index
     * @param sizeOfPod is size of a Pod
     * @param prePod is the previous pod 
     * @param numOfHosts is the host number
     * @param dst is the destination host
     * @return index
     */
	private int CalIndex(int index, int sizeOfPod, int prePod,int numOfHosts, int dst) {
		 if(index / sizeOfPod == prePod)
        {
            index = (index + sizeOfPod) % numOfHosts;
            
            if(index / sizeOfPod == dst / sizeOfPod)
            {
                index = (index + sizeOfPod) % numOfHosts;
            }
        }
		 return index;
	}
	/**
	 * this method is for paring src and dst hosts when the src is the expectedsrc
	 * @param allHosts  list of all the host
	 * @param destinations list of destination host
	 * @param sources list of sources host
	 * @param sizeOfPod is size of a pod
	 * @param index
	 * @param expectedSrc 
	 * @param dst
	 * @param found
	 * @param i
	 * @param currPod 
	 */
	private void add(Integer[] allHosts,  List<Integer> destinations,List<Integer> sources
			 ,int sizeOfPod,int index,int expectedSrc,int dst,boolean found,int i,int currPod) {

		for(int j = index + 1; j < (index/sizeOfPod + 1)*sizeOfPod; j++)
		{
			expectedSrc = allHosts[j];
			if(!sources.contains(expectedSrc)
       && ((expectedSrc / sizeOfPod) != (dst / sizeOfPod))
					)
			{
				found = true;
				sources.add(expectedSrc);
				destinations.add(dst);
				if((i + 1) % sizeOfPod == 0)
				{
       currPod = (i + 1) / sizeOfPod;
				}
				else {
					currPod = j / sizeOfPod;
				}

				sources.add(dst);
				destinations.add(expectedSrc);
				break;
			}
		}
}	
    @Override
    public void checkValid()
    {
        Map<Integer, Integer> flowPerCore = new HashMap<Integer, Integer>();
        List<Integer> sources = getSources();
        List<Integer> destinations = getDestinations();
        int realCore = 0;
        //int count = 0;
        int sizeOfPod = k*k/4;
        checkPair(sources, destinations, realCore);    
        checkPod(flowPerCore, sources, destinations, sizeOfPod, realCore);
        int average = k;
        int equal = 0;
        for(int core : flowPerCore.keySet())
        {
            if(average == flowPerCore.get(core))
            {
                equal++;
            }
        }
        if(equal == k*k*k/4)
        {

        }
    }
    private void checkPair( List<Integer> sources, List<Integer> destinations, int realCore) {
    	if(sources.size() != k*k*k/4)
          {
    	for(int i = 0; i < sources.size(); i++)
          {
            realCore = getRealCoreSwitch(sources.get(i), destinations.get(i));
            System.out.println("From " + sources.get(i) + " through " +
                    getCoreSwitch(sources.get(i), destinations.get(i))
                    + "/" + realCore
                    + " to "
                    + destinations.get(i)
            );
        }
        System.exit(0);
      }
    }
    /**
     * this method is for checking whether or not source and destunation host are in the same pod
     * @param flowPerCore
     * @param sources
     * @param destinations
     * @param sizeOfPod
     * @param realCore
     */
    private void checkPod( Map<Integer, Integer> flowPerCore,List<Integer> sources, List<Integer> destinations, 
    						int sizeOfPod, int realCore) {
    	  for(int i = 0; i < sources.size(); i++)
          {
              realCore = getRealCoreSwitch(sources.get(i), destinations.get(i));
              System.out.println("From " + sources.get(i) + " through " +
                      getCoreSwitch(sources.get(i), destinations.get(i))
                      + "/" + realCore
                      + " to "
                      + destinations.get(i)
              );

              if(flowPerCore.containsKey(realCore))
              {
                  int value = flowPerCore.get(realCore) + 1;
                  flowPerCore.put(realCore, value);
              }
              else{
                  flowPerCore.put(realCore, 1);
              }

              if(sources.get(i) / sizeOfPod ==
                      destinations.get(i) / sizeOfPod)
              {
                  System.out.print("Source and destination are in the same pod. INVALID!!!!");
                  System.exit(0);
                  //count++;
                  break;
              }
          }
    }
    public void transform(int[][] M, int length)
    {
        for(int i = 0; i < length; i++)
        {
            for(int j = 0; j < length; j++)
            {
                if(i > j) {
                    int temp = M[j][i];
                    M[j][i] = M[i][j];
                    M[i][j] = temp;
                }
            }
        }
    }

    public int getCoreSwitch(int source, int destination)
    {
        int edge = G.adj(source).get(0);
        int agg = G.adj(edge).get(k/2);
        int core = G.adj(agg).get(k/2);
        return core;
    }

    public int getRealCoreSwitch(int source, int destination)
    {
        int edge = G.adj(source).get(0);
        Address address = G.getAddress(destination);
        Map<Integer, Map<Integer, Integer>> suffixTables = routing.getSuffixTables();
        Map<Integer, Map<Triplet<Integer, Integer, Integer>, Integer>> prefixTables = routing.getPrefixTables();

        Map<Integer, Integer> suffixTable = suffixTables.get(edge);
        int suffix = address._4;
        int agg = suffixTable.get(suffix);

        Triplet<Integer, Integer, Integer> prefix
                = new Triplet<>(address._1, address._2, address._3);

        Map<Triplet<Integer, Integer, Integer>, Integer> prefixTable =
                prefixTables.get(agg);
        suffixTable = suffixTables.get(agg);

        if (prefixTable.containsKey(prefix)) {
            return prefixTable.get(prefix);
        } else {
            return suffixTable.get(suffix);
        }
    }


}
