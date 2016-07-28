package net.enilink.rap.workbench;

import net.enilink.komma.edit.ui.views.AbstractEditingDomainView;

public class AclView extends AbstractEditingDomainView {
	public static final String ID = "net.enilink.rap.workbench.aclView";

	public AclView() {
		setEditPart(new AclPart());
	}

	@Override
	protected void installSelectionProvider() {
		// do nothing
	}
}