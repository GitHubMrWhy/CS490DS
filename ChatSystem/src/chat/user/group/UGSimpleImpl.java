/**
 * CS490-Chat system
 * Author: Zhiyuan Zheng 
 * @ Purdue 2015
 */
package chat.user.group;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chat.constant.ChatSystemConstants;

/**
 * This is a naive implementation of UserGroup.
 * It is ideal for single-thread chat server. It is not thread-safe!!!
 * 
 */
public class UGSimpleImpl implements UserGroup {

	/**
	 * The valid time period of the cached active user list.
	 */
	private final static int VALID_PERIOD = (int) (ChatSystemConstants.HEARTBEAT_RATE*1.2);


	/**
	 * The time stamp of the cached set.
	 */
	private long activeTimestamp = 0;

	/**
	 * The user group is a map as <userName, User object>. 
	 */
	private final Map<String, User> userGroup;

	
	public UGSimpleImpl(){
		userGroup =
				new HashMap<String, User>(ChatSystemConstants.INIT_CAP);
	}

	public boolean add(final User user) {
		

		if(contains(user.getName())){
			return false;
		}
		userGroup.put(user.getName(), user);

		return true;

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
		
		final long current_time = System.currentTimeMillis();

		// Check if the cached set of active users is valid.
		if((activeTimestamp + VALID_PERIOD) > current_time ){
			return userGroup.values();
		}

		// Else refresh the cached set of active users.
		Set<String> inactive_names = new HashSet<String>();

		// Check if user is alive.
		for(User usr : userGroup.values()){
				
			if(! usr.isActive(current_time)){
				// User becomes inactive
				inactive_names.add(usr.getName());
			}
		}

		// Remove inactive users from user group.
		for(String inactive_name: inactive_names){
			remove(inactive_name);
		}

		// Update time stamp
		activeTimestamp = System.currentTimeMillis();

		return userGroup.values();
	}


}
