package org.phenoscape.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang.StringUtils;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.PhenotypeProposal;
import org.phenoscape.model.State;
import org.phenoscape.model.Character;

public class PhenotypeProposalsLoader {

	private final DataSet dataset;
	private final OBOSession session;

	public PhenotypeProposalsLoader(DataSet dataset, OBOSession session) {
		this.dataset = dataset;
		this.session = session;
	}

	public void loadProposals(File file) throws IOException {
		final CSVParser parser = new CSVParser(new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")));
		final String[][] csv = parser.getAllValues();
		for (String[] line : csv) {
			if (!StringUtils.isBlank(line[2])) {
				this.addProposalToDataSet(this.createProposal(line));
			}
		}
	}

	private PhenotypeProposal createProposal(String[] line) {
		final PhenotypeProposal proposal = new PhenotypeProposal(line[1], line[2]);
		proposal.setEntityText(StringUtils.trimToNull(line[4]));
		proposal.setQualityText(StringUtils.trimToNull(line[5]));
		if (proposal.getQualityText() == null) {
			proposal.setQualityText(StringUtils.trimToNull(line[16]));
		}
		proposal.setQualityModifierText(StringUtils.trimToNull(line[6]));
		proposal.setEntityLocatorText(StringUtils.trimToNull(line[7]));
		proposal.getEntities().addAll(this.getTerms(StringUtils.trimToEmpty(line[21])));
		proposal.getQualities().addAll(this.getTerms(StringUtils.trimToEmpty(line[11])));
		proposal.getEntityLocators().addAll(this.getTerms(StringUtils.trimToEmpty(line[19])));
		proposal.getQualityModifiers().addAll(this.getTerms(StringUtils.trimToEmpty(line[13])));
		proposal.setQualityIsNegated(!StringUtils.isBlank(line[16]));
		final List<OBOClass> negatedQualityParent = this.getTerms(StringUtils.trimToEmpty(line[18]));
		if (!negatedQualityParent.isEmpty()) {
			proposal.setNegatedQualityParent(negatedQualityParent.get(0));
		}
		return proposal;
	}

	private void addProposalToDataSet(PhenotypeProposal proposal) {
		final State state = this.findState(proposal.getCharacterID(), proposal.getStateID());
		if (state != null) {
			state.setProposal(proposal);
		}
	}

	private List<OBOClass> getTerms(String ids) {
		final String[] items = ids.split(",");
		final List<OBOClass> terms = new ArrayList<OBOClass>();
		for (String item : items) {
			final IdentifiedObject term = this.session.getObject(item);
			if (term instanceof OBOClass) {
				terms.add((OBOClass)term);
			}
		}
		return terms;
	}

	private State findState(String characterID, String stateID) {
		for (Character character : this.dataset.getCharacters()) {
			if (characterID.equals(character.getStatesNexmlID())) {
				for (State state : character.getStates()) {
					if (stateID.equals(state.getNexmlID())) {
						return state;
					}
				}
			}
		}
		return null;
	}

}