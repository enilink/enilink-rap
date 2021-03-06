package net.enilink.rap.workbench;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;

import net.enilink.komma.common.adapter.IAdapterFactory;
import net.enilink.komma.edit.ui.provider.AdapterFactoryContentProvider;
import net.enilink.komma.edit.ui.provider.AdapterFactoryLabelProvider;
import net.enilink.komma.edit.ui.util.PartListener2Adapter;
import net.enilink.komma.edit.ui.views.AbstractEditingDomainPart;
import net.enilink.komma.model.IModel;
import net.enilink.komma.model.IModelSet;

public class ModelsPart extends AbstractEditingDomainPart {
	protected TableViewer openModelsViewer;
	protected TableViewer allModelsViewer;
	protected IModelSet modelSet;
	private IAdapterFactory adapterFactory;

	private List<IModel> openModels = new ArrayList<IModel>();
	private IModel currentModel;
	private OpenModelListener listener;

	private class OpenModelListener extends PartListener2Adapter {
		private IModel getModel(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(true);
			if (part instanceof IEditorPart
					&& ((IEditorPart) part).getEditorInput() instanceof ModelEditorInput) {
				return ((ModelEditorInput) ((IEditorPart) part)
						.getEditorInput()).getModel();
			}
			return null;
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			IModel model = getModel(partRef);
			if (model != null) {
				IModel lastModel = currentModel;
				currentModel = model;
				if (lastModel != null) {
					openModelsViewer.update(lastModel, null);
				}
				openModelsViewer.update(currentModel, null);
			}
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			IModel model = getModel(partRef);
			if (model != null && !openModels.contains(model)) {
				openModels.add(model);
				openModelsViewer.refresh();
			}
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			IModel model = getModel(partRef);
			if (model != null && openModels.remove(model)
					&& PlatformUI.isWorkbenchRunning()) {
				openModelsViewer.refresh();
			}
		}
	}

	private IDoubleClickListener openEditorListener = new IDoubleClickListener() {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			Object selected = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (selected instanceof IModel) {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					page.openEditor(new ModelEditorInput((IModel) selected),
							"net.enilink.rap.workbench.modelEditor", true,
							IWorkbenchPage.MATCH_INPUT);
				} catch (PartInitException e) {
					EnilinkWorkbenchPlugin.INSTANCE.log(e);
				}
			}
		}
	};

	@Override
	public void createContents(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		createActions();

		Section section = getWidgetFactory().createSection(parent,
				Section.EXPANDED);
		section.setText("Recent");

		Table openModelsTable = getWidgetFactory().createTable(section,
				SWT.NONE);
		section.setClient(openModelsTable);
		openModelsViewer = new TableViewer(openModelsTable);
		openModelsViewer.setContentProvider(new ArrayContentProvider());
		openModelsViewer.setInput(openModels);
		openModelsViewer.addDoubleClickListener(openEditorListener);
		openModelsViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						getForm().fireSelectionChanged(ModelsPart.this,
								event.getSelection());
					}
				});

		MenuManager menuManager = new MenuManager();
		menuManager.add(new Action("Close") {
			@Override
			public void run() {
				Object selected = ((IStructuredSelection) openModelsViewer
						.getSelection()).getFirstElement();
				if (selected instanceof IModel) {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					IEditorPart editor = page.findEditor(new ModelEditorInput(
							(IModel) selected));
					if (editor != null) {
						page.closeEditor(editor, false);
					}
				}
			}
		});
		Menu menu = menuManager
				.createContextMenu(openModelsViewer.getControl());
		openModelsViewer.getControl().setMenu(menu);

		listener = new OpenModelListener();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
				.addPartListener(listener);

		section = getWidgetFactory().createSection(parent,
				Section.EXPANDED | Section.TWISTIE);
		section.setText("All");

		Table allModelsTable = getWidgetFactory()
				.createTable(section, SWT.NONE);
		section.setClient(allModelsTable);

		allModelsViewer = new TableViewer(allModelsTable);
		allModelsViewer.setUseHashlookup(true);
		allModelsViewer.setSorter(new ViewerSorter());
		allModelsViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						getForm().fireSelectionChanged(ModelsPart.this,
								event.getSelection());
					}
				});
		allModelsViewer.addDoubleClickListener(openEditorListener);
		menuManager = new MenuManager();
		menu = menuManager.createContextMenu(openModelsViewer.getControl());
		allModelsViewer.getControl().setMenu(menu);

		IWorkbenchPartSite site = (IWorkbenchPartSite) getForm().getAdapter(
				IWorkbenchPartSite.class);
		if (site != null) {
			site.registerContextMenu(menuManager, allModelsViewer);
		}
	}

	public void createActions() {
		IToolBarManager toolBarManager = (IToolBarManager) getForm()
				.getAdapter(IToolBarManager.class);
		if (toolBarManager == null) {
			return;
		}
	}

	public boolean setFocus() {
		if (allModelsViewer != null && allModelsViewer.getControl().setFocus()) {
			return true;
		}
		return super.setFocus();
	}

	@Override
	public void setInput(Object input) {
		if (input instanceof IModelSet) {
			this.modelSet = (IModelSet) input;
		} else {
			this.modelSet = null;
		}
		setStale(true);
	}

	@Override
	public void refresh() {
		if (modelSet != null) {
			IAdapterFactory newAdapterFactory = getAdapterFactory();
			if (adapterFactory == null
					|| !adapterFactory.equals(newAdapterFactory)) {
				adapterFactory = newAdapterFactory;

				allModelsViewer
						.setContentProvider(new AdapterFactoryContentProvider(
								getAdapterFactory()));
				allModelsViewer
						.setLabelProvider(new AdapterFactoryLabelProvider(
								getAdapterFactory()));
				openModelsViewer
						.setLabelProvider(new AdapterFactoryLabelProvider(
								getAdapterFactory()) {
							@Override
							public String getColumnText(Object object,
									int columnIndex) {
								String text = super.getColumnText(object,
										columnIndex);
								if (object == currentModel) {
									return ">> " + text;
								}
								return text;
							}
						});

				createContextMenuFor(allModelsViewer);
			}

			allModelsViewer.setInput(modelSet);
		} else if (adapterFactory != null) {
			allModelsViewer.setInput(null);
		}

		super.refresh();
	}

	@Override
	public void dispose() {
		if (listener != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getPartService().removePartListener(listener);
			listener = null;
		}
	}
}
