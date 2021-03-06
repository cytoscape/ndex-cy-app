/*
 * Copyright (c) 2014, the Cytoscape Consortium and the Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.cytoscape.ndex.internal.singletons;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;


public enum NetworkManager
{
    INSTANCE;
//    private NetworkSummary selectedNetworkSummary;
    
    private final Map<Long, CXInfoHolder> cxNetworkInfoTable;
    private final Map<Long,UUID> networkIdTable; // store the network ids for collections from NDEx
    
    NetworkManager() { 
    	cxNetworkInfoTable = new TreeMap<>();
    	networkIdTable = new TreeMap<>();	}
 
    public CXInfoHolder getCXInfoHolder(Long subNetworkId) {
    	return this.cxNetworkInfoTable.get(subNetworkId);
    }
    
    public void setCXInfoHolder(Long subNetworkId, CXInfoHolder cxInfoHolder) {
    	this.cxNetworkInfoTable.put(subNetworkId, cxInfoHolder);
    }
    
    public void addNetworkUUID (Long subNetworkId, UUID networkId) {
    	networkIdTable.put(subNetworkId, networkId);
    }
    
    public UUID getNdexNetworkId(Long subNetworkId) {
    	return networkIdTable.get(subNetworkId);
    }
    
    public void deleteCyNetworkEntry(Long subNetworkId) {
    	this.cxNetworkInfoTable.remove(subNetworkId);
    	this.networkIdTable.remove(subNetworkId);
    }
}
