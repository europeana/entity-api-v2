
package eu.europeana.api.commons.nosql.service;

import eu.europeana.api.commons.definitions.exception.ApiWriteLockException;
import eu.europeana.api.commons.nosql.entity.ApiWriteLock;

/**
 * duplicated interface from API Commons to avoid dependency on commons-nosql
 * @author GordeaS
 *
 */
public interface ApiWriteLockService {

	ApiWriteLock lock(String lockType) throws ApiWriteLockException;
	
	void unlock(ApiWriteLock writeLock) throws ApiWriteLockException;
	
	ApiWriteLock getLastActiveLock(String lockType) throws ApiWriteLockException;
	
	ApiWriteLock getLockById(String id) throws ApiWriteLockException;

	void deleteAllLocks() throws ApiWriteLockException;
	
}
