package com.github.catageek.ByteCart.Routing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.catageek.ByteCart.Util.DirectionRegistry;

public class RoutingTableExchange {
	
	private Map<Integer, Integer> tablemap = new HashMap<Integer, Integer>();

	public int getDistance(int entry) {
		return tablemap.get(entry);
	}
	
	public int getSize() {
		return tablemap.size();
	}
	
	public boolean hasRouteTo(int ring) {
		return tablemap.containsKey(ring);
	}
	
	public Set<Entry<Integer,Integer>> getEntrySet() {
		return tablemap.entrySet();
	}

	public RoutingTableExchange(RoutingTable table, DirectionRegistry direction) {
		super();
		for(int i = 0; i< table.getSize(); i++) {
			if(! table.isEmpty(i) && table.getDirection(i) != direction)
				tablemap.put(i, table.getDistance(i));
		}
	}
	
}