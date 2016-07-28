package net.enilink.rap.workbench;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;

import net.enilink.komma.common.util.IResourceLocator;
import net.enilink.komma.edit.domain.AdapterFactoryEditingDomain;
import net.enilink.komma.edit.ui.editor.KommaMultiPageEditor;
import net.enilink.komma.edit.ui.editor.KommaMultiPageEditorSupport;
import net.enilink.komma.edit.ui.views.IViewerMenuSupport;
import net.enilink.komma.model.IModel;
import net.enilink.komma.model.IModelSet;

/**
 * This is the basic eniLINK editor.
 */
public class BasicModelEditor extends KommaMultiPageEditor implements IViewerMenuSupport {
	/**
	 * This creates a model editor.
	 */
	public BasicModelEditor() {
		super();
	}

	class BasicEditorSupport extends KommaMultiPageEditorSupport<BasicModelEditor> {
		public BasicEditorSupport() {
			super(BasicModelEditor.this);
			disposeModelSet = false;
		}

		@Override
		protected IResourceLocator getResourceLocator() {
			return EnilinkWorkbenchPlugin.INSTANCE;
		}

		@Override
		protected IModelSet createModelSet() {
			ModelEditorInput input = (ModelEditorInput) editor.getEditorInput();
			return input.getModel().getModelSet();
		}

		@Override
		public void createModel() {
			ModelEditorInput input = (ModelEditorInput) editor.getEditorInput();
			model = input.getModel();
		}

		// @Override
		// protected AdapterFactoryEditingDomain getExistingEditingDomain(
		// IModelSet modelSet) {
		// // force the creation of a new editing domain for this editor
		// return null;
		// }

		@Override
		public Object getAdapter(Class key) {
			if (IModel.class.equals(key)) {
				return model;
			}
			return super.getAdapter(key);
		}
	}

	@Override
	protected KommaMultiPageEditorSupport<? extends KommaMultiPageEditor> createEditorSupport() {
		return new BasicEditorSupport();
	}

	@Override
	public void createPages() {
		// Creates the model from the editor input
		getEditorSupport().createModel();

		addPage(new Composite(getContainer(), SWT.NONE));
		setPageText(0, getEditorSupport().getString("_UI_SelectionPage_label"));

		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				setActivePage(0);
			}
		});

		// Ensures that this editor will only display the page's tab
		// area if there are more than one page
		getContainer().addControlListener(new ControlAdapter() {
			boolean guard = false;

			@Override
			public void controlResized(ControlEvent event) {
				if (!guard) {
					guard = true;
					getEditorSupport().hideTabs();
					guard = false;
				}
			}
		});

		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				getEditorSupport().updateProblemIndication();
			}
		});
	}

	@Override
	public void createContextMenuFor(StructuredViewer viewer) {
		getEditorSupport().createContextMenuFor(viewer);
	}
}
