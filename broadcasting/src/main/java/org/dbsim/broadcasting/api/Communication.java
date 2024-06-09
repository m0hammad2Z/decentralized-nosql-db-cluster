package org.dbsim.broadcasting.api;

import org.dbsim.broadcasting.exception.ConnectionFailedException;

public interface Communication extends Runnable{
    void start() throws ConnectionFailedException;
    void shutdown() ;
}
