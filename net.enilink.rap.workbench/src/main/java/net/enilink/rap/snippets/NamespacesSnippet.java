package net.enilink.rap.snippets;

import net.enilink.commons.ui.editor.IEditorPart;
import net.enilink.komma.owl.editor.ontology.NamespacesPart;

public class NamespacesSnippet extends AbstractSnippet {
	@Override
	IEditorPart createEditorPart() {
		return new NamespacesPart();
	}
}
