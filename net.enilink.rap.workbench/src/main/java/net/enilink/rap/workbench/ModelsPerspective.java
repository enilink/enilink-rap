package net.enilink.rap.workbench;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import net.enilink.komma.edit.ui.properties.EditUIViews;
import net.enilink.komma.owl.editor.OWLViews;

public class ModelsPerspective implements IPerspectiveFactory {
	@Override
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout topLeft = layout.createFolder("topLeft",
				IPageLayout.LEFT, 0.3f, editorArea);
		topLeft.addView(ModelsView.ID);

		// Bottom left: Outline view and Property Sheet view
		IFolderLayout bottomLeft = layout.createFolder("bottomLeft",
				IPageLayout.BOTTOM, 0.7f, "topLeft");
		bottomLeft.addView(OWLViews.ID_IMPORTS);

		IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, IPageLayout.RATIO_MIN, editorArea);
		// bottom.addView(IPageLayout.ID_PROP_SHEET);
		bottom.addView(EditUIViews.ID_DETAILS);
		bottom.addView(OWLViews.ID_NAMESPACES);
		bottom.addView(AclView.ID);
		bottom.addView("net.enilink.komma.sparql.ui.views.SparqlView");
	}
}
