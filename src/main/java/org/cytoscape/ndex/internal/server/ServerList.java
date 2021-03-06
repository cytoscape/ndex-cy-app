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

package org.cytoscape.ndex.internal.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;

import org.cytoscape.ndex.internal.singletons.CyObjectManager;
import org.cytoscape.ndex.internal.strings.ErrorMessage;
import org.cytoscape.ndex.internal.strings.FilePath;
import org.cytoscape.ndex.internal.strings.ResourcePath;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author David Welker
 */
public class ServerList extends AbstractListModel<Server>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//A list of servers, both DEFAULT and ADDED, displayed to the user.
    private List<Server> serverList = new ArrayList<>();
    //A list of default servers with CREDENTIALS.
    private List<Server> defaultServerCredentials = new ArrayList<>();
    //Efficiently tracks server names already used to prevent duplicates.
    private HashSet<String> namesUsed = new HashSet<>();
    //A list of DEFAULT servers
    private List<Server> defaultServerList = new ArrayList<>();
    
    public ServerList()
    {
        super();
        readServers();
    }

    public Server getDefaultServer()
    {
        return defaultServerList.get(0);
    }
    
    private void readServers()
    {
        readDefaultServers();
        readAddedServers();
        readDefaultServerCredentials();
        mergeDefaultServerCredentials();
    }
   
    private void readDefaultServers()
    {
        Collection<Server> defaultServers = readServerCollection(ResourcePath.DEFAULT_NDEX_SERVERS); 
        serverList.addAll(defaultServers);
        defaultServerList.addAll(defaultServers);
        for( Server server : defaultServers )
            namesUsed.add( server.getName() );
    }
    
    private void readDefaultServerCredentials()
    {
        File configDir = CyObjectManager.INSTANCE.getConfigDir();
        File defaultServersJsonFile = new File(configDir, FilePath.DEFAULT_SERVER_CREDENTIALS);
        Collection<Server> credentials = readServerCollection(defaultServersJsonFile);
        defaultServerCredentials.addAll(credentials);
        for( Server server : credentials )
            namesUsed.add( server.getName() );
    }
    
    private void mergeDefaultServerCredentials()
    {
        for( Server credentials : defaultServerCredentials )
            mergeDefaultServerCredentials(credentials);
    }
    
    private void mergeDefaultServerCredentials(Server credentials )
    {   
        for( Server server : getDefaultServers() )
        {
            if( server.hasSameName( credentials ) )
            {
                server.useCredentialsOf( credentials );
            }
        }      
    }
       
    private void readAddedServers()
    {
        File configDir = CyObjectManager.INSTANCE.getConfigDir();
        File addedServersJsonFile = new File(configDir, FilePath.ADDED_SERVERS);
        Collection<Server> addedServers = readServerCollection(addedServersJsonFile);     
        serverList.addAll(addedServers);
        for( Server server : addedServers )
            namesUsed.add( server.getName() );
    }
    
    private Collection<Server> readServerCollection(String resourcePath)
    {  
        URL json = ServerList.class.getClassLoader().getResource(resourcePath);
        try
        {
            return readServerCollection( new InputStreamReader (json.openStream()) );
        }
        catch (IOException ex)
        {
            Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
            //Return null because this is an error you want to detect right away.
            return null;
        }
    }
    
    private Collection<Server> readServerCollection(File jsonFile)
    {
        try
        {
            return readServerCollection( new FileReader(jsonFile) );
        }
        catch (IOException ex)
        {
            Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
            //Return an empty server list, because sometimes a file won't exist and that is perfectly normal.
            return new ArrayList<>();
        }
    }
    
    private Collection<Server> readServerCollection(Reader reader) throws IOException
    {
        try (BufferedReader br = new BufferedReader( reader )) {
        	Gson gson = new Gson();
        	Type collectionType = new TypeToken<Collection<Server>>(){}.getType();
        	Collection<Server> result =  gson.fromJson(br, collectionType);
            return result;        
        }
    }
    
    private List<Server> getAddedServers()
    {
        List<Server> result = new ArrayList<>();
        for( Server s : serverList )
        {
            if( s.getType() == Server.Type.ADDED )
                result.add(s);
        }
        return result;
    }
    
    private List<Server> getDefaultServers()
    {
        List<Server> result = new ArrayList<>();
        for( Server s : serverList )
        {
            if( s.getType() == Server.Type.DEFAULT )
                result.add(s);
        }
        return result;
    }
    
    public void save()
    {
        saveAddedServers();
        saveDefaultServerCredentials();
    }
    
    private void saveAddedServers()
    {
        File configDir = CyObjectManager.INSTANCE.getConfigDir();
        File addedServersFile = new File(configDir, FilePath.ADDED_SERVERS);
        saveServerList( getAddedServers(), addedServersFile.getAbsolutePath() );
    }
    
    private void saveDefaultServerCredentials()
    {
        File configDir = CyObjectManager.INSTANCE.getConfigDir();
        File defaultServerCredentialsFile = new File(configDir, FilePath.DEFAULT_SERVER_CREDENTIALS );
        saveServerList( defaultServerCredentials, defaultServerCredentialsFile.getAbsolutePath() );
    }
    
    private void saveServerList( List<Server> serverList2, String filePath )
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson( serverList2 );
        File serverFile = new File( filePath );
        try
        {
            Files.write(json, serverFile, Charsets.UTF_8);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }    
    }
    
    /**
     * Checks whether a proposed name for a server has already been used.
     * This is currently used when editing an existing server.
     * @param name The name to be checked.
     * @return Whether the name is used or not.
     */
    public boolean isNameUsed(String name)
    {
        return namesUsed.contains(name);
    }
    
    /**
     * This method is used to inform the server list that one of the servers
     * already in the list has been edited so that it can fire the appropriate
     * events to notify and update the GUI.
     * @param editedServer The server that was just edited.
     */
    public void edited(Server editedServer)
    {
        int indexOfEditedServer = serverList.indexOf(editedServer);
        fireContentsChanged(this, indexOfEditedServer, indexOfEditedServer);
    }
    
 /*   public void rename(Server server)
    {
        //TODO Implement this
    } */
    
    public void delete(Server server)
    {
        if( !serverList.contains(server) )
            throw new IllegalArgumentException("The server to be deleted must exist in the list.");
        int indexOfDeletedServer = serverList.indexOf(server);
        serverList.remove(server);
        this.fireIntervalRemoved(server, indexOfDeletedServer, indexOfDeletedServer);
    }
    
    public void add(Server server) throws Exception
    {
        if( namesUsed.contains(server.getName()) )
            throw new Exception( ErrorMessage.serverNameAlreadyUsed );
        namesUsed.add(server.getName());
        serverList.add(server);
        int indexOfAddedServer = serverList.indexOf(server);
        fireContentsChanged(this, indexOfAddedServer, indexOfAddedServer);
    }
    
    /**
     * This method registers the credentials for a default server so they may be saved and retrieved later.
     * @param defaultServer The default server for which credentials are being saved.
     */
    public void registerDefaultServerCredentials(Server defaultServer)
    {
        if( defaultServer.getType() != Server.Type.DEFAULT )
            throw new IllegalArgumentException("Only DEFAULT server credentials may be be seperately registered.");
        Server credentials = new Server(defaultServer);
        //Change the type to CREDENTIALS.
        credentials.setType(Server.Type.CREDENTIALS);
        //Search for an existing credentials to replace, if any.
        Server existingCredentialsToReplace = null;
        for( Server s : defaultServerCredentials )
        {
            if( credentials.hasSameName(s) )
                existingCredentialsToReplace = s;
        }
        //If credentials for the server do not already exist, add them. Otherwise, replace them.
        if( existingCredentialsToReplace == null )
            defaultServerCredentials.add(credentials);
        else
        {
            int index = defaultServerCredentials.indexOf(existingCredentialsToReplace);
            defaultServerCredentials.set( index, credentials);
        }
    }
    
    public void serverDescriptionChanged(Server changedServer)
    {
        if( !serverList.contains(changedServer) )
            return;
        if( changedServer.getType() == Server.Type.DEFAULT )
        {
            registerDefaultServerCredentials(changedServer);
        }
        int indexOfChangedServer = serverList.indexOf(changedServer);
        fireContentsChanged(this, indexOfChangedServer, indexOfChangedServer);
    }
    
    public String findNextAvailableName(String startName)
    {
        int count = 0;
        while( true )
        {
            String candidate = startName + "-" + ++count;
            if( !namesUsed.contains( candidate ) )
                return candidate;           
        }
    }
    
    public Server get(int index)
    {
        return serverList.get(index);
    }
    
    @Override
    public int getSize()
    {
        return serverList.size();
    }

    @Override
    public Server getElementAt(int index)
    {
        return serverList.get(index);
    }
    
}
