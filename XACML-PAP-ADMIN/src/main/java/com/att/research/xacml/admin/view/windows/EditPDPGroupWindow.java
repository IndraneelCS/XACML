/*
 *
 *          Copyright (c) 2014,2019  AT&T Knowledge Ventures
 *                     SPDX-License-Identifier: MIT
 */
package com.att.research.xacml.admin.view.windows;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.att.research.xacml.admin.model.PDPPIPContainer;
import com.att.research.xacml.admin.model.PDPPolicyContainer;
import com.att.research.xacml.admin.util.AdminNotification;
import com.att.research.xacml.api.pap.PAPEngine;
import com.att.research.xacml.api.pap.PAPException;
import com.att.research.xacml.api.pap.PDPGroup;
import com.att.research.xacml.api.pap.PDPPIPConfig;
import com.att.research.xacml.api.pap.PDPPolicy;
import com.att.research.xacml.std.pap.StdPDPGroup;
import com.att.research.xacml.std.pap.StdPDPPolicy;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class EditPDPGroupWindow extends Window {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private Button buttonSave;
	@AutoGenerated
	private Table tablePIP;
	@AutoGenerated
	private Table tablePolicies;
	@AutoGenerated
	private TextArea textDescription;
	@AutoGenerated
	private TextField textName;
	
	
	private static final Action ADD_POLICY = 		new Action ("Add New Policy");
	private static final Action REMOVE_POLICY = 	new Action ("Remove Policy");
	private static final Action MAKE_ROOT = 		new Action ("Make Root");
	private static final Action MAKE_REFERENCED = 	new Action ("Make Referenced");
	
	private static final Action EDIT_CONFIG = 		new Action("Edit Configurations");

	//
	// ?? Why is this static?
	//
	private static PDPPolicyContainer policyContainer;
	private PDPPIPContainer pipContainer;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final EditPDPGroupWindow self = this;
	private final StdPDPGroup group;
	private boolean isSaved = false;
	
	// this is the version that contains all of the edits made by the user.
	// it may be a copy of the original object (edited) or a new one.
	private StdPDPGroup updatedObject;
	
	private PAPEngine papEngine;
	private List<PDPGroup> groups;
	
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 * @param group 
	 * @param list 
	 * @param engine 
	 */
	public EditPDPGroupWindow(StdPDPGroup group, List<PDPGroup> list, PAPEngine engine) {
		buildMainLayout();
		//setCompositionRoot(mainLayout);
		setContent(mainLayout);
		//
		// Save pointers
		//
		this.group = group;
		this.groups = list;
		this.papEngine = engine;
		//
		// Initialize
		//
		this.initialize();
		//
		// Shortcuts
		//
		this.setCloseShortcut(KeyCode.ESCAPE);
		this.buttonSave.setClickShortcut(KeyCode.ENTER);
		//
		// Focus
		//
		this.textName.focus();
	}
	
	protected void initialize() {
		this.initializeText();
		this.initializeButton();
		this.initializeTables();
	}
	
	protected void initializeText() {
		this.textName.setNullRepresentation("");
		this.textDescription.setNullRepresentation("");
		if (this.group != null) {
			this.textName.setValue(this.group.getName());
			this.textDescription.setValue(this.group.getDescription());
		}
		//
		// Validation
		//
		this.textName.addValidator(new Validator() {
			private static final long serialVersionUID = 1L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				assert(value instanceof String);
				if (value == null) {
					throw new InvalidValueException("The name cannot be blank.");
				}
				// Group names must be unique so that user can distinguish between them (and we can create unique IDs from them)
				for (PDPGroup g : self.groups) {
					if (group != null && g.getId().equals(group.getId())) {
						// ignore this group - we may or may not be changing the name
						continue;
					}
					if (g.getName().equals(value.toString())) {
						throw new InvalidValueException("Name must be unique");
					}
				}
			}
		});
		this.textName.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void textChange(TextChangeEvent event) {
				if (event.getText() == null || event.getText().isEmpty()) {
					self.buttonSave.setEnabled(false);
				} else {
					self.buttonSave.setEnabled(true);
				}
			}
		});
	}
	
	protected void initializeTables() {
		this.initializePolicyTable();
		this.initializePIPTable();
	}
	
	protected void initializePolicyTable() {
		if (this.group == null) {
			this.tablePolicies.setVisible(false);
			return;
		}
		//
		// GUI properties
		//
		EditPDPGroupWindow.policyContainer = new PDPPolicyContainer(group);
		this.tablePolicies.setContainerDataSource(EditPDPGroupWindow.policyContainer);
		this.tablePolicies.setVisibleColumns("Root", "Name", "Version", "Id");//, "Description");
		this.tablePolicies.setPageLength(EditPDPGroupWindow.policyContainer.size() + 1);
		this.tablePolicies.setSelectable(true);
		this.tablePolicies.setSizeFull();
		/*
		 * Not in this release.
		 * 
		this.tablePolicies.setColumnCollapsingAllowed(true);
		this.tablePolicies.setColumnCollapsed("Description", true);
		//
		// Generated columns
		//
		this.tablePolicies.addGeneratedColumn("Description", new ColumnGenerator() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				TextArea area = new TextArea();
				if (itemId != null && itemId instanceof PDPPolicy) {
					area.setValue(((PDPPolicy)itemId).getDescription());
				}
				area.setNullRepresentation("");
				area.setWidth("100.0%");
				return area;
			}
		});
		*/
		//
		// Actions
		//
		this.tablePolicies.addActionHandler(new Handler() {
			private static final long serialVersionUID = 1L;

			@Override
			public Action[] getActions(Object target, Object sender) {
				if (target == null) {
					return new Action[] {ADD_POLICY};
				}
				return new Action[] {ADD_POLICY, REMOVE_POLICY, MAKE_ROOT, MAKE_REFERENCED};
			}

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (action == ADD_POLICY) {

					final SelectWorkspacePoliciesWindow policiesWindow = new SelectWorkspacePoliciesWindow();
					policiesWindow.setCaption("Select Policy to Add");
					policiesWindow.setModal(true);
					policiesWindow.addCloseListener(new CloseListener() {
						private static final long serialVersionUID = 1L;
					
						@Override
						public void windowClose(CloseEvent event) {
							//
							// Did the user hit save?
							//
							if (policiesWindow.isSaved() == false) {
								return;
							}
							//
							// Get the selected policy
							//
							StdPDPPolicy selectedPolicy = policiesWindow.getSelectedPolicy();
							if (selectedPolicy != null) {

								// do not allow multiple copies of same policy
								for (Object existingPolicy : EditPDPGroupWindow.policyContainer.getItemIds()) {
									if (selectedPolicy.getId().equals(((PDPPolicy)existingPolicy).getId())) {
										AdminNotification.warn("Cannot re-add Policy with the same ID (i.e. same Name, source Sub-Domain and Version)");
										return;
									}
								}
								// copy policy to PAP
								try {
									papEngine.copyPolicy(selectedPolicy, self.group);
								} catch (PAPException e) {
									AdminNotification.warn("Unable to copy Policy '" + selectedPolicy.getPolicyId() + "' to PAP: " + e.getMessage());
									return;
								}
								
								// add Policy to group
								EditPDPGroupWindow.policyContainer.addItem(selectedPolicy);
								self.markAsDirtyRecursive();								
							}
						}
					});
					policiesWindow.center();
					UI.getCurrent().addWindow(policiesWindow);
					return;
				}
				if (action == REMOVE_POLICY) {
					assert (target != null);
					PDPPolicy policy = (PDPPolicy)target;
					EditPDPGroupWindow.policyContainer.removeItem(policy);
					self.markAsDirtyRecursive();
					return;
				}
				if (action == MAKE_ROOT) {
					assert (target != null);
					assert (target instanceof StdPDPPolicy);
					StdPDPPolicy policy = (StdPDPPolicy)target;
					EditPDPGroupWindow.policyContainer.getItem(policy).getItemProperty("Root").setValue(true);
					self.markAsDirtyRecursive();
					return;
				}
				if (action == MAKE_REFERENCED) {
					assert (target != null);
					assert (target instanceof StdPDPPolicy);
					StdPDPPolicy policy = (StdPDPPolicy)target;
					EditPDPGroupWindow.policyContainer.getItem(policy).getItemProperty("Root").setValue(false);
					self.markAsDirtyRecursive();
					return;
				}
				
				AdminNotification.error("Unrecognized action '" + action + "' on target '" + target + "'");
			}
		});
	}
	
	protected void initializePIPTable() {
		if (this.group == null) {
			this.tablePIP.setVisible(false);
			return;
		}
		//
		// Setup data source and GUI properties
		//
		this.pipContainer = new PDPPIPContainer(group);
		this.tablePIP.setContainerDataSource(this.pipContainer);
		this.tablePIP.setPageLength(this.pipContainer.size() + 2);
		this.tablePIP.setSelectable(true);
		this.tablePIP.setSizeFull();
		//
		// Add the action handler
		//
		this.tablePIP.addActionHandler(new Handler() {
			private static final long serialVersionUID = 1L;

			@Override
			public Action[] getActions(Object target, Object sender) {
				return new Action[] {EDIT_CONFIG};
			}

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (action == EDIT_CONFIG) {
					self.editPIPConfiguration();
					return;
				}
			}
		});
	}

	protected void editPIPConfiguration() {
		final SelectPIPConfigurationWindow window = new SelectPIPConfigurationWindow(this.group);
		window.setCaption("Select PIP Configurations");
		window.setModal(true);
		window.addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				//
				// Did the user click save button?
				//
				if (window.isSaved() == false) {
					return;
				}
				//
				// Yes - save the PIP configuration
				//
				Set<PDPPIPConfig> configs = window.getSelectedConfigs();
				assert(configs != null);
				self.group.setPipConfigs(configs);
				//
				// Update the container
				//
				self.pipContainer.refresh();
			}
		});
		window.center();
		UI.getCurrent().addWindow(window);
	}

	protected void initializeButton() {
		this.buttonSave.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					//
					// Validate
					//
					self.textName.commit();
					//
					// All good save everything
					//
					self.doSave();
					//
					// mark ourselves as saved
					//
					self.isSaved = true;
					//
					// Close the window
					//
					self.close();
				} catch (InvalidValueException e) {
					//
					// Ignore, Vaadin will display our message
					//
				}
			}
		});
	}
	
	protected void doSave() {
		if (this.group == null) {
			return;
		}
		StdPDPGroup updatedGroupObject = new StdPDPGroup(
				this.group.getId(), 
				this.group.isDefaultGroup(), 
				this.textName.getValue(), 
				this.textDescription.getValue(), 
				null);
		// replace the original set of Policies with the set from the container (possibly modified by the user)
		Set<PDPPolicy> changedPolicies = new HashSet<PDPPolicy>();
		changedPolicies.addAll((Collection<PDPPolicy>) EditPDPGroupWindow.policyContainer.getItemIds());
		updatedGroupObject.setPolicies(changedPolicies);
		updatedGroupObject.setPdps(this.group.getPdps());
		// replace the original set of PIP Configs with the set from the container
//TODO - get PIP Configs from a container used to support editing
//			selfPDPObject.getPipConfigs().clear();
//			selfPDPObject.getPipConfigs().addAll(containerGroup.getPipConfigs());
		updatedGroupObject.setPipConfigs(this.group.getPipConfigs());
		// copy those things that the user cannot change from the original to the new object
		updatedGroupObject.setStatus(this.group.getStatus());
		this.updatedObject = updatedGroupObject;			
	}
	
	public boolean isSaved() {
		return this.isSaved;
	}
	
	public String getGroupName() {
		return this.textName.getValue();
	}
	
	public String getGroupDescription() {
		return this.textDescription.getValue();
	}
	
	public PDPGroup getUpdatedObject() {
		return this.updatedObject;
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100.0%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		
		// top-level component properties
		setWidth("-1px");
		setHeight("-1px");
		
		// textName
		textName = new TextField();
		textName.setCaption("Group Name");
		textName.setImmediate(false);
		textName.setWidth("-1px");
		textName.setHeight("-1px");
		textName.setRequired(true);
		mainLayout.addComponent(textName);
		
		// textDescription
		textDescription = new TextArea();
		textDescription.setCaption("Group Description");
		textDescription.setImmediate(false);
		textDescription.setWidth("100.0%");
		textDescription.setHeight("-1px");
		textDescription.setNullSettingAllowed(true);
		mainLayout.addComponent(textDescription);
		mainLayout.setExpandRatio(textDescription, 1.0f);
		
		// tablePolicies
		tablePolicies = new Table();
		tablePolicies.setCaption("Policies");
		tablePolicies.setImmediate(false);
		tablePolicies.setWidth("-1px");
		tablePolicies.setHeight("-1px");
		mainLayout.addComponent(tablePolicies);
		mainLayout.setExpandRatio(tablePolicies, 1.0f);
		
		// tablePIP
		tablePIP = new Table();
		tablePIP.setCaption("PIP Configurations");
		tablePIP.setImmediate(false);
		tablePIP.setWidth("-1px");
		tablePIP.setHeight("-1px");
		mainLayout.addComponent(tablePIP);
		mainLayout.setExpandRatio(tablePIP, 1.0f);
		
		// buttonSave
		buttonSave = new Button();
		buttonSave.setCaption("Save");
		buttonSave.setImmediate(true);
		buttonSave.setWidth("-1px");
		buttonSave.setHeight("-1px");
		mainLayout.addComponent(buttonSave);
		mainLayout.setComponentAlignment(buttonSave, new Alignment(48));
		
		return mainLayout;
	}
}
