package com.ur.urcap.sample.scriptwrapper.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class ScriptWrapperProgramNodeService implements ProgramNodeService {

	@Override
	public String getId() {
		return "ScriptWrapperNode";
	}

	@Override
	public String getTitle() {
		return "Wrap Script";
	}

	@Override
	public InputStream getHTML() {
		return this.getClass().getResourceAsStream("/com/ur/urcap/sample/scriptwrapper/impl/programnode.html");
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public boolean isChildrenAllowed() {
		return false;
	}

	@Override
	public ProgramNodeContribution createNode(URCapAPI urCapAPI, DataModel dataModel) {
		return new ScriptWrapperProgramNodeContribution(urCapAPI, dataModel);
	}
}
