/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.user.group;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import chat.constant.*;

/** 
 * An implementation of UserGroup
 * using ConcurrentHashMap as its underlying data structure,
 * which is thread-safe and guarantees performance
 * under intensive read/write operations.
 * 
 * This implementation is ideal for multi-threaded chat server.
 */
public class UGConcurrentHashMapImpl implements UserGroup {

	/**
	 * The valid time period of active_users.
	 * The factor is for making the period longer than heartbeat_rate
	 * in case that service executor is busy.
	 */
	private final static int VALID_PERIOD = (int) (ChatSystemConstants.HEARTBEAT_RATE * 1.2);
	
	/**
	 * The time stamp of the cached set.
	 */
	private long activeTimestamp = 0;
	
	/**
	 * The user group is a map as <userName, User object>. 
	 */
	private final Map<String, User> userGroup;
	
	
	
	public UGConcurrentHashMapImpl(){
		userGroup = new ConcurrentHashMap<String, User>();
	}
	
	/**
	 * Constructor.
	 * @param concurrencyLevel
	 * 		the estimated number of concurrently updating threads.
	 */
	public UGConcurrentHashMapImpl(int concurrencyLevel){
		userGroup =
			new ConcurrentHashMap<String, User>(ChatSystemConstants.INIT_CAP, 
					ChatSystemConstants.LOAD_FACTOR,
					concurrencyLevel);
	}
	
	public boolean add(final User user) {
		
		/**
		 * Use synchronized block to help avoid adding
		 * two users with the same name.
		 */
		synchronized(userGroup){
			if(contains(user.getName())){
				return false;
			}
			userGroup.put(user.getName(), user);
			
			// TODO: Iterator of activeUser will throw ConcurrentModificationException;
			return true;	
		}		
	}

	public User remove(final String name) {
		
		return userGroup.remove(name);
	}

	public boolean contains(final String name) {
		final User user = userGroup.get(name);
		
		if (user == null){
			return false;
		}
		else {
			if(user.isActive(System.currentTimeMillis())) {
				return true;
			}
			else{
				userGroup.remove(name);
				return false;
			}
		}
	}
	
	public String toString(){
		final StringBuilder sb = new StringBuilder();
		for(User usr : userGroup.values()){
			sb.append(usr);
			sb.append('\n');
		}
		
		return sb.toString();
	}

	public User get(final String name) {
		return userGroup.get(name);
	}

	public Collection<User> getActiveUsers() {

		synchronized(this){
			long current_time = System.currentTimeMillis();

			// Check if the cached set of active users is valid.
			if((activeTimestamp + VALID_PERIOD) > current_time ){
				return userGroup.values();
			}

			Set<String> inactive_names = new HashSet<String>();
			
			// Check if user is alive.
			for(User usr : userGroup.values()){
				
				if(!usr.isActive(current_time)){
					// User becomes inactive
					inactive_names.add(usr.getName());
				}
			}

			// Remove inactive users from user group.
			for(String inactive_name: inactive_names){
				this.remove(inactive_name);
			}

			// Update time stamp
			activeTimestamp = System.currentTimeMillis();

			return userGroup.values();
		}
	}
}
