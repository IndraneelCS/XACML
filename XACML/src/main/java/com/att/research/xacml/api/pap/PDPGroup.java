/*
 *
 *          Copyright (c) 2014,2019  AT&T Knowledge Ventures
 *                     SPDX-License-Identifier: MIT
 */
package com.att.research.xacml.api.pap;

import java.util.Properties;
import java.util.Set;

public interface PDPGroup {
	
	/*
	 * The ID is a string that is unique within the current set of groups.
	 * It is generated by the code.
	 * It is distinct from the name in that the ID must be able to be a directory name
	 * and a key in a properties file.
	 * The ID does not need to be unique forever.  It just needs to distinguish this group from other groups that currently exist.
	 */
	public String						getId();
	
	public boolean						isDefaultGroup();
	
	/*
	 * The name is a free-form string that is user input.
	 */
	public String 						getName();
	
	public void							setName(String name);
	
	public String 						getDescription();
	
	public void							setDescription(String description);
	
	public PDPGroupStatus				getStatus();

	public Set<PDP> 					getPdps();
	
	public Set<PDPPolicy>	 			getPolicies();
	
	public PDPPolicy					getPolicy(String id);

	public Properties		 			getPolicyProperties();

	public Set<PDPPIPConfig> 			getPipConfigs();
	
	public PDPPIPConfig					getPipConfig(String id);
	
	public Properties		 			getPipConfigProperties();

	public void							repair();
		
}
