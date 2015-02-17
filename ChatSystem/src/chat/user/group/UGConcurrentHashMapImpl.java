/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.user.group;

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
	 */
	private final static int FRESH_PERIOD = 6*1000;
	
	/**
	 * The cached set of active users.
	 */
	private Set<User> activeUsers;
	
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
		activeUsers = new HashSet<User>(ChatSystemConstants.INIT_CAP);
	}
	
	public UGConcurrentHashMapImpl(int concurrencyLevel){
		userGroup =
			new ConcurrentHashMap<String, User>(ChatSystemConstants.INIT_CAP, 
					ChatSystemConstants.LOAD_FACTOR,
					concurrencyLevel);
		activeUsers = new HashSet<User>(ChatSystemConstants.INIT_CAP);
	}
	
	public boolean add(final User user) {
		
		/**
		 * Use synchronized block to help avoid adding
		 * two users with the same name.
		 */
		synchronized(userGroup){
			if(userGroup.containsKey(user.getName())){
				return false;
			}
			userGroup.put(user.getName(), user);
			
			return true;
		}		
	}

	public User remove(final String name) {
		
		return userGroup.remove(name);
	}

	public boolean contains(final String name) {
		return userGroup.containsKey(name);
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

	public Set<User> getActiveUsers() {

		synchronized(this){
			long current_time = System.currentTimeMillis();

			// Check if the cached set of active users is valid.
			if((activeTimestamp + FRESH_PERIOD) > current_time ){
				return activeUsers;
			}

			// Else refresh the cached set of active users.
			Set<User> new_active_users = new HashSet<User>(userGroup.size());
			Set<String> inactive_names = new HashSet<String>();
			
			// Check if user is alive.
			for(User usr : userGroup.values()){
				if((usr.getLastHeartBeat() + ChatSystemConstants.HEARTBEAT_RATE) 
						< current_time){
					// User becomes inactive
					inactive_names.add(usr.getName());
				}
				else{
					new_active_users.add(usr);
				}
			}

			// Remove inactive users from user group.
			for(String inactive_name: inactive_names){
				remove(inactive_name);
			}

			// Update time stamp
			activeTimestamp = System.currentTimeMillis();
			activeUsers = new_active_users;
			
			return activeUsers;
		}
	}
}
