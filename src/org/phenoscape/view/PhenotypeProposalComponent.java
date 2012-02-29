package org.phenoscape.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.obo.annotation.base.OBOUtil;
import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.util.TermUtil;
import org.oboedit.controller.SelectionManager;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.PhenotypeProposal;
import org.phenoscape.model.State;

import ca.odell.glazedlists.EventList;

public class PhenotypeProposalComponent extends PhenoscapeGUIComponent {

	private JTextField entityField;
	private JTextField locatorField;
	private JTextField qualityField;
	private JTextField relatedEntityField;
	private JCheckBox useEntity;
	private JComboBox selectedEntity;
	private JCheckBox useEntityLocator;
	private JComboBox selectedEntityLocator;
	private JCheckBox useQuality;
	private JComboBox selectedQuality;
	private JCheckBox useRelatedEntity;
	private JComboBox selectedRelatedEntity;

	public PhenotypeProposalComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
	}

	private void initializeInterface() {
		this.getController().getCurrentStatesSelectionModel().addListSelectionListener(new StateSelectionListener());
		this.setLayout(new GridBagLayout());
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridy = 0;
		constraints.gridx = 0;
		final JPanel phenotypePanel = new JPanel(new GridBagLayout());
		phenotypePanel.setBorder(BorderFactory.createTitledBorder("Proposed Phenotype"));
		this.add(phenotypePanel, constraints);
		final JButton phenotypeButton = new JButton(new AbstractAction("Add Phenotype") {

			@Override
			public void actionPerformed(ActionEvent event) {
				createAndAddPhenotype();
			}
		});
		constraints.anchor = GridBagConstraints.EAST;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.NONE;
		this.add(phenotypeButton, constraints);

		final GridBagConstraints phenotypeConstraints = new GridBagConstraints();
		phenotypeConstraints.gridx = 0;
		phenotypeConstraints.gridy = 0;
		phenotypeConstraints.anchor = GridBagConstraints.EAST;
		phenotypePanel.add(new JLabel("Entity:"), phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(new JLabel("Entity Locator:"), phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(new JLabel("Quality:"), phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(new JLabel("Related Entity:"), phenotypeConstraints);
		phenotypeConstraints.anchor = GridBagConstraints.CENTER;
		phenotypeConstraints.fill = GridBagConstraints.HORIZONTAL;
		phenotypeConstraints.weightx = 1;
		phenotypeConstraints.gridy = 0;
		phenotypeConstraints.gridx = 1;
		this.entityField  = new JTextField();
		this.entityField.setEditable(false);
		phenotypePanel.add(this.entityField, phenotypeConstraints);
		this.locatorField = new JTextField();
		this.locatorField.setEditable(false);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(this.locatorField, phenotypeConstraints);
		this.qualityField = new JTextField();
		this.qualityField.setEditable(false);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(this.qualityField, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		this.relatedEntityField = new JTextField();
		this.relatedEntityField.setEditable(false);
		phenotypePanel.add(this.relatedEntityField, phenotypeConstraints);

		phenotypeConstraints.gridy = 0;
		phenotypeConstraints.gridx = 2;
		phenotypeConstraints.anchor = GridBagConstraints.WEST;
		phenotypeConstraints.weightx = 0;
		this.useEntity = new JCheckBox();
		this.selectedEntity = new JComboBox();
		this.useEntityLocator = new JCheckBox();
		this.selectedEntityLocator = new JComboBox();
		this.useQuality = new JCheckBox();
		this.selectedQuality = new JComboBox();
		this.useRelatedEntity = new JCheckBox();
		this.selectedRelatedEntity = new JComboBox();
		phenotypePanel.add(selectedEntity, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(selectedEntityLocator, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(selectedQuality, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(selectedRelatedEntity, phenotypeConstraints);
		phenotypeConstraints.gridy = 0;
		phenotypeConstraints.gridx += 1;
		phenotypePanel.add(useEntity, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(useEntityLocator, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(useQuality, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(useRelatedEntity, phenotypeConstraints);
	}

	private State getSelectedState() {
		final EventList<State> selected = this.getController().getCurrentStatesSelectionModel().getSelected();
		if (selected.size() == 1) {
			return selected.get(0);
		} else {
			return null;
		}
	}

	private void stateSelectionDidChange() {
		final String unselectedTitle = "Phenotypes";
		final String selectedPrefix = "Phenotype Proposal for State: ";
		final State state = this.getSelectedState();
		if (state == null) {
			this.updatePanelTitle(unselectedTitle);
		} else {
			this.updatePanelTitle(selectedPrefix + state);
			this.clearFields();
			final PhenotypeProposal proposal = state.getProposal();
			if (proposal != null) {
				this.entityField.setText(proposal.getEntityText());
				this.locatorField.setText(proposal.getEntityLocatorText());
				this.qualityField.setText(proposal.getQualityText());
				this.relatedEntityField.setText(proposal.getQualityModifierText());
				this.configureTermSelector(this.useEntity, this.selectedEntity, proposal.getEntities());
				this.configureTermSelector(this.useEntityLocator, this.selectedEntityLocator, proposal.getEntityLocators());
				this.configureTermSelector(this.useQuality, this.selectedQuality, proposal.getProcessedQualities(this.getController().getOntologyController().getOBOSession()));
				this.configureTermSelector(this.useRelatedEntity, this.selectedRelatedEntity, proposal.getQualityModifiers());

				SelectionManager.selectTerms(this, this.collectTerms(proposal));
			}

		}
	}

	private Set<LinkedObject> collectTerms(PhenotypeProposal proposal) {
		final Set<LinkedObject> terms = new HashSet<LinkedObject>();
		terms.addAll(proposal.getEntities());
		terms.addAll(proposal.getEntityLocators());
		terms.addAll(proposal.getQualities());
		terms.addAll(proposal.getQualityModifiers());
		if (proposal.getNegatedQualityParent() != null) {
			terms.add(proposal.getNegatedQualityParent());
		}
		final Set<LinkedObject> ancestors = new HashSet<LinkedObject>();
		boolean first = true;
		for (LinkedObject term : terms) {
			if (first) {
				ancestors.addAll(TermUtil.getAncestors(term, null));
			} else {
				ancestors.retainAll(TermUtil.getAncestors(term, null));
			}
		}
		terms.addAll(ancestors);
		return terms;
	}

	private void createAndAddPhenotype() {
		final State state = this.getSelectedState();
		if (state != null) {
			final Phenotype phenotype = new Phenotype();
			if (this.useEntity.isEnabled()) {
				if (this.useEntityLocator.isEnabled()) {
					final OBOClass entity = (OBOClass)(this.selectedEntity.getSelectedItem());
					final OBOClass entityLocator = (OBOClass)(this.selectedEntityLocator.getSelectedItem());
					final Differentium diff = new Differentium();
					diff.setRelation((OBOProperty)(this.getController().getOntologyController().getOBOSession().getObject("OBO_REL:part_of")));
					diff.setTerm(entityLocator);
					final OBOClass composition = OBOUtil.createPostComposition(entity, Collections.singletonList(diff));
					phenotype.setEntity(composition);
				} else {
					phenotype.setEntity((OBOClass)(this.selectedEntity.getSelectedItem()));	
				}

			} else {
				if (this.useEntityLocator.isEnabled()) {
					phenotype.setEntity((OBOClass)(this.selectedEntityLocator.getSelectedItem()));
				}
			}
			if (this.useQuality.isEnabled()) {
				phenotype.setQuality((OBOClass)(this.selectedQuality.getSelectedItem()));
			}
			if (this.useRelatedEntity.isEnabled()) {
				phenotype.setRelatedEntity((OBOClass)(this.selectedRelatedEntity.getSelectedItem()));
			}
			if (phenotype.getEntity() != null || phenotype.getQuality() != null || phenotype.getRelatedEntity() != null) {
				state.addPhenotype(phenotype);
			}
		}
	}

	private void configureTermSelector(JCheckBox checkbox, JComboBox combobox, List<OBOClass> terms) {
		if (!terms.isEmpty()) {
			checkbox.setEnabled(true);
			checkbox.setSelected(true);
			combobox.setEnabled(true);
			for (OBOClass entity: terms) {
				combobox.addItem(entity);
			}
		}
	}

	private void clearFields() {
		this.entityField.setText(null);
		this.locatorField.setText(null);
		this.qualityField.setText(null);
		this.relatedEntityField.setText(null);
		this.selectedEntity.removeAllItems();
		this.selectedEntity.setEnabled(false);
		this.selectedEntityLocator.removeAllItems();
		this.selectedEntityLocator.setEnabled(false);
		this.selectedQuality.removeAllItems();
		this.selectedQuality.setEnabled(false);
		this.selectedRelatedEntity.removeAllItems();
		this.selectedRelatedEntity.setEnabled(false);
		this.useEntity.setEnabled(false);
		this.useEntity.setSelected(false);
		this.useQuality.setEnabled(false);
		this.useQuality.setSelected(false);
		this.useEntityLocator.setEnabled(false);
		this.useEntityLocator.setSelected(false);
		this.useQuality.setEnabled(false);
		this.useQuality.setSelected(false);
		this.useRelatedEntity.setEnabled(false);
		this.useRelatedEntity.setSelected(false);
	}

	private class StateSelectionListener implements ListSelectionListener {

		public StateSelectionListener() {
			stateSelectionDidChange();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			stateSelectionDidChange();      
		}

	}

}